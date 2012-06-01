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

package org.onehippo.forge.utilities.commons.ftp;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import com.google.common.base.Splitter;

import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SimpleFtpClient
 *
 * @version $Id$
 */
public class SimpleFtpClient implements Closeable {
    @SuppressWarnings("unused")
    private static Logger log = LoggerFactory.getLogger(SimpleFtpClient.class);

    private final String userName;
    private final String password;
    private final String server;
    private final int port;
    private final FTPClient client;
    private String homeDirectory;

    /**
     * Constructor if port is different than port 21
     *
     * @param userName login name
     * @param password login password
     * @param server   server address (name or ip address))
     * @param port     port number
     */
    public SimpleFtpClient(final String userName, final String password, final String server, final int port) {
        this.userName = userName;
        this.password = password;
        this.server = server;
        this.port = port;
        this.client = new FTPClient();
    }

    /**
     * Uses port 21 as default
     *
     * @param userName login name
     * @param password login password
     * @param server   server address (name or ip address))
     */
    public SimpleFtpClient(final String userName, final String password, final String server) {
        this(userName, password, server, 21);
    }


    /**
     * Save binary file, which will overwrite existing file
     *
     * @param stream    input stream
     * @param fileName  name of the file
     * @param directory directory to save to
     * @return result message
     */
    public SimpleFtpClientResult overwriteBinary(final InputStream stream, final String fileName, final String directory) {
        return saveFile(stream, fileName, homeDirectory, true, true, true);
    }

    /**
     * Save binary file. If file with the same name exists, a new one, with unique name will be created
     *
     * @param stream    input stream
     * @param fileName  name of the file
     * @param directory directory to save to
     * @return result message
     */
    public SimpleFtpClientResult saveUniqueBinary(final InputStream stream, final String fileName, final String directory) {
        return saveFile(stream, fileName, directory, true, true, true);
    }

    /**
     * Save ASCII file, which will existing  file
     *
     * @param stream    input stream
     * @param fileName  name of the file
     * @param directory directory to save to
     * @return result message
     */

    public SimpleFtpClientResult overwriteAscii(final InputStream stream, final String fileName, final String directory) {
        return saveFile(stream, fileName, directory, false, true, true);
    }

    /**
     * Save ASCII file. If file with the same name exists, a new one, with unique name will be created
     *
     * @param stream    input stream
     * @param fileName  name of the file
     * @param directory directory to save to
     * @return result message
     */
    public SimpleFtpClientResult saveUniqueAscii(final InputStream stream, final String fileName, final String directory) {
        return saveFile(stream, fileName, directory, false, false, true);
    }

    /**
     * Save given stream into given directory
     *
     * @param stream            stream we want to save
     * @param fileName          name of the file
     * @param directory         directory file should be saved to.
     *                          Note: directory is relative to user home directory, even when provided as absolute directory (e.g. /foo/bar is treated as foo/bar)
     * @param binaryUpload      is this a binary upload
     * @param overwrite         overwrite existing file?
     * @param createDirectories create directories if not exist
     * @return SimpleFtpClientResult.CREATED if file created, SimpleFtpClientResult.FILE_OVERWRITTEN; if file overwritten, SimpleFtpClientResult.ERROR otherwise;
     */
    public SimpleFtpClientResult saveFile(final InputStream stream, final String fileName, final String directory, final boolean binaryUpload, final boolean overwrite, final boolean createDirectories) {

        try {
            connectClient();

            if (binaryUpload) {
                client.setFileType(FTP.BINARY_FILE_TYPE);
            }
            if (createDirectories) {
                SimpleFtpClientResult result = createDirectories(directory);
                if (result != SimpleFtpClientResult.CREATED) {
                    return SimpleFtpClientResult.FAILED_CREATE_DIR;
                }
            }
            changeToDir(directory);

            if (overwrite) {
                client.storeFile(fileName, stream);
                stream.close();
                return SimpleFtpClientResult.FILE_OVERWRITTEN;
            } else {
                // check if file exists:
                String[] names = client.listNames();
                boolean stored = false;
                for (String name : names) {
                    if (fileName.equals(name)) {
                        client.storeUniqueFile(fileName, stream);
                        stored = true;
                        break;
                    }
                }
                if (!stored) {
                    client.storeFile(fileName, stream);
                }

                stream.close();
                return SimpleFtpClientResult.CREATED;
            }


        } catch (IOException e) {
            log.error("Error saving file to FTP server: " + server, e);
            return SimpleFtpClientResult.ERROR;
        }

    }

    /**
     * Return input stream for given file name
     *
     * @param fileName  name of the remote file
     * @param directory remote directory
     * @return input stream or null
     */
    public InputStream getInputStream(final String fileName, final String directory) {

        try {
            connectClient();
            changeToDir(directory);
            return client.retrieveFileStream(fileName);

        } catch (IOException e) {
            log.error("Error retrieving file from FTP server: " + server, e);
        }
        return null;
    }

    public String getString(final String fileName, final String directory) {

        InputStream inputStream = null;
        try {
            connectClient();
            changeToDir(directory);
            inputStream = client.retrieveFileStream(fileName);
            String result = IOUtils.toString(inputStream);
            client.completePendingCommand();
            return result;

        } catch (IOException e) {
            log.error("Error retrieving file from FTP server: " + server, e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("Error closing stream", e);
                }

            }
        }
        return null;
    }

    /**
     * Changes working director
     *
     * @param directory directory (relative to user home dir)
     * @throws IOException on error
     */
    private void changeToDir(final String directory) throws IOException {
        Iterable<String> iterable = Splitter.on('/').omitEmptyStrings().split(directory);
        // note: important: we first go to user home
        client.changeWorkingDirectory(getHomeDirectory());
        for (String dir : iterable) {
            client.changeWorkingDirectory(dir);
        }
    }

    /**
     * Connects client if not connected yet. must be called before we do any actions
     *
     * @throws IOException on error
     */
    public final void connectClient() throws IOException {
        if (!client.isConnected()) {
            client.connect(server, port);
            client.login(userName, password);
            int reply = client.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                client.disconnect();
                log.error("Negative reply form FTP server, aborting, id was {}:", reply);
                throw new IOException("failed to connect to FTP server");
            }
            // initialize working directory
            homeDirectory = client.printWorkingDirectory();
        }
    }


    /**
     * Creates directories on FTP server
     *
     * @param directory directory to create
     * @return SimpleFtpClientResult.CREATED on succes SimpleFtpClientResult.ERROR on error
     * @throws IOException on error
     */
    private SimpleFtpClientResult createDirectories(final String directory) throws IOException {
        Iterable<String> iterable = Splitter.on('/').omitEmptyStrings().split(directory);
        for (String dir : iterable) {
            boolean dirExists = client.changeWorkingDirectory(dir);
            if (!dirExists) {
                // try to create directory:
                client.makeDirectory(dir);
                dirExists = client.changeWorkingDirectory(dir);
            }
            if (!dirExists) {
                log.error("failed to change FTP directory (forms), not doing anything");
                return SimpleFtpClientResult.ERROR;
            }
        }
        return SimpleFtpClientResult.CREATED;
    }


    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getServer() {
        return server;
    }

    public int getPort() {
        return port;
    }

    public void close() throws IOException {
        client.logout();
        client.disconnect();
    }

    public FTPClient getClient() {
        return client;
    }

    public String getHomeDirectory() {
        if (homeDirectory == null) {
            try {
                connectClient();
            } catch (IOException e) {
                log.error("Error getting home dir:", e);
            }
        }
        return homeDirectory;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("SimpleFtpClient");
        sb.append("{userName='").append(userName).append('\'');
        sb.append(", password='").append(password).append('\'');
        sb.append(", server='").append(server).append('\'');
        sb.append(", port=").append(port);
        sb.append('}');
        return sb.toString();
    }
}

