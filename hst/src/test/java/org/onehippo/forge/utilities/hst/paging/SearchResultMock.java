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

package org.onehippo.forge.utilities.hst.paging;

import java.util.Date;

/**
 * SearchResultMock
 *
 * @version $Id: SearchResultMock.java 88059 2010-06-10 10:23:38Z mmilicevic $
 */
public class SearchResultMock<T> {

    private T item;
    private String title;
    private String text;

    public SearchResultMock(final T item) {
        this.item = item;
    }

    public SearchResultMock(T item, String title, String text, Date date) {
        this.item = item;
        this.title = title;
        this.text = text;
    }


    public T getItem() {
        return item;
    }

    public void setItem(T item) {
        this.item = item;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("SearchResultMock");
        sb.append("{item=").append(item);
        sb.append(", title='").append(title).append('\'');
        sb.append(", text='").append(text).append('\'');
        sb.append('}');
        return sb.toString();
    }
}

