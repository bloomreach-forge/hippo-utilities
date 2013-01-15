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

import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.Plugin;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GalleryProcessorPlugin extends Plugin {
    @SuppressWarnings("unused")
    private static final String SVN_ID = "$Id: GalleryProcessorPlugin.java 448 2011-06-15 16:59:16Z jjoachimsthal $";
    
    private static final Logger LOG = LoggerFactory.getLogger(GalleryProcessorPlugin.class);

    protected static final String NAMESPACE = "namespace";
    protected static final String IMAGE_PATH = "image.path";
    protected static final String THUMBNAIL_PRIMARY = "thumbnail.primary";
    protected static final String THUMBNAILS = "thumbnails";
    protected static final String THUMBNAIL_PREFIX = "thumbnail.";
    protected static final String THUMBNAIL_SIZE_POSTFIX = ".size";

    private static final long serialVersionUID = 1L;

    public GalleryProcessorPlugin(IPluginContext context, IPluginConfig config) {
        super(context, config);

        GalleryProcessor processor = new GalleryProcessor();

        if (config.containsKey(NAMESPACE)) {
            processor.setNamespace(config.getString(NAMESPACE).trim());
        }
        else {
            LOG.error("Misconfiguration of GalleryProcessorPlugin: needs item '" + NAMESPACE + "'");
        }    
        
        if (config.containsKey(IMAGE_PATH)) {
            processor.setImagePath(config.getString(IMAGE_PATH).trim());
        }
        else {
            LOG.error("Misconfiguration of GalleryProcessorPlugin: needs item '" + IMAGE_PATH + "'");
        }    
        
        if (config.containsKey(THUMBNAIL_PRIMARY)) {
            processor.setPrimaryThumbnail(config.getString(THUMBNAIL_PRIMARY).trim());
        }
        else {
            LOG.error("Misconfiguration of GalleryProcessorPlugin: needs item '" + THUMBNAIL_PRIMARY + "'");
        }    
        
        if (config.containsKey(THUMBNAILS)) {
            
            final String thumbnails = config.getString(THUMBNAILS);
            
            final String[] thumbnailNames = thumbnails.split(",");
            for (String thumbnailName : thumbnailNames) {

                final String thumbnail = thumbnailName.trim();
                final String sizeKey = THUMBNAIL_PREFIX + thumbnail + THUMBNAIL_SIZE_POSTFIX;
                
                if (config.containsKey(sizeKey)) {
                    processor.addThumbnail(thumbnail, config.getInt(sizeKey));
                }
                else {
                    LOG.warn("Misconfiguration of GalleryProcessorPlugin: lacking item '" + sizeKey + "'");
                }    
            }
        }
        
        context.registerService(processor, config.getString("gallery.processor.id", "gallery.processor.service"));
    }

}
