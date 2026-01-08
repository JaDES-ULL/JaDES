package es.ull.simulation.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link TimeStamp}.
 * Tests time value representation and conversion operations.
 */
public class TimeStampTest {

    @Test
    public void shouldCreateTimeStampWithUnitAndValue() {
        // Given: a time unit and value
        TimeUnit unit = TimeUnit.HOUR;
        long value = 5;

        // When: creating a timestamp
        TimeStamp ts = new TimeStamp(unit, value);

        // Then: it should store the correct values
        assertEquals(unit, ts.getUnit());
        assertEquals(value, ts.getValue());
    }

    @Test
    public void shouldConvertBetweenTimeUnits() {
        // Given: a timestamp of 2 hours
        TimeStamp twoHours = new TimeStamp(TimeUnit.HOUR, 2);

        // When: converting to minutes
        TimeStamp inMinutes = twoHours.convert(TimeUnit.MINUTE);

        // Then: it should be 120 minutes
        assertEquals(TimeUnit.MINUTE, inMinutes.getUnit());
        assertEquals(120, inMinutes.getValue());
    }

    @Test
    public void shouldConvertDaysToHours() {
        // Given: a timestamp of 1 day
        TimeStamp oneDay = new TimeStamp(TimeUnit.DAY, 1);

        // When: converting to hours
        TimeStamp inHours = oneDay.convert(TimeUnit.HOUR);

        // Then: it should be 24 hours
        assertEquals(TimeUnit.HOUR, inHours.getUnit());
        assertEquals(24, inHours.getValue());
    }

    @Test
    public void shouldAddTwoTimeStamps() {
        // Given: two timestamps in the same unit
        TimeStamp ts1 = new TimeStamp(TimeUnit.MINUTE, 30);
        TimeStamp ts2 = new TimeStamp(TimeUnit.MINUTE, 45);

        // When: adding them
        TimeStamp result = ts1.add(ts2);

        // Then: result should be the sum
        assertEquals(TimeUnit.MINUTE, result.getUnit());
        assertEquals(75, result.getValue());
    }

    @Test
    public void shouldAddTimeStampsWithDifferentUnits() {
        // Given: two timestamps in different units
        TimeStamp oneHour = new TimeStamp(TimeUnit.HOUR, 1);
        TimeStamp thirtyMinutes = new TimeStamp(TimeUnit.MINUTE, 30);

        // When: adding them
        TimeStamp result = oneHour.add(thirtyMinutes);

        // Then: result should be 1.5 hours in the first unit (HOUR)
        assertEquals(TimeUnit.HOUR, result.getUnit());
        assertEquals(2, result.getValue(), 0.01); // Rounded
    }

    @Test
    public void shouldMultiplyByFactor() {
        // Given: a timestamp of 10 minutes
        TimeStamp tenMinutes = new TimeStamp(TimeUnit.MINUTE, 10);

        // When: multiplying by 2.5
        TimeStamp result = tenMinutes.multiply(2.5);

        // Then: result should be 25 minutes
        assertEquals(TimeUnit.MINUTE, result.getUnit());
        assertEquals(25, result.getValue());
    }

    @Test
    public void shouldProvideZeroTimeStamp() {
        // When: getting zero timestamp
        TimeStamp zero = TimeStamp.getZero();

        // Then: it should be 0 minutes
        assertEquals(TimeUnit.MINUTE, zero.getUnit());
        assertEquals(0, zero.getValue());
    }

    @Test
    public void shouldProvideOneMinuteTimeStamp() {
        // When: getting one minute timestamp
        TimeStamp minute = TimeStamp.getMinute();

        // Then: it should be 1 minute
        assertEquals(TimeUnit.MINUTE, minute.getUnit());
        assertEquals(1, minute.getValue());
    }

    @Test
    public void shouldProvideOneHourTimeStamp() {
        // When: getting one hour timestamp
        TimeStamp hour = TimeStamp.getHour();

        // Then: it should be 1 hour
        assertEquals(TimeUnit.HOUR, hour.getUnit());
        assertEquals(1, hour.getValue());
    }

    @Test
    public void shouldProvideOneDayTimeStamp() {
        // When: getting one day timestamp
        TimeStamp day = TimeStamp.getDay();

        // Then: it should be 1 day
        assertEquals(TimeUnit.DAY, day.getUnit());
        assertEquals(1, day.getValue());
    }

    @Test
    public void shouldProvideOneWeekTimeStamp() {
        // When: getting one week timestamp
        TimeStamp week = TimeStamp.getWeek();

        // Then: it should be 1 week
        assertEquals(TimeUnit.WEEK, week.getUnit());
        assertEquals(1, week.getValue());
    }

    @Test
    public void shouldProvideOneMonthTimeStamp() {
        // When: getting one month timestamp
        TimeStamp month = TimeStamp.getMonth();

        // Then: it should be 1 month
        assertEquals(TimeUnit.MONTH, month.getUnit());
        assertEquals(1, month.getValue());
    }

    @Test
    public void shouldProvideOneYearTimeStamp() {
        // When: getting one year timestamp
        TimeStamp year = TimeStamp.getYear();

        // Then: it should be 1 year
        assertEquals(TimeUnit.YEAR, year.getUnit());
        assertEquals(1, year.getValue());
    }

    @Test
    public void shouldFormatToString() {
        // Given: a timestamp
        TimeStamp ts = new TimeStamp(TimeUnit.DAY, 5);

        // When: converting to string
        String str = ts.toString();

        // Then: it should include value and unit name
        assertEquals("5 Days", str);
    }
}
