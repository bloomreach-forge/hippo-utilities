/*
 * Copyright 2012-2022 Bloomreach
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onehippo.forge.utilities.hst.xml;

import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.hippoecm.hst.content.beans.standard.HippoHtml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

public final class XMLStripper {

  private static Logger log = LoggerFactory.getLogger(XMLStripper.class);

  private XMLStripper() {
  }

  /**
   * Get HippoHtml content as text, i.e. parse it's inner HTML and add only
   * the content of the elements.
   *
   * @param hippoHtml {@link HippoHtml} bean
   * @return text content of a HippoHtml bean or {@literal null} if something goes wrong
   */
  public static String getHippoHTMLAsText(HippoHtml hippoHtml) {

    if (hippoHtml == null) {
      return null;
    }

    try {
      if (!hippoHtml.getNode().hasProperty("hippostd:content")) {
        log.error("hippoHtml does not have property hippostd:content, path={}", hippoHtml.getPath());
        return null;
      }

      return getXMLPropertyAsText(hippoHtml.getNode().getProperty("hippostd:content"));
    } catch (RepositoryException e) {
      log.error("RepositoryException parsing xml property as text", e);
    }
    return null;
  }

  /**
   * Get an XML property as text, i.e. parse it and add only the content of
   * the elements.
   *
   * @param property JCR {@link Property}
   * @return String with the text of a JCR Property or {@literal null} if something goes wrong
   */
  public static String getXMLPropertyAsText(Property property) {
    String text = null;

    try {
      SAXParserFactory spf = SAXParserFactory.newInstance();
      spf.setNamespaceAware(true);
      SAXParser saxParser = spf.newSAXParser();

      XMLReader xmlReader = saxParser.getXMLReader();

      XmlStripperSaxHandler stripXmlHandler = new XmlStripperSaxHandler();
      xmlReader.setContentHandler(stripXmlHandler);

      InputSource source = new InputSource(property.getBinary().getStream());
      xmlReader.parse(source);

      text = stripXmlHandler.getString();
    } catch (Exception e) {
      try {
        log.error("Could not parse xml property as text, path={}", property.getPath(), e);
      } catch (RepositoryException e1) {
        log.error("Exception occurred while parsing xml property as text (plus RepositoryException getting path)", e);
      }
    }

    return text;
  }
}