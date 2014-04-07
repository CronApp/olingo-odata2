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
package org.apache.olingo.odata2.core.ep.producer;

import java.util.Locale;

import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.xml.XMLStreamException;
import org.apache.olingo.odata2.api.xml.XMLStreamWriter;
import org.apache.olingo.odata2.core.ep.util.FormatXml;

public class XmlErrorDocumentProducer {

  public void writeErrorDocument(final XMLStreamWriter writer, final String errorCode, final String message,
      final Locale locale, final String innerError) throws XMLStreamException {
    writer.writeStartDocument();
    writer.writeStartElement(FormatXml.M_ERROR);
    writer.writeDefaultNamespace(Edm.NAMESPACE_M_2007_08);
    writer.writeStartElement(FormatXml.M_CODE);
    if (errorCode != null) {
      writer.writeCharacters(errorCode);
    }
    writer.writeEndElement();
    writer.writeStartElement(FormatXml.M_MESSAGE);
    if (locale != null) {
      writer.writeAttribute(Edm.PREFIX_XML, Edm.NAMESPACE_XML_1998, FormatXml.XML_LANG, getLocale(locale));
    } else {
      writer.writeAttribute(Edm.PREFIX_XML, Edm.NAMESPACE_XML_1998, FormatXml.XML_LANG, "");
    }
    if (message != null) {
      writer.writeCharacters(message);
    }
    writer.writeEndElement();

    if (innerError != null) {
      writer.writeStartElement(FormatXml.M_INNER_ERROR);
      writer.writeCharacters(innerError);
      writer.writeEndElement();
    }

    writer.writeEndDocument();
  }

  /**
   * Gets language and country as defined in RFC 4646 based on {@link Locale}.
   */
  private String getLocale(final Locale locale) {
    if (locale.getCountry().isEmpty()) {
      return locale.getLanguage();
    } else {
      return locale.getLanguage() + "-" + locale.getCountry();
    }
  }

}
