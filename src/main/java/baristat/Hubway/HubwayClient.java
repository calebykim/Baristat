package baristat.Hubway;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.joda.time.DateTime;

import com.google.common.collect.Multiset;

import baristat.API.Client;
import baristat.API.ClientHelper;
import baristat.API.ProductProfile;
import baristat.Dates.Dates;
import baristat.Dates.UnitTime;
import baristat.Main.Constants;
import baristat.Square.SquareUsers;
import baristat.Weather.WeatherClient;

/**
 * Class models a client by analyzing Hubway data.
 *
 * @author Tristin Falk
 *
 */
public class HubwayClient implements Client {
  private HubwayReader reader;
  private WeatherClient weatherClient;
  private DateTime now;
  private DateTime start;
  private DateTime middle;
  private DateTime end;
  private SquareUsers db;
  private String locationId;
  private ClientHelper helper;

  /**
   * Constructor.
   *
   * @param start
   *          the start date
   * @param middle
   *          the split date
   * @param end
   *          the end date
   * @param db
   *          the database of users, locations, and items
   * @param locationId
   *          the location id
   */
  public HubwayClient(DateTime start, DateTime middle, DateTime end,
      SquareUsers db, String locationId) {
    reader = new HubwayReader();
    weatherClient = new HubwayWeatherClient();
    now = new DateTime(2017, 2, 28, 0, 0, 0);
    this.start = start;
    this.middle = middle;
    this.end = end;
    this.db = db;
    this.locationId = locationId;
    this.helper = new ClientHelper(this, weatherClient, "boston", "ma");
  }

  /**
   * @return hubway's version of now.
   */
  @Override
  public DateTime getNow() {
    return now;
  }

  /**
   * Get map from dates to total sales.
   *
   * @param ut
   *          unit of time (hour, day, week)
   * @return Map from DateTime to total sales
   */
  @Override
  public Map<DateTime, Double> getTotalSalesInDateRange(UnitTime ut) {

    Map<DateTime, Double> totalSales = new TreeMap<>();

    if (start.compareTo(middle) > 0) {
      return totalSales;
    }

    if (ut.equals(UnitTime.HOUR) && start.compareTo(DateTime.now()) > 0) {
      return totalSales;
    }

    for (HubwayItem item : reader.getItems()) {
      Multiset<DateTime> numSold = reader.getNumSoldInDateRange(item, start,
          middle, ut);

      for (DateTime dt : numSold.elementSet()) {
        Double prevTotal = totalSales.get(dt);
        if (prevTotal == null) {
          prevTotal = 0.0;
        }

        totalSales.put(dt, prevTotal + numSold.count(dt) * item.getCost());
      }
    }

    for (DateTime dt : Dates.getDatesBetween(start, middle, ut)) {
      if (totalSales.get(dt) == null) {
        totalSales.put(dt, 0.0);
      }
    }

    return totalSales;
  }

  /**
   * @return map from days to num sold on each hour on that day.
   */
  @Override
  public Map<Integer, Map<Integer, Integer>> getNumTransactionsInDateRange() {

    Map<Integer, Map<Integer, Integer>> result = new TreeMap<>();

    int totalWeeks = 0;

    DateTime currDate = start;
    while (currDate.compareTo(end) < 0
        && result.keySet().size() < Constants.DAYS_PER_WEEK) {
      if (result.get(currDate.getDayOfWeek()) == null) {
        result.put(currDate.getDayOfWeek(), new TreeMap<Integer, Integer>());
        totalWeeks = 1;
      } else {
        totalWeeks = 4;
      }

      // Go through each item and add its transactions to the date
      for (HubwayItem item : reader.getItems()) {

        // Multiset of Datetimes representing number of times item is sold
        Multiset<DateTime> numSold = reader.getNumSoldInDateRange(item,
            currDate, currDate, UnitTime.HOUR);

        for (DateTime dt : numSold.elementSet()) {
          int prevTotal = 0;
          if (result.get(currDate.getDayOfWeek())
              .get(dt.getHourOfDay()) != null) {
            prevTotal = result.get(currDate.getDayOfWeek())
                .get(dt.getHourOfDay());
          }
          // Add to transactions on date
          result.get(currDate.getDayOfWeek()).put(dt.getHourOfDay(),
              numSold.count(dt) + prevTotal);
        }
      }
      currDate = Dates.incrementDate(currDate, UnitTime.DAY);
    }

    for (Integer day : result.keySet()) {
      for (Integer hour : result.get(day).keySet()) {
        int totalHour = result.get(day).get(hour);
        result.get(day).replace(hour,
            ((int) (Math.round(totalHour / totalWeeks))));
      }
    }

    // Check to ensure any hours where items weren't sold have a zero attacked
    // to them
    currDate = start;

    while (currDate.compareTo(end) < 0) {

      Set<Integer> hours = result.get(currDate.getDayOfWeek()).keySet();
      List<DateTime> allHours = Dates.getDatesBetween(currDate, currDate,
          UnitTime.HOUR);

      for (DateTime hour : allHours) {
        if (!hours.contains(hour.getHourOfDay())) {
          result.get(currDate.getDayOfWeek()).put(hour.getHourOfDay(), 0);
        }
      }
      currDate = Dates.incrementDate(currDate, UnitTime.DAY);
    }
    return result;
  }

  /**
   * Get items within date range.
   *
   * @param ut
   *          unit of time (hour, day, week)
   * @return List of ProductProfiles within dates
   */

  @Override
  public List<ProductProfile> getItemsInDateRange(UnitTime ut) {

    /*
     * Products must be decorated with total number of sales
     */

    List<ProductProfile> products = new ArrayList<>();

    if (start.compareTo(middle) > 0) {
      return products;
    }

    if (start.compareTo(getNow()) > 0) {
      return products;
    }

    if (ut.equals(UnitTime.HOUR) && start.compareTo(DateTime.now()) > 0) {
      return products;
    }

    for (HubwayItem item : reader.getItems()) {
      Multiset<DateTime> numSold = reader.getNumSoldInDateRange(item, start,
          middle, ut);

      int itemTotalNumSold = 0;
      for (DateTime dt : numSold.elementSet()) {
        int dateTotal = numSold.count(dt);
        itemTotalNumSold += dateTotal;
      }

      item.setTotalNumberSold(itemTotalNumSold);
      item.setTotalNumberSoldPerUnitTimeMap(numSold, start, middle, ut);
      products.add(item);
      assert (products.contains(item));
    }

    Collections.sort(products);

    // after being sorted, their index is their rank.
    for (int i = 0; i < products.size(); i++) {
      products.get(i).setRank(i + 1);
    }

    return products;
  }

  /**
   * @return list of items sold by hubway.
   */
  @Override
  public List<ProductProfile> getItems() {
    List<ProductProfile> items = new ArrayList<>();
    for (ProductProfile item : this.reader.getItems()) {
      items.add(item);
    }
    return items;
  }

  @Override
  public Map<DateTime, Double> predictTotalSalesInDateRange(UnitTime ut) {
    return this.helper.predictTotalSalesInDateRange(middle, end, ut, db,
        locationId);
  }

  @Override
  public List<ProductProfile> predictItemsInDateRange(UnitTime ut) {
    return this.helper.predictItemsInDateRange(middle, end, ut, db, locationId);
  }

  @Override
  public String getTrendBlurbs() {
    return helper.getTrendBlurbs(start, end);
  }

  @Override
  public String getNextWeekBlurbs() {
    return helper.getTrendBlurbs(start, end);
  }

  @Override
  public Map<DateTime, Double> getTemperatureInDateRange(UnitTime ut) {
    return helper.getTemperatureInDateRange(start, end, ut);
  }

  @Override
  public Map<DateTime, Double> getPrecipitationInDateRange(UnitTime ut) {
    return helper.getPrecipitationInDateRange(start, end, ut);
  }
}
