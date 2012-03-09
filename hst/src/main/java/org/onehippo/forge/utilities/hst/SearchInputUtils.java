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

package org.onehippo.forge.utilities.hst;

import org.apache.commons.lang.StringUtils;

/**
 * Input utilities for user searches.
 *
 * @author Jeroen Reijn
 * @author Adolfo Benedetti
 * @author Rob van der Linden Vooren
 */
public final class SearchInputUtils {

    public static final int MAX_KEYWORDS_LENGTH = 250;
    private static final String WHITESPACE_PATTERN = "\\s+";

    private SearchInputUtils() {
        // hidden constructor
    }

    /**
     * Rewrite the provided keywords. The following criteria determine if the input needs to be rewritten:
     * <ul>
     * <li>the total length of the keywords can not be above 250 characters</li>
     * <li>the query does not contain more than one wildcard</li>
     * </ul>
     *
     * @param input the user input
     * @return the clean version of the query
     */
    public static String rewriteKeywords(String input) {
        String rewrittenKeywords = compressWhitespace(input);
        rewrittenKeywords = removeTrailingOrOperator(rewrittenKeywords);
        rewrittenKeywords = escape(rewrittenKeywords);
        rewrittenKeywords = rewriteNotOperatorsToMinus(rewrittenKeywords);
        rewrittenKeywords = removeAndOperators(rewrittenKeywords);
        rewrittenKeywords = removeQuestionmarkWildcards(rewrittenKeywords);
        rewrittenKeywords = removeAsteriksWildcards(rewrittenKeywords);
        rewrittenKeywords = removeTextIfLengtExceedsMaxLength(rewrittenKeywords);
        return rewrittenKeywords;
    }

    /**
     * Checks the search keywords for incorrect start operators. If the keywords only match <em>OR</em> or start with
     * "OR " this query is invalid, since this will result in a Lucene query parse exception.
     *
     * @param keywords the search keywords
     * @return true if the query starts with an invalid search operatoror, false otherwise
     */
    public static boolean startsWithInvalidSearchOperator(String keywords) {
        String trimmedKeywords = StringUtils.trim(keywords);
        if (trimmedKeywords.equals("OR")) {
            return true;
        } else if (StringUtils.startsWith(trimmedKeywords, "OR ")) {
            return true;
        }
        return false;
    }

    /**
     * Checks to see if the provided keywords only exist of a single wildcard term (i.e * or ?).
     *
     * @param keywords the user provided search query
     * @return if the keywords only exist of <em>*</em> or <em>?</em> <code>true</code> is returned, <code>false<code>
     *         otherwise
     */
    public static boolean doKeywordsExistOfOnlyInvalidSearchOperators(String keywords) {
        if (StringUtils.isNotEmpty(keywords) && (StringUtils.trim(keywords).equals("*")
                || StringUtils.trim(keywords).equals("?"))) {
            return true;
        }
        return false;
    }

    /**
     * Questionmarks are lucene wildcard queries. This makes queries slower then necessary. We remove all question marks
     * from the keywords, so that there is no wildcard search being done.
     *
     * @param validatedKeywords the keywords provided for the search.
     * @return the original keywords if no question marks are found, the keywords without questions marks if a question
     *         mark is found.
     */
    public static String removeQuestionmarkWildcards(String validatedKeywords) {
        return StringUtils.remove(validatedKeywords, '?');
    }

    /**
     * An asteriks is a lucene wildcard character. We currently allow only <em>one</em> asterics wildcard for the entire query
     * and this wildcard must be part of a sentence. If the the keyword starts with a wilcard or is just the only keyword
     * it will also be removed.
     *
     * @param validatedKeywords the keywords provided for the search
     * @return the keywords used for the query
     */
    public static String removeAsteriksWildcards(String validatedKeywords) {
        String keywords = validatedKeywords;

        keywords = collapseMultipleAsterics(keywords).trim();

        if (StringUtils.startsWith(keywords, "*")) {
            keywords = keywords.substring(1, keywords.length());
        }

        if (keywords.contains(" * ")) {
            keywords = StringUtils.replace(keywords, " * ", " ");
        }

        if (keywords.contains(" *")) {
            keywords = StringUtils.replace(keywords, " *", " ");
        }

        String trimmedKeywords = keywords.trim();
        if (trimmedKeywords.startsWith("*")) {
            trimmedKeywords = removeAsteriksWildcards(trimmedKeywords).trim();
        }

        if (StringUtils.countMatches(trimmedKeywords, "*") > 0) {
            int start = trimmedKeywords.indexOf('*');
            if (start > -1 && start < trimmedKeywords.length()) {
                String leftOf = trimmedKeywords.substring(0, start + 1);
                String rightOf = trimmedKeywords.substring(start + 1, trimmedKeywords.length());
                rightOf = StringUtils.remove(rightOf, '*');
                trimmedKeywords = leftOf + rightOf;
            }
        }

        return trimmedKeywords.trim();
    }

    /**
     * Removes text from the query if the length of the query exceeds 250 characters.
     *
     * @param validatedKeywords the provided keywords
     * @return the original keywords if the size is less then 250 charachters, otherwise the first 250 characters
     */
    public static String removeTextIfLengtExceedsMaxLength(String validatedKeywords) {
        if (StringUtils.length(validatedKeywords) > MAX_KEYWORDS_LENGTH) {
            return validatedKeywords.substring(0, MAX_KEYWORDS_LENGTH);
        }
        return validatedKeywords;
    }

    /**
     * Replaces all occurrences of 2 or more sequential asteriks with a single asteriks.
     *
     * @param text the input
     * @return string with no multiple asterics next to each other
     */
    public static String collapseMultipleAsterics(String text) {
        return text.replaceAll("[*]{2,}", "*");
    }


    /**
     * Rewrites any "NOT" operators in the keywords to a minus symbol (-). This is necessary because Jackrabbit doesn't
     * fully support the Lucene query language. Jackrabbit <em>does</em> support the minus symbol to exclude keywords
     * from search results but <em>does not</em> support the "NOT" keyword.
     *
     * @param keywords keywords to rewrite
     * @return rewritten keywords
     */
    private static String rewriteNotOperatorsToMinus(String keywords) {
        return keywords.replace("NOT ", "-");
    }

    /**
     * Removes any "AND" operators in the keywords. This is necessary because Jackrabbit doesn't
     * fully support the Lucene query language. Lucene by default applies "and" to all keywords so to support the "AND"
     * operator it can be simply removed from the keywords.
     *
     * @param keywords keywords to rewrite
     * @return rewritten keywords
     */
    private static String removeAndOperators(String keywords) {
        return keywords.replace("AND ", "");
    }

    /**
     * Removes the logical operator "OR" at the end of the query. Otherwise this will result in a Lucene parse exception.
     *
     * @param queryString the original (possibly invalid) query string
     * @return a valid query string
     */
    public static String removeTrailingOrOperator(String queryString) {
        String cleanedQueryString = queryString.trim();
        return StringUtils.removeEnd(cleanedQueryString, " OR");
    }

    /**
     * Escapes characters that expects to be escaped by a preceding '{@code \}' or for quote like characters by the
     * character itself. <p/> According to 6.6.4.9 of the * JCR-170 specification, the apostrophe (') and quotation
     * mark(") must be escaped according to the standard rules of XPath with regard to string literals: If the literal
     * is delimited by apostrophes, two adjacent apostrophes within the literal are interpreted as a single apostrophe.
     * Similarly, if the literal is delimited by quotation marks, two adjacent quotation marks within the literal are
     * interpreted as one quotation mark.
     *
     * @param keywords the string to escape
     * @return a String where characters that QueryParser expects to be
     *         escaped, are escaped by a preceding ' {@code \}' or for quote like characters by itself
     */
    public static String escape(String keywords) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < keywords.length(); i++) {
            char c = keywords.charAt(i);
            if (c == '\'') {
                sb.append('\\');
                sb.append(c);
            }
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * Compress whitespace (tab, newline, multiple spaces) by removing leading and trailing whitespace, and reducing
     * inbetween whitespace to one space.
     *
     * @param text the text to compress (may be null)
     * @return the compressed text, or null if the text to compress was null
     */
    public static String compressWhitespace(String text) {
        if (text == null) {
            return null;
        }
        String trimmedText = StringUtils.trim(text);
        return trimmedText.replaceAll(WHITESPACE_PATTERN, " ");
    }


}
