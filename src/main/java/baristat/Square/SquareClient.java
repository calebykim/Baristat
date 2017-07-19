package baristat.Square;

import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.gson.Gson;

import baristat.API.Client;
import baristat.API.ClientHelper;
import baristat.API.ProductProfile;
import baristat.Dates.Dates;
import baristat.Dates.UnitTime;
import baristat.Weather.WeatherClient;

/**
 * This class is responsible for retrieving sales data from Square.
 *
 * @author adrianturcu
 */
public class SquareClient implements Client {

  // The base URL for every Connect API request
  private SquareReader reader;
  private WeatherClient weatherClient;
  private DateTime now;
  private DateTime start;
  private DateTime middle;
  private DateTime end;
  private String city;
  private String state;
  private ClientHelper helper;
  private SquareUsers db;
  private String accessToken;
  private String locationId;

  private static final Gson GSON = new Gson();

  /**
   * This is the constructor for the DataHandler class.
   *
   * @param accessToken
   *          the accessToken
   * @param locationId
   *          the locationId
   * @param start
   *          the start date
   * @param middle
   *          the middle date
   * @param end
   *          the end date
   * @param db
   *          the database of users, locations, and items
   */
  public SquareClient(String accessToken, String locationId, DateTime start,
      DateTime middle, DateTime end, SquareUsers db) {
    this.accessToken = accessToken;
    this.locationId = locationId;
    reader = new SquareReader(this.accessToken, this.locationId);
    weatherClient = new WeatherClient();
    this.now = DateTime.now();
    this.start = start;
    this.middle = middle;
    this.end = end;
    this.db = db;
    String[] cityState = null;
    try {
      cityState = this.db.getCityState(this.locationId);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    this.city = cityState[0];
    this.state = cityState[1];
    this.helper = new ClientHelper(this, weatherClient, city, state);
  }

  /**
   * This method is responsible for getting all of the sales made in a certain
   * date range for the sandbox.
   *
   * @param start
   *          the start date
   * @param end
   *          the end date
   * @param ut
   *          the unit time
   * @return a map
   */
  public Map<DateTime, Double> getSandboxTotalSalesInDateRange(DateTime start,
      DateTime end, UnitTime ut) {

    Map<DateTime, Double> dateToSales = new TreeMap<DateTime, Double>();

    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    Date startDate = start.toDate();
    Date endDate = end.toDate();
    String startFormatted = df.format(startDate);
    String endFormatted = df.format(endDate);

    HttpClient client = HttpClients.createDefault();
    HttpGet get = new HttpGet(connectHost + "/v2/locations/" + locationId
        + "/transactions?begin_time=" + startFormatted + "&" + "end_time="
        + endFormatted);

    get.addHeader("Authorization", "Bearer " + accessToken);
    get.addHeader("Content-Type", "application/json");
    get.addHeader("Accept", "application/json");
    get.addHeader("begin_time", startFormatted);
    get.addHeader("end_time", endFormatted);

    HttpResponse res = null;
    try {
      res = client.execute(get);
    } catch (IOException e1) {
      e1.printStackTrace();
    }
    HttpEntity entity = res.getEntity();

    if (entity != null) {
      String retSrc = null;
      try {
        retSrc = EntityUtils.toString(entity);
      } catch (ParseException | IOException e) {
        e.printStackTrace();
      }
      JSONObject obj = new JSONObject(retSrc);
      JSONArray array = obj.getJSONArray("transactions");

      for (int i = 0; i < array.length(); i++) {

        JSONObject curr = array.getJSONObject(i);

        String dateCreated = curr.getString("created_at");
        DateTimeFormatter formatter = DateTimeFormat
            .forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        DateTime dt = formatter.parseDateTime(dateCreated);
        JSONObject money = curr.getJSONArray("tenders").getJSONObject(0)
            .getJSONObject("amount_money");
        double price = .01 * money.getInt("amount");

        DateTime rounded = null;
        switch (ut) {
          case HOUR:
            rounded = Dates.roundToNearestHour(dt);
            break;
          case DAY:
            rounded = Dates.roundToNearestDay(dt);
            break;
          default:
            break;
        }

        if (dateToSales.containsKey(rounded)) {
          double currVal = dateToSales.get(rounded);
          dateToSales.replace(rounded, currVal + price);
        } else {
          dateToSales.put(rounded, price);
        }

      }
    }

    return dateToSales;
  }

  /**
   * This method is responsible for getting all of the sales made in a certain
   * date range.
   *
   * @param ut
   *          the unit time
   * @return a map
   */
  @Override
  public Map<DateTime, Double> getTotalSalesInDateRange(UnitTime ut) {

    Map<DateTime, Double> dateToSales = new TreeMap<DateTime, Double>();

    if (start.compareTo(end) > 0) {
      return dateToSales;
    }

    if (ut.equals(UnitTime.HOUR) && start.compareTo(DateTime.now()) > 0) {
      return dateToSales;
    }

    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    Date startDate = start.toDate();
    Date endDate = Dates.incrementDate(end, UnitTime.DAY).toDate();
    String startFormatted = df.format(startDate);
    String endFormatted = df.format(endDate);

    HttpClient client = HttpClients.createDefault();
    HttpGet get = new HttpGet(
        connectHost + "/v1/" + locationId + "/payments?begin_time="
            + startFormatted + "&end_time=" + endFormatted);

    get.addHeader("Authorization", "Bearer " + accessToken);
    get.addHeader("Content-Type", "application/json");
    get.addHeader("Accept", "application/json");
    get.addHeader("begin_time", startFormatted);
    get.addHeader("end_time", endFormatted);

    HttpResponse res = null;
    try {
      res = client.execute(get);
    } catch (ClientProtocolException e1) {
      e1.printStackTrace();
    } catch (IOException e1) {
      e1.printStackTrace();
    }
    HttpEntity entity = res.getEntity();

    if (entity != null) {
      String retSrc = null;
      try {
        retSrc = EntityUtils.toString(entity);
      } catch (ParseException | IOException e) {
        e.printStackTrace();
      }

      JSONArray array = new JSONArray(retSrc);

      for (int i = 0; i < array.length(); i++) {

        JSONObject obj = array.getJSONObject(i);

        String dateCreated = obj.getString("created_at");
        DateTimeFormatter formatter = DateTimeFormat
            .forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        DateTime dt = formatter.parseDateTime(dateCreated);
        double price = .01
            * obj.getJSONObject("total_collected_money").getInt("amount");

        DateTime rounded = null;
        switch (ut) {
          case HOUR:
            rounded = Dates.roundToNearestHourInDay(dt);
            break;
          case DAY:
            rounded = Dates.roundToNearestDay(dt);
            break;
          default:
            break;
        }

        if (dateToSales.containsKey(rounded)) {
          double currVal = dateToSales.get(rounded);
          dateToSales.replace(rounded, currVal + price);
        } else {
          dateToSales.put(rounded, price);
        }
      }

    }

    for (DateTime dt : Dates.getDatesBetween(start, end, ut)) {
      if (!dateToSales.containsKey(dt)) {
        dateToSales.put(dt, 0.0);
      }
    }

    return dateToSales;

  }

  @Override
  public List<ProductProfile> getItemsInDateRange(UnitTime ut) {

    List<ProductProfile> products = new ArrayList<ProductProfile>();

    if (start.compareTo(end) > 0) {
      return products;
    }

    if (ut.equals(UnitTime.HOUR) && start.compareTo(DateTime.now()) > 0) {
      return products;
    }

    // end = Dates.incrementDate(end, UnitTime.DAY);
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    Date startDate = start.toDate();
    Date endDate = Dates.incrementDate(end, UnitTime.DAY).toDate();
    String startFormatted = df.format(startDate);
    String endFormatted = df.format(endDate);

    Map<String, ProductProfile> itemToProfile;
    itemToProfile = new HashMap<String, ProductProfile>();

    HttpClient client = HttpClients.createDefault();
    HttpGet get = new HttpGet(
        connectHost + "/v1/" + locationId + "/payments?begin_time="
            + startFormatted + "&end_time=" + endFormatted);

    get.addHeader("Authorization", "Bearer " + accessToken);
    get.addHeader("Content-Type", "application/json");
    get.addHeader("Accept", "application/json");
    get.addHeader("begin_time", startFormatted);
    get.addHeader("end_time", endFormatted);

    HttpResponse res = null;

    try {
      res = client.execute(get);
    } catch (IOException e1) {
      e1.printStackTrace();
    }

    HttpEntity entity = res.getEntity();

    if (entity != null) {
      String retSrc = null;

      try {
        retSrc = EntityUtils.toString(entity);
      } catch (ParseException | IOException e) {
        e.printStackTrace();
      }

      JSONArray array = new JSONArray(retSrc);

      for (int i = 0; i < array.length(); i++) {

        JSONObject obj = array.getJSONObject(i);

        String dateCreated = obj.getString("created_at");
        DateTimeFormatter formatter = DateTimeFormat
            .forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        DateTime dt = formatter.parseDateTime(dateCreated);

        DateTime rounded = null;
        switch (ut) {
          case HOUR:
            rounded = Dates.roundToNearestHourInDay(dt);
            break;
          case DAY:
            rounded = Dates.roundToNearestDay(dt);
            break;
          default:
            break;
        }

        JSONArray itemizations = obj.getJSONArray("itemizations");
        for (int j = 0; j < itemizations.length(); j++) {
          JSONObject itemInfo = itemizations.getJSONObject(j);
          String name = itemInfo.getString("name");
          if (itemToProfile.containsKey(name)) {
            ProductProfile updatedProfile = itemToProfile.get(name);
            Multiset<DateTime> newNumSold = updatedProfile.getNumSoldMap();
            newNumSold.add(rounded, (int) itemInfo.getDouble("quantity"));
            updatedProfile.setTotalNumberSoldPerUnitTimeMap(newNumSold, start,
                end, ut);
            updatedProfile.setTotalNumberSold(updatedProfile.getTotalNumSold()
                + (int) itemInfo.getDouble("quantity"));
            itemToProfile.replace(name, updatedProfile);
          } else {

            Multiset<DateTime> numSold = HashMultiset.create();

            double cost = .01
                * itemInfo.getJSONObject("total_money").getInt("amount");
            ProductProfile newProfile = new ProductProfile(name, cost);
            numSold.add(rounded, (int) itemInfo.getDouble("quantity"));
            newProfile.setTotalNumberSoldPerUnitTimeMap(numSold, start, end,
                ut);
            newProfile.setTotalNumberSold(newProfile.getTotalNumSold()
                + (int) itemInfo.getDouble("quantity"));
            itemToProfile.put(name, newProfile);
          }

        }

      }
    }

    for (String item : itemToProfile.keySet()) {
      products.add(itemToProfile.get(item));
    }

    Collections.sort(products);

    for (int i = 0; i < products.size(); i++) {
      products.get(i).setRank(i + 1);
    }

    return products;
  }

  public Map<Integer, Map<Integer, Integer>> getNumTransactionsInDateRange() {

    Map<Integer, Map<Integer, Integer>> result = new TreeMap<>();

    int totalWeeks = 0;

    DateTime currDate = start;
    while (currDate.compareTo(end) < 0 && result.keySet().size() < 7) {
      if (result.get(currDate.getDayOfWeek()) == null) {
        result.put(currDate.getDayOfWeek(), new TreeMap<Integer, Integer>());
        totalWeeks = 1;
      } else {
        totalWeeks = 4;
      }

      List<ProductProfile> items = this.getItemsInDateRange(UnitTime.HOUR);

      // Go through each item and add its transactions to the date
      for (ProductProfile item : items) {
        // Multiset of Datetimes representing number of times item is sold
        Multiset<DateTime> numSold = item.getNumSoldMap();

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
