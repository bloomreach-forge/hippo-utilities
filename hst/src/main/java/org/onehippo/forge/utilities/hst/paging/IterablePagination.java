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
import java.util.List;

import org.hippoecm.hst.content.beans.standard.HippoBean;
import org.hippoecm.hst.content.beans.standard.HippoBeanIterator;
import org.hippoecm.hst.content.beans.standard.HippoDocumentIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * IterablePagination: a Pageable with HippoBean items.
 */
public class IterablePagination<T extends HippoBean> extends Pageable {

    private static Logger log = LoggerFactory.getLogger(IterablePagination.class);

    private static final int DEFAULT_PAGE_RANGE = 10;

    private int defaultPageRange = DEFAULT_PAGE_RANGE;

    private List<T> items;

    /**
     * Constructor to be used when the paging has been done beforehand (for example in HST query).
     * The beans iterator size should be the same as pageSize (except maybe for the last page).
     *
     * E.g. when HstQuery is used to get the beans, both HstQuery#setLimit and HstQuery#setOffset has been used.
     */
    public IterablePagination(final HippoBeanIterator beans, final int totalSize, final int pageSize, final int currentPage) {
        super(totalSize, currentPage, pageSize);

        // add all from iterator; assumption that paging is done beforehand
        processItems(beans);
    }

    /**
     * Constructor to be used when the paging has been done beforehand (for example in HST query).
     */
    public IterablePagination(int totalSize, List<T> items) {
        super(totalSize);
        this.items = new ArrayList<T>(items);
    }

    /**
     * Constructor to be used when the paging is not done beforehand (for example in HST query), but has to be done by
     * this class, for instance paging on facet navigation results.
     */
    @SuppressWarnings({"unchecked"})
    public IterablePagination(final HippoBeanIterator beans, final int currentPage) {
        super(beans.getSize(), currentPage);
        processOffset(beans);
    }

    /**
     * Constructor to be used when the paging is not done beforehand (for example in HST query), but has to be done by
     * this class, for instance paging on facet navigation results.
     */
    public IterablePagination(final HippoBeanIterator beans, final int pageSize, final int currentPage) {
        super(beans.getSize(), currentPage, pageSize);
        items = new ArrayList<T>();
        processOffset(beans);
    }

    /**
     * Constructor to be used when the paging is not done beforehand (for example in HST query), but has to be done by
     * this class, for instance paging on facet navigation results.
     */
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

    /**
     * Constructor to be used when the paging is not done beforehand (for example in HST query), but has to be done by
     * this class, for instance paging on facet navigation results.
     */
    public IterablePagination(final HippoDocumentIterator<T> beans, final int totalSize, final int pageSize, final int pageNumber) {
        super(totalSize, pageNumber, pageSize);
        processDocumentsOffset(beans);
    }

    /**
     * Add an item
     *
     * @param item the item
     */
    public void addItem(T item) {
        items.add(item);
    }

    /**
     * Set the default page range, i.e. the page range width, that will be used to determine the actual page range
     * by subtracting the page fill (== half the default page range) from current page and adding the same fill to the
     * current page.
     *
     * @param defaultPageRange value of default page range
     */
    public void setDefaultPageRange(int defaultPageRange) {
        this.defaultPageRange = defaultPageRange;
    }

    /**
     * Get all paged items
     *
     * @return all paged items
     */
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
     * @see #getDefaultPageFill()
     */
    public List<Long> getPageRange(final int page) {
        return getPageRangeWithFill(page, getDefaultPageFill());
    }

    /**
     * Default Page range for current selected page, it is "google alike" page range with x pages before selected item
     * and x+1 after selected item.
     * @return range based on default fill {@literal 1, 2, 3, 4, 5 <selected 6>, 7, 8,9 etc. }
     * @see #getDefaultPageFill()
     */
    public List<Long> getCurrentRange() {
        return getPageRangeWithFill(getCurrentPage(), getDefaultPageFill());
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
        // fill in lower range: e.g. for 2 it will be 1
        long start = page - fillIn;
        long endCorrectionCorrection = 0;
        if (start <= 0) {
            endCorrectionCorrection =  1 - start;
            start = 1;
        }
        // end part:
        long end = page + fillIn;
        long startCorrection = 0;
        if (end > getTotalPages()) {
            startCorrection = getTotalPages() - end;
            end = getTotalPages();
        }

        // put corrections to keep range correct, adding to end what was subtracted from start or vice versa
        if ((start + startCorrection) > 0) {
            start += startCorrection;
        }
        if ((end + endCorrectionCorrection) <= getTotalPages()) {
            end += endCorrectionCorrection;
        }

        for (long i = start; i <= end; i++) {
            pages.add(i);
        }
        return pages;
    }

    protected int getDefaultPageRange() {
        return defaultPageRange;
    }

    protected int getDefaultPageFill() {
        return defaultPageRange / 2;
    }

    protected void processDocumentsOffset(HippoDocumentIterator<T> documentsIterator) {
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

    protected void processOffset(HippoBeanIterator beans) {
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

    /**
     * Process items without offset
     */
    protected void processItems(HippoBeanIterator beans) {
        items = new ArrayList<T>();
        while (beans.hasNext()) {
            T bean = (T) beans.nextHippoBean();
            if (bean != null) {
                items.add(bean);
            }
        }
    }

}