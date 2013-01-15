/*
 * Copyright 2012-2013 Hippo B.V. (http://www.onehippo.com)
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

package org.onehippo.forge.utilities.repository.scheduler.jcr;

import javax.jcr.Session;

import org.quartz.core.SchedulingContext;

/**
 * Scheduling context extending Quartz standard scheduling context, which has a JCR Session for retrieval by jobs.
 */
public class JCRSchedulingContext extends SchedulingContext {
    @SuppressWarnings("unused")

    private final Session session;

    public JCRSchedulingContext(Session session) {
        super();
        this.session = session;
    }

    /**
     * Get the JCR session
     * @return the JCR session
     */
    public Session getSession() {
        return session;
    }
}
