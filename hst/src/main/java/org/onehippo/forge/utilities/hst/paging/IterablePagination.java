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

package org.onehippo.forge.utilities.hst.paging;

import java.util.ArrayList;
import java.util.List;

import org.hippoecm.hst.content.beans.standard.HippoBean;
import org.hippoecm.hst.content.beans.standard.HippoBeanIterator;
import org.hippoecm.hst.content.beans.standard.HippoDocumentIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * IterablePagination
 * @version $Id: IterablePagination.java 92541 2010-08-05 10:22:01Z mmilicevic $
 */
public class IterablePagination<T extends HippoBean> extends Pageable {

    private static Logger log = LoggerFactory.getLogger(IterablePagination.class);

    private static final int DEFAULT_PAGE_RANGE = 10;

    private List<T> items;

    @SuppressWarnings({"unchecked"})
    public IterablePagination(final HippoBeanIterator beans, final int currentPage) {
        super(beans.getSize(), currentPage);
        items = new ArrayList<T>();
        process(beans);
    }

    public IterablePagination(final HippoBeanIterator beans, final int pageSize, final int currentPage) {
        super(beans.getSize(), currentPage, pageSize);
        items = new ArrayList<T>();
        process(beans);
    }

    public IterablePagination(int total, List<T> items) {
        super(total);
        this.items = new ArrayList<T>(items);
    }

    public IterablePagination(List<T> items, final int pageSize, final int currentPage) {
        super(items.size(), currentPage, pageSize);
        this.items = new ArrayList<T>();
        int fromIndex = (currentPage - 1) * pageSize;
        if (fromIndex >= 0 && fromIndex <= items.size()) {
            int toIndex = currentPage * pageSize;
            if (toIndex > items.size()) {
                toIndex = items.size();
            }
            try {
                this.items = items.subList(fromIndex, toIndex);
            } catch (IndexOutOfBoundsException iobe) {
                log.error("Sublist out of bounds: fromIndex=" + fromIndex + ", toIndex=" + toIndex + ", list size=" +
                    items.size(), iobe);
            }
        }
    }

    public IterablePagination(final HippoDocumentIterator<T> beans, final int results, final int pageSize,
        final int pageNumber) {
        super(results, pageNumber, pageSize);
        items = new ArrayList<T>();
        processDocuments(beans);
    }

    public void addItem(T item) {
        items.add(item);
    }

    private void processDocuments(HippoDocumentIterator<T> documentsIterator) {
        items = new ArrayList<T>();
        int startAt = getStartOffset();
        if (startAt < getTotal()) {
            documentsIterator.skip(startAt);
        }
        int count = 0;
        while (documentsIterator.hasNext()) {
            if (count == getPageSize()) {
                break;
            }
            T bean = documentsIterator.next();
            if (bean != null) {
                items.add(bean);
                count++;
            }
        }
    }

    private void process(HippoBeanIterator beans) {
        items = new ArrayList<T>();
        int startAt = getStartOffset();
        if (startAt < getTotal()) {
            beans.skip(startAt);
        }
        int count = 0;
        while (beans.hasNext()) {
            if (count == getPageSize()) {
                break;
            }
            Object bean = beans.next();
            if (bean != null) {
                items.add((T) bean);
                count++;
            }
        }
    }

    public List<? extends HippoBean> getItems() {
        return items;
    }

    @SuppressWarnings({"unchecked"})
    public void setItems(List<? extends HippoBean> items) {
        this.items = (List<T>) items;
    }

    /**
     * Default page range for given page
     * @param page current page
     * @return page surrounded by results on both side e.g. {@literal 1, 2, 3, 4<selected page>5, 6 ,7 ,8,9 etc.>}
     * @see #getDefaultPageRange()
     */
    public List<Long> getPageRange(final int page) {
        return getPageRangeWithFill(page, getDefaultPageRange());
    }

    /**
     * Default Page range for current selected page, it is "google alike" page range with x pages before selected item
     * and x+1 after selected item.
     * @return range based on default fill {@literal 1, 2, 3, 4, 5 <selected 6>, 7, 8,9 etc. }
     * @see #getDefaultPageRange()
     */
    public List<Long> getCurrentRange() {
        return getPageRangeWithFill(getCurrentPage(), getDefaultPageRange());
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

    protected int getDefaultPageRange() {
        return DEFAULT_PAGE_RANGE;
    }
}