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

package org.onehippo.forge.utilities.commons.ftp;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockftpserver.fake.filesystem.UnixFakeFileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

/**
 * SimpleFtpClientTest
 *
 * @version $Id$
 */
public class SimpleFtpClientTest {

    @SuppressWarnings("unused")
    private static Logger log = LoggerFactory.getLogger(SimpleFtpClientTest.class);
    private static final int SERVER_CONTROL_PORT = 1222;
    private static final String TEST_FILE_TXT = "test_file.txt";
    private static final String TESTING_DIR = "/testing/testingnested/onemore";
    private static final String TXT = "foobar";
    private static final String USERHOME = "/userhome";


    @Test
    public void testSaveToFtp() throws Exception {
        FakeFtpServer server = new FakeFtpServer();
        server.setServerControlPort(SERVER_CONTROL_PORT);
        UnixFakeFileSystem fileSystem = new UnixFakeFileSystem();
        fileSystem.add(new DirectoryEntry(USERHOME));
        fileSystem.add(new DirectoryEntry("/userhome" + TESTING_DIR));
        fileSystem.add(new FileEntry("/userhome" + TESTING_DIR + "/tst.txt", TXT));
        server.setFileSystem(fileSystem);
        String user = "user";
        String password = "password";
        server.addUserAccount(new UserAccount(user, password, USERHOME));

        server.start();
        SimpleFtpClient client = new SimpleFtpClient(user, password, "localhost", SERVER_CONTROL_PORT);
        InputStream stream = new ByteArrayInputStream(TXT.getBytes("UTF-8"));
        SimpleFtpClientResult result = client.saveFile(stream, TEST_FILE_TXT, TESTING_DIR, false, false, true);
        assertTrue(result == SimpleFtpClientResult.CREATED, " expected created, got " + result);
        result = client.saveFile(stream, TEST_FILE_TXT, TESTING_DIR, false, false, true);
        assertTrue(result == SimpleFtpClientResult.CREATED, " expected created, got " + result);
        result = client.saveFile(stream, TEST_FILE_TXT, TESTING_DIR, false, true, true);
        assertTrue(result == SimpleFtpClientResult.FILE_OVERWRITTEN, " expected overwritten, got " + result);
        String file = client.getString("tst.txt", TESTING_DIR);
        assertTrue(file.equals(TXT), "Expected: " + TXT + " but got: " + file);

        client.close();
    }
}
