/*
 * Copyright 2011 Hippo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onehippo.forge.utilities.simpleocm.test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.hippoecm.hst.content.beans.ContentNodeBindingException;
import org.onehippo.forge.utilities.jcrmockup.JcrMockUp;
import org.onehippo.forge.utilities.simpleocm.build.NodeBuilder;
import org.onehippo.forge.utilities.simpleocm.build.NodeBuilderImpl;
import org.onehippo.forge.utilities.simpleocm.test.model.Agenda;
import org.onehippo.forge.utilities.simpleocm.test.model.ExtraAgenda;
import org.onehippo.forge.utilities.simpleocm.test.model.FooNoSameName;
import org.onehippo.forge.utilities.simpleocm.test.model.FooSameName;
import org.onehippo.forge.utilities.simpleocm.test.model.HippoHtml;
import org.onehippo.forge.utilities.simpleocm.test.model.Preference;
import org.onehippo.forge.utilities.simpleocm.test.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 * @version "$Id: NodeBuilderTest.java 103480 2011-01-19 21:27:01Z jbloemendal $"
 */
public class NodeBuilderTest {

    private static final Logger log = LoggerFactory.getLogger(NodeBuilderTest.class);

    @Test
    public void basicTest() throws RepositoryException, ContentNodeBindingException {
        NodeBuilderImpl nodeBuilder = new NodeBuilderImpl();

        Agenda agenda = new Agenda();
        agenda.title = "agenda";
        agenda.body = new HippoHtml();
        agenda.body.content = "html content";
        agenda.doubleNumber = 4.2;
        agenda.booleanField = Boolean.FALSE;
        agenda.longNumber = Long.MAX_VALUE;
        agenda.calendar = Calendar.getInstance();

        Session session = JcrMockUp.mockJcrSession("/content.xml");
        Node newAgendaNode = nodeBuilder.build(session.getRootNode(), "newNode", agenda);

        Node node = (Node) session.getItem("/newNode");
        assert "agenda".equals(node.getProperty("jcrmockup:title").getString());

        Node childNode = (Node) session.getItem("/newNode/jcrmockup:body");
        assert agenda.body.content.equals(childNode.getProperty("hippostd:content").getString());
        assert agenda.doubleNumber.equals(newAgendaNode.getProperty("jcrmockup:double").getDouble());
        assert agenda.booleanField.equals(newAgendaNode.getProperty("jcrmockup:boolean").getBoolean());
        assert agenda.longNumber.equals(newAgendaNode.getProperty("jcrmockup:long").getLong());
        assert agenda.calendar.getTime().equals(newAgendaNode.getProperty("jcrmockup:date").getDate().getTime());
    }

    @Test
    public void testMap() throws RepositoryException, ContentNodeBindingException {
        HashMap<String, Preference> preferences = new HashMap<String, Preference>();
        Preference preference = new Preference();
        preference.setKey("foo2");
        preference.setValue("bar2");
        preferences.put("foo2", preference);

        preference = new Preference();
        preference.setKey("foo3");
        preference.setValue("bar3");
        preferences.put("foo3", preference);

        User user = new User();
        user.setPreferences(preferences);

        NodeBuilderImpl nodeBuilder = new NodeBuilderImpl();
        Session session = JcrMockUp.mockJcrSession("/content.xml");
        nodeBuilder.build(session.getRootNode(), "jannis", user);

        Node node = (Node) session.getItem("/jannis/foo2");
        assert "bar2".equals(node.getProperty("jcrmockup:value").getString());
    }

    @Test
    public void testMapUpdate() throws RepositoryException, ContentNodeBindingException {
        HashMap<String, Preference> preferences = new HashMap<String, Preference>();
        Preference preference = new Preference();
        preference.setKey("foo2");
        preference.setValue("bar2");
        preferences.put("foo2", preference);

        preference = new Preference();
        preference.setKey("foo3");
        preference.setValue("bar3");
        preferences.put("foo3", preference);

        User user = new User();
        user.setPreferences(preferences);

        NodeBuilderImpl nodeBuilder = new NodeBuilderImpl();
        Session session = JcrMockUp.mockJcrSession("/content.xml");

        nodeBuilder.build(session.getRootNode(), "jannis", user);
        Node userNode = (Node) session.getItem("/jannis");
        assert userNode.hasNode("foo3");

        preferences.remove("foo3");

        preference.setKey("foo2");
        preference.setValue("foo_overwrite");
        preferences.put("foo2", preference);

        nodeBuilder.bind(user, userNode);

        Node node = (Node) session.getItem("/jannis/foo2");
        assert "foo_overwrite".equals(node.getProperty("jcrmockup:value").getString());

        assert !userNode.hasNode("foo3");
    }

    @Test
    public void testList() throws RepositoryException, ContentNodeBindingException {
        Agenda agenda = new Agenda();
        agenda.tags = new ArrayList<String>();
        agenda.tags.add("foo");
        agenda.tags.add("bar");

        NodeBuilderImpl nodeBuilder = new NodeBuilderImpl();
        Session session = JcrMockUp.mockJcrSession("/content.xml");

        nodeBuilder.build(session.getRootNode(), "agenda1", agenda);
        Node node = (Node) session.getItem("/agenda1");
        Property property = node.getProperty("jcrmockup:tags");
        for (Value value : property.getValues()) {
            assert "foo".equals(value.getString()) || "bar".equals(value.getString());
        }
    }

    @Test
    public void testCompoundList() throws RepositoryException, ContentNodeBindingException {
        Session session = JcrMockUp.mockJcrSession("/content.xml");
        NodeBuilderImpl nodeBuilder = new NodeBuilderImpl();

        Agenda agenda = new Agenda();
        agenda.paragraphs = new ArrayList<HippoHtml>();

        HippoHtml p1 = new HippoHtml();
        p1.content = "p1 content";
        agenda.paragraphs.add(p1);

        HippoHtml p2 = new HippoHtml();
        p2.content = "p2 content";
        agenda.paragraphs.add(p2);

        nodeBuilder.build(session.getRootNode(), "agenda2", agenda);
        Node node = (Node) session.getItem("/agenda2");
        NodeIterator iterator = node.getNodes("jcrmockup:paragraph");
        assert iterator.hasNext();
        while (iterator.hasNext()) {
            Node childNode = iterator.nextNode();
            assert p1.content.equals(childNode.getProperty("hippostd:content").getString()) || p2.content.equals(childNode.getProperty("hippostd:content").getString());
        }
    }

    @Test
    public void testUpdate() throws RepositoryException, ContentNodeBindingException {
        NodeBuilderImpl nodeBuilder = new NodeBuilderImpl();

        Agenda agenda = new Agenda();
        agenda.title = "agenda";
        agenda.body = new HippoHtml();
        agenda.body.content = "html content";

        Session session = JcrMockUp.mockJcrSession("/content.xml");
        nodeBuilder.build(session.getRootNode(), "newNode", agenda);

        agenda.body.content = "html content update";
        nodeBuilder.build(session.getRootNode(), "newNode", agenda);

        Node node = (Node) session.getItem("/newNode");
        assert "agenda".equals(node.getProperty("jcrmockup:title").getString());

        assert session.getRootNode().getNodes("newNode").getSize() == 1;

        Node childNode = (Node) session.getItem("/newNode/jcrmockup:body");
        assert agenda.body.content.equals(childNode.getProperty("hippostd:content").getString());
    }

    @Test
    public void testInheritance() throws RepositoryException, ContentNodeBindingException {
        Session session = JcrMockUp.mockEmptySession();
        Node rootNode = session.getRootNode();

        ExtraAgenda extraAgenda = new ExtraAgenda();
        extraAgenda.setExtraAgendaProperty("extra agenda");
        extraAgenda.setBooleanField(Boolean.TRUE);

        NodeBuilder nodeBuilder = new NodeBuilderImpl();
        nodeBuilder.build(rootNode, "extraAgenda", extraAgenda);

        assert rootNode.hasNode("extraAgenda");
        Node extraAgendaNode = rootNode.getNode("extraAgenda");
        assert extraAgendaNode.getProperty("jcrmockup:boolean").getBoolean();
        assert extraAgendaNode.getProperty("jcr:extraAgendaProperty").getString().equals(extraAgenda.getExtraAgendaProperty());
    }

    @Test
    public void testSameNameSiblings() throws RepositoryException, ContentNodeBindingException {
        Session session = JcrMockUp.mockEmptySession();
        NodeBuilder nodeBuilder = new NodeBuilderImpl();

        FooSameName foo = new FooSameName();
        foo.setStringProperty("foo1");

        nodeBuilder.build(session.getRootNode(), "foo", foo);

        FooSameName foo2 = new FooSameName();
        foo2.setStringProperty("foo2");

        nodeBuilder.build(session.getRootNode(), "foo", foo2);

        Set<String> stringProperties = new HashSet<String>();
        stringProperties.add(foo.getStringProperty());
        stringProperties.add(foo2.getStringProperty());

        boolean sameName = false;
        NodeIterator nodeIterator = session.getRootNode().getNodes();
        while (nodeIterator.hasNext()) {
            Node node = nodeIterator.nextNode();
            stringProperties.remove(node.getProperty("jcrmockup:string").getString());
            if ("foo".equals(node.getName())) {
                sameName = true;
            } else {
                sameName = false;
                break;
            }
        }

        assert sameName;
        assert stringProperties.size() == 0;
    }

    private void testSameNameSiblings2() throws RepositoryException, ContentNodeBindingException {
         Session session = JcrMockUp.mockEmptySession();
        NodeBuilder nodeBuilder = new NodeBuilderImpl();

        FooNoSameName foo = new FooNoSameName();
        foo.setStringProperty("foo1");

        nodeBuilder.build(session.getRootNode(), "foo", foo);

        FooNoSameName foo2 = new FooNoSameName();
        foo2.setStringProperty("foo2");

        nodeBuilder.build(session.getRootNode(), "foo", foo2);

        Node node = null;
        NodeIterator nodeIterator = session.getRootNode().getNodes();
        if (nodeIterator.hasNext()) {
            node = nodeIterator.nextNode();
        }

        assert node != null;
        assert "foo".equals(node.getName());
        assert !nodeIterator.hasNext();
        assert "foo2".equals(node.getProperty("jcrmockup:string").getString());
    }

}
