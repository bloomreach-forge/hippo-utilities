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

package org.onehippo.forge.utilities.hst.simpleocm;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.hippoecm.hst.content.beans.ContentNodeBindingException;
import org.onehippo.forge.utilities.RepositoryUtil;
import org.onehippo.forge.utilities.hst.simpleocm.load.BeanLoaderImpl;
import org.onehippo.forge.utilities.hst.simpleocm.model.Agenda;
import org.onehippo.forge.utilities.jcrmockup.JcrMockUp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;


public class BeanLoaderTest {

    private static final Logger log = LoggerFactory.getLogger(BeanLoaderTest.class);

    @Test
    public void testPrimitiveProperties() throws RepositoryException, ContentNodeBindingException {
        Agenda agenda = new Agenda();
        BeanLoaderImpl beanLoader = new BeanLoaderImpl();

        Session session = JcrMockUp.mockJcrSession("/content.xml");
        beanLoader.loadBean((Node) session.getItem("/14-9-wie-denk-je-wel-dat-je-bent/14-9-wie-denk-je-wel-dat-je-bent"), agenda);
        assert "14-9: ‘Wie denk je wel dat je bent!’".equals(agenda.getTitle());

    }

    @Test
    public void testChildNode() throws RepositoryException, ContentNodeBindingException {
        final Agenda agenda = new Agenda();
        BeanLoaderImpl beanLoader = new BeanLoaderImpl();

        final Session session = JcrMockUp.mockJcrSession("/content.xml");
        Node node = session.getRootNode().getNode("14-9-wie-denk-je-wel-dat-je-bent");
        node = RepositoryUtil.getDocumentVariant(node, "jcrmockup:agenda", null);
        beanLoader.loadBean(node, agenda);
        assert agenda.getBody() != null && agenda.getBody().getContent().contains("middagsymposium wordt vanuit");
    }

    @Test
    public void testPrimitiveCollections() throws RepositoryException, ContentNodeBindingException {
        Agenda agenda = new Agenda();
        Session session = JcrMockUp.mockJcrSession("/content.xml");
        BeanLoaderImpl beanLoader = new BeanLoaderImpl();
        beanLoader.loadBean((Node) session.getItem("/14-9-wie-denk-je-wel-dat-je-bent/14-9-wie-denk-je-wel-dat-je-bent"), agenda);

        assert agenda.paths.contains("3b2c1d74-46cb-4b3f-ab05-b505095bcf43");
    }

}
