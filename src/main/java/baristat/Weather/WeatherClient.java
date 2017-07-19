package baristat.Weather;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;

import baristat.API.APIClient;
import baristat.Dates.Dates;
import baristat.Dates.UnitTime;
import baristat.Geography.GeoCode;
import baristat.Geography.Geography;

/**
 * A client for the Aeris API.
 *
 * @author jbreuch
 */
public class WeatherClient {

  private APIClient apiRequest;
  private static final String KEY = "6411fb26e9851d246c377b2af0b3e560";

  /**
   * This is the constructor for the WeatherClient class.
   */
  public WeatherClient() {
    this.apiRequest = new APIClient(
        String.format("%s%s/", "https://api.darksky.net/forecast/", KEY), "");
  }

  /**
   * This method is responsible for getting weather by date for a city.
   *
   * @param city
   *          the city
   * @param state
   *          the state
   * @param start
   *          the start date
   * @param end
   *          the end date
   * @param ut
   *          the UnitTime
   * @return a list of WeatherProfiles
   */
  public List<WeatherProfile<Double>> getWeatherByDate(String city,
      String state, DateTime start, DateTime end, UnitTime ut) {
    if (city == null || state == null || start == null || end == null
        || city.length() == 0 || state.length() == 0) {
      throw new IllegalArgumentException("ERROR: Parameters can't be null");
    }

    List<WeatherProfile<Double>> profiles = new ArrayList<>();

    // Check for valid state
    if (!Geography.isValidState(state)) {
      throw new IllegalArgumentException("ERROR: Invalid state");
    }

    List<DateTime> dates = Dates.getDatesBetween(start, end);

    GeoCode converter = new GeoCode();

    JSONObject latLon = null;
    try {
      latLon = converter.getCityState(city, state);
    } catch (IOException e) {
      e.printStackTrace();
    }
    String lat = Double.toString(latLon.getDouble("lat"));
    String lng = Double.toString(latLon.getDouble("lng"));

    for (DateTime date : dates) {
      String formattedDate = Dates.parseDarkSkyDateTime(date);
      String response = apiRequest
          .get(String.format("%s,%s,%s", lat, lng, formattedDate));

      JSONObject jsonObject = new JSONObject(response);
      List<WeatherProfile<Double>> fetched;
      if (ut == UnitTime.HOUR) {
        fetched = parseHour(jsonObject);
      } else {
        fetched = parseSummaries(jsonObject);
      }

      if (fetched.size() > 0) {
        profiles.addAll(fetched);
      }
    }

    return profiles;
  }

  /**
   * Returns a list of weather from start to end dates.
   *
   * @param city
   *          the city
   * @param state
   *          the state
   * @param day
   *          the starting day
   * @param ut
   *          the UnitTime
   * @return a list of weather from start to end dates
   */
  public List<WeatherProfile<Double>> getWeatherSummaryForDay(String city,
      String state, DateTime day, UnitTime ut) {

    return getWeatherByDate(city, state, day, day, ut);

  }

  /**
   * Gets forecast for next week.
   *
   * @param city
   *          the city for the forecast
   * @param state
   *          the state for the forecast
   * @return a list of weather profiles for the next week
   */
  public List<WeatherProfile<Double>> getWeeklyForecast(String city,
      String state) {

    if (city == null || state == null || city.length() == 0
        || state.length() == 0) {
      throw new IllegalArgumentException("ERROR: Parameters can't be null");
    }

    List<WeatherProfile<Double>> profiles = new ArrayList<>();

    // Check for valid state
    if (!Geography.isValidState(state)) {
      throw new IllegalArgumentException("ERROR: Invalid state");
    }

    GeoCode converter = new GeoCode();

    JSONObject latLon = null;
    try {
      latLon = converter.getCityState(city, state);

    } catch (IOException e) {
      e.printStackTrace();
    }
    String lat = Double.toString(latLon.getDouble("lat"));
    String lng = Double.toString(latLon.getDouble("lng"));

    String response = apiRequest.get(String.format("%s,%s", lat, lng));

    JSONObject jsonObject = new JSONObject(response);
    List<WeatherProfile<Double>> fetched;
    fetched = parseSummaries(jsonObject);

    if (fetched.size() > 0) {
      profiles.addAll(fetched);
    }

    return profiles;
  }

  /**
   * Gets forecast for next numDays - capped at 14.
   *
   * @param city
   *          the city for the forecast
   * @param state
   *          the state for the forecast
   * @return a list of weather profiles for the next numDays
   */
  public List<WeatherProfile<Double>> getHourlyForecast(String city,
      String state) {
    if (city == null || state == null || city.length() == 0
        || state.length() == 0) {
      throw new IllegalArgumentException("ERROR: Parameters can't be null");
    }

    List<WeatherProfile<Double>> profiles = new ArrayList<>();

    // Check for valid state
    if (!Geography.isValidState(state)) {
      throw new IllegalArgumentException("ERROR: Invalid state");
    }

    GeoCode converter = new GeoCode();

    JSONObject latLon = null;
    try {
      latLon = converter.getCityState(city, state);
    } catch (IOException e) {
      e.printStackTrace();
    }

    Double latDouble = latLon.getDouble("lat");
    Double lngDouble = latLon.getDouble("lng");

    String lat;
    String lng;

    if (latDouble != null && lngDouble != null) {
      lat = Double.toString(latLon.getDouble("lat"));
      lng = Double.toString(latLon.getDouble("lng"));
    } else {
      lat = "Error";
      lng = "Error";
    }

    String response = apiRequest.get(String.format("%s,%s", lat, lng));

    JSONObject jsonObject = new JSONObject(response);
    List<WeatherProfile<Double>> fetched;
    fetched = parseHour(jsonObject);

    if (fetched.size() > 0) {
      profiles.addAll(fetched);
    }

    return profiles;
  }

  /**
   * Takes in JSONArray of Weather and parses it into weather profiles.
   *
   * @param response
   *          a JSONArray of summaries
   * @return list of weather profiles for each summary
   */
  public List<WeatherProfile<Double>> parseSummaries(JSONObject response) {

    JSONObject daily = response.getJSONObject("daily");
    JSONArray data = daily.getJSONArray("data");

    List<WeatherProfile<Double>> weatherProfiles = new ArrayList<>();
    for (int i = 0; i < data.length(); i++) {
      JSONObject entry = data.getJSONObject(i);
      WeatherProfile<Double> weather = new WeatherProfile<>(
          entry.getLong("time"), response.getDouble("latitude"),
          response.getDouble("longitude"));

      // Append additional info to weather data
      Double min = entry.getDouble("temperatureMin");
      Double max = entry.getDouble("temperatureMax");
      DecimalFormat df = new DecimalFormat("#.##");
      df.setRoundingMode(RoundingMode.CEILING);
      Double avg = (min + max) / 2;
      avg = Double.parseDouble(df.format(avg));
      weather.addTemperatureInfo(min, avg, max);
      weather.addInfo(WeatherData.INCHES_PRECIP,
          entry.getDouble("precipIntensity"));

      weatherProfiles.add(weather);

    }
    return weatherProfiles;
  }

  /**
   * Takes in JSONArray of Weather and parses it into weather profiles.
   *
   * @param response
   *          a JSONArray of summaries
   * @return list of weather profiles for each summary
   */
  public List<WeatherProfile<Double>> parseForecasts(JSONArray response) {

    List<WeatherProfile<Double>> weatherProfiles = new ArrayList<>();
    JSONObject entity = response.getJSONObject(0);
    JSONObject location = entity.getJSONObject("loc");
    JSONArray periods = entity.getJSONArray("periods");

    for (int i = 0; i < periods.length(); i++) {
      JSONObject day = periods.getJSONObject(i);

      WeatherProfile<Double> weather = new WeatherProfile<>(
          day.getLong("timestamp"), location.getDouble("lat"),
          location.getDouble("long"));

      weather.addTemperatureInfo(day.getDouble("minTempF"),
          day.getDouble("avgTempF"), day.getDouble("maxTempF"));

      weather.addInfo(WeatherData.INCHES_PRECIP, day.getDouble("precipIN"));
      weatherProfiles.add(weather);
    }

    return weatherProfiles;
  }

  private List<WeatherProfile<Double>> parseHour(JSONObject response) {
    JSONObject hourly = response.getJSONObject("hourly");
    JSONArray data = hourly.getJSONArray("data");

    List<WeatherProfile<Double>> weatherProfiles = new ArrayList<>();

    for (int i = 0; i < data.length(); i++) {
      JSONObject entry = data.getJSONObject(i);
      WeatherProfile<Double> weather = new WeatherProfile<>(
          entry.getLong("time"), response.getDouble("latitude"),
          response.getDouble("longitude"));

      // Append additional info to weather data
      Double min = entry.getDouble("temperature");
      weather.addTemperatureInfo(min, min, min);
      weather.addInfo(WeatherData.INCHES_PRECIP,
          entry.getDouble("precipIntensity"));

      weatherProfiles.add(weather);

    }
    return weatherProfiles;
  }

}
