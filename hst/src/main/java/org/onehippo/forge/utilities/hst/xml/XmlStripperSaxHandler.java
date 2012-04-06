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

package org.onehippo.forge.utilities.hst.xml;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class XmlStripperSaxHandler extends DefaultHandler {
    private StringBuffer buffer = null;
    private boolean isLastCharacterWhitespace = true;
    
    public XmlStripperSaxHandler() {
        super();
    }
    
    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
        buffer = new StringBuffer();
        isLastCharacterWhitespace = true;
    }
    
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        for(int i = start; i < (start + length) && i  < ch.length; i++) {
            if(isWhitespaceCharacter(ch[i])) {
                if(!isLastCharacterWhitespace) {
                    addWhitespaceCharacterToBuffer();
                }
                isLastCharacterWhitespace = true;
            } else {
                isLastCharacterWhitespace = false;
                addCharacterToBuffer(ch[i]);
            }
        }
    }
    
    protected void addCharacterToBuffer(char character) {
        buffer.append(character);
    }
    
    protected void addWhitespaceCharacterToBuffer() {
        addCharacterToBuffer(' ');
    }
    
    protected boolean isWhitespaceCharacter(char character) {
        if(character == ' ' || character == '\n' || character == '\r' || character == '\t') {
            return true;
        }
        return false;
    }
    
    public String getString() {
        if(buffer != null) {
            return buffer.toString();
        }
        return null;
    }
}
