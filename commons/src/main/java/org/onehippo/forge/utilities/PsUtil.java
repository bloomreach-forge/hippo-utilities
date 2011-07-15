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

package org.onehippo.forge.utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Joiner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PsUtil
 *
 * @version $Id: PsUtil.java 92541 2010-08-05 10:22:01Z mmilicevic $
 */
public final class PsUtil {
    @SuppressWarnings("unused")
    private static Logger log = LoggerFactory.getLogger(PsUtil.class);

    public static final Map<String, String> mimeTypeToExtensionMap = 
        new HashMap<String , String>() {
            private static final long serialVersionUID = -3566373789286953339L;
        {
            put("text/plain",                    "txt");
            put("text/html",                     "html");
            put("application/pdf",               "pdf");
            put("application/zip",               "zip");
            put("application/msword",            "doc");
            put("application/vnd.ms-excel",      "xls");
            put("application/vnd.ms-powerpoint", "ppt");
            put("application/vnd.openxmlformats-officedocument.wordprocessingml.document",   "docx");
            put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",         "xlsx");
            put("application/vnd.openxmlformats-officedocument.presentationml.presentation", "pptx");
            put("application/vnd.oasis.opendocument.text",         "odt");
            put("application/vnd.oasis.opendocument.graphics",     "odg");
            put("application/vnd.oasis.opendocument.spreadsheet",  "ods");
            put("application/vnd.oasis.opendocument.presentation", "odp");
            put("application/vnd.oasis.opendocument.graphics",     "odg");
            put("application/vnd.oasis.opendocument.chart",        "odc");
            put("application/vnd.oasis.opendocument.formula",      "odf");
            put("application/vnd.oasis.opendocument.image",        "odi");
            put("application/vnd.oasis.opendocument.text-master",  "odm");
        }};

    public static final String DEFAULT_APPEND_TEXT = " ...";
    public static final String[] EMPTY_STRING_ARRAY = new String[0];
    
    /**
     * Abbreviate a text to _approximately_ some number of characters, trying to 
     * find a nice word end and then appending some string (defaulting to three dots).  
     */
    public static String abbreviateText(final String text, final int numberOfCharacters, final String appendText) {

        int nrChars = numberOfCharacters;

        if (nrChars > 0 && text != null && text.length() > nrChars) {

            // search a nice word end, _backwards_ from nrChars

            int spaceIndex = text.lastIndexOf(' ', nrChars);
            int dotIndex = text.lastIndexOf('.', nrChars);
            int commaIndex = text.lastIndexOf(',', nrChars);
            int questionIndex = text.lastIndexOf('?', nrChars);
            int exclamationIndex = text.lastIndexOf('!', nrChars);

            // set index to last space
            nrChars = spaceIndex;

            if (dotIndex > nrChars) {
                nrChars = dotIndex;
            }
            if (commaIndex > nrChars) {
                nrChars = commaIndex;
            }
            if (questionIndex > nrChars) {
                nrChars = questionIndex;
            }
            if (exclamationIndex > nrChars) {
                nrChars = exclamationIndex;
            }
            // final check for < 0
            if (nrChars < 0) {
                nrChars = 0;
            }

            if (appendText != null) {
                return text.substring(0, nrChars) + appendText;
            }
            else {
                return text.substring(0, nrChars) + DEFAULT_APPEND_TEXT;
            }
        }
        return text;
    }

    /**
     * Abbreviate a text to _approximately_ some number of characters, trying to 
     * find a nice word end and then appending three dots.  
     */
    public static String abbreviateText(final String text, final int numberOfCharacters) {
        return abbreviateText(text, numberOfCharacters, null);
    }

    /**
     * Concatenates a collection of strings by concatenating the strings and inserting a separator in between
     * each of them. Nulls are handled automatically and there is no separator at the end of sequence*
     *
     * @param strings   collection of strings (collection may contain null objects, those are ignored)
     * @param separator the separator
     * @return concatenated string
     */
    public static String concat(Collection<String> strings, String separator) {
        StringBuilder builder = new StringBuilder();
        Joiner.on(separator).skipNulls().appendTo(builder, strings);
        return builder.toString();
    }

    /**
     * concatenates an array of strings by concatenating the strings and inserting a separator in between
     * each of them. Nulls are handled automatically and there is no separator at the end of sequence
     *
     * @param strings   the strings
     * @param separator the separator
     * @return concatenated string
     */
    public static String concat(String[] strings, String separator) {
        StringBuilder builder = new StringBuilder();
        Joiner.on(separator).skipNulls().appendTo(builder, strings);
        return builder.toString();
    }

    /**
     * Remove empty (length zero when trimmed) values from a list. 
     */
    public static List<String> removeEmptyValues(final List<String> values) {
        
        if (values == null) {
            return null;
        }

        final List<String> result = new ArrayList<String>(values.size());
        for (String value : values) {
            if (!(value.trim().length() == 0)) {
                result.add(value);
            }
        }
        return result;
    }

    /**
     * Remove empty (length zero when trimmed) values from an array. 
     */
    public static String[] removeEmptyValues(final String[] values) {
        
        if (values == null) {
            return null;
        }
        
        final List<String> result = removeEmptyValues(Arrays.asList(values)); 
        return result.toArray(new String[result.size()]);
    }

    /**
     * Replaces <strong>{@code ${variableName}}</strong> variable with string replacement provided
     *
     * @param variableName     variable name
     * @param replacementValue replacement value
     * @param template         string which contains variable e.g
     *                         <p /><strong>{@code My name is ${username} and my login is ${login}}</strong>
     * @return string (template with string replacements)
     */
    public static String replacePlaceHolders(final String variableName, final String replacementValue,
            final String template) {
        Pattern pattern = Pattern.compile("(\\$\\{" + variableName + "*\\})");
        Matcher matcher = pattern.matcher(template);
        StringBuffer buffer = new StringBuffer();
        while ((matcher.find())) {
            matcher.appendReplacement(buffer, replacementValue);
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    /**
     * Simplify a genuine mime type into a simple one like "pdf", "image" or an extension like "doc".
     */
    public static String simplifyMimeType(final String mimeType) {

        String simplified = "";
        if (mimeType != null) {

            final String extension = getExtensionFromMimeType(mimeType);
            if (extension != null) {
                simplified = extension;
            }
            else if (mimeType.startsWith("image/")) {
                simplified = "image";
            }
            else if (mimeType.indexOf("pdf") > -1) {
                simplified = "pdf";
            }
        }

        return simplified;
    }

    /**
     * Get an extension from a mime type
     */
    public static String getExtensionFromMimeType(final String mimeType) {

        if (mimeType == null) {
            return null;
        }

        return mimeTypeToExtensionMap.get(mimeType); 
    }

}
