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

import baristat.Dates.Dates;
import baristat.Dates.UnitTime;
import baristat.Weather.WeatherClient;
import baristat.Weather.WeatherData;
import baristat.Weather.WeatherProfile;

/**
 *
 * Class to perform queries on the Boston weather database. We included the
 * weather database of boston so that we did not have to make API calls for
 * testing with Hubway.
 *
 * @author Tristin Falk
 *
 */
public class HubwayWeatherClient extends WeatherClient {
  private static Connection conn;

  /**
   * Constructor.
   */
  public HubwayWeatherClient() {
    this.setupDb();
  }

  /**
   * Gets a list of weather profiles starting at start time, ending at the end
   * time, in increments of ut (UnitTime).
   *
   * @param start
   *          start time
   * @param end
   *          end time
   * @param ut
   *          unit of time to separate by
   * @return list of weather profiles
   */
  @Override
  public List<WeatherProfile<Double>> getWeatherByDate(String city,
      String state, DateTime start, DateTime end, UnitTime ut) {
    List<WeatherProfile<Double>> profiles = new ArrayList<>();
    WeatherProfile<Double> currProfile = null;
    end = Dates.incrementDate(end, UnitTime.DAY);

    String query = "SELECT * FROM bostonWeather WHERE date BETWEEN ? AND ?";

    try (PreparedStatement prep = conn.prepareStatement(query)) {

      prep.setString(1, this.dateTimeToSqlDate(start).toString());
      prep.setString(2, this.dateTimeToSqlDate(end).toString());

      try (ResultSet rs = prep.executeQuery()) {
        while (rs.next()) {
          double minTemp = rs.getDouble(3);
          double avgTemp = rs.getDouble(4);
          double maxTemp = rs.getDouble(5);

          double avgPrecip = rs.getDouble(6);

          final int numHoursInDay = 23;

          // if the caller wants the weather in increments of day,
          // we must sum up the next 23 rows because the db is in
          // increments of hour.
          if (ut.equals(UnitTime.DAY)) {
            for (int i = 0; i < numHoursInDay; i++) {
              if (rs.next()) {
                if (rs.getDouble(3) < minTemp) {
                  minTemp = rs.getDouble(3);
                }
                avgTemp += rs.getDouble(4);
                if (rs.getDouble(5) > maxTemp) {
                  maxTemp = rs.getDouble(5);
                }
                avgPrecip += rs.getDouble(6);
              } else {
                break;
              }
            }
            avgTemp = avgTemp / (numHoursInDay + 1);
            avgPrecip = avgPrecip / (numHoursInDay + 1);
          }

          currProfile = new WeatherProfile<>(0, 0, 0);
          currProfile.addTemperatureInfo(minTemp, avgTemp, maxTemp);
          currProfile.addInfo(WeatherData.INCHES_PRECIP, avgPrecip);
          profiles.add(currProfile);
        }
      }
    } catch (SQLException e) {
      System.out.println("Error: could not find dates in range");
      e.printStackTrace();
    }
    return profiles;
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

  /**
   * Closes connection to db.
   */
  public void closeConnection() {
    try {
      conn.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private Date dateTimeToSqlDate(DateTime dt) {
    return new Date(dt.getMillis());
  }

}
