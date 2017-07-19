package baristat.API;

import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import baristat.Dates.UnitTime;

/**
 * Provides interface for Square Clients and Hubway Clients.
 *
 * @author Tristin Falk
 *
 */
public interface Client {

  /**
   * Get map from dates to total sales.
   *
   * @param ut
   *          unit of time (hour, day, week)
   * @return Map from DateTime to total sales
   */
  Map<DateTime, Double> getTotalSalesInDateRange(UnitTime ut);

  /**
   * Get map from dates to total sales.
   *
   * @param ut
   *          unit of time (hour, day, week) the location id
   * @return Map from DateTime to total sales
   */
  Map<DateTime, Double> predictTotalSalesInDateRange(UnitTime ut);

  /**
   * Get items within date range.
   *
   * @param ut
   *          unit of time (hour, day, week)
   * @return List of ProductProfiles within dates
   */
  List<ProductProfile> getItemsInDateRange(UnitTime ut);

  /**
   * Get items within date range.
   *
   * @param ut
   *          unit of time (hour, day, week) the location id
   * @return List of ProductProfiles within dates
   */
  List<ProductProfile> predictItemsInDateRange(UnitTime ut);

  /**
   * This method gets information about recent trends.
   *
   * @return a JSONObject of trend information
   */
  String getTrendBlurbs();

  /**
   * This method gets information about predicted figures for next week.
   *
   * @return a JSONObject of predicted information for next week
   */
  String getNextWeekBlurbs();

  /**
   * Get temperature in a date range.
   *
   * @param ut
   *          unit of time (hour, day, week) the state
   * @return List of temperatures within dates
   */
  Map<DateTime, Double> getTemperatureInDateRange(UnitTime ut);

  /**
   * Get temperature in a date range.
   *
   * @return Map from days to total sold on each hour of day
   */
  Map<Integer, Map<Integer, Integer>> getNumTransactionsInDateRange();

  /**
   * @return clients version of 'now' - for hubway, feb28.
   */
  DateTime getNow();

  /**
   * @return a list of all items a store sells
   */
  List<ProductProfile> getItems();

  /**
   * Get precipitation in a date range.
   *
   * @param ut
   *          unit of time (hour, day, week)
   * @return List of inches precipitation within dates
   */
  Map<DateTime, Double> getPrecipitationInDateRange(UnitTime ut);

}
