/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 ******************************************************************************/
package org.apache.olingo.odata2.fit.basic;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.net.URI;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.olingo.odata2.api.commons.HttpContentType;
import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.api.edm.provider.EdmProvider;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataSingleProcessor;
import org.apache.olingo.odata2.testutil.mock.EdmTestProvider;
import org.apache.olingo.odata2.testutil.server.ServletType;
import org.junit.Before;
import org.junit.Test;

/**
 *  
 */
public class RequestContentTypeTest extends AbstractBasicTest {

  public RequestContentTypeTest(final ServletType servletType) {
    super(servletType);
  }

  @Override
  protected ODataSingleProcessor createProcessor() throws ODataException {
    return mock(ODataSingleProcessor.class);
  }

  @Override
  protected EdmProvider createEdmProvider() {
    return new EdmTestProvider();
  }

  @Before
  public void disableLoggingForAll() {
    disableLogging();
  }

  @Test
  public void illegalContentType() throws Exception {
    HttpPost post = new HttpPost(URI.create(getEndpoint().toString() + "Rooms"));
    post.setHeader(HttpHeaders.CONTENT_TYPE, "illegal");
    final HttpResponse response = getHttpClient().execute(post);
    assertEquals(HttpStatusCodes.UNSUPPORTED_MEDIA_TYPE.getStatusCode(), response.getStatusLine().getStatusCode());
  }

  @Test
  public void wildcardContentType() throws Exception {
    HttpPost post = new HttpPost(URI.create(getEndpoint().toString() + "Rooms"));
    post.setHeader(HttpHeaders.CONTENT_TYPE, HttpContentType.WILDCARD);
    final HttpResponse response = getHttpClient().execute(post);
    assertEquals(HttpStatusCodes.UNSUPPORTED_MEDIA_TYPE.getStatusCode(), response.getStatusLine().getStatusCode());
  }

  @Test
  public void subTypeWildcardContentType() throws Exception {
    HttpPost post = new HttpPost(URI.create(getEndpoint().toString() + "Rooms"));
    post.setHeader(HttpHeaders.CONTENT_TYPE, "application/*");
    final HttpResponse response = getHttpClient().execute(post);
    assertEquals(HttpStatusCodes.UNSUPPORTED_MEDIA_TYPE.getStatusCode(), response.getStatusLine().getStatusCode());
  }

  @Test
  public void unsupportedContentType() throws Exception {
    HttpPut put = new HttpPut(URI.create(getEndpoint().toString() + "Rooms('1')"));
    put.setHeader(HttpHeaders.CONTENT_TYPE, HttpContentType.TEXT_PLAIN);
    final HttpResponse response = getHttpClient().execute(put);
    assertEquals(HttpStatusCodes.UNSUPPORTED_MEDIA_TYPE.getStatusCode(), response.getStatusLine().getStatusCode());
  }

  @Test
  public void unsupportedContentTypeParameter() throws Exception {
    HttpPatch patch = new HttpPatch(URI.create(getEndpoint().toString() + "Rooms('1')"));
    patch.setHeader(HttpHeaders.CONTENT_TYPE, HttpContentType.APPLICATION_JSON + ";illegal=wrong");
    final HttpResponse response = getHttpClient().execute(patch);
    assertEquals(HttpStatusCodes.UNSUPPORTED_MEDIA_TYPE.getStatusCode(), response.getStatusLine().getStatusCode());
  }

  @Test
  public void xmlContentType() throws Exception {
    HttpPut put = new HttpPut(URI.create(getEndpoint().toString() + "Rooms('1')"));
    put.setHeader(HttpHeaders.CONTENT_TYPE, "xml");
    final HttpResponse response = getHttpClient().execute(put);
    assertEquals(HttpStatusCodes.UNSUPPORTED_MEDIA_TYPE.getStatusCode(), response.getStatusLine().getStatusCode());
  }

  @Test
  public void validApplicationXmlContentType() throws Exception {
    HttpPut put = new HttpPut(URI.create(getEndpoint().toString() + "Teams('1')"));
    put.setHeader(HttpHeaders.CONTENT_TYPE, HttpContentType.APPLICATION_XML);
    final HttpResponse response = getHttpClient().execute(put);
    // We expect an internal server error due to the incomplete processor implementation.
    assertEquals(HttpStatusCodes.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusLine().getStatusCode());
  }

  @Test
  public void textContentType() throws Exception {
    HttpPut put = new HttpPut(URI.create(getEndpoint().toString() + "Rooms('1')/Seats/$value"));
    put.setHeader(HttpHeaders.CONTENT_TYPE, "text");
    final HttpResponse response = getHttpClient().execute(put);
    assertEquals(HttpStatusCodes.UNSUPPORTED_MEDIA_TYPE.getStatusCode(), response.getStatusLine().getStatusCode());
  }

  @Test
  public void validTextPlainContentType() throws Exception {
    HttpPut put = new HttpPut(URI.create(getEndpoint().toString() + "Teams('1')/isScrumTeam/$value"));
    put.setHeader(HttpHeaders.CONTENT_TYPE, HttpContentType.TEXT_PLAIN);
    final HttpResponse response = getHttpClient().execute(put);
    // We expect an internal server error due to the incomplete processor implementation.
    assertEquals(HttpStatusCodes.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusLine().getStatusCode());
  }

  @Test
  public void noContentType() throws Exception {
    final HttpPost post = new HttpPost(URI.create(getEndpoint().toString() + "Rooms"));
    final HttpResponse response = getHttpClient().execute(post);
    assertEquals(HttpStatusCodes.UNSUPPORTED_MEDIA_TYPE.getStatusCode(), response.getStatusLine().getStatusCode());
  }

  @Test
  public void emptyContentType() throws Exception {
    HttpPost post = new HttpPost(URI.create(getEndpoint().toString() + "Rooms"));
    post.setHeader(HttpHeaders.CONTENT_TYPE, "");
    final HttpResponse response = getHttpClient().execute(post);
    assertEquals(HttpStatusCodes.UNSUPPORTED_MEDIA_TYPE.getStatusCode(), response.getStatusLine().getStatusCode());
  }

  @Test
  public void twoContentTypes() throws Exception {
    HttpPost post = new HttpPost(URI.create(getEndpoint().toString() + "Rooms"));
    post.addHeader(HttpHeaders.CONTENT_TYPE, "illegal");
    post.addHeader(HttpHeaders.CONTENT_TYPE, HttpContentType.APPLICATION_JSON);
    final HttpResponse response = getHttpClient().execute(post);
    assertEquals(HttpStatusCodes.UNSUPPORTED_MEDIA_TYPE.getStatusCode(), response.getStatusLine().getStatusCode());
  }

  @Test
  public void contentTypeAndSubtypeIllegal() throws Exception {
    HttpPost post = new HttpPost(URI.create(getEndpoint().toString() + "Rooms"));
    post.addHeader(HttpHeaders.CONTENT_TYPE, "illegal/illegal");
    final HttpResponse response = getHttpClient().execute(post);
    assertEquals(HttpStatusCodes.UNSUPPORTED_MEDIA_TYPE.getStatusCode(), response.getStatusLine().getStatusCode());
  }
}
