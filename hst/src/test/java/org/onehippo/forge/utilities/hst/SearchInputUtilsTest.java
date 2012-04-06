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

package org.onehippo.forge.utilities.hst;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link SearchInputUtils}.
 *
 * @author Jeroen Reijn
 * @author Adolfo Benedetti
 * @author Rob van der Linden Vooren
 *
 */
public class SearchInputUtilsTest {

    @Test
    public void testStartsWithInvalidSearchOperator() throws Exception {
        assertTrue(SearchInputUtils.startsWithInvalidSearchOperator("OR test"));
        assertFalse(SearchInputUtils.startsWithInvalidSearchOperator("AND test"));
        assertFalse(SearchInputUtils.startsWithInvalidSearchOperator(" test"));
    }

    @Test
    public void testDoKeywordsExistOfOnlyInvalidSearchOperators() throws Exception {
        assertTrue(SearchInputUtils.doKeywordsExistOfOnlyInvalidSearchOperators("*"));
        assertTrue(SearchInputUtils.doKeywordsExistOfOnlyInvalidSearchOperators("?"));
        assertFalse(SearchInputUtils.doKeywordsExistOfOnlyInvalidSearchOperators("test"));
    }

    @Test
    public void testRemoveQuestionmarkWildcards() throws Exception {
        assertEquals("test1 test2", SearchInputUtils.rewriteKeywords("test1? test2?"));
    }

    @Test
    public void testRemoveAsteriksWildcards() throws Exception {
        assertEquals("test test and some more", SearchInputUtils.rewriteKeywords("*test ***test and some more *"));
    }

    @Test
    public void testRemoveMultipleAsteriksWildcards() throws Exception {
        assertEquals("test test and* some more",
                SearchInputUtils.removeAsteriksWildcards("** * * *test ***test and* some* more *"));
    }

    @Test
    public void testRemoveSingleWildcardWithSurroundingSpaces() throws Exception {
        assertEquals("Unexpected result because the wildcard is not removed: ",
                "ditto testo", SearchInputUtils.rewriteKeywords("ditto * testo"));
    }

    @Test
    public void testRemoveTextIfLengtExceedsMaxLength() throws Exception {
        String morethentwohundredfiftychars = "test *test and some more test *test and some* more test *test and some " +
                "more test *test and some more test *test and some more test *test and some more test *test and some " +
                "more test *test and some more test *test and some more test *test and some more test documents";
        String expected = "test test and some more test test and some* more test test and some more test test and some " +
                "more test test and some more test test and some more test test and some more test test and some more " +
                "test test and some more test test and some more test docu";
        assertEquals(expected, SearchInputUtils.rewriteKeywords(morethentwohundredfiftychars));
    }

    @Test
    public void createsValidSearchExpressionFromInvalidQueryString() throws Exception {
        assertEquals("rewritten keywords", "kind", SearchInputUtils.removeTrailingOrOperator("kind OR"));
        assertEquals("rewritten keywords", "kind or", SearchInputUtils.removeTrailingOrOperator("kind or"));
        assertEquals("rewritten keywords", "foo OR bar", SearchInputUtils.removeTrailingOrOperator("foo OR bar"));
    }

    @Test
    public void doesNotLeaveAsteriksAtEndAndBeginning() throws Exception {
        assertEquals("balkenende", SearchInputUtils.rewriteKeywords("* balkenende *"));
    }

    @Test
    public void doesLeaveAsteriksAtEndWhenMultiWord() throws Exception {
        assertEquals("balkenende balkenende*", SearchInputUtils.rewriteKeywords("* balkenende balkenende*"));
    }

    @Test
    public void doesNotLeaveAsteriksAtEndWhenSingleWord() throws Exception {
        assertEquals("balkenende*", SearchInputUtils.rewriteKeywords("* balkenende*"));
    }

    @Test
    public void doesNotLeaveAsteriksAndTildesInSingleWord() throws Exception {
        assertEquals("~~balkenende*", SearchInputUtils.rewriteKeywords("~~balkenende*"));
    }

    @Test
    public void compressWhitespace_compressesWhitespace() {
        assertEquals("tabs are replaced with single space", ". .", SearchInputUtils.compressWhitespace(".\t."));
        assertEquals("newlines are replaced with single space", ". .", SearchInputUtils.compressWhitespace(".\n."));
        assertEquals("multiple spaces are replaced with single space", ". .", SearchInputUtils.compressWhitespace(".    ."));
        assertEquals("leading and trailing whitespace is trimmed", ".", SearchInputUtils.compressWhitespace(" . "));
        assertEquals("a b c d", SearchInputUtils.compressWhitespace(" \t a \n b    c          d  "));
        assertEquals("is nullsafe", null, SearchInputUtils.compressWhitespace(null));
    }
}
