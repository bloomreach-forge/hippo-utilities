/*
 * Copyright 2012 Hippo
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

package org.onehippo.forge.utilities.hst.simpleocm;

import java.util.ArrayList;
import java.util.Collection;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.onehippo.forge.utilities.commons.GenericsUtil;
import org.onehippo.forge.utilities.commons.jcrmockup.JcrMockUp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;


public class UtilsTest {

    private static final Logger log = LoggerFactory.getLogger(UtilsTest.class);

    @Test
    public void testGetPropertyValues() throws RepositoryException, InstantiationException, IllegalAccessException {
        final Session session = JcrMockUp.mockJcrSession("/content.xml");
        final Node rootNode = session.getRootNode();
        final Property property = rootNode.getProperty("hippo:paths");
        final Collection<String> list = GenericsUtil.getPropertyValues(property, ArrayList.class, String.class);
        assert list.contains("2c21a29c-a5d1-4e84-aec4-594e5bf93b25");
    }

    @Test
    public void testPropertyValue() throws RepositoryException {
        final Session session = JcrMockUp.mockJcrSession("/content.xml");
        final Node rootNode = session.getRootNode();
        Property property = rootNode.getProperty("jcr:uuid");
        assert "98267ef6-37c9-4842-b8d5-4bcab85e06eb".equals(GenericsUtil.getPropertyValue(property, String.class));
    }

    @Test
    public void testPropertyValue2() throws RepositoryException {
        final Session session = JcrMockUp.mockJcrSession("/content.xml");
        final Node rootNode = session.getRootNode();
        assert "98267ef6-37c9-4842-b8d5-4bcab85e06eb".equals(GenericsUtil.getPropertyValue(rootNode, "jcr:uuid", String.class));
    }

}
