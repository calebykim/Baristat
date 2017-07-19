package baristat.Weather;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.joda.time.DateTime;
import org.junit.Ignore;
import org.junit.Test;

import baristat.Dates.UnitTime;

/**
 * This the testing suite for the weather client.
 *
 * @author jbreuch
 */
public class WeatherClientTest {

  /**
   * This checks the construction of the WeatherClient is done properly.
   */
  @Test
  @Ignore
  public void testConstruction() {
    assertNotNull(new WeatherClient());
  }

  /**
   * Test exception.
   */
  @Test(expected = IllegalArgumentException.class)
  @Ignore
  public void testGetWeatherByDateNull() {
    new WeatherClient().getWeatherSummaryForDay(null, null, null, null);
  }

  /**
   * Test exception.
   */
  @Test(expected = IllegalArgumentException.class)
  @Ignore
  public void testGetWeatherByDateEmpty() {
    new WeatherClient().getWeatherSummaryForDay("", "", null, null);
  }

  /**
   * Test exception.
   */
  @Test(expected = IllegalArgumentException.class)
  @Ignore
  public void testGetWeeklyForecastNull() {
    new WeatherClient().getWeeklyForecast(null, null);
  }

  /**
   * Test exception.
   */
  @Test(expected = IllegalArgumentException.class)
  @Ignore
  public void testGetWeeklyForecastEmpty() {
    new WeatherClient().getWeeklyForecast("", "");
  }

  /**
   * Test exception.
   */
  @Test(expected = IllegalArgumentException.class)
  @Ignore
  public void testGetWeatherByDateBadState() {
    final int year = 2016;
    new WeatherClient().getWeatherSummaryForDay("providence", "XX",
        new DateTime(year, 1, 1, 0, 0), null);
  }

  /**
   * Test exception.
   */
  @Test(expected = IllegalArgumentException.class)
  @Ignore
  public void testGetWeeklyForecastBadState() {
    new WeatherClient().getWeeklyForecast("providence", "XX");
  }

  /**
   * Test exception.
   */
  @Test
  @Ignore
  public void testGetWeatherByDateBadCity() {
    final int year = 2016;
    assertNull(new WeatherClient().getWeatherSummaryForDay("notacity", "az",
        new DateTime(year, 1, 1, 0, 0), null));
  }

  /**
   * Test bad city.
   */
  @Test
  @Ignore
  public void testGetWeeklyForecastBadCity() {
    assertNull(new WeatherClient().getWeeklyForecast("notacity", "az"));
  }

  /**
   * Test summary for day.
   */
  @Test
  @Ignore
  public void testGetWeatherSummaryForDay() {
    WeatherClient client = new WeatherClient();
    assertNotNull(client);

    final int year = 2016;
    final int month = 7;
    final int day = 10;

    List<WeatherProfile<Double>> profiles = client.getWeatherSummaryForDay(
        "providence", "ri", new DateTime(year, month, day, 0, 0), UnitTime.DAY);
    assertNotNull(profiles);

    final double[] expectedTemps = {57.0, 64.5, 74.0};

    for (WeatherProfile<Double> profile : profiles) {
      assertNotNull(profile);
      assertTrue(
          profile.get(WeatherData.MIN_TEMP).compareTo(expectedTemps[0]) == 0);
      assertTrue(
          profile.get(WeatherData.AVG_TEMP).compareTo(expectedTemps[1]) == 0);
      assertTrue(
          profile.get(WeatherData.MAX_TEMP).compareTo(expectedTemps[2]) == 0);
    }
  }

  /**
   * Test for summary by date.
   */
  @Test
  @Ignore
  public void testGetWeatherSummaryByDate() {
    WeatherClient client = new WeatherClient();
    assertNotNull(client);

    DateTime start = new DateTime(2016, 7, 10, 0, 0);
    DateTime end = new DateTime(2016, 7, 13, 0, 0);
    List<WeatherProfile<Double>> profiles = client.getWeatherByDate("boston",
        "ma", start, end, UnitTime.DAY);
    assertNotNull(profiles);
    assertTrue(profiles.size() == 4);

    for (WeatherProfile<Double> profile : profiles) {
      System.out.println(profile.get(WeatherData.AVG_TEMP));
      assertNotNull(profile);
    }
  }

  /**
   * Tests for weekly forecast.
   */
  @Test
  @Ignore
  public void testGetWeeklyWeatherForecast() {
    WeatherClient client = new WeatherClient();
    assertNotNull(client);

    List<WeatherProfile<Double>> profiles = client.getWeeklyForecast("austin",
        "tx");
    assertNotNull(profiles);
    assertTrue(profiles.size() == 7);

    for (WeatherProfile<Double> profile : profiles) {
      assertNotNull(profile);
      assertNotNull(profile.get(WeatherData.MIN_TEMP));
      assertNotNull(profile.get(WeatherData.AVG_TEMP));
      System.out.println(profile.get(WeatherData.AVG_TEMP));
      assertNotNull(profile.get(WeatherData.MAX_TEMP));
    }
  }

  /**
   * Test for strange city.
   */
  @Test
  @Ignore
  public void testGetWeeklyWeatherForecastNonEscapedCity() {
    WeatherClient client = new WeatherClient();
    assertNotNull(client);

    List<WeatherProfile<Double>> profiles = client.getWeeklyForecast("New York",
        "NY");
    assertNotNull(profiles);
    assertTrue(profiles.size() == 7);
  }

  /**
   * Tests for 14 day forecast.
   */
  @Test
  @Ignore
  public void testGet14DayForecast() {
    WeatherClient client = new WeatherClient();
    assertNotNull(client);

    List<WeatherProfile<Double>> profiles = client.getWeeklyForecast("austin",
        "tx");
    assertNotNull(profiles);
    assertTrue(profiles.size() == 7);
  }

  /**
   * Tests for an hourly forecast.
   */
  @Test
  @Ignore
  public void getHourlyForecast() {
    WeatherClient client = new WeatherClient();
    assertNotNull(client);
    List<WeatherProfile<Double>> profiles = client.getHourlyForecast("boston",
        "ma");
    assertNotNull(profiles);
    assertTrue(profiles.size() == 49);

  }

}
