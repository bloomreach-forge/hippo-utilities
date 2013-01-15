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

package org.onehippo.forge.utilities.hst.paging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DefaultPagination
 * @version $Id: DefaultPagination.java 92541 2010-08-05 10:22:01Z mmilicevic $
 */
public class DefaultPagination<T> extends Pageable {

    @SuppressWarnings("unused")
    private static Logger log = LoggerFactory.getLogger(DefaultPagination.class);

    public static final int DEFAULT_PAGE_FILL = 9;

    private List<T> items;

    @SuppressWarnings({"RawUseOfParameterizedType"})
    private static final DefaultPagination EMPTY_IMMUTABLE = new DefaultPagination(0, true);

    /**
     * Returns empty immutable collection
     * @param <E> return type
     * @return empty, immutable list
     */
    @SuppressWarnings({"unchecked"})
    public static <E> DefaultPagination<E> emptyCollection() {
        return (DefaultPagination<E>) EMPTY_IMMUTABLE;
    }

    private DefaultPagination(final int total, final boolean empty) {
        super(total);
        if (empty) {
            items = Collections.emptyList();
        }
    }

    public DefaultPagination(final int total) {
        super(total);
        items = new ArrayList<T>();
    }

    public DefaultPagination(int total, List<T> items) {
        super(total);
        this.items = new ArrayList<T>(items);
    }

    public void addItem(T item) {
        items.add(item);
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }

    /**
     * Default page range for given page
     * @param page current page
     * @return page surrounded by results on both side e.g. {@literal 1, 2, 3, 4 &lt;selected page&gt; 5, 6 ,7 ,8,9
     *         etc.>}
     * @see #DEFAULT_PAGE_FILL
     */
    public List<Long> getPageRange(final int page) {
        return getPageRangeWithFill(page, DEFAULT_PAGE_FILL);
    }

    /**
     * Default Page range for current selected page, it is "google alike" page range with x pages before selected item
     * and x+1 after selected item.
     * @return range based on default fill {@literal 1, 2, 3, 4, 5 <selected 6>, 7, 8,9 etc. }
     * @see #DEFAULT_PAGE_FILL
     */
    public List<Long> getCurrentRange() {
        return getPageRangeWithFill(getCurrentPage(), DEFAULT_PAGE_FILL);
    }

    /**
     * Return previous X and next X pages for given page, based on total pages.
     * @param page selected page
     * @param fillIn selected page
     * @return page range for given page
     */
    public List<Long> getPageRangeWithFill(long page, final int fillIn) {
        final List<Long> pages = new ArrayList<Long>();
        // do bound checking
        if (page < 0) {
            page = 1;
        }
        if (page > getTotalPages()) {
            page = getTotalPages();
        }
        // fill in lower range: e.g. for 2 it will  be 1
        long start = page - fillIn;
        if (start <= 0) {
            start = 1;
        }
        // end part:
        long end = page + fillIn + 1;
        if (end > getTotalPages()) {
            end = getTotalPages();
        }
        for (long i = start; i <= end; i++) {
            pages.add(i);
        }
        return pages;
    }
}


