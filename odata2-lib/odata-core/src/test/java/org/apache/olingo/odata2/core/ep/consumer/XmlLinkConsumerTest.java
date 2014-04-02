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
package org.apache.olingo.odata2.core.ep.consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.core.xml.XMLStreamReader;
import org.junit.Test;

/**
 *  
 */
public class XmlLinkConsumerTest extends AbstractXmlConsumerTest {

  public XmlLinkConsumerTest(final StreamWriterImplType type) {
    super(type);
  }

  private static final String SERVICE_ROOT = "http://localhost:80/odata/";
  private static final String MANAGER_1_EMPLOYEES =
      "<links xmlns=\"" + Edm.NAMESPACE_D_2007_08 + "\">"
          + "<uri>" + SERVICE_ROOT + "Employees('1')</uri>"
          + "<uri>" + SERVICE_ROOT + "Employees('2')</uri>"
          + "<uri>" + SERVICE_ROOT + "Employees('3')</uri>"
          + "<uri>" + SERVICE_ROOT + "Employees('6')</uri>"
          + "</links>";
  private static final String SINGLE_LINK =
      "<uri xmlns=\"" + Edm.NAMESPACE_D_2007_08 + "\">" + SERVICE_ROOT + "Employees('6')</uri>";

  @Test
  public void readLink() throws Exception {
    XMLStreamReader reader = createReaderForTest(SINGLE_LINK, true);
    final String link = new XmlLinkConsumer().readLink(reader, null);
    assertEquals(SERVICE_ROOT + "Employees('6')", link);
  }

  @Test
  public void readLinks() throws Exception {
    XMLStreamReader reader = createReaderForTest(MANAGER_1_EMPLOYEES, true);
    final List<String> links = new XmlLinkConsumer().readLinks(reader, null);

    assertEquals(4, links.size());
    assertEquals(SERVICE_ROOT + "Employees('1')", links.get(0));
    assertEquals(SERVICE_ROOT + "Employees('2')", links.get(1));
    assertEquals(SERVICE_ROOT + "Employees('3')", links.get(2));
    assertEquals(SERVICE_ROOT + "Employees('6')", links.get(3));
  }

  @Test
  public void readEmptyList() throws Exception {
    final String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><links xmlns=\"" + Edm.NAMESPACE_D_2007_08 + "\"/>";
    final List<String> links = new XmlLinkConsumer().readLinks(createReaderForTest(xml, true), null);
    assertNotNull(links);
    assertTrue(links.isEmpty());
  }

  @Test
  public void readEmptyList2() throws Exception {
    final String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        + "<links xmlns=\"" + Edm.NAMESPACE_D_2007_08 + "\"></links>";
    final List<String> links = new XmlLinkConsumer().readLinks(createReaderForTest(xml, true), null);
    assertNotNull(links);
    assertTrue(links.isEmpty());
  }

  @Test
  public void withInlineCount() throws Exception {
    final String xml = "<links xmlns=\"" + Edm.NAMESPACE_D_2007_08 + "\">"
        + "<m:count xmlns:m=\"" + Edm.NAMESPACE_M_2007_08 + "\">4</m:count>"
        + "<uri>" + SERVICE_ROOT + "Employees('5')</uri>"
        + "</links>";
    final List<String> links = new XmlLinkConsumer().readLinks(createReaderForTest(xml, true), null);
    assertEquals(1, links.size());
  }

  @Test(expected = EntityProviderException.class)
  public void wrongNamespace() throws Exception {
    new XmlLinkConsumer().readLink(createReaderForTest(SINGLE_LINK.replace(Edm.NAMESPACE_D_2007_08,
        Edm.NAMESPACE_M_2007_08), true), null);
  }

  @Test(expected = EntityProviderException.class)
  public void xmlContent() throws Exception {
    final String xml = "<uri xmlns=\"" + Edm.NAMESPACE_D_2007_08 + "\"><uri>X</uri></uri>";
    new XmlLinkConsumer().readLink(createReaderForTest(xml, true), null);
  }
}
