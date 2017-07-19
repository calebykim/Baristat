package baristat.Holidays;

import org.joda.time.DateTime;

import baristat.Dates.Dates;

/**
 * This is the HolidayProfile class that implements the Holiday interface and is
 * used to keep track of the name, relevance, and date of a Holiday.
 *
 * @author adrianturcu
 *
 */
public class HolidayProfile implements Holiday {

  private String name;
  private double relevance;
  private DateTime date;

  /**
   * Constructs a holidays client to interact with holidaysapi.com.
   *
   * @param name
   *          the name of the holiday
   * @param date
   *          the date of the holiday
   */
  public HolidayProfile(String name, DateTime date) {
    this.name = name;
    this.relevance = 0;
    this.date = date;
  }

  /**
   * Returns name of the holiday.
   *
   * @return the name of the holiday
   */
  @Override
  public String getName() {
    return name;
  }

  /**
   * Returns the relevance of the holiday.
   *
   * @return relevance of the holiday
   */
  @Override
  public double getRelevance() {
    return relevance;
  }

  /**
   * Returns the date of the holiday.
   *
   * @return date of the holiday
   */
  @Override
  public DateTime getDate() {
    return date;
  }

  /**
   * Renders the holiday as a readable string.
   *
   * @return the holiday as a string
   */
  @Override
  public String toString() {
    return String.format("%s: %s", name, Dates.parseDateTime(date));
  }

}
