package baristat.Dates;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.joda.time.DateTime;
import org.junit.Test;

/**
 * This the testing suite for the date utilities.
 *
 * @author jbreuch
 */
public class DatesTest {

  /**
   * This tests if the isWeekend function works properly.
   */
  @Test
  public void testIsWeekend() {

    // Start with Monday
    DateTime day = new DateTime(2017, 4, 24, 0, 0);

    for (int i = 0; i < 5; i++) {
      assertFalse(Dates.isWeekend(day));
      day = day.plusDays(1);
    }

    // Weekends
    for (int i = 0; i < 2; i++) {
      assertTrue(Dates.isWeekend(day));
      day = day.plusDays(1);
    }

  }

  /**
   * This tests if the parseStringToDateTime function works properly.
   */
  @Test
  public void testParseStringToDateTime() {
    DateTime transformed = Dates.parseStringToDateTime("01/01/2017");
    assertTrue(transformed.getDayOfMonth() == 1);
    assertTrue(transformed.getMonthOfYear() == 1);
    assertTrue(transformed.getYear() == 2017);
  }

}
