package baristat.Hubway;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import baristat.Dates.Dates;
import baristat.Dates.UnitTime;

/**
 * Class to perform queries on database. Has no concept of weather
 *
 * Uses start_date as time sold, and maps start_station IDs to items.
 *
 *
 * @author Tristin Falk
 *
 */
public class HubwayReader {
  private static Connection conn;

  /**
   * Constructor.
   */
  public HubwayReader() {
    this.setupDb();
  }

  /**
   *
   * @param item
   *          item to count
   * @param start
   *          start of time range
   * @param end
   *          end of time range
   * @param unitOfTime
   *          unit of time to split by
   * @return map from DateTime to numSold in that DateTime
   */
  public Multiset<DateTime> getNumSoldInDateRange(HubwayItem item,
      DateTime start, DateTime end, UnitTime unitOfTime) {
    Multiset<DateTime> numSold = HashMultiset.create();

    // increment date to have inclusive
    end = Dates.incrementDate(end, UnitTime.DAY);

    List<DateTime> dates = getDatesForItemSoldInRange(item, start, end);

    for (DateTime dt : dates) {
      switch (unitOfTime) {
        case HOUR:
          numSold.add(Dates.roundToNearestHourInDay(dt));
          break;
        case DAY:
          numSold.add(Dates.roundToNearestDay(dt));
          break;
        default:
          break;
      }
    }

    return numSold;

  }

  private List<DateTime> getDatesForItemSoldInRange(HubwayItem item,
      DateTime start, DateTime end) {
    List<DateTime> dateList = new ArrayList<>();

    String query = "SELECT start_date FROM trips WHERE "
        + "(start_station = ?) " + "AND start_date BETWEEN ? AND ?";

    try (PreparedStatement prep = conn.prepareStatement(query)) {

      prep.setInt(1, item.getStartStnId());
      prep.setString(2, this.dateTimeToSqlDate(start).toString());
      prep.setString(3, this.dateTimeToSqlDate(end).toString());

      try (ResultSet rs = prep.executeQuery()) {
        while (rs.next()) {
          dateList.add(this.sqlDateStringToJodaDate(rs.getString(1)));
        }
      }
    } catch (SQLException e) {
      System.out.println("Error: could not find dates in range");
      e.printStackTrace();
    }

    return dateList;
  }

  /**
   *
   * @return list of hubway items
   */
  public List<HubwayItem> getItems() {
    // num items = 10 currently
    final int[] starts = {17, 31, 46, 61, 76, 91, 116, 131, 146, 161, 176, 191,
        216, 230, 68};
    final int[] ends = {30, 45, 60, 75, 90, 115, 130, 145, 160, 175, 67, 67, 0,
        0, 0};
    final double[] costs = {5.05, 5.1, 4.95, 5.25, 4.75, 5, 5.4, 5, 5, 5, 5, 5,
        5, 5, 5};
    final String[] names = {"Mocha", "Nitro", "Macchiato", "Frappe", "Tea",
        "Water", "Cappucino", "Smoothie", "Latte", "Coffee", "Fudge", "Matcha",
        "Cortado", "McCringleberry", "Flat White"};

    List<HubwayItem> items = new ArrayList<>();

    for (int i = 0; i < starts.length; i++) {
      items.add(new HubwayItem(names[i], starts[i], ends[i], costs[i]));
    }

    return items;
  }

  /**
   * Gets the number of times an item was sold in the date range.
   *
   * @param item
   *          item to search for
   * @param start
   *          start date
   * @param end
   *          end date
   * @return num times sold in range
   */
  public Integer getNumItemInDateRange(HubwayItem item, DateTime start,
      DateTime end) {
    String query = "SELECT COUNT(*) FROM trips WHERE " + "start_station = ? "
        + "AND start_date BETWEEN ? AND ?";

    String startStr = this.dateTimeToSqlDate(start).toString();
    String endStr = this.dateTimeToSqlDate(end).toString();

    try (PreparedStatement prep = conn.prepareStatement(query)) {

      prep.setInt(1, item.getStartStnId());
      prep.setString(2, startStr);
      prep.setString(3, endStr);

      try (ResultSet rs = prep.executeQuery()) {
        while (rs.next()) {
          return rs.getInt(1);
        }
      }
    } catch (SQLException e) {
      System.out.println("Error: could not find dates in range");
    }
    return 0;
  }

  private void setupDb() {
    // this line loads the driver manager class, and must be
    // present for everything else to work properly
    try {
      Class.forName("org.sqlite.JDBC");
    } catch (ClassNotFoundException e) {
      System.out.println("ERROR: Class not found");
    }
    String urlToDB = "jdbc:sqlite:" + "../fullHubway.sqlite3";
    try {
      conn = DriverManager.getConnection(urlToDB);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    // these two lines tell the database to enforce foreign
    // keys during operations, and should be present
    try (Statement stat = conn.createStatement()) {
      stat.executeUpdate("PRAGMA foreign_keys = ON;");
    } catch (SQLException e) {
      e.printStackTrace();
    }
    // this.closeConnection();
  }

  /**
   * Closes DB connection.
   */
  public void close() {
    try {
      conn.close();
    } catch (SQLException e) {
      System.out.println("ERROR: Could not close hubway reader");
    }
  }

  private Date dateTimeToSqlDate(DateTime dt) {
    return new Date(dt.getMillis());
  }

  /**
   *
   * Converts strings like 'yyyy-MM-dd' to 'dd/MM/yyyy'.
   *
   * @param sqlStr
   * @return jodaStr
   */
  private DateTime sqlDateStringToJodaDate(String sqlStr) {
    DateTimeFormatter sqlFormatter = DateTimeFormat
        .forPattern("yyyy-MM-dd HH:mm:ss");
    return sqlFormatter.parseDateTime(sqlStr);
  }
}
