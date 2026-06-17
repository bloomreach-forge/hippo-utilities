package org.onehippo.forge.utilities.commons;

import org.junit.jupiter.api.Test;
import org.onehippo.forge.utilities.commons.ftp.SimpleFtpClientResult;
import org.onehippo.forge.utilities.commons.jcrmockup.ISO8601;

import java.util.Calendar;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;

class UtilitiesTest {

    @Test void simpleFtpClientResult_allValues_present() {
        assertTrue(SimpleFtpClientResult.values().length >= 5);
        assertNotNull(SimpleFtpClientResult.SUCCESS);
        assertNotNull(SimpleFtpClientResult.ERROR);
        assertNotNull(SimpleFtpClientResult.CREATED);
    }

    @Test void simpleFtpClientResult_valueOf_roundTrips() {
        assertEquals(SimpleFtpClientResult.SUCCESS, SimpleFtpClientResult.valueOf("SUCCESS"));
    }

    @Test void iso8601_format_returnsNonNullString() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2024, 0, 15, 12, 30, 0);
        String formatted = ISO8601.format(cal);
        assertNotNull(formatted);
        assertTrue(formatted.contains("2024"));
    }

    @Test void iso8601_parse_validString_returnsCalendar() {
        Calendar cal = ISO8601.parse("2024-01-15T12:30:00.000Z");
        assertNotNull(cal);
        assertEquals(2024, cal.get(Calendar.YEAR));
    }

    @Test void iso8601_parse_invalidString_returnsNull() {
        assertNull(ISO8601.parse("not-a-date"));
    }
}
