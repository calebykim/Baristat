package baristat.Hubway;

import static org.junit.Assert.assertNotNull;

import java.util.Map;

import org.joda.time.DateTime;
import org.junit.Ignore;
import org.junit.Test;

import baristat.API.Client;
import baristat.Dates.Dates;
import baristat.Dates.UnitTime;
import baristat.Square.SquareUsers;

/**
 * This tests the HubwayClient class.
 *
 * @author adrianturcu
 *
 */
public class HubwayClientTest {

  /**
   * Tests the HubwayClient constructor.
   */
  @Test
  public void constructionTest() {
    assertNotNull(new HubwayClient());
  }

  /**
   * Tests the getNumTransactions method.
   */
  @Test
  @Ignore
  public void getNumTransactionTest() {
    // HubwayClient client = new HubwayClient();
    // DateTime start = new DateTime(2017, 1, 1, 0, 0, 0);
    // DateTime end = new DateTime(2017, 1, 8, 0, 0, 0);
    // Map<Integer, Map<Integer, Integer>> numTransactions = client
    // .getNumTransactionsInDateRange(start, end);
    //
    // for (Map<Integer, Integer> hours : numTransactions.values()) {
    //
    // for (Entry<Integer, Integer> hour : hours.entrySet()) {
    // assertNotNull(hour.getKey());
    // assertNotNull(hour.getValue());
    // }
    // }
    //
    // assertNotNull(numTransactions);
  }

  /**
   * This method tests if sales predictions work properly.
   */
  @Test
  @Ignore
  public void predictSalesBetweenPastAndFutureTest() {
    Client client = new HubwayClient();
    DateTime start = new DateTime(2017, 2, 28, 0, 0, 0);
    DateTime end = new DateTime(2017, 3, 5, 0, 0, 0);
    DateTime split = Dates.findSplitDate(start, end, true);

    Map<DateTime, Double> sales = client.predictTotalSalesInDateRange(start,
        end, UnitTime.DAY, new SquareUsers(), "fakeLocation");

    System.out.println("Middle: " + split.toString());
    assert split.getDayOfMonth() == 28;
    System.out.println("Size: " + sales.size());
    System.out.println("Sales: " + sales);
  }

  /**
   * This method checks if item predictions work properly.
   */
  @Test
  @Ignore
  public void predictItemsBetweenPastAndFutureTest() {
    Client client = new HubwayClient();
    DateTime start = new DateTime(2017, 3, 1, 0, 0, 0);
    DateTime end = new DateTime(2017, 3, 13, 0, 0, 0);

    System.out.println(end.toString() + " has day: " + end.getDayOfWeek());

    client.predictItemsInDateRange(start, end, UnitTime.DAY, new SquareUsers(),
        "3ZTP5WJTB0ZWD");
  }

  /**
   * This method checks if getting items works properly.
   */
  @Test
  public void getItemsBetweenPastAndFutureTest() {
    Client client = new HubwayClient();
    DateTime start = new DateTime(2017, 1, 1, 0, 0, 0);
    DateTime end = new DateTime(2017, 1, 20, 0, 0, 0);

    client.getItemsInDateRange(start, end, UnitTime.DAY);

    // for (DateTime dt : sales.get(1).getNumSoldMap().elementSet()) {
    // System.out.println(
    // dt.toString() + " " + sales.get(0).getNumSoldMap().count(dt));
    // }
  }

  /**
   * This tests if getting items for one day by hour works properly.
   */
  @Test
  public void getItemsInSameDayHour() {
    Client client = new HubwayClient();
    DateTime start = new DateTime(2017, 1, 5, 0, 0, 0);
    DateTime end = new DateTime(2017, 1, 5, 0, 0, 0);
    client.getItemsInDateRange(start, end, UnitTime.HOUR);

  }

}
