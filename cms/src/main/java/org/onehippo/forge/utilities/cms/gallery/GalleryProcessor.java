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

package org.onehippo.forge.utilities.cms.gallery;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.hippoecm.frontend.plugins.gallery.model.DefaultGalleryProcessor;
import org.hippoecm.frontend.plugins.gallery.model.GalleryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GalleryProcessor extends DefaultGalleryProcessor {

    private static final long serialVersionUID = 1L;
    protected static final Logger log = LoggerFactory.getLogger(GalleryProcessor.class);

    protected String namespace;  
    protected String imagePath;
    protected String primaryThumbnail;
    protected final List<Thumbnail> thumbnails = new ArrayList<Thumbnail>(3);  

    private static final String JCR_DATA_PROPERTY = "jcr:data";
    private static final String JCR_MIME_TYPE_PROPERTY = "jcr:mimeType";
    private static final String JCR_LAST_MODIFIED_PROPERTY = "jcr:lastModified";
    private static final String IMAGE_MIME_TYPE = "image";


    public void setNamespace(final String namespace) {
        if (namespace.endsWith(":")) {
            this.namespace = namespace;
        }
        else {
            this.namespace = namespace + ":";
        }    
    }

    public void setImagePath(final String imagePath) {
        this.imagePath = imagePath;
    }

    public void setPrimaryThumbnail(final String primaryThumbnail) {
        this.primaryThumbnail = primaryThumbnail;
    }
    
    public void addThumbnail(String name, int size) {
        thumbnails.add(new Thumbnail(name, size));
    }

    @Override
    protected void makeRegularImage(Node node, String name, InputStream istream, String mimeType, Calendar lastModified)
            throws RepositoryException {

        // this method is called for all properties of type hippo:resource
        // besides the primary item, which should be a thumbnail

        if (!node.hasNode(name)) {

            if ((namespace + imagePath).equals(name)) {
                final Node child = node.addNode(name);
                child.setProperty(JCR_DATA_PROPERTY, istream);
                child.setProperty(JCR_MIME_TYPE_PROPERTY, mimeType);
                child.setProperty(JCR_LAST_MODIFIED_PROPERTY, lastModified);
            }
            else {
                for (Thumbnail thumbnail : thumbnails) {
                    
                    // skip primary, makeThumbnailImage is called for that one
                    if (thumbnail.path.equals(primaryThumbnail)) {
                        continue;
                    }    
                    
                    if ((namespace + thumbnail.path).equals(name)) {
                        final Node child = node.addNode(name);
                        child.setProperty(JCR_LAST_MODIFIED_PROPERTY, lastModified);
                        doMakeThumbnailImage(child, istream, thumbnail.size, mimeType);
                    }
                }
            }
        }
    }

    @Override
    protected void makeThumbnailImage(Node node, InputStream resourceData, String mimeType) throws GalleryException,
            RepositoryException {

        // this method is called for the primary item in cnd, which should be a 
        // thumbnail
        
        for (Thumbnail thumbnail : thumbnails) {
            if (thumbnail.path.equals(primaryThumbnail)) {
                doMakeThumbnailImage(node, resourceData, thumbnail.size, mimeType);
            }
        }
        
    }

    private void doMakeThumbnailImage(Node node, InputStream resourceData, int size, String mimeType)
            throws RepositoryException {
        if (mimeType.startsWith(IMAGE_MIME_TYPE)) {
            try {
                InputStream thumbnail = this.createThumbnail(resourceData, size, mimeType);
                node.setProperty(JCR_DATA_PROPERTY, thumbnail);
            }
            catch (GalleryException ex) {
                log.warn("exception creating thumbnail", ex);
                node.setProperty(JCR_DATA_PROPERTY, resourceData);
            }
        }
        else {
            node.setProperty(JCR_DATA_PROPERTY, resourceData);
        }
        node.setProperty(JCR_MIME_TYPE_PROPERTY, mimeType);
    }

    
    public class Thumbnail {
        public String path;
        public int size;
        
        public Thumbnail(String path, int size) {
            this.path = path;
            this.size = size;
        }
    }


}
