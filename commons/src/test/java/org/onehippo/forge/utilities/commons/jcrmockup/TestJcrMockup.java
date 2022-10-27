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

package org.onehippo.forge.utilities.commons.jcrmockup;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;


public class TestJcrMockup {

    private static final Logger log = LoggerFactory.getLogger(TestJcrMockup.class);

    @Test
    public void testSetProperties() throws RepositoryException {
        Node node = JcrMockUp.mockJcrNode("/content.xml");
        node.setProperty("string", "foobar");
        node.setProperty("multistring", new String[]{"foo", "bar"});
        node.setProperty("boolean", true);
        node.setProperty("long", 1234);
        node.setProperty("double", 123.2);
        Calendar calendar = Calendar.getInstance();
        node.setProperty("date", calendar);

        assert "foobar".equals(node.getProperty("string").getString());
        assert node.getProperty("boolean").getBoolean();
        assert new Long(1234).equals(node.getProperty("long").getLong());
        assert 123.2 == node.getProperty("double").getDouble();
        assert node.getProperty("date").getDate().getTime().equals(calendar.getTime());

        assert node.getProperty("multistring").getDefinition().isMultiple();
        assert node.getProperty("multistring").getValues().length == 2;
        assert "foo".equals(node.getProperty("multistring").getValues()[0].getString());
    }

    @Test
    public void testGetValue() throws RepositoryException {
        Node node = JcrMockUp.mockJcrNode("/content.xml");
        node.setProperty("string", "foobar");
        node.setProperty("boolean", true);
        node.setProperty("long", 1234);
        node.setProperty("double", 123.2);
        node.setProperty("date", Calendar.getInstance());

        assert "foobar".equals(node.getProperty("string").getValue().getString());
        assert node.getProperty("boolean").getValue().getBoolean();
        assert node.getProperty("long").getValue().getLong() == 1234;
        assert node.getProperty("double").getDouble() == 123.2;
        assert node.getProperty("date").getDate() != null;
    }

    @Test
    public void testSession() throws RepositoryException {
        Session session = JcrMockUp.mockJcrSession("/content.xml");
        Node node = session.getRootNode();
        assert "98267ef6-37c9-4842-b8d5-4bcab85e06eb".equals(node.getIdentifier());
    }

    @Test
    public void hasNode() throws RepositoryException {
        Session session = JcrMockUp.mockJcrSession("/content.xml");
        Node node = session.getRootNode();
        assert node.hasNode("14-9-wie-denk-je-wel-dat-je-bent") && node.hasNode("thema-avond-stress-en-mantelzorg");
        assert node.hasProperty("hippo:paths") && node.hasProperty("hippostd:foldertype");
    }

    @Test
    public void testMixins() throws RepositoryException {
        Session session = JcrMockUp.mockJcrSession("/content.xml");
        Node node = session.getRootNode();
        boolean hasMixin = false;
        for (NodeType nodeType : node.getMixinNodeTypes()) {
            if ("hippo:harddocument".equals(nodeType.getName())) {
                hasMixin = true;
            } else {
                hasMixin = false;
                break;
            }
        }
        assert hasMixin;
    }

    @Test
    public void testItemExists() throws RepositoryException {
        Session session = JcrMockUp.mockJcrSession("/content.xml");
        assert session.itemExists("/14-9-wie-denk-je-wel-dat-je-bent/jcr:uuid");
        Item item = session.getItem("/14-9-wie-denk-je-wel-dat-je-bent/jcr:uuid");
        assert item instanceof Property;
        assert "c3085f6c-1a30-4c4d-a278-445b0267746d".equals(((Property) item).getString());
    }

    @Test
    public void testGetItem() throws RepositoryException {
        Session session = JcrMockUp.mockJcrSession("/content.xml");
        Node node = (Node) session.getItem("/14-9-wie-denk-je-wel-dat-je-bent/14-9-wie-denk-je-wel-dat-je-bent/jcrmockup:body");
        assert node.getProperty("hippostd:content").getString().contains("middagsymposium wordt vanuit diverse");

        Property property = (Property) session.getItem("/14-9-wie-denk-je-wel-dat-je-bent/14-9-wie-denk-je-wel-dat-je-bent/jcrmockup:body/hippostd:content");
        assert property.getString().contains("middagsymposium wordt vanuit diverse");
    }

    @Test
    public void testGetProperties() throws RepositoryException {
        Session session = JcrMockUp.mockJcrSession("/content.xml");
        Item item = session.getItem("/14-9-wie-denk-je-wel-dat-je-bent/14-9-wie-denk-je-wel-dat-je-bent");
        Node node = (Node) item;

        assert "3b2c1d74-46cb-4b3f-ab05-b505095bcf43".equals(node.getProperty("jcr:uuid").getString());
        Calendar calendar = node.getProperty("jcrmockup:agendadate_from").getDate();
        assert calendar != null && 2010 == calendar.get(Calendar.YEAR) && calendar.get(Calendar.MONTH) == 8 && calendar.get(Calendar.DAY_OF_MONTH) == 14;
        assert Boolean.FALSE.equals(node.getProperty("jcrmockup:generate_anchors").getBoolean());
        boolean liveOrPreview = false;
        Value[] values = node.getProperty("hippo:availability").getValues();
        for (Value value : values) {
            if ("live".equals(value.getString()) || "preview".equals(value.getString())) {
                liveOrPreview = true;
            } else {
                liveOrPreview = false;
                break;
            }
        }
        assert liveOrPreview;
    }

    @Test
    public void testGetPropertyException() throws RepositoryException {
        Session session = JcrMockUp.mockJcrSession("/content.xml");
        boolean pathNotFound = false;
        try {
            session.getRootNode().getProperty("propertydoesntexsit");
        } catch (PathNotFoundException pathNotFoundException) {
            pathNotFound = true;
        }
        assert pathNotFound;
    }

    @Test
    public void testGetPropertyPathNotFound() throws RepositoryException {
        Session session = JcrMockUp.mockJcrSession("/content.xml");
        Node node = (Node) session.getItem("/14-9-wie-denk-je-wel-dat-je-bent/14-9-wie-denk-je-wel-dat-je-bent");
        boolean pathNotFound = false;
        try {
            node.getProperty("pathdoesntexist");
        } catch (RepositoryException repositoryException) {
            pathNotFound = true;
        }
        assert pathNotFound;
    }

    @Test
    public void testGetNode() throws RepositoryException {
        Session session = JcrMockUp.mockJcrSession("/content.xml");
        Node node = session.getRootNode();
        Node child = node.getNode("14-9-wie-denk-je-wel-dat-je-bent");
        assert "14-9-wie-denk-je-wel-dat-je-bent".equals(child.getName());
    }

    @Test
    public void testGetNodePathNotFound() throws RepositoryException {
        Session session = JcrMockUp.mockJcrSession("/content.xml");
        Node node = session.getRootNode();
        boolean pathNotFound = false;
        try {
            node.getNode("notexistingchildnode");
        } catch (PathNotFoundException pathNotFoundException) {
            pathNotFound = true;
        }
        assert pathNotFound;
    }

    @Test
    public void testGetChildNodes() throws RepositoryException {
        Session session = JcrMockUp.mockJcrSession("/content.xml");
        Node node = (Node) session.getItem("/afscheidssymposium-peter-van-harten-als-a-opleider/afscheidssymposium-peter-van-harten-als-a-opleider");
        NodeIterator nodeIterator = node.getNodes();
        Set<String> childNodeNames = new HashSet<String>();
        childNodeNames.add("jcrmockup:body");
        childNodeNames.add("jcrmockup:agenda_location_extrainfo");
        childNodeNames.add("jcrmockup:metadata");
        childNodeNames.add("relateddocs:docs");
        boolean containsChildNodeName = false;
        while (nodeIterator.hasNext()) {
            Node childNode = nodeIterator.nextNode();
            if (childNodeNames.contains(childNode.getName())) {
                containsChildNodeName = true;
            } else {
                containsChildNodeName = false;
                break;
            }
        }
        assert containsChildNodeName;
    }

    @Test
    public void testGetChildNodeByName() throws RepositoryException {
        Session session = JcrMockUp.mockJcrSession("/content.xml");
        Node node = (Node) session.getItem("/afscheidssymposium-peter-van-harten-als-a-opleider/afscheidssymposium-peter-van-harten-als-a-opleider");
        NodeIterator nodeIterator = node.getNodes("jcrmockup:body");
        assert nodeIterator.hasNext();
        assert nodeIterator.getSize() == 1;
    }

    @Test
    public void testAddNode() throws RepositoryException {
        Session session = JcrMockUp.mockJcrSession("/content.xml");
        Node node = session.getRootNode();
        node.addNode("foobar", "jcrmockup:foobar");
        Node newNode = node.getNode("foobar");
        assert "jcrmockup:foobar".equals(newNode.getProperty("jcr:primaryType").getString());
    }

    @Test
    public void testRemoveNode() throws RepositoryException {
        Session session = JcrMockUp.mockJcrSession("/content.xml");

        Node child = session.getRootNode().getNode("14-9-wie-denk-je-wel-dat-je-bent");
        child.remove();
        boolean pathNotFound = false;
        try {
            session.getItem("/14-9-wie-denk-je-wel-dat-je-bent");
        } catch (PathNotFoundException notFound) {
            pathNotFound = true;
        }
        assert pathNotFound;
        assert !session.getRootNode().hasNode("14-9-wie-denk-je-wel-dat-je-bent");
    }

    @Test
    public void testNodeIteratorRemove() throws RepositoryException {
        Session session = JcrMockUp.mockJcrSession("/content.xml");
        NodeIterator nodeIterator = session.getRootNode().getNodes();
        assert nodeIterator.getSize() > 0;
        while (nodeIterator.hasNext()) {
            nodeIterator.nextNode();
            nodeIterator.remove();
        }

        assert session.getRootNode().getNodes().getSize() == 0;
    }

    @Test
    public void testNodeGetPath() throws RepositoryException {
        Session session = JcrMockUp.mockJcrSession("/content.xml");
        String path = "/14-9-wie-denk-je-wel-dat-je-bent";
        Item child = session.getItem(path);
        assert path.equals(child.getPath());
    }

    @Test
    public void testPropertyGetPath() throws RepositoryException {
        Session session = JcrMockUp.mockJcrSession("/content.xml");
        String path = "/14-9-wie-denk-je-wel-dat-je-bent/jcr:uuid";
        Item child = session.getItem(path);
        assert path.equals(child.getPath());
    }

    @Test
    public void testGetParent() throws RepositoryException {
        Session session = JcrMockUp.mockJcrSession("/content.xml");
        Item item = session.getItem("/14-9-wie-denk-je-wel-dat-je-bent/14-9-wie-denk-je-wel-dat-je-bent");
        Item parent = item.getParent();
        assert "14-9-wie-denk-je-wel-dat-je-bent".equals(parent.getName());

        Item item2 = session.getItem("/14-9-wie-denk-je-wel-dat-je-bent/14-9-wie-denk-je-wel-dat-je-bent/jcrmockup:documentdate");
        assert "14-9-wie-denk-je-wel-dat-je-bent".equals(item2.getParent().getName());
    }

    @Test
    public void testIsNodeType() throws RepositoryException {
        Session session = JcrMockUp.mockJcrSession("/content.xml");
        Node node = (Node) session.getItem("/14-9-wie-denk-je-wel-dat-je-bent/14-9-wie-denk-je-wel-dat-je-bent");
        assert node.isNodeType("jcrmockup:agenda");
    }

    @Test
    public void testGetUuid() throws RepositoryException {
        Session session = JcrMockUp.mockJcrSession("/content.xml");
        Node node = (Node) session.getItem("/14-9-wie-denk-je-wel-dat-je-bent/14-9-wie-denk-je-wel-dat-je-bent");
        assert "3b2c1d74-46cb-4b3f-ab05-b505095bcf43".equals(node.getIdentifier());
    }

    @Test
    public void testPropertyIterator() throws RepositoryException {
        Session session = JcrMockUp.mockJcrSession("/content.xml");
        Node node = (Node) session.getItem("/14-9-wie-denk-je-wel-dat-je-bent/14-9-wie-denk-je-wel-dat-je-bent");

        Set<String> propertyNames = new HashSet<String>();
        propertyNames.add("jcr:primaryType");
        propertyNames.add("jcr:mixinTypes");
        propertyNames.add("jcr:uuid");
        propertyNames.add("jcrmockup:agendadate_from");
        propertyNames.add("jcrmockup:agendadate_to");
        propertyNames.add("jcrmockup:documentdate");
        propertyNames.add("jcrmockup:generate_anchors");
        propertyNames.add("jcrmockup:introduction");
        propertyNames.add("jcrmockup:title");
        propertyNames.add("hippo:availability");
        propertyNames.add("hippo:paths");
        propertyNames.add("hippo:related___pathreference");
        propertyNames.add("hippostd:holder");
        propertyNames.add("hippostd:state");
        propertyNames.add("hippostd:stateSummary");
        propertyNames.add("hippostd:tags");
        propertyNames.add("hippostdpubwf:createdBy");
        propertyNames.add("hippostdpubwf:creationDate");
        propertyNames.add("hippostdpubwf:lastModificationDate");
        propertyNames.add("hippostdpubwf:lastModifiedBy");
        propertyNames.add("hippostdpubwf:publicationDate");

        boolean propertyNotInSet = false;
        PropertyIterator propertyIterator = node.getProperties();
        while (propertyIterator.hasNext()) {
            Property property = propertyIterator.nextProperty();
            if (!propertyNames.contains(property.getName())) {
                propertyNotInSet = true;
                break;
            }
            propertyNames.remove(property.getName());
        }

        assert propertyNames.size() == 0 && !propertyNotInSet;
    }

    @Test
    public void testPropertyIteratorRemove() throws RepositoryException {
        Session session = JcrMockUp.mockJcrSession("/content.xml");
        Node node = (Node) session.getItem("/14-9-wie-denk-je-wel-dat-je-bent/14-9-wie-denk-je-wel-dat-je-bent");
        PropertyIterator propertyIterator = node.getProperties();
        while (propertyIterator.hasNext()) {
            propertyIterator.nextProperty();
            propertyIterator.remove();
        }

        Set<String> propertyNames = new HashSet<String>();
        propertyNames.add("jcr:primaryType");
        propertyNames.add("jcr:mixinTypes");
        propertyNames.add("jcr:uuid");
        propertyNames.add("jcrmockup:agendadate_from");
        propertyNames.add("jcrmockup:agendadate_to");
        propertyNames.add("jcrmockup:documentdate");
        propertyNames.add("jcrmockup:generate_anchors");
        propertyNames.add("jcrmockup:introduction");
        propertyNames.add("jcrmockup:title");
        propertyNames.add("hippo:availability");
        propertyNames.add("hippo:paths");
        propertyNames.add("hippo:related___pathreference");
        propertyNames.add("hippostd:holder");
        propertyNames.add("hippostd:state");
        propertyNames.add("hippostd:stateSummary");
        propertyNames.add("hippostd:tags");
        propertyNames.add("hippostdpubwf:createdBy");
        propertyNames.add("hippostdpubwf:creationDate");
        propertyNames.add("hippostdpubwf:lastModificationDate");
        propertyNames.add("hippostdpubwf:lastModifiedBy");
        propertyNames.add("hippostdpubwf:publicationDate");

        boolean nodeHasProperty = false;
        for (String propertyName : propertyNames) {
            if (node.hasProperty(propertyName)) {
                nodeHasProperty = true;
                break;
            }
        }
        assert !nodeHasProperty;
    }

    @Test
    public void sameNameSiblingPathTest() throws RepositoryException {
        Session session = JcrMockUp.mockEmptySession();
        Node rootNode = session.getRootNode();

        Node firstFooNode = rootNode.addNode("foo", "jcrmock:testnode");
        Node secondFooNode = rootNode.addNode("foo", "jcrmock:testnode");

        Node firstBarNode = secondFooNode.addNode("bar", "jcrmock:testnode");
        Node secondBarNode = secondFooNode.addNode("bar", "jcrmock:testnode");

        assert "/foo".equals(firstFooNode.getPath());
        assert "/foo[2]".equals(secondFooNode.getPath());
        assert "/foo[2]/bar".equals(firstBarNode.getPath());
        assert "/foo[2]/bar[2]".equals(secondBarNode.getPath());
    }
}
