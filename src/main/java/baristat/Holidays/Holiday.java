package baristat.Holidays;

import org.joda.time.DateTime;

/**
 * This is the Holiday interface that outlines the functionality that all
 * Holidays should have.
 *
 * @author adrianturcu
 *
 */
public interface Holiday {

  /**
   * Returns name of the holiday.
   *
   * @return the name of the holiday
   */
  String getName();

  /**
   * Returns the relevance of the holiday.
   *
   * @return relevance of the holiday
   */
  double getRelevance();

  /**
   * Returns the date of the holiday.
   *
   * @return date of the holiday
   */
  DateTime getDate();

}
