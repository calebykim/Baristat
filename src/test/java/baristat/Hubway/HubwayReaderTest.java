package baristat.Hubway;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;

import baristat.Dates.UnitTime;

/**
 * This tests the HubwayReader class.
 *
 * @author adrianturcu
 *
 */
public class HubwayReaderTest {

  /**
   * This tests the if the getting number of items sold in a date range function
   * works.
   */
  @Test
  public void getNumSoldInRangeTest() {
    HubwayReader reader = new HubwayReader();

    final int startStn = 0;
    final int endStn = 300;
    final double cost = 5.0;
    HubwayItem fakeItem = new HubwayItem("Iced Coffee", startStn, endStn, cost);
    DateTimeFormatter formatter = DateTimeFormat
        .forPattern("dd/MM/yyyy HH:mm:ss");
    DateTime startDate = formatter.parseDateTime("01/01/2017 00:00:00");
    DateTime endDate = formatter.parseDateTime("01/01/2017 00:00:00");

    reader.getNumSoldInDateRange(fakeItem, startDate, endDate, UnitTime.HOUR);

    // for (DateTime dt : numSold.elementSet()) {
    // System.out
    // .println("Time: " + dt.toString() + " Count: " + numSold.count(dt));
    // }
  }

  /**
   * This tests if getting total sales in a date range works properly.
   */
  @Test
  public void getTotalSalesInRangeTest() {
    HubwayClient client = new HubwayClient();

    DateTimeFormatter formatter = DateTimeFormat
        .forPattern("dd/MM/yyyy HH:mm:ss");
    DateTime startDate = formatter.parseDateTime("01/01/2017 00:00:00");

    DateTime endDate = formatter.parseDateTime("01/01/2017 10:00:00");

    client.getTotalSalesInDateRange(startDate, endDate, UnitTime.HOUR);

    // for (DateTime dt : totalSales.keySet()) {
    // System.out.println(
    // "Date: " + dt.toString() + " Total Sales : $" + totalSales.get(dt));
    // }

  }

}
