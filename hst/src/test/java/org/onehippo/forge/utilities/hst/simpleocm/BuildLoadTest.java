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

package org.onehippo.forge.utilities.hst.simpleocm;


import java.util.HashMap;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.hippoecm.hst.content.beans.ContentNodeBindingException;
import org.onehippo.forge.utilities.commons.jcrmockup.JcrMockUp;
import org.onehippo.forge.utilities.hst.simpleocm.build.NodeBuilder;
import org.onehippo.forge.utilities.hst.simpleocm.build.NodeBuilderImpl;
import org.onehippo.forge.utilities.hst.simpleocm.load.BeanLoader;
import org.onehippo.forge.utilities.hst.simpleocm.load.BeanLoaderImpl;
import org.onehippo.forge.utilities.hst.simpleocm.model.Agenda;
import org.onehippo.forge.utilities.hst.simpleocm.model.ExtraAgenda;
import org.onehippo.forge.utilities.hst.simpleocm.model.HippoHtml;
import org.onehippo.forge.utilities.hst.simpleocm.model.Preference;
import org.onehippo.forge.utilities.hst.simpleocm.model.User;
import org.testng.annotations.Test;

public class BuildLoadTest {

    @Test
    public void testMap() throws RepositoryException, ContentNodeBindingException {
        Session session = JcrMockUp.mockJcrSession("/content.xml");

        HashMap<String, Preference> preferences = new HashMap<String, Preference>();
        Preference preference = new Preference();
        preference.setKey("foo2");
        preference.setValue("bar2");
        preferences.put("foo2", preference);

        User user = new User();
        user.setPreferences(preferences);

        NodeBuilderImpl nodeBuilder = new NodeBuilderImpl();
        nodeBuilder.build(session.getRootNode(), "jannis", user);

        BeanLoaderImpl beanLoader = new BeanLoaderImpl();
        User loadedUser = new User();
        beanLoader.loadBean((Node) session.getItem("/jannis"), loadedUser);

        assert "bar2".equals(loadedUser.getPreferences().get("foo2").getValue());
    }

    @Test
    public void testInheritanceLoader() throws RepositoryException, ContentNodeBindingException {
        Session session = JcrMockUp.mockEmptySession();
        Node rootNode = session.getRootNode();

        ExtraAgenda extraAgenda = new ExtraAgenda();
        extraAgenda.setExtraAgendaProperty("extra agenda");
        extraAgenda.setBooleanField(Boolean.TRUE);

        NodeBuilder nodeBuilder = new NodeBuilderImpl();
        nodeBuilder.build(rootNode, "extraAgenda", extraAgenda);

        ExtraAgenda extraAgenda2 = new ExtraAgenda();
        BeanLoader beanLoader = new BeanLoaderImpl();
        beanLoader.loadBean(rootNode.getNode("extraAgenda"), extraAgenda2);

        assert extraAgenda.getBooleanField() == extraAgenda2.getBooleanField();
        assert extraAgenda.getExtraAgendaProperty().equals(extraAgenda2.getExtraAgendaProperty());
    }

    @Test
    public void testCollectionUpdate() throws RepositoryException, ContentNodeBindingException {
        Session session = JcrMockUp.mockJcrSession("/content.xml");
        // load bean
        BeanLoader beanLoader = new BeanLoaderImpl();
        Agenda agenda = new Agenda();
        Node agendaNode = (Node) session.getItem("/14-9-wie-denk-je-wel-dat-je-bent/14-9-wie-denk-je-wel-dat-je-bent");
        beanLoader.loadBean(agendaNode, agenda);

        // change bean
        HippoHtml html1 = new HippoHtml();
        html1.content = "content1";
        agenda.paragraphs.add(html1);

        // write bean back
        NodeBuilder nodeBuilder = new NodeBuilderImpl();
        nodeBuilder.bind(agenda, agendaNode);

        // add second node and update
        HippoHtml html2 = new HippoHtml();
        html2.content = "content2";
        agenda.paragraphs.add(html2);
        nodeBuilder.bind(agenda, agendaNode);

        int containsContent1 = 0;
        int containsContent2 = 0;
        NodeIterator nodeIterator = agendaNode.getNodes("jcrmockup:paragraph");
        while (nodeIterator.hasNext()) {
            Node paragraphNode = nodeIterator.nextNode();
            if (html1.content.equals(paragraphNode.getProperty("hippostd:content").getString())) {
                containsContent1++;
            }
            if (html2.content.equals(paragraphNode.getProperty("hippostd:content").getString())) {
                containsContent2++;
            }
        }
        assert containsContent1 == 1 && containsContent2 == 1;
    }

    @Test
    public void testConverter() throws RepositoryException, ContentNodeBindingException {
        final Session session = JcrMockUp.mockEmptySession();
        Agenda agenda = new Agenda();
        agenda.setObscuredString("hello world");

        NodeBuilder nodeBuilder = new NodeBuilderImpl();
        final Node agendaNode = nodeBuilder.build(session.getRootNode(), "agenda", agenda);

        String obscuredStringProperty = agendaNode.getProperty("jcrmockup:obscured").getString();
        assert obscuredStringProperty != null && !agenda.getObscuredString().equals(obscuredStringProperty);

        BeanLoader beanLoader = new BeanLoaderImpl();
        Agenda agenda2 = new Agenda();
        beanLoader.loadBean(session.getRootNode().getNode("agenda"), agenda2);

        assert agenda2.getObscuredString().equals(agenda.getObscuredString());
    }


}
