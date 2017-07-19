package baristat.Main;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Ignore;
import org.junit.Test;

import Jama.Matrix;
import baristat.API.ProductProfile;
import baristat.Dates.UnitTime;
import baristat.Hubway.HubwayClient;
import baristat.Hubway.HubwayWeatherClient;
import baristat.Stats.Anova;
import baristat.Stats.RegressionTools;
import baristat.Weather.WeatherData;
import baristat.Weather.WeatherProfile;

/**
 * This tests the MixedMultipleRegression class.
 *
 * @author adrianturcu
 *
 */
public class MixedMultipleRegressionTest {

  /**
   * This tests the multiple regression for total sales.
   */
  @Test
  @Ignore
  public void totalSalesMultipleRegression() {
    HubwayClient hubClient = new HubwayClient();

    DateTimeFormatter formatter = DateTimeFormat
        .forPattern("dd/MM/yyyy HH:mm:ss");
    // DateTime startDateLastYear = formatter.parseDateTime("01/03/2016
    // 00:00:00");
    DateTime startDateLastYear = formatter.parseDateTime("01/03/2016 00:00:00");
    DateTime endDateLastYear = formatter.parseDateTime("28/02/2017 00:00:00");

    HubwayWeatherClient weatherClient = new HubwayWeatherClient();

    List<WeatherProfile<Double>> profiles = weatherClient.getWeatherInDateRange(
        startDateLastYear, endDateLastYear, UnitTime.DAY);

    Map<DateTime, Double> salesMap = hubClient.getTotalSalesInDateRange(
        startDateLastYear, endDateLastYear, UnitTime.DAY);

    List<Double> weatherCol = new ArrayList<>();
    List<Double> precipCol = new ArrayList<>();
    for (WeatherProfile<Double> prof : profiles) {
      weatherCol.add(prof.get(WeatherData.AVG_TEMP));
      precipCol.add(prof.get(WeatherData.INCHES_PRECIP));
    }

    // List<Double> isWeekendCol = RegressionTools
    // .datesToWeekendCol(startDateLastYear, endDateLastYear);

    List<List<Double>> weekdayCols = RegressionTools
        .getDayCols(startDateLastYear, endDateLastYear, UnitTime.DAY);

    List<List<Double>> monthCols = RegressionTools
        .getMonthCols(startDateLastYear, endDateLastYear, UnitTime.DAY);

    List<Double> holidayCol = RegressionTools.getHolidayCol(startDateLastYear,
        endDateLastYear, UnitTime.DAY);

    // List<List<Double>> hourCols =
    // RegressionTools.getHourCols(startDateLastYear,
    // endDateLastYear, UnitTime.DAY);

    List<Double> salesCol = RegressionTools.mapToSortedCol(salesMap);

    System.out.println(weatherCol.size());
    System.out.println(salesCol.size());

    assert (weatherCol.size() == salesCol.size());

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

    System.out.println("Adjusted R2: " + anova.getAdjustedRSquared());
    System.out.println("R2: " + anova.getRSquared());
  }

  /**
   * This tests the multiple regression for items.
   */
  @Test
  @Ignore
  public void itemTotalMultipleRegression() {
    HubwayClient hubClient = new HubwayClient();

    DateTimeFormatter formatter = DateTimeFormat
        .forPattern("dd/MM/yyyy HH:mm:ss");
    // DateTime startDateLastYear = formatter.parseDateTime("01/03/2016
    // 00:00:00");
    DateTime startDateLastYear = formatter.parseDateTime("01/03/2016 00:00:00");
    DateTime endDateLastYear = formatter.parseDateTime("28/02/2017 00:00:00");

    HubwayWeatherClient weatherClient = new HubwayWeatherClient();

    List<WeatherProfile<Double>> profiles = weatherClient.getWeatherInDateRange(
        startDateLastYear, endDateLastYear, UnitTime.DAY);

    List<ProductProfile> items = hubClient
        .getItemsInDateRange(startDateLastYear, endDateLastYear, UnitTime.DAY);

    List<Double> numSoldCol = RegressionTools.multisetToSortedCol(
        startDateLastYear, endDateLastYear, UnitTime.DAY,
        items.get(0).getNumSoldMap());

    List<Double> weatherCol = new ArrayList<>();
    List<Double> precipCol = new ArrayList<>();
    for (WeatherProfile<Double> prof : profiles) {
      weatherCol.add(prof.get(WeatherData.AVG_TEMP));
      precipCol.add(prof.get(WeatherData.INCHES_PRECIP));
    }

    List<List<Double>> weekdayCols = RegressionTools
        .getDayCols(startDateLastYear, endDateLastYear, UnitTime.DAY);

    List<List<Double>> monthCols = RegressionTools
        .getMonthCols(startDateLastYear, endDateLastYear, UnitTime.DAY);

    List<Double> holidayCol = RegressionTools.getHolidayCol(startDateLastYear,
        endDateLastYear, UnitTime.DAY);

    System.out.println(weatherCol.size());
    System.out.println(numSoldCol.size());

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

    System.out.println("Adjusted R2: " + anova.getAdjustedRSquared());
    System.out.println("R2: " + anova.getRSquared());
  }

}
