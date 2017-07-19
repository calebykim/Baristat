package baristat.Holidays;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.joda.time.DateTime;
import org.junit.Test;

/**
 * This the testing suite for the weather client.
 *
 * @author jbreuch
 */
public class HolidaysClientTest {

  /**
   * This tests the construction of the HolidaysClient.
   */
  @Test
  public void testConstruction() {
    assertNotNull(new HolidaysClient());
  }

  /**
   * This tests if the correct exception is caught.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testGetHolidayByDateNull() {
    new HolidaysClient().getHolidayByDate(null);
  }

  /**
   * This tests if the getHolidayByDate method works properly.
   */
  @Test
  public void testGetHolidayByDate() {
    HolidaysClient client = new HolidaysClient();
    assertNotNull(client);

    DateTime holiday = new DateTime(2016, 1, 1, 0, 0);
    Holiday found = client.getHolidayByDate(holiday);
    assertNotNull(found);
    assertTrue(found.getDate().compareTo(holiday) == 0);
  }

}
