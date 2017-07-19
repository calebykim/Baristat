package baristat.Weather;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import baristat.Dates.Dates;
import baristat.Dates.UnitTime;

/**
 * Class for uploading weather API calls into database for future use.
 *
 * @author Tristin Falk
 *
 */
public class WeatherDbUploader {
  private static Connection conn;
  private String errorMessage;
  private WeatherClient weatherClient;

  /**
   * Constructor.
   */
  public WeatherDbUploader() {
    this.errorMessage = null;
    this.setupDb();
    try {
      this.setupTables();
    } catch (SQLException e) {
      this.printError("could not setup tables");
      e.printStackTrace();
    }
    weatherClient = new WeatherClient();
  }

  /**
   * Inserts all weather data between the given dates.
   *
   * @param start
   *          start of range
   * @param end
   *          end of range
   * @param ut
   *          the UnitTime
   */
  public void insertWeather(DateTime start, DateTime end, UnitTime ut) {
    List<WeatherProfile<Double>> profs = weatherClient
        .getWeatherByDate("boston", "ma", start, end, ut);
    List<DateTime> dates = Dates.getDatesBetween(start, end, ut);

    // assert (profs.size() == dates.size());

    // NEED TO ADD BY HOUR, THIS WILL ADD BY DAY

    for (int i = 0; i < profs.size(); i++) {
      try {
        this.insertDate(profs.get(i), dates.get(i));
        System.out.println(dates.get(i).toString());
      } catch (SQLException e) {
        this.printError("could not upload weather dates");
        e.printStackTrace();
      }
    }
  }

  private void setupDb() {
    // this line loads the driver manager class, and must be
    // present for everything else to work properly
    try {
      Class.forName("org.sqlite.JDBC");
    } catch (ClassNotFoundException e) {
      System.out.println("ERROR: Class not found");
    }
    String urlToDB = "jdbc:sqlite:" + "bostonWeather.sqlite3";
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
  }

  private void setupTables() throws SQLException {
    /*
     * Columns: id, date, min_temp, avg_temp, max_temp, precip
     */
    try (PreparedStatement creation = conn.prepareStatement(
        "CREATE TABLE IF NOT EXISTS bostonWeather(id INTEGER NOT NULL, "
            + "date TEXT," + "min_temp REAL, " + "avg_temp REAL, "
            + "max_temp REAL, " + "precip REAL, " + "PRIMARY KEY (id));")) {
      creation.executeUpdate();
    }

  }

  private void insertDate(WeatherProfile<Double> prof, DateTime date)
      throws SQLException {
    try (PreparedStatement insert = conn.prepareStatement(
        "INSERT INTO bostonWeather VALUES (NULL, datetime(?), ?, ?, ?, ?);")) {

      String pattern = "yyyy-MM-dd HH:mm:ss";
      DateTimeFormatter fmt = DateTimeFormat.forPattern(pattern);

      // use prof to fill values
      String dateString = fmt.print(date);
      double minTemp = prof.get(WeatherData.MIN_TEMP);
      double avgTemp = prof.get(WeatherData.AVG_TEMP);
      double maxTemp = prof.get(WeatherData.MAX_TEMP);
      double precip = prof.get(WeatherData.INCHES_PRECIP);

      insert.setString(1, dateString); // date
      insert.setDouble(2, minTemp); // min_temp
      insert.setDouble(3, avgTemp); // avg_temp
      insert.setDouble(4, maxTemp); // max_temp
      insert.setDouble(5, precip); // precip
      insert.addBatch();
      insert.executeBatch();
    }
  }

  private void printError(String error) {
    errorMessage = error;
    System.out.println("ERROR: " + error);
  }

  /**
   * @return latest error message;
   */
  public String getError() {
    return errorMessage;
  }

}
