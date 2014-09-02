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
package org.apache.olingo.odata2.core.batch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.olingo.odata2.api.batch.BatchException;
import org.apache.olingo.odata2.api.batch.BatchRequestPart;
import org.apache.olingo.odata2.api.commons.HttpHeaders;
import org.apache.olingo.odata2.api.commons.ODataHttpMethod;
import org.apache.olingo.odata2.api.ep.EntityProviderBatchProperties;
import org.apache.olingo.odata2.api.processor.ODataRequest;
import org.apache.olingo.odata2.core.ODataPathSegmentImpl;
import org.apache.olingo.odata2.core.PathInfoImpl;
import org.apache.olingo.odata2.testutil.helper.StringHelper;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 *  
 */
public class BatchRequestParserTest {

  private static final String CRLF = "\r\n";
  private static final String CONTENT_ID_REFERENCE = "NewEmployee";
  private static final String PUT_MIME_HEADER_CONTENT_ID = "BBB_MIMEPART1";
  private static final String PUT_REQUEST_HEADER_CONTENT_ID = "BBB_REQUEST1";
  private static final String SERVICE_ROOT = "http://localhost/odata/";
  private static EntityProviderBatchProperties batchProperties;
  private static final String contentType = "multipart/mixed;boundary=batch_8194-cf13-1f56";
  private static final String MIME_HEADERS = "Content-Type: application/http" + CRLF
      + "Content-Transfer-Encoding: binary" + CRLF;
  private static final String GET_REQUEST = MIME_HEADERS + CRLF
      + "GET Employees('1')/EmployeeName HTTP/1.1" + CRLF
      + CRLF
      + CRLF;

  @BeforeClass
  public static void setProperties() throws URISyntaxException {
    PathInfoImpl pathInfo = new PathInfoImpl();
    pathInfo.setServiceRoot(new URI(SERVICE_ROOT));
    batchProperties = EntityProviderBatchProperties.init().pathInfo(pathInfo).build();

  }

  @Test
  public void test() throws IOException, BatchException, URISyntaxException {
    String fileName = "/batchWithPost.batch";
    InputStream in = ClassLoader.class.getResourceAsStream(fileName);
    if (in == null) {
      throw new IOException("Requested file '" + fileName + "' was not found.");
    }

    BatchRequestParser parser = new BatchRequestParser(contentType, batchProperties);
    List<BatchRequestPart> batchRequestParts = parser.parse(in);
    assertNotNull(batchRequestParts);
    assertEquals(false, batchRequestParts.isEmpty());
    for (BatchRequestPart object : batchRequestParts) {
      if (!object.isChangeSet()) {
        assertEquals(1, object.getRequests().size());
        ODataRequest retrieveRequest = object.getRequests().get(0);
        assertEquals(ODataHttpMethod.GET, retrieveRequest.getMethod());
        if (!retrieveRequest.getAcceptableLanguages().isEmpty()) {
          assertEquals(3, retrieveRequest.getAcceptableLanguages().size());
        }
        assertEquals(new URI(SERVICE_ROOT), retrieveRequest.getPathInfo().getServiceRoot());
        ODataPathSegmentImpl pathSegment = new ODataPathSegmentImpl("Employees('2')", null);
        assertEquals(pathSegment.getPath(), retrieveRequest.getPathInfo().getODataSegments().get(0).getPath());
        if (retrieveRequest.getQueryParameters().get("$format") != null) {
          assertEquals("json", retrieveRequest.getQueryParameters().get("$format"));
        }
        assertEquals(SERVICE_ROOT + "Employees('2')/EmployeeName?$format=json", retrieveRequest.getPathInfo()
            .getRequestUri().toASCIIString());
      } else {
        List<ODataRequest> requests = object.getRequests();
        for (ODataRequest request : requests) {

          assertEquals(ODataHttpMethod.PUT, request.getMethod());
          assertEquals("100000", request.getRequestHeaderValue(HttpHeaders.CONTENT_LENGTH.toLowerCase()));
          assertEquals("application/json;odata=verbose", request.getContentType());
          assertEquals(3, request.getAcceptHeaders().size());
          assertNotNull(request.getAcceptableLanguages());
          assertTrue(request.getAcceptableLanguages().isEmpty());
          assertEquals("*/*", request.getAcceptHeaders().get(2));
          assertEquals("application/atomsvc+xml", request.getAcceptHeaders().get(0));
          assertEquals(new URI(SERVICE_ROOT + "Employees('2')/EmployeeName").toASCIIString(), request.getPathInfo()
              .getRequestUri().toASCIIString());

          ODataPathSegmentImpl pathSegment = new ODataPathSegmentImpl("Employees('2')", null);
          assertEquals(pathSegment.getPath(), request.getPathInfo().getODataSegments().get(0).getPath());
          ODataPathSegmentImpl pathSegment2 = new ODataPathSegmentImpl("EmployeeName", null);
          assertEquals(pathSegment2.getPath(), request.getPathInfo().getODataSegments().get(1).getPath());

        }
      }
    }
  }

  @Test
  public void testImageInContent() throws IOException, BatchException, URISyntaxException {
    String fileName = "/batchWithContent.batch";
    InputStream contentInputStream = ClassLoader.class.getResourceAsStream(fileName);
    if (contentInputStream == null) {
      throw new IOException("Requested file '" + fileName + "' was not found.");
    }
    String content = StringHelper.inputStreamToString(contentInputStream);
    String batch = "--batch_8194-cf13-1f56" + CRLF
        + MIME_HEADERS
        + CRLF
        + "GET Employees?$filter=Age%20gt%2040 HTTP/1.1" + CRLF
        + "Accept: application/atomsvc+xml;q=0.8, application/json;odata=verbose;q=0.5, */*;q=0.1" + CRLF
        + "MaxDataServiceVersion: 2.0" + CRLF
        + CRLF
        + CRLF
        + "--batch_8194-cf13-1f56" + CRLF
        + "Content-Type: multipart/mixed; boundary=changeset_f980-1cb6-94dd" + CRLF
        + CRLF
        + "--changeset_f980-1cb6-94dd" + CRLF
        + "content-type:     Application/http" + CRLF
        + "content-transfer-encoding: Binary" + CRLF
        + "Content-ID: 1" + CRLF
        + CRLF
        + "POST Employees HTTP/1.1" + CRLF
        + "Content-length: 100000" + CRLF
        + "Content-type: application/octet-stream" + CRLF
        + CRLF
        + content + CRLF
        + CRLF
        + "--changeset_f980-1cb6-94dd--" + CRLF
        + "--batch_8194-cf13-1f56--";
    List<BatchRequestPart> BatchRequestParts = parse(batch);
    for (BatchRequestPart object : BatchRequestParts) {
      if (!object.isChangeSet()) {
        assertEquals(1, object.getRequests().size());
        ODataRequest retrieveRequest = object.getRequests().get(0);
        assertEquals(ODataHttpMethod.GET, retrieveRequest.getMethod());
        assertEquals("Age gt 40", retrieveRequest.getQueryParameters().get("$filter"));
        assertEquals(new URI("http://localhost/odata/Employees?$filter=Age%20gt%2040"), retrieveRequest.getPathInfo()
            .getRequestUri());
      } else {
        List<ODataRequest> requests = object.getRequests();
        for (ODataRequest request : requests) {
          assertEquals(ODataHttpMethod.POST, request.getMethod());
          assertEquals("100000", request.getRequestHeaderValue(HttpHeaders.CONTENT_LENGTH.toLowerCase()));
          assertEquals("1", request.getRequestHeaderValue(BatchHelper.MIME_HEADER_CONTENT_ID.toLowerCase()));
          assertEquals("application/octet-stream", request.getContentType());
          InputStream body = request.getBody();
          assertEquals(content, StringHelper.inputStreamToString(body));

        }

      }
    }
  }

  @Test
  public void testPostWithoutBody() throws IOException, BatchException, URISyntaxException {
    String fileName = "/batchWithContent.batch";
    InputStream contentInputStream = ClassLoader.class.getResourceAsStream(fileName);
    if (contentInputStream == null) {
      throw new IOException("Requested file '" + fileName + "' was not found.");
    }
    StringHelper.inputStreamToString(contentInputStream);
    String batch = CRLF
        + "--batch_8194-cf13-1f56" + CRLF
        + "Content-Type: multipart/mixed; boundary=changeset_f980-1cb6-94dd" + CRLF
        + CRLF
        + "--changeset_f980-1cb6-94dd" + CRLF
        + MIME_HEADERS
        + CRLF
        + "POST Employees('2') HTTP/1.1" + CRLF
        + "Content-Length: 100" + CRLF
        + "Content-Type: application/octet-stream" + CRLF
        + CRLF
        + CRLF
        + "--changeset_f980-1cb6-94dd--" + CRLF
        + CRLF
        + "--batch_8194-cf13-1f56--";
    List<BatchRequestPart> batchRequestParts = parse(batch);
    for (BatchRequestPart object : batchRequestParts) {
      if (object.isChangeSet()) {
        List<ODataRequest> requests = object.getRequests();
        for (ODataRequest request : requests) {
          assertEquals(ODataHttpMethod.POST, request.getMethod());
          assertEquals("100", request.getRequestHeaderValue(HttpHeaders.CONTENT_LENGTH.toLowerCase()));
          assertEquals("application/octet-stream", request.getContentType());
          assertNotNull(request.getBody());
        }
      }
    }
  }

  @Test
  public void testBoundaryParameterWithQuotas() throws BatchException {
    String contentType = "multipart/mixed; boundary=\"batch_1.2+34:2j)0?\"";

    String batch = "--batch_1.2+34:2j)0?" + CRLF
        + GET_REQUEST
        + "--batch_1.2+34:2j)0?--";
    InputStream in = new ByteArrayInputStream(batch.getBytes());
    BatchRequestParser parser = new BatchRequestParser(contentType, batchProperties);
    List<BatchRequestPart> batchRequestParts = parser.parse(in);
    assertNotNull(batchRequestParts);
    assertEquals(false, batchRequestParts.isEmpty());
  }

  @Test(expected = BatchException.class)
  public void testBatchWithInvalidContentType() throws BatchException {
    String invalidContentType = "multipart;boundary=batch_1740-bb84-2f7f";

    String batch = "--batch_1740-bb84-2f7f" + CRLF
        + GET_REQUEST
        + "--batch_1740-bb84-2f7f--";
    InputStream in = new ByteArrayInputStream(batch.getBytes());
    BatchRequestParser parser = new BatchRequestParser(invalidContentType, batchProperties);
    parser.parse(in);
  }

  @Test(expected = BatchException.class)
  public void testBatchWithoutBoundaryParameter() throws BatchException {
    String invalidContentType = "multipart/mixed";
    String batch = "--batch_1740-bb84-2f7f" + CRLF
        + GET_REQUEST
        + "--batch_1740-bb84-2f7f--";
    InputStream in = new ByteArrayInputStream(batch.getBytes());
    BatchRequestParser parser = new BatchRequestParser(invalidContentType, batchProperties);
    parser.parse(in);
  }

  @Test(expected = BatchException.class)
  public void testBoundaryParameterWithoutQuota() throws BatchException {
    String invalidContentType = "multipart;boundary=batch_1740-bb:84-2f7f";
    String batch = "--batch_1740-bb:84-2f7f" + CRLF
        + GET_REQUEST
        + "--batch_1740-bb:84-2f7f--";
    InputStream in = new ByteArrayInputStream(batch.getBytes());
    BatchRequestParser parser = new BatchRequestParser(invalidContentType, batchProperties);
    parser.parse(in);
  }

  @Test(expected = BatchException.class)
  public void testWrongBoundaryString() throws BatchException {
    String batch = "--batch_8194-cf13-1f5" + CRLF
        + GET_REQUEST
        + "--batch_8194-cf13-1f56--";
    parseInvalidBatchBody(batch);
  }

  @Test(expected = BatchException.class)
  public void testBoundaryWithoutHyphen() throws BatchException {
    String batch = "--batch_8194-cf13-1f56" + CRLF
        + GET_REQUEST
        + "batch_8194-cf13-1f56" + CRLF
        + GET_REQUEST
        + "--batch_8194-cf13-1f56--";
    parseInvalidBatchBody(batch);
  }

  @Test(expected = BatchException.class)
  public void testNoBoundaryString() throws BatchException {
    String batch = "--batch_8194-cf13-1f56" + CRLF
        + GET_REQUEST
        // + no boundary string
        + GET_REQUEST
        + "--batch_8194-cf13-1f56--";
    parseInvalidBatchBody(batch);
  }

  @Test(expected = BatchException.class)
  public void testBatchBoundaryEqualsChangeSetBoundary() throws BatchException {
    String batch = "--batch_8194-cf13-1f56" + CRLF
        + "Content-Type: multipart/mixed;boundary=batch_8194-cf13-1f56" + CRLF
        + CRLF
        + "--batch_8194-cf13-1f56" + CRLF
        + MIME_HEADERS
        + CRLF
        + "PUT Employees('2')/EmployeeName HTTP/1.1" + CRLF
        + "Accept: application/atomsvc+xml;q=0.8, application/json;odata=verbose;q=0.5, */*;q=0.1" + CRLF
        + "Content-Type: application/json;odata=verbose" + CRLF
        + "MaxDataServiceVersion: 2.0" + CRLF
        + CRLF
        + "{\"EmployeeName\":\"Frederic Fall MODIFIED\"}" + CRLF
        + CRLF
        + "--batch_8194-cf13-1f56--";
    parseInvalidBatchBody(batch);
  }

  @Test(expected = BatchException.class)
  public void testNoContentType() throws BatchException {
    String batch = "--batch_8194-cf13-1f56" + CRLF
        + "Content-Transfer-Encoding: binary" + CRLF
        + CRLF
        + "GET Employees('1')/EmployeeName HTTP/1.1" + CRLF
        + CRLF
        + "--batch_8194-cf13-1f56--";
    parseInvalidBatchBody(batch);
  }

  @Test(expected = BatchException.class)
  public void testMimeHeaderContentType() throws BatchException {
    String batch = "--batch_8194-cf13-1f56" + CRLF
        + "Content-Type: text/plain" + CRLF
        + "Content-Transfer-Encoding: binary" + CRLF
        + CRLF
        + "GET Employees('1')/EmployeeName HTTP/1.1" + CRLF
        + CRLF
        + "--batch_8194-cf13-1f56--";
    parseInvalidBatchBody(batch);
  }

  @Test(expected = BatchException.class)
  public void testMimeHeaderEncoding() throws BatchException {
    String batch = "--batch_8194-cf13-1f56" + CRLF
        + "Content-Type: application/http" + CRLF
        + "Content-Transfer-Encoding: 8bit" + CRLF
        + CRLF
        + "GET Employees('1')/EmployeeName HTTP/1.1" + CRLF
        + CRLF
        + "--batch_8194-cf13-1f56--";
    parseInvalidBatchBody(batch);
  }

  @Test(expected = BatchException.class)
  @Ignore("What should here throw an exception")
  public void testMimeHeaderContentId() throws BatchException {
    String batch = "--batch_8194-cf13-1f56" + CRLF
        + MIME_HEADERS
        + "Content-ID: 1" + CRLF
        + CRLF
        + "GET Employees('1')/EmployeeName HTTP/1.1" + CRLF
        + CRLF
        + "--batch_8194-cf13-1f56--";
    parseInvalidBatchBody(batch);
  }

  @Test(expected = BatchException.class)
  public void testInvalidMethodForBatch() throws BatchException {
    String batch = "--batch_8194-cf13-1f56" + CRLF
        + MIME_HEADERS
        + CRLF
        + "POST Employees('1')/EmployeeName HTTP/1.1" + CRLF
        + CRLF
        + "--batch_8194-cf13-1f56--";
    parseInvalidBatchBody(batch);
  }

  @Test(expected = BatchException.class)
  public void testNoMethod() throws BatchException {
    String batch = "--batch_8194-cf13-1f56" + CRLF
        + MIME_HEADERS
        + CRLF
        + /* GET */"Employees('1')/EmployeeName HTTP/1.1" + CRLF
        + CRLF
        + "--batch_8194-cf13-1f56--";
    parseInvalidBatchBody(batch);
  }

  @Test(expected = BatchException.class)
  public void testInvalidMethodForChangeset() throws BatchException {
    String batch = "--batch_8194-cf13-1f56" + CRLF
        + "Content-Type: multipart/mixed; boundary=changeset_f980-1cb6-94dd" + CRLF
        + CRLF
        + "--changeset_f980-1cb6-94dd" + CRLF
        + MIME_HEADERS
        + CRLF
        + "GET Employees('2')/EmployeeName HTTP/1.1" + CRLF
        + "Content-Type: application/json;odata=verbose" + CRLF
        + "MaxDataServiceVersion: 2.0" + CRLF
        + CRLF
        + "--batch_8194-cf13-1f56--";
    parseInvalidBatchBody(batch);
  }

  @Test(expected = BatchException.class)
  public void testInvalidChangeSetBoundary() throws BatchException {
    String batch = "--batch_8194-cf13-1f56" + CRLF
        + "Content-Type: multipart/mixed;boundary=changeset_f980-1cb6-94dd" + CRLF
        + CRLF
        + "--changeset_f980-1cb6-94d"/* +"d" */+ CRLF
        + MIME_HEADERS
        + CRLF
        + "POST Employees('2') HTTP/1.1" + CRLF
        + "Content-Type: application/json;odata=verbose" + CRLF
        + "MaxDataServiceVersion: 2.0" + CRLF
        + CRLF
        + "--batch_8194-cf13-1f56--";
    parseInvalidBatchBody(batch);
  }

  @Test(expected = BatchException.class)
  public void testNoCloseDelimiter() throws BatchException {
    String batch = "--batch_8194-cf13-1f56" + CRLF
        + GET_REQUEST;
    parseInvalidBatchBody(batch);
  }

  @Test(expected = BatchException.class)
  public void testNoCloseDelimiter2() throws BatchException {
    String batch = "--batch_8194-cf13-1f56" + CRLF
        + MIME_HEADERS
        + CRLF
        + "GET Employees('1')/EmployeeName HTTP/1.1" + CRLF;
    parseInvalidBatchBody(batch);
  }

  @Test(expected = BatchException.class)
  public void testInvalidUri() throws BatchException {
    String batch = "--batch_8194-cf13-1f56" + CRLF
        + MIME_HEADERS
        + CRLF
        + "GET http://localhost/aa/odata/Employees('1')/EmployeeName HTTP/1.1" + CRLF
        + CRLF
        + CRLF
        + "--batch_8194-cf13-1f56--";
    parseInvalidBatchBody(batch);
  }

  @Test(expected = BatchException.class)
  public void testUriWithAbsolutePath() throws BatchException {
    String batch = "--batch_8194-cf13-1f56" + CRLF
        + MIME_HEADERS
        + CRLF
        + "GET /odata/Employees('1')/EmployeeName HTTP/1.1" + CRLF
        + CRLF
        + CRLF
        + "--batch_8194-cf13-1f56--";
    parseInvalidBatchBody(batch);
  }

  @Test(expected = BatchException.class)
  public void testNoCloseDelimiter3() throws BatchException {
    String batch = "--batch_8194-cf13-1f56" + CRLF + GET_REQUEST + "--batch_8194-cf13-1f56-"/* no hash */;
    parseInvalidBatchBody(batch);
  }

  @Test
  public void testAcceptHeaders() throws BatchException, URISyntaxException {
    String batch =
        "--batch_8194-cf13-1f56"
            + CRLF
            + MIME_HEADERS
            + CRLF
            + "GET Employees('2')/EmployeeName HTTP/1.1"
            + CRLF
            + "Content-Length: 100000"
            + CRLF
            + "Content-Type: application/json;odata=verbose"
            + CRLF
            + "Accept: application/xml;q=0.3, application/atomsvc+xml;q=0.8, " +
            "application/json;odata=verbose;q=0.5, */*;q=0.001"
            + CRLF
            + CRLF
            + CRLF
            + "--batch_8194-cf13-1f56--";
    List<BatchRequestPart> batchRequestParts = parse(batch);
    for (BatchRequestPart multipart : batchRequestParts) {
      if (!multipart.isChangeSet()) {
        assertEquals(1, multipart.getRequests().size());
        ODataRequest retrieveRequest = multipart.getRequests().get(0);
        assertEquals(ODataHttpMethod.GET, retrieveRequest.getMethod());
        assertNotNull(retrieveRequest.getAcceptHeaders());
        assertEquals(4, retrieveRequest.getAcceptHeaders().size());
        assertEquals("application/atomsvc+xml", retrieveRequest.getAcceptHeaders().get(0));
        assertEquals("*/*", retrieveRequest.getAcceptHeaders().get(3));
      }

    }
  }

  @Test
  public void testAcceptHeaders2() throws BatchException, URISyntaxException {
    String batch = "--batch_8194-cf13-1f56" + CRLF
        + MIME_HEADERS
        + CRLF
        + "GET Employees('2')/EmployeeName HTTP/1.1" + CRLF
        + "Content-Length: 100000" + CRLF
        + "Content-Type: application/json;odata=verbose" + CRLF
        + "Accept: */*;q=0.5, application/json;odata=verbose;q=1.0,application/atom+xml" + CRLF
        + CRLF
        + CRLF
        + "--batch_8194-cf13-1f56--";
    List<BatchRequestPart> batchRequestParts = parse(batch);
    for (BatchRequestPart multipart : batchRequestParts) {
      if (!multipart.isChangeSet()) {
        assertEquals(1, multipart.getRequests().size());
        ODataRequest retrieveRequest = multipart.getRequests().get(0);
        assertEquals(ODataHttpMethod.GET, retrieveRequest.getMethod());
        assertNotNull(retrieveRequest.getAcceptHeaders());
        assertEquals(3, retrieveRequest.getAcceptHeaders().size());
        assertEquals("application/json;odata=verbose", retrieveRequest.getAcceptHeaders().get(0));
        assertEquals("application/atom+xml", retrieveRequest.getAcceptHeaders().get(1));
        assertEquals("*/*", retrieveRequest.getAcceptHeaders().get(2));
      }

    }
  }

  @Test
  public void testAcceptHeaders3() throws BatchException, URISyntaxException {
    String batch = "--batch_8194-cf13-1f56" + CRLF
        + MIME_HEADERS
        + CRLF
        + "GET Employees('2')/EmployeeName HTTP/1.1" + CRLF
        + "Content-Length: 100000" + CRLF
        + "Content-Type: application/json;odata=verbose" + CRLF
        + "accept: */*,application/atom+xml,application/atomsvc+xml,application/xml" + CRLF
        + CRLF
        + CRLF
        + "--batch_8194-cf13-1f56--";
    List<BatchRequestPart> batchRequestParts = parse(batch);
    for (BatchRequestPart multipart : batchRequestParts) {
      if (!multipart.isChangeSet()) {
        assertEquals(1, multipart.getRequests().size());
        ODataRequest retrieveRequest = multipart.getRequests().get(0);
        assertEquals(ODataHttpMethod.GET, retrieveRequest.getMethod());
        assertNotNull(retrieveRequest.getAcceptHeaders());
        assertEquals(4, retrieveRequest.getAcceptHeaders().size());

        assertEquals("application/atom+xml", retrieveRequest.getAcceptHeaders().get(0));
        assertEquals("application/atomsvc+xml", retrieveRequest.getAcceptHeaders().get(1));

        assertEquals("application/xml", retrieveRequest.getAcceptHeaders().get(2));
      }

    }
  }

  @Test
  public void testContentId() throws BatchException {
    String batch = "--batch_8194-cf13-1f56" + CRLF
        + MIME_HEADERS
        + CRLF
        + "GET Employees HTTP/1.1" + CRLF
        + "accept: */*,application/atom+xml,application/atomsvc+xml,application/xml" + CRLF
        + "Content-Id: BBB" + CRLF
        + CRLF + CRLF
        + "--batch_8194-cf13-1f56" + CRLF
        + "Content-Type: multipart/mixed; boundary=changeset_f980-1cb6-94dd" + CRLF
        + CRLF
        + "--changeset_f980-1cb6-94dd" + CRLF
        + MIME_HEADERS
        + "Content-Id: " + CONTENT_ID_REFERENCE + CRLF
        + CRLF
        + "POST Employees HTTP/1.1" + CRLF
        + "Content-type: application/octet-stream" + CRLF
        + CRLF
        + "/9j/4AAQSkZJRgABAQEBLAEsAAD/4RM0RXhpZgAATU0AKgAAAAgABwESAAMAAAABAAEAAAEaAAUAAAABAAAAYgEbAAUAAAA" + CRLF
        + CRLF
        + "--changeset_f980-1cb6-94dd" + CRLF
        + MIME_HEADERS
        + "Content-ID: " + PUT_MIME_HEADER_CONTENT_ID + CRLF
        + CRLF
        + "PUT $" + CONTENT_ID_REFERENCE + "/EmployeeName HTTP/1.1" + CRLF
        + "Content-Type: application/json;odata=verbose" + CRLF
        + "Content-Id:" + PUT_REQUEST_HEADER_CONTENT_ID + CRLF
        + CRLF
        + "{\"EmployeeName\":\"Peter Fall\"}" + CRLF
        + "--changeset_f980-1cb6-94dd--" + CRLF
        + CRLF
        + "--batch_8194-cf13-1f56--";
    InputStream in = new ByteArrayInputStream(batch.getBytes());
    BatchRequestParser parser = new BatchRequestParser(contentType, batchProperties);
    List<BatchRequestPart> batchRequestParts = parser.parse(in);
    assertNotNull(batchRequestParts);
    for (BatchRequestPart multipart : batchRequestParts) {
      if (!multipart.isChangeSet()) {
        assertEquals(1, multipart.getRequests().size());
        ODataRequest retrieveRequest = multipart.getRequests().get(0);
        assertEquals("BBB", retrieveRequest.getRequestHeaderValue(BatchHelper.REQUEST_HEADER_CONTENT_ID.toLowerCase()));
      } else {
        for (ODataRequest request : multipart.getRequests()) {
          if (ODataHttpMethod.POST.equals(request.getMethod())) {
            assertEquals(CONTENT_ID_REFERENCE, request.getRequestHeaderValue(BatchHelper.MIME_HEADER_CONTENT_ID
                .toLowerCase()));
          } else if (ODataHttpMethod.PUT.equals(request.getMethod())) {
            assertEquals(PUT_MIME_HEADER_CONTENT_ID, request.getRequestHeaderValue(BatchHelper.MIME_HEADER_CONTENT_ID
                .toLowerCase()));
            assertEquals(PUT_REQUEST_HEADER_CONTENT_ID, request
                .getRequestHeaderValue(BatchHelper.REQUEST_HEADER_CONTENT_ID.toLowerCase()));
            assertNull(request.getPathInfo().getRequestUri());
            assertEquals("$" + CONTENT_ID_REFERENCE, request.getPathInfo().getODataSegments().get(0).getPath());
          }
        }
      }
    }
  }

  private List<BatchRequestPart> parse(final String batch) throws BatchException {
    InputStream in = new ByteArrayInputStream(batch.getBytes());
    BatchRequestParser parser = new BatchRequestParser(contentType, batchProperties);
    List<BatchRequestPart> batchRequestParts = parser.parse(in);
    assertNotNull(batchRequestParts);
    assertEquals(false, batchRequestParts.isEmpty());
    return batchRequestParts;
  }

  private void parseInvalidBatchBody(final String batch) throws BatchException {
    InputStream in = new ByteArrayInputStream(batch.getBytes());
    BatchRequestParser parser = new BatchRequestParser(contentType, batchProperties);
    parser.parse(in);
  }
}
