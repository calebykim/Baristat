package baristat.Dates;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.DurationFieldType;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Class puts constraints on geography based functionality.
 *
 * @author jbreuch
 *
 */
public final class Dates {

  private Dates() {

  }

  /**
   * This method gets the dates between a start and end date.
   *
   * @param start
   *          the start date
   * @param end
   *          the end date
   * @return a list of dates between start and end
   */
  public static List<DateTime> getDatesBetween(DateTime start, DateTime end) {
    // Plus one makes it exclusive
    int days = Days.daysBetween(start, end.plusDays(1)).getDays();
    List<DateTime> dates = new ArrayList<>(days);

    for (int i = 0; i < days; i++) {
      DateTime d = start.withFieldAdded(DurationFieldType.days(), i);
      dates.add(d);
    }

    return dates;
  }

  /**
   * This method gets the dates between a start and end date, taking into
   * consideration the unit of time (days or hours).
   *
   * @param start
   *          the start date
   * @param end
   *          the end date
   * @param ut
   *          the unit time
   * @return a list of dates between start and end
   */
  public static List<DateTime> getDatesBetween(DateTime start, DateTime end,
      UnitTime ut) {

    int interval = Days.daysBetween(start, end.plusDays(1)).getDays();

    if (ut.equals(UnitTime.HOUR)) {
      interval *= 24;
    }

    List<DateTime> dates = new ArrayList<>(interval);

    for (int i = 0; i < interval; i++) {
      DateTime d = null;
      if (ut.equals(UnitTime.DAY)) {
        d = start.withFieldAdded(DurationFieldType.days(), i);
      } else if (ut.equals(UnitTime.HOUR)) {
        d = start.withFieldAdded(DurationFieldType.hours(), i);
      }
      dates.add(d);
    }

    return dates;
  }

  /**
   * This method returns the input DateTime as a String.
   *
   * @param input
   *          the date
   * @return the date as a String
   */
  public static String parseDateTime(DateTime input) {
    String pattern = "MM/dd/yyyy";
    DateTimeFormatter fmt = DateTimeFormat.forPattern(pattern);
    return fmt.print(input);
  }

  /**
   * This method returns the input String as a DateTime.
   *
   * @param input
   *          the date String
   * @return the String as a DateTime
   */
  public static DateTime parseStringToDateTime(String input) {
    String pattern = "MM/dd/yyyy";
    DateTimeFormatter fmt = DateTimeFormat.forPattern(pattern);
    return fmt.parseDateTime(input);
  }

  /**
   * This method returns the weight of a day (1.0 if weekend, 0 if weekday).
   *
   * @param date
   *          the DateTime
   * @return the weight corresponding to the date
   */
  public static double getDayWeight(DateTime date) {
    if (Dates.isWeekend(date)) {
      return 1.0;
    } else {
      return 0.0;
    }
  }

  /**
   * This method returns whether or not the date is a weekend.
   *
   * @param date
   *          the DateTime
   * @return true if a weekend, false if not
   */
  public static boolean isWeekend(DateTime date) {
    String day = date.dayOfWeek().getAsText();
    return day.compareTo("Saturday") == 0 || day.compareTo("Sunday") == 0;
  }

  /**
   * This method returns the DateTime entered rounded to the nearest day.
   *
   * @param dt
   *          the date
   * @return the date rounded down to the nearest day
   */
  public static DateTime roundToNearestDay(DateTime dt) {

    return new DateTime(dt.getYear(), dt.getMonthOfYear(), dt.getDayOfMonth(),
        0, 0);

  }

  /**
   * This method returns the DateTime entered rounded to the nearest hour in the
   * day.
   *
   * @param dt
   *          the date
   * @return the date rounded to the nearest hour in the day
   */
  public static DateTime roundToNearestHourInDay(DateTime dt) {

    DateTime flooredDt = dt.hourOfDay().roundHalfFloorCopy();

    if (flooredDt.getDayOfMonth() != dt.getDayOfMonth()) {
      flooredDt = flooredDt.minusHours(1);
    }

    return flooredDt;
  }

  /**
   * This method returns the DateTime entered rounded to the nearest hour.
   *
   * @param dt
   *          the date
   * @return the date rounded down to the nearest hour
   */
  public static DateTime roundToNearestHour(DateTime dt) {

    return dt.hourOfDay().roundHalfFloorCopy();

  }

  /**
   * This method takes in a DateTime and converts it to a String that is
   * readable by the DarkSky API.
   *
   * @param dt
   *          the date
   * @return the date as a String
   */
  public static String parseDarkSkyDateTime(DateTime dt) {
    String patternDate = "yyyy-MM-dd";
    DateTimeFormatter fmt = DateTimeFormat.forPattern(patternDate);
    String output = fmt.print(dt);

    String patternTime = "HH:mm:ss";
    fmt = DateTimeFormat.forPattern(patternTime);
    output += "T" + fmt.print(dt);
    return output;
  }

  /**
   * Gets date to split by.
   *
   * @param start
   *          the start date
   * @param end
   *          the end date
   * @param isHubway
   *          whether or not we are using hubway data
   * @return the split date between the start and end
   */
  public static DateTime findSplitDate(DateTime start, DateTime end,
      boolean isHubway) {
    assert start.compareTo(end) <= 0;

    DateTime now;
    if (isHubway) {
      // hubway data cuts off at the end of Feb
      now = new DateTime(2017, 2, 28, 0, 0);
    } else {
      now = DateTime.now();
    }

    if (end.compareTo(now) < 0) {
      // entirely in past
      return end;
    } else if (start.compareTo(now) > 0) {
      // entirely in future
      return start;
    }
    // now is the split
    return now;
  }

  /**
   * This method takes in a day as an integer and returns the String day
   * corresponding to it.
   *
   * @param day
   *          the day of the week as an integer
   * @return the day as a String
   */
  public static String integerToDayOfWeek(Integer day) {
    switch (day) {
      case 7:
        return "Sunday";
      case 1:
        return "Monday";
      case 2:
        return "Tuesday";
      case 3:
        return "Wednesday";
      case 4:
        return "Thursday";
      case 5:
        return "Friday";
      case 6:
        return "Saturday";
      default:
        return "ERROR INVALID DATE";
    }
  }

  /**
   * This method increments the DateTime passed in by the indicated unit of
   * time.
   *
   * @param start
   *          the start date
   * @param unitOfTime
   *          the unit of time to increment by
   * @return the increment DateTime
   */
  public static DateTime incrementDate(DateTime start, UnitTime unitOfTime) {
    switch (unitOfTime) {
      case HOUR:
        start = start.plusHours(1);
        break;
      case DAY:
        start = start.plusDays(1);
        break;
      case WEEK:
        start = start.plusWeeks(1);
        break;
      default:
        break;
    }
    return start;
  }
}
