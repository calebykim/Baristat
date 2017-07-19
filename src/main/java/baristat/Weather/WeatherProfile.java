package baristat.Weather;

import java.util.HashMap;
import java.util.Map;

/**
 * Outlines the structure of a weather summary.
 *
 * @author jbreuch
 * @param <V>
 *          values for weather data
 */
public class WeatherProfile<V> {

  private long timestamp;
  private double lat;
  private double lng;

  private Map<WeatherData, V> data;

  /**
   * This is the constructor for the WeatherProfile class.
   *
   * @param l
   *          the timestamp
   * @param lat
   *          the latitude
   * @param lng
   *          the longitude
   */
  public WeatherProfile(long l, double lat, double lng) {
    this.timestamp = l;
    this.lat = lat;
    this.lng = lng;
    this.data = new HashMap<>();
  }

  /**
   * Adds details to data map.
   *
   * @param metric
   *          the WeatherData
   * @param value
   *          the value
   * @return the value added
   */
  public V addInfo(WeatherData metric, V value) {
    data.put(metric, value);
    return value;
  }

  /**
   * Checks data map for WeatherData, returns the value if it has been set,
   * otherwise null.
   *
   * @param metric
   *          the data to check for
   * @return returns the value if it has been set, otherwise null.
   */
  public V get(WeatherData metric) {
    return data.get(metric);
  }

  /**
   * Provides the number inches of precipitation if there are any, otherwise
   * null.
   *
   * @return inches of precipitation if there are any, otherwise null.
   */
  public V getPrecipitation() {
    return data.get(WeatherData.INCHES_PRECIP);
  }

  /**
   * Provides the number inches of precipitation if there are any, otherwise
   * null.
   *
   * @return inches of precipitation if there are any, otherwise null.
   */
  public V getAverageFahrenheit() {
    return data.get(WeatherData.AVG_TEMP);
  }

  /**
   * Sets standard temperature information.
   *
   * @param min
   *          minimum temperature
   * @param avg
   *          average temperature
   * @param max
   *          max temperature
   */
  public void addTemperatureInfo(V min, V avg, V max) {
    data.put(WeatherData.AVG_TEMP, avg);
    data.put(WeatherData.MAX_TEMP, max);
    data.put(WeatherData.MIN_TEMP, min);
  }

  @Override
  public String toString() {
    return String.format("timestamp: %d%n" + "coordinates: (%f, %f)", timestamp,
        lat, lng);
  }
}
