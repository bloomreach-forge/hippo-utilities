/*
 * Copyright 2018 Hippo B.V. (http://www.onehippo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onehippo.forge.utilities.repository.scheduling;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.hippoecm.repository.api.HippoNodeType;
import org.hippoecm.repository.util.JcrUtils;
import org.onehippo.repository.scheduling.RepositoryJob;
import org.onehippo.repository.scheduling.RepositoryJobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Generic Updater Executing Scheduler Job implementation.
 * <P>
 * Configuration example (assuming that you have an updater at <CODE>/hippo:configuration/hippo:update/hippo:registry/DemoUpdater</CODE>):
 * </P>
 * <PRE>
 * /hippo:configuration/hippo:modules/scheduler/hippo:moduleconfig/system/demoUpdaterExecutor:
 *   jcr:primaryType: hipposched:repositoryjob
 *   hipposched:attributeNames: [updatername]
 *   hipposched:attributeValues: [DemoUpdater]
 *   hipposched:enabled: true
 *   hipposched:repositoryJobClass: org.onehippo.forge.utilities.repository.scheduling.UpdaterExecutingJob
 *   /hipposched:triggers:
 *     jcr:primaryType: hipposched:triggers
 *     /nightly:
 *       jcr:primaryType: hipposched:crontrigger
 *       jcr:mixinTypes: ['hippo:lockable', 'mix:lockable']
 *       hipposched:cronExpression: 0 0 3 * * ?
 *       hipposched:enabled: true
 * </PRE>
 */
public class UpdaterExecutingJob implements RepositoryJob {

    private static final Logger log = LoggerFactory.getLogger(UpdaterExecutingJob.class);

    private static final String CONFIG_UPDATER_NAME = "updatername";

    private static final String UPDATE_PATH = "/hippo:configuration/hippo:update";
    private static final String UPDATE_QUEUE_PATH = UPDATE_PATH + "/hippo:queue";
    private static final String UPDATE_REGISTRY_PATH = UPDATE_PATH + "/hippo:registry";

    @Override
    public void execute(final RepositoryJobExecutionContext context) throws RepositoryException {
        String updaterName = context.getAttribute(CONFIG_UPDATER_NAME);

        if (updaterName != null) {
            updaterName = updaterName.trim();
        }

        if (updaterName == null || updaterName.isEmpty()) {
            log.warn("Skipping updater executing job due to the blank updater name configuration.");
            return;
        }

        Session session = null;

        try {
            session = context.createSystemSession();

            final Node queueNode = session.getNode(UPDATE_QUEUE_PATH);
            final Node registryNode = session.getNode(UPDATE_REGISTRY_PATH);

            if (!registryNode.hasNode(updaterName)) {
                log.error("Skipping updater script execution because the configured updater node doesn't exist at {}/{}.",
                        UPDATE_REGISTRY_PATH, updaterName);
                return;
            }

            final Node registeredUpdaterNode = registryNode.getNode(updaterName);
            final Node queuedUpdaterNode = JcrUtils.copy(registeredUpdaterNode, registeredUpdaterNode.getName(),
                    queueNode);
            queuedUpdaterNode.setProperty(HippoNodeType.HIPPOSYS_STARTEDBY, session.getUserID());
            session.save();
        } finally {
            if (session != null) {
                session.logout();
            }
        }
    }

}