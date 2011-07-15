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

import static org.testng.AssertJUnit.assertTrue;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 * DefaultPaginationTest
 *
 * @version $Id: DefaultPaginationTest.java 88098 2010-06-10 13:13:16Z mmilicevic $
 */
public class DefaultPaginationTest {
    @SuppressWarnings("unused")
    private static Logger log = LoggerFactory.getLogger(DefaultPaginationTest.class);

    @Test
    public void testDefaultCollection() throws Exception {
        final DefaultPagination<SearchResultMock<BaseBeanMock>> pc = new DefaultPagination<SearchResultMock<BaseBeanMock>>(1014);
        pc.setPageSize(10);
        assertTrue("Expected 102 pages, but found: " + pc.getTotalPages(), pc.getTotalPages() == 102);
        assertTrue("Expected 101, but found: " + pc.getMaxSize(), pc.getMaxSize() == 101);
        pc.setPageNumber(1);
        List<Long> pageRange = pc.getPageRange(1);
        assertTrue("Expected 11 items, but got:" + pageRange.size(), pageRange.size() == 11);
        assertTrue("Expected 101, but found: " + pc.getMaxSize(), pc.getMaxSize() == 101);
        pageRange = pc.getCurrentRange();
        assertTrue("Expected 11 items, but got:" + pageRange.size(), pageRange.size() == 11);
        pc.setPageNumber(10);
        pageRange = pc.getCurrentRange();
        assertTrue("Expected 20 items, but got:" + pageRange.size(), pageRange.size() == 20);
        assertTrue("Expected 101, but found: " + pc.getMaxSize(), pc.getMaxSize() == 101);
        pc.setPageNumber(20);
        pageRange = pc.getCurrentRange();
        assertTrue("Expected 20 items, but got:" + pageRange.size(), pageRange.size() == 20);
        assertTrue("Expected 201, but found: " + pc.getMaxSize(), pc.getMaxSize() == 201);
        pc.setPageNumber(100);
        pageRange = pc.getCurrentRange();
        assertTrue("Expected 12 items, but got:" + pageRange.size(), pageRange.size() == 12);
        assertTrue("Expected 1001, but found: " + pc.getMaxSize(), pc.getMaxSize() == 1001);
        pc.setPageNumber(102);
        pageRange = pc.getCurrentRange();
        assertTrue("Expected 10 items, but got:" + pageRange.size(), pageRange.size() == 10);
        assertTrue("Expected 1021, but found: " + pc.getMaxSize(), pc.getMaxSize() == 1021);
    }
}
