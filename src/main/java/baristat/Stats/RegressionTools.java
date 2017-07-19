package baristat.Stats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import com.google.common.collect.Multiset;

import Jama.Matrix;
import baristat.Dates.Dates;
import baristat.Dates.UnitTime;
import baristat.Holidays.HolidaysClient;

/**
 * This is a utility class used for creating regressions. It gives the caller
 * simple and easy methods to get data for regressions, and it also provides
 * useful conversions that are necessary for the regression.
 *
 * @author Tristin Falk
 *
 */
public final class RegressionTools {

  private RegressionTools() {
    // private constructor for
  }

  /**
   * Computes an isWeekend col within dates.
   *
   * @param startDate
   *          start date
   * @param endDate
   *          end date
   * @return binary list of doubles
   */
  // public static List<Double> datesToWeekendCol(DateTime startDate,
  // DateTime endDate) {
  // List<Double> isWeekendCol = new ArrayList<>();
  //
  // for (DateTime dt : Dates.getDatesBetween(startDate, endDate)) {
  // isWeekendCol.add(Dates.getDayWeight(dt));
  // }
  // return isWeekendCol;
  // }

  /**
   *
   * @param startDate
   *          start date
   * @param endDate
   *          end date
   * @param ut
   *          unit time to separate by
   * @return list of weights for each day
   */
  public static List<Double> datesToWeekendCol(DateTime startDate,
      DateTime endDate, UnitTime ut) {
    List<Double> isWeekendCol = new ArrayList<>();

    for (DateTime dt : Dates.getDatesBetween(startDate, endDate, ut)) {
      isWeekendCol.add(Dates.getDayWeight(dt));
    }
    return isWeekendCol;
  }

  /**
   *
   * @param startDate
   *          start date
   * @param endDate
   *          end date
   * @param ut
   *          unit time
   * @return a list of lists containing 1s and 0s. one list for each season (-1
   *         to prevent multicollinearity)
   */
  public static List<List<Double>> getSeasonsCols(DateTime startDate,
      DateTime endDate, UnitTime ut) {

    List<DateTime> dates = Dates.getDatesBetween(startDate, endDate, ut);

    int size = dates.size();

    List<Double> summer = new ArrayList<>(size);
    List<Double> fall = new ArrayList<>(size);
    List<Double> winter = new ArrayList<>(size);
    List<Double> spring = new ArrayList<>(size);

    for (int i = 0; i < dates.size(); i++) {
      summer.add(0.0);
      fall.add(0.0);
      winter.add(0.0);
      spring.add(0.0);
    }

    for (int i = 0; i < dates.size(); i++) {
      String month = dates.get(i).monthOfYear().getAsText();
      switch (month) {
        case "June":
        case "July":
        case "August":
          summer.set(i, 1.0);
          break;
        case "September":
        case "October":
        case "November":
          fall.set(i, 1.0);
          break;
        case "December":
        case "January":
        case "February":
          winter.set(i, 1.0);
          break;
        case "March":
        case "April":
        case "May":
          spring.set(i, 1.0);
          break;
        default:
          System.out.println("Error: unknown month");
          break;
      }
    }

    List<List<Double>> lists = new ArrayList<>();
    lists.add(summer);
    lists.add(fall);
    lists.add(winter);
    lists.add(spring);

    return lists;
  }

  /**
   *
   * @param startDate
   *          start date
   * @param endDate
   *          end date
   * @param ut
   *          unit time
   * @return a list of lists containing 1s and 0s. one list for each month (-1
   *         to prevent multicollinearity)
   */
  public static List<List<Double>> getMonthCols(DateTime startDate,
      DateTime endDate, UnitTime ut) {

    List<DateTime> dates = Dates.getDatesBetween(startDate, endDate, ut);

    final int numMonths = 12;

    int size = dates.size();

    List<List<Double>> lists = new ArrayList<>();

    for (int i = 0; i < numMonths; i++) {
      lists.add(new ArrayList<>(size));
      for (int j = 0; j < dates.size(); j++) {
        lists.get(i).add(0.0);
      }
    }

    for (int i = 0; i < dates.size(); i++) {
      int month = dates.get(i).getMonthOfYear() - 1;

      lists.get(month).set(i, 1.0);
    }

    // remove december to prevent multicollinearity
    lists.remove(numMonths - 1);
    return lists;
  }

  /**
   *
   * @param startDate
   *          start date
   * @param endDate
   *          end date
   * @param ut
   *          unit time
   * @return a list of lists containing 1s and 0s. one list for each day (-1 to
   *         prevent multicollinearity)
   */
  public static List<List<Double>> getDayCols(DateTime startDate,
      DateTime endDate, UnitTime ut) {

    List<DateTime> dates = Dates.getDatesBetween(startDate, endDate, ut);

    int size = dates.size();

    final int numDays = 7;

    List<List<Double>> lists = new ArrayList<>();

    for (int i = 0; i < numDays; i++) {
      lists.add(new ArrayList<>(size));
      for (int j = 0; j < dates.size(); j++) {
        lists.get(i).add(0.0);
      }
    }

    for (int i = 0; i < dates.size(); i++) {
      int day = dates.get(i).getDayOfWeek() - 1;

      lists.get(day).set(i, 1.0);
    }

    // remove to prevent multicollinearity
    lists.remove(numDays - 1);
    return lists;
  }

  /**
   *
   * @param startDate
   *          start date
   * @param endDate
   *          end date
   * @param ut
   *          unit time
   * @return a list of lists containing 1s and 0s. one list for each hour (-1 to
   *         prevent multicollinearity)
   */
  public static List<List<Double>> getHourCols(DateTime startDate,
      DateTime endDate, UnitTime ut) {

    List<DateTime> dates = Dates.getDatesBetween(startDate, endDate, ut);

    int size = dates.size();

    final int numHours = 24;

    List<List<Double>> lists = new ArrayList<>();

    for (int i = 0; i < numHours; i++) {
      lists.add(new ArrayList<>(size));
      for (int j = 0; j < dates.size(); j++) {
        lists.get(i).add(0.0);
      }
    }

    for (int i = 0; i < dates.size(); i++) {
      int hour = dates.get(i).getHourOfDay();

      lists.get(hour).set(i, 1.0);
    }

    // remove last hour to prevent multicollinearity
    lists.remove(numHours - 1);
    return lists;
  }

  /**
   *
   * @param startDate
   *          start date
   * @param endDate
   *          end date
   * @param ut
   *          unit time
   * @return a list containing 1s and 0s (-1 to prevent multicollinearity)
   */
  public static List<Double> getHolidayCol(DateTime startDate, DateTime endDate,
      UnitTime ut) {
    List<Double> isHolidayCol = new ArrayList<>();

    HolidaysClient client = new HolidaysClient();
    for (DateTime dt : Dates.getDatesBetween(startDate, endDate, ut)) {
      if (client.getHolidayByDate(dt) != null) {
        isHolidayCol.add(1.0);
      } else {
        isHolidayCol.add(0.0);
      }
    }
    return isHolidayCol;
  }

  /**
   *
   * @param cols
   *          List of List of doubles (2D Matrix)
   * @return Converts Lists of Lists to a matrix
   */
  public static Matrix colsToMatrix(List<List<Double>> cols) {
    if (cols.size() == 1) {
      return new Matrix(listToArray(cols.get(0)), cols.get(0).size());
    }
    double[][] colArrays = new double[cols.get(0).size()][cols.size()];
    for (int i = 0; i < cols.get(0).size(); i++) {
      double[] row = new double[cols.size()];
      for (int j = 0; j < cols.size(); j++) {
        row[j] = cols.get(j).get(i);
      }
      colArrays[i] = row;
    }

    if (cols.size() == 1) {
      return new Matrix(colArrays[0], colArrays[0].length);
    }
    return new Matrix(colArrays);
  }

  // helper for colsToMatrix
  private static double[] listToArray(List<Double> list) {
    double[] result = new double[list.size()];
    for (int i = 0; i < list.size(); i++) {
      result[i] = (double) list.get(i);
    }
    return result;
  }

  /**
   * Converts an array into a list.
   *
   * @param array
   *          to convert
   * @return list
   */
  public static List<Double> arrayToList(double[] array) {
    List<Double> result = new ArrayList<>();
    for (int i = 0; i < array.length; i++) {
      result.add(array[i]);
    }
    return result;
  }

  /**
   * Gets a col of 1s of size size.
   *
   * Used for the constant row of X-Matrix.
   *
   * @param size
   *          size of col
   * @return list of 1s
   */
  public static List<Double> getOnesCol(int size) {
    List<Double> result = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      result.add(1.0);
    }
    return result;
  }

  /**
   * Takes in a Map of DateTimes and converts it into a list that is sorted in
   * ascending order.
   *
   * @param map
   *          map to sort
   * @return list
   */
  public static List<Double> mapToSortedCol(Map<DateTime, Double> map) {
    List<DateTime> keys = new ArrayList<>(map.keySet());
    List<Double> col = new ArrayList<>();
    Collections.sort(keys);
    for (DateTime key : keys) {
      col.add(map.get(key));
    }
    return col;
  }

  /**
   * Converts multiset to sortted list.
   *
   *
   * @param start
   *          start date
   * @param end
   *          end date
   * @param ut
   *          unitTime
   * @param set
   *          multiset to sort
   * @return list ascending by dateime of double
   */
  public static List<Double> multisetToSortedCol(DateTime start, DateTime end,
      UnitTime ut, Multiset<DateTime> set) {

    List<DateTime> dates = Dates.getDatesBetween(start, end, ut);
    List<Double> col = new ArrayList<>();

    // assert (dates.size() == set.elementSet().size());

    for (DateTime dt : dates) {
      col.add((double) set.count(dt));
    }

    return col;
  }

  /**
   * Returns the first and third quartile of a set of numbers.
   *
   * @param nums
   *          list of nums
   * @return list of size 2 containing the quartiles.
   */
  public static List<Integer> getQuartiles(List<Integer> nums) {
    List<Integer> quarts = new ArrayList<>();
    List<Integer> copy = new ArrayList<>();
    for (int i = 0; i < nums.size(); i++) {
      copy.add(0);
    }
    Collections.copy(copy, nums);
    Collections.sort(copy);

    int firstQuart = (int) Math.round((copy.size() * 0.3));

    int thirdQuart = (int) Math.round((copy.size() * 0.7));

    quarts.add(copy.get(firstQuart));
    quarts.add(copy.get(thirdQuart));
    return quarts;
  }
}
