package baristat.API;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.gson.Gson;

import Jama.Matrix;
import baristat.Dates.Dates;
import baristat.Dates.UnitTime;
import baristat.Main.Constants;
import baristat.Square.SquareUsers;
import baristat.Stats.Anova;
import baristat.Stats.RegressionTools;
import baristat.Weather.WeatherClient;
import baristat.Weather.WeatherData;
import baristat.Weather.WeatherProfile;

/**
 * Helper class for clients, specifically SquareClient and HubwayClient.
 *
 * @author Tristin Falk
 *
 */
public class ClientHelper {
  private Client client;
  private WeatherClient weatherClient;
  private static final Gson GSON = new Gson();
  private String city;
  private String state;

  /**
   * Constructor.
   *
   * @param cli
   *          client to help
   * @param weatherCli
   *          weather client to get weather from
   * @param city
   *          city to get weather from
   * @param state
   *          state to get weather from
   */
  public ClientHelper(Client cli, WeatherClient weatherCli, String city,
      String state) {
    this.client = cli;
    this.weatherClient = weatherCli;
    this.city = city;
    this.state = state;
  }

  /**
   * Gets Trend blurbs.
   *
   * @param start
   *          start date
   * @param end
   *          end date
   * @return JSON of trend blurbs.
   */
  public String getTrendBlurbs(DateTime start, DateTime end) {
    Map<Integer, Map<Integer, Integer>> traffic = client
        .getNumTransactionsInDateRange();

    String bestDay = "Did not get best day";
    String worstDay = "Did not get worst day";

    double avgTraffic = 0.0;
    int maxDay = 0;
    int minDay = Integer.MAX_VALUE;

    for (Integer day : traffic.keySet()) {
      int dayTotal = 0;
      for (Integer hourTotal : traffic.get(day).values()) {
        dayTotal += hourTotal;
      }
      if (dayTotal > maxDay) {
        maxDay = dayTotal;
        bestDay = Dates.integerToDayOfWeek(day);
      } else if (dayTotal < minDay) {
        minDay = dayTotal;
        worstDay = Dates.integerToDayOfWeek(day);
      }
      avgTraffic += dayTotal;
    }

    avgTraffic = avgTraffic / Constants.DAYS_PER_WEEK;

    Map<String, Object> variables = ImmutableMap.of("traffic", traffic,
        "bestDay", bestDay, "worstDay", worstDay, "avgTraffic", avgTraffic);
    return GSON.toJson(variables);

  }

  /**
   * Gets next week blurbs.
   *
   * @param db
   *          database to use for checking for models
   * @param locationId
   *          locId
   * @return JSON of blurbs for next week
   */
  public String getNextWeekBlurbs(SquareUsers db, String locationId) {
    // WILL NEED TO CHANGE THIS TO DATETIME.NOW()
    DateTime nextStart = client.getNow();

    DateTime nextEnd = nextStart.plusDays(Constants.DAYS_PER_WEEK - 1);

    // DateTime lastStart = nextStart.minusDays(Constants.DAYS_PER_WEEK - 1);
    // DateTime lastEnd = nextEnd.minusDays(Constants.DAYS_PER_WEEK - 1);

    Map<DateTime, Double> lastWeeksSales = client
        .getTotalSalesInDateRange(UnitTime.DAY);

    Map<DateTime, Double> nextWeeksSales = this.predictTotalSalesInDateRange(
        nextStart, nextEnd, UnitTime.DAY, db, locationId);

    List<ProductProfile> nextWeekItems = this.predictItemsInDateRange(nextStart,
        nextEnd, UnitTime.DAY, db, locationId);

    List<ProductProfile> lastWeekItems = client
        .getItemsInDateRange(UnitTime.DAY);

    String busiestDay = "Did not get busiest day"; // determined by traffic
    double maxDay = 0;
    double minDay = Double.MAX_VALUE;

    ProductProfile mostPopularItem = new ProductProfile("No Item", 0.0);
    ProductProfile itemOnTheRise = new ProductProfile("No Item", 0.0);

    if (nextWeekItems.size() > 1) {
      itemOnTheRise = nextWeekItems.get(1);
    }
    // highest percent growth from last
    // week

    double percentChangeFromLastWeek = 0.0;

    double nextWeekTotalSales = 0.0;
    double lastWeekTotalSales = 0.0;

    double maxItem = 0.0;

    double bestItemRatio = Double.MIN_VALUE;

    List<Integer> dayVisits = new ArrayList<>();

    for (DateTime nextDay : nextWeeksSales.keySet()) {
      double nextSale = nextWeeksSales.get(nextDay);
      if (nextSale > maxDay) {
        maxDay = nextSale;
        busiestDay = Dates.integerToDayOfWeek(nextDay.getDayOfWeek());
      } else if (nextSale < minDay) {
        minDay = nextSale;
      }
      dayVisits.add((int) nextSale);
      nextWeekTotalSales += nextSale;
    }

    for (DateTime lastDay : lastWeeksSales.keySet()) {
      lastWeekTotalSales += lastWeeksSales.get(lastDay);
    }

    List<Integer> quarts = RegressionTools.getQuartiles(dayVisits);
    quarts.set(0, quarts.get(0) / 5); // divide by 5 because average transaction
                                      // is $5
    quarts.set(1, quarts.get(1) / 5);

    Map<String, ProductProfile> pastItems = new HashMap<>();
    for (ProductProfile product : lastWeekItems) {
      pastItems.put(product.getName(), product);
    }

    // if item did not show up last week, xthen definitely
    // on the rise. if multiple, choose one with highest
    // total num sold
    for (ProductProfile item : nextWeekItems) {
      if (!(pastItems.containsKey(item.getName()))) {
        if (item.getTotalNumSold() > bestItemRatio) {
          bestItemRatio = item.getTotalNumSold();
          itemOnTheRise = item;
        }
      }
    }

    // if did not just find item on the rise
    if (itemOnTheRise.getName().equals("No Item")) {
      for (ProductProfile item : nextWeekItems) {
        if (pastItems.get(item.getName()).getTotalNumSold() == 0) {
          if (item.getTotalNumSold() > bestItemRatio) {
            bestItemRatio = item.getTotalNumSold();
            itemOnTheRise = item;
          }
          continue;
        }
        double itemRatio = item.getTotalNumSold()
            / pastItems.get(item.getName()).getTotalNumSold();
        if (itemRatio > bestItemRatio) {
          bestItemRatio = itemRatio;
          itemOnTheRise = item;
        }
      }
    }

    // find most popular item
    for (ProductProfile item : nextWeekItems) {
      if (item.getTotalNumSold() > maxItem) {
        maxItem = item.getTotalNumSold();
        mostPopularItem = item;
      }
    }

    percentChangeFromLastWeek = nextWeekTotalSales / lastWeekTotalSales;

    Map<String, Object> items = ImmutableMap.of("mostPopularItem",
        mostPopularItem, "itemOnRise", itemOnTheRise);

    Map<String, Object> variables = ImmutableMap.of("busiestDay", busiestDay,
        "percentChange", percentChangeFromLastWeek, "items", items,
        "predictedTotalSales", nextWeekTotalSales, "quartiles", quarts);
    return GSON.toJson(variables);

  }

  /**
   * Get map from dates to total sales.
   *
   * @param start
   *          start of date range
   * @param end
   *          end of date range
   * @param ut
   *          unit of time (hour, day, week)
   * @param db
   *          users database
   * @param locationId
   *          locationId of user invoking method
   * @return Map from DateTime to total sales
   */
  public Map<DateTime, Double> predictTotalSalesInDateRange(DateTime start,
      DateTime end, UnitTime ut, SquareUsers db, String locationId) {

    Map<DateTime, Double> predictions = new TreeMap<>();

    DateTime now = client.getNow();

    if (ut.equals(UnitTime.DAY) && start.compareTo(end) > 0) {
      return predictions;
    }

    if (start.compareTo(end) == 0 && now.compareTo(end) > 0) {
      return predictions;
    }

    if (ut.equals(UnitTime.HOUR) && start.compareTo(now) > 0) {
      return predictions;
    }

    boolean isExpired = false;
    try {
      isExpired = db.modelIsExpired(locationId);
    } catch (SQLException | ParseException e1) {
      e1.printStackTrace();
    }

    List<Double> betaValues = null;

    if (!isExpired) {
      try {
        betaValues = db.getModelFromDb(locationId);
      } catch (SQLException e) {
        e.printStackTrace();
      }
    } else {
      betaValues = getModel();
      try {
        db.addModelToLocation(locationId, betaValues);
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }

    // With model, create predictions.

    int numPredictions = Dates.getDatesBetween(start, end, ut).size();
    List<Double> tempValues = new ArrayList<>();
    List<Double> precipValues = new ArrayList<>();
    List<List<Double>> monthValues = RegressionTools.getMonthCols(start, end,
        ut);
    List<List<Double>> dayValues = RegressionTools.getDayCols(start, end, ut);
    List<Double> holidayValues = RegressionTools.getHolidayCol(start, end, ut);

    List<WeatherProfile<Double>> weatherForecast;

    if (start.compareTo(DateTime.now()) > 0) {
      if (ut.equals(UnitTime.DAY)) {
        weatherForecast = weatherClient.getWeeklyForecast(city, state);
      } else {
        weatherForecast = weatherClient.getHourlyForecast(city, state);
        assert weatherForecast.size() == 49;
      }
    } else if (end.compareTo(DateTime.now()) < 0) {
      weatherForecast = weatherClient.getWeatherByDate(city, state, start, end,
          ut);
    } else {
      // combination of both
      weatherForecast = weatherClient.getWeatherByDate(city, state, start,
          Dates.roundToNearestDay(DateTime.now()), ut);
      weatherForecast.addAll(weatherClient.getWeeklyForecast(city, state));
    }

    for (WeatherProfile<Double> prof : weatherForecast) {
      tempValues.add(prof.get(WeatherData.AVG_TEMP));
      precipValues.add(prof.get(WeatherData.INCHES_PRECIP));
    }

    // This will all need to change

    DateTime currDate = start;
    for (int i = 0; i < numPredictions; i++) {

      double dateTotalSales = 0.0;

      dateTotalSales += betaValues.get(0); // constant
      dateTotalSales += betaValues.get(1) * tempValues.get(i); // weather
      dateTotalSales += betaValues.get(2) * precipValues.get(i); // weather

      for (int j = 0; j < monthValues.size(); j++) {
        dateTotalSales += betaValues.get(2 + j) * monthValues.get(j).get(i);
      }

      for (int j = 0; j < dayValues.size(); j++) {
        dateTotalSales += betaValues.get(monthValues.size() + 2 + j)
            * dayValues.get(j).get(i);
      }

      dateTotalSales += betaValues.get(
          monthValues.size() + dayValues.size() + 2) * holidayValues.get(i);

      predictions.put(currDate, Math.max(dateTotalSales, 0));

      currDate = Dates.incrementDate(currDate, ut);
    }

    return predictions;
  }

  private List<Double> getModel() {
    DateTimeFormatter formatter = DateTimeFormat
        .forPattern("dd/MM/yyyy HH:mm:ss");
    DateTime startDate = formatter.parseDateTime("01/03/2016 00:00:00");
    DateTime endDate = formatter.parseDateTime("28/02/2017 00:00:00");

    // Weather
    List<WeatherProfile<Double>> profiles = weatherClient.getWeatherByDate(city,
        state, startDate, endDate, UnitTime.DAY);
    Map<DateTime, Double> salesMap = client
        .getTotalSalesInDateRange(UnitTime.DAY);
    List<Double> weatherCol = new ArrayList<>();
    List<Double> precipCol = new ArrayList<>();
    for (WeatherProfile<Double> prof : profiles) {
      weatherCol.add(prof.get(WeatherData.AVG_TEMP));
      precipCol.add(prof.get(WeatherData.INCHES_PRECIP));
    }

    List<Double> salesCol = RegressionTools.mapToSortedCol(salesMap);

    assert weatherCol.size() == salesCol.size();

    List<List<Double>> weekdayCols = RegressionTools.getDayCols(startDate,
        endDate, UnitTime.DAY);

    List<List<Double>> monthCols = RegressionTools.getMonthCols(startDate,
        endDate, UnitTime.DAY);

    List<Double> holidayCol = RegressionTools.getHolidayCol(startDate, endDate,
        UnitTime.DAY);

    List<List<Double>> xLists = new ArrayList<>();
    xLists.add(RegressionTools.getOnesCol(weatherCol.size()));

    xLists.add(weatherCol);
    xLists.add(precipCol);
    xLists.addAll(monthCols);
    xLists.addAll(weekdayCols);
    xLists.add(holidayCol);

    List<List<Double>> yLists = new ArrayList<>();
    yLists.add(salesCol);

    Matrix x = RegressionTools.colsToMatrix(xLists);
    Matrix y = RegressionTools.colsToMatrix(yLists);

    Anova anova = new Anova(x, y);
    return anova.getBeta();
  }

  /**
   * Gets temperature within date range.
   *
   * @param start
   *          start date
   * @param end
   *          end date
   * @param ut
   *          unit time
   * @return map from date to temp in farenheit
   */
  public Map<DateTime, Double> getTemperatureInDateRange(DateTime start,
      DateTime end, UnitTime ut) {

    WeatherClient weatherCli = new WeatherClient();

    List<WeatherProfile<Double>> weatherForecast;

    Map<DateTime, Double> dateToTemp = new TreeMap<DateTime, Double>();

    if (ut.compareTo(UnitTime.HOUR) == 0) {
      return dateToTemp;
    }

    if (start.compareTo(DateTime.now()) > 0) {
      weatherForecast = weatherCli.getWeeklyForecast(city, state);
    } else if (end.compareTo(DateTime.now()) < 0) {
      weatherForecast = weatherClient.getWeatherByDate(city, state, start, end,
          ut);
    } else {
      // combination of both
      weatherForecast = weatherClient.getWeatherByDate(city, state, start,
          Dates.roundToNearestDay(DateTime.now()), ut);
      weatherForecast.addAll(weatherCli.getWeeklyForecast(city, state));
    }

    DateTime curr = start;

    for (int i = 0; i < Dates.getDatesBetween(start, end).size(); i++) {
      dateToTemp.put(curr, weatherForecast.get(i).getAverageFahrenheit());
      curr = Dates.incrementDate(curr, ut);
    }

    return dateToTemp;
  }

  /**
   * Gets precipitation within date range.
   *
   * @param start
   *          start date
   * @param end
   *          end date
   * @param ut
   *          unit time
   * @return map from date to precip in intensity
   */
  public Map<DateTime, Double> getPrecipitationInDateRange(DateTime start,
      DateTime end, UnitTime ut) {

    WeatherClient weatherCli = new WeatherClient();

    List<WeatherProfile<Double>> weatherForecast;

    Map<DateTime, Double> dateToPrecip = new TreeMap<DateTime, Double>();

    if (ut.compareTo(UnitTime.HOUR) == 0) {
      return dateToPrecip;
    }

    if (start.compareTo(DateTime.now()) > 0) {
      weatherForecast = weatherCli.getWeeklyForecast(city, state);
    } else if (end.compareTo(DateTime.now()) < 0) {
      weatherForecast = weatherClient.getWeatherByDate(city, state, start, end,
          ut);
    } else {
      // combination of both
      weatherForecast = weatherClient.getWeatherByDate(city, state, start,
          Dates.roundToNearestDay(DateTime.now()), ut);
      weatherForecast.addAll(weatherCli.getWeeklyForecast(city, state));
    }

    DateTime curr = start;

    for (int i = 0; i < Dates.getDatesBetween(start, end).size(); i++) {
      dateToPrecip.put(curr, weatherForecast.get(i).getPrecipitation());
      curr = Dates.incrementDate(curr, ut);
    }

    return dateToPrecip;
  }

  /**
   * Get map from dates to total sales.
   *
   * @param start
   *          start of date range
   * @param end
   *          end of date range
   * @param ut
   *          unit of time (hour, day, week)
   * @param db
   *          db for users
   * @param locationId
   *          id of location for user
   *
   * @return Map from DateTime to total sales
   */
  public List<ProductProfile> predictItemsInDateRange(DateTime start,
      DateTime end, UnitTime ut, SquareUsers db, String locationId) {

    List<ProductProfile> items = new ArrayList<>();

    Multimap<String, Double> itemModels = LinkedListMultimap.create();

    DateTime now = client.getNow();

    if (ut.equals(UnitTime.DAY) && start.compareTo(end) > 0) {
      return items;
    }

    if (start.compareTo(end) == 0 && now.compareTo(end) > 0) {
      return items;
    }

    if (ut.equals(UnitTime.HOUR) && start.compareTo(now) > 0) {
      return items;
    }

    // if (ut.equals(UnitTime.HOUR)) {
    // // might need to change end date
    // end = end.plusDays(1);
    // }

    try {
      if (db.itemModelsNeedUpdate(locationId)) {
        List<ProductProfile> pastItems = client.getItemsInDateRange(ut);
        for (ProductProfile oldItem : pastItems) {
          for (Double dub : this.getItemModel(oldItem)) {
            itemModels.put(oldItem.getName(), dub);
          }
          db.insertOrReplaceItem(locationId, oldItem.getName(),
              itemModels.get(oldItem.getName()).iterator());
        }
      } else {
        itemModels = db.getItemModels(locationId);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } catch (ParseException e) {
      e.printStackTrace();
    }

    WeatherClient weatherCli = new WeatherClient();

    // Get all columns

    List<List<Double>> cols = new ArrayList<>();

    List<Double> tempValues = new ArrayList<>();
    List<Double> precipValues = new ArrayList<>();
    List<List<Double>> monthValues = RegressionTools.getMonthCols(start, end,
        ut);
    List<List<Double>> dayValues = RegressionTools.getDayCols(start, end, ut);
    List<Double> holidayValues = RegressionTools.getHolidayCol(start, end, ut);
    List<Double> ones = RegressionTools.getOnesCol(dayValues.get(0).size());

    List<WeatherProfile<Double>> weatherForecast;

    if (start.compareTo(DateTime.now()) > 0) {
      if (ut.equals(UnitTime.DAY)) {
        weatherForecast = weatherCli.getWeeklyForecast(city, state);
      } else {
        weatherForecast = weatherCli.getHourlyForecast(city, state);
        assert weatherForecast.size() == 49;
      }
    } else if (end.compareTo(DateTime.now()) < 0) {
      weatherForecast = weatherClient.getWeatherByDate(city, state, start, end,
          ut);
    } else {
      // combination of both
      weatherForecast = weatherClient.getWeatherByDate(city, state, start,
          Dates.roundToNearestDay(DateTime.now()), ut);
      weatherForecast.addAll(weatherCli.getWeeklyForecast(city, state));
    }

    for (WeatherProfile<Double> prof : weatherForecast) {
      tempValues.add(prof.get(WeatherData.AVG_TEMP));
      precipValues.add(prof.get(WeatherData.INCHES_PRECIP));
    }

    cols.add(ones);
    cols.add(tempValues);
    cols.add(precipValues);
    cols.addAll(monthValues);
    cols.addAll(dayValues);
    cols.add(holidayValues);

    Matrix xMatrix = RegressionTools.colsToMatrix(cols);

    // Have models, have columns, just need to predict

    DateTime currDate = start;

    for (ProductProfile item : client.getItems()) {
      Multiset<DateTime> numSold = this.predictNumSoldInDateRange(item,
          itemModels.get(item.getName()).iterator(), xMatrix, start, end, ut);

      int itemTotalNumSold = 0;
      for (DateTime dt : numSold.elementSet()) {
        int dateTotal = numSold.count(dt);
        itemTotalNumSold += dateTotal;
      }

      item.setTotalNumberSold(itemTotalNumSold);
      item.setTotalNumberSoldPerUnitTimeMap(numSold, start, end, ut);
      items.add(item);
      currDate = Dates.incrementDate(currDate, ut);
    }

    return items;
  }

  private Multiset<DateTime> predictNumSoldInDateRange(ProductProfile item,
      Iterator<Double> iter, Matrix x, DateTime start, DateTime end,
      UnitTime ut) {
    int numPredictions = Dates.getDatesBetween(start, end, ut).size();

    Multiset<DateTime> result = HashMultiset.create();

    double[][] betaArray = new double[x.getColumnDimension()][1];

    int i = 0;
    while (iter.hasNext()) {
      betaArray[i][0] = iter.next();
      i++;
    }

    // List<List<Double>> betas = new ArrayList<>();
    // betas.add(betaValues);
    Matrix beta = new Matrix(betaArray);

    Matrix predictions = x.times(beta);
    double[] predArray = predictions.getColumnPackedCopy();

    // assert (predictions.getColumnDimension() == numPredictions);

    DateTime currDate = start;
    for (i = 0; i < numPredictions; i++) {
      result.add(currDate, Math.max((int) predArray[i], 0));
      currDate = Dates.incrementDate(currDate, ut);
    }

    return result;
  }

  private List<Double> getItemModel(ProductProfile item) {
    DateTimeFormatter formatter = DateTimeFormat
        .forPattern("dd/MM/yyyy HH:mm:ss");
    DateTime startDate = formatter.parseDateTime("01/03/2016 00:00:00");
    DateTime endDate = formatter.parseDateTime("28/02/2017 00:00:00");

    List<WeatherProfile<Double>> profiles = weatherClient.getWeatherByDate(city,
        state, startDate, endDate, UnitTime.DAY);

    List<Double> numSoldCol = RegressionTools.multisetToSortedCol(startDate,
        endDate, UnitTime.DAY, item.getNumSoldMap());

    List<Double> weatherCol = new ArrayList<>();
    List<Double> precipCol = new ArrayList<>();
    for (WeatherProfile<Double> prof : profiles) {
      weatherCol.add(prof.get(WeatherData.AVG_TEMP));
      precipCol.add(prof.get(WeatherData.INCHES_PRECIP));
    }

    List<List<Double>> weekdayCols = RegressionTools.getDayCols(startDate,
        endDate, UnitTime.DAY);

    List<List<Double>> monthCols = RegressionTools.getMonthCols(startDate,
        endDate, UnitTime.DAY);

    List<Double> holidayCol = RegressionTools.getHolidayCol(startDate, endDate,
        UnitTime.DAY);

    assert (weatherCol.size() == numSoldCol.size());

    List<List<Double>> xLists = new ArrayList<>();
    xLists.add(RegressionTools.getOnesCol(weatherCol.size()));

    xLists.add(weatherCol);
    xLists.add(precipCol);

    xLists.addAll(monthCols);
    xLists.addAll(weekdayCols);

    xLists.add(holidayCol);

    List<List<Double>> yLists = new ArrayList<>();
    yLists.add(numSoldCol);

    Matrix x = RegressionTools.colsToMatrix(xLists);
    Matrix y = RegressionTools.colsToMatrix(yLists);

    Anova anova = new Anova(x, y);
    return anova.getBeta();
  }
}
