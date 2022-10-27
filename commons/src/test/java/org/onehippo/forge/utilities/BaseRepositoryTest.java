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

package org.onehippo.forge.utilities;


import java.io.File;
import java.net.URL;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.apache.jackrabbit.core.TransientRepository;
import org.apache.jackrabbit.core.config.RepositoryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

/**
 * BaseTest
 *
 * @version $Id: BaseRepositoryTest.java 88118 2010-06-10 14:18:20Z mmilicevic $
 */
@Test
public class BaseRepositoryTest {


    private static Logger log = LoggerFactory.getLogger(BaseRepositoryTest.class);


    private Session session;
    private static String configFileName = "repository.xml";
    private static URL resource = BaseRepositoryTest.class.getClassLoader().getResource(configFileName);

    protected File storageDirectory;
    protected static TransientRepository transientRepository;

    @BeforeSuite(alwaysRun = true)
    protected void setup() throws Exception {
        log.info(">>>>>>>>>>>> running setup");
        storageDirectory = new File(System.getProperty("java.io.tmpdir"), "jcr");
        deleteDirectory(storageDirectory);
        transientRepository = new TransientRepository(RepositoryConfig.create(resource.toURI(), storageDirectory.getAbsolutePath()));
    }

    @AfterSuite(alwaysRun = true)
    protected void tearDown() throws Exception {
        log.info("<<<<<<<<<<<< running tear down");
        storageDirectory = null;
        if (session != null) {
            session.logout();
        }
        transientRepository.shutdown();
        transientRepository = null;
    }

    public boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    boolean deleted = file.delete();
                    if (!deleted) {
                        log.error("Failed to delete: {}", file.getAbsolutePath());
                    }
                }
            }
        }
        return path.delete();
    }

    public Session getSession() throws RepositoryException {
        if (session != null) {
            return session;
        }
        session = transientRepository.login(new SimpleCredentials("admin", "admin".toCharArray()));
        return session;
    }
}
