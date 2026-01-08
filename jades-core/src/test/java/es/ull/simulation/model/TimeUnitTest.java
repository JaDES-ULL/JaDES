package es.ull.simulation.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link TimeUnit}.
 * Tests time unit conversion operations and constants.
 */
public class TimeUnitTest {

    @Test
    public void shouldHaveCorrectName() {
        // Given: various time units
        // When: getting their names
        // Then: they should have descriptive names
        assertEquals("Milliseconds", TimeUnit.MILLISECOND.getName());
        assertEquals("Seconds", TimeUnit.SECOND.getName());
        assertEquals("Minutes", TimeUnit.MINUTE.getName());
        assertEquals("Hours", TimeUnit.HOUR.getName());
        assertEquals("Days", TimeUnit.DAY.getName());
        assertEquals("Weeks", TimeUnit.WEEK.getName());
        assertEquals("Months", TimeUnit.MONTH.getName());
        assertEquals("Years", TimeUnit.YEAR.getName());
    }

    @Test
    public void shouldConvertSecondsToMilliseconds() {
        // Given: 1 second
        // When: converting to milliseconds
        long result = TimeUnit.MILLISECOND.convert(1.0, TimeUnit.SECOND);

        // Then: it should be 1000 milliseconds
        assertEquals(1000, result);
    }

    @Test
    public void shouldConvertMinutesToSeconds() {
        // Given: 2 minutes
        // When: converting to seconds
        long result = TimeUnit.SECOND.convert(2.0, TimeUnit.MINUTE);

        // Then: it should be 120 seconds
        assertEquals(120, result);
    }

    @Test
    public void shouldConvertHoursToMinutes() {
        // Given: 1.5 hours
        // When: converting to minutes
        long result = TimeUnit.MINUTE.convert(1.5, TimeUnit.HOUR);

        // Then: it should be 90 minutes
        assertEquals(90, result);
    }

    @Test
    public void shouldConvertDaysToHours() {
        // Given: 2 days
        // When: converting to hours
        long result = TimeUnit.HOUR.convert(2.0, TimeUnit.DAY);

        // Then: it should be 48 hours
        assertEquals(48, result);
    }

    @Test
    public void shouldConvertWeeksToDays() {
        // Given: 1 week
        // When: converting to days
        long result = TimeUnit.DAY.convert(1.0, TimeUnit.WEEK);

        // Then: it should be 7 days
        assertEquals(7, result);
    }

    @Test
    public void shouldConvertMonthsToWeeks() {
        // Given: 1 month
        // When: converting to weeks
        long result = TimeUnit.WEEK.convert(1.0, TimeUnit.MONTH);

        // Then: it should be approximately 4 weeks
        assertEquals(4, result);
    }

    @Test
    public void shouldConvertYearsToMonths() {
        // Given: 1 year
        // When: converting to months
        long result = TimeUnit.MONTH.convert(1.0, TimeUnit.YEAR);

        // Then: it should be 12 months
        assertEquals(12, result);
    }

    @Test
    public void shouldConvertUsingTimeStamp() {
        // Given: a timestamp of 120 seconds
        TimeStamp ts = new TimeStamp(TimeUnit.SECOND, 120);

        // When: converting to minutes
        long result = TimeUnit.MINUTE.convert(ts);

        // Then: it should be 2 minutes
        assertEquals(2, result);
    }

    @Test
    public void shouldGetConversionFactor() {
        // Given: converting from hours to minutes
        // When: getting the conversion factor
        double factor = TimeUnit.MINUTE.getConversionFactor(TimeUnit.HOUR);

        // Then: it should be 60
        assertEquals(60.0, factor, 0.001);
    }

    @Test
    public void shouldGetConversionFactorForDaysToHours() {
        // Given: converting from days to hours
        // When: getting the conversion factor
        double factor = TimeUnit.HOUR.getConversionFactor(TimeUnit.DAY);

        // Then: it should be 24
        assertEquals(24.0, factor, 0.001);
    }

    @Test
    public void shouldConvertSameUnitReturnsSameValue() {
        // Given: 100 minutes
        // When: converting to minutes
        long result = TimeUnit.MINUTE.convert(100.0, TimeUnit.MINUTE);

        // Then: it should be 100 minutes
        assertEquals(100, result);
    }

    @Test
    public void shouldRoundConversionResults() {
        // Given: 1.6 hours
        // When: converting to minutes
        long result = TimeUnit.MINUTE.convert(1.6, TimeUnit.HOUR);

        // Then: it should be rounded to 96 minutes
        assertEquals(96, result);
    }
}
