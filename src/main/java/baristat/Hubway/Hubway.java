package baristat.Hubway;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import baristat.DataReaders.CsvReader;

/**
 *
 * Class to deal with the creation and management of the Hubway database.
 *
 * @author Tristin Falk
 *
 */
public class Hubway {
  private static Connection conn;
  private List<String> expectedHeaders;
  private String errorMessage;

  /**
   * Constructor.
   */
  public Hubway() {
    this.errorMessage = null;
    this.setupDb();
  }

  /**
   * Inserts Hubway csv into database.
   *
   * @param pathToFile
   *          path to .csv file
   */
  public void insertCsv(String pathToFile) {
    CsvReader csvReader = null;
    try {
      csvReader = new CsvReader(pathToFile);
    } catch (IOException e) {
      this.printError("could not open csv");
      return;
    }

    if (!checkHeaders(csvReader.readHeaders())) {
      printError("csv is malformed");
      return;
    }

    try {
      this.setupTables();
    } catch (SQLException ex) {
      printError("Unable to create tables");
      return;
    }

    // csvReader will auto-close when null is reached
    List<String> currRow;
    while ((currRow = csvReader.readLine()) != null) {
      try {
        System.out.println(currRow.get(Column.STARTTIME.ordinal()));
        this.insertStations(currRow);
        this.insertTrips(currRow);
      } catch (SQLException ex) {
        ex.printStackTrace();
      }
    }

  }

  private boolean checkHeaders(List<String> headers) {
    getExpectedHeaders();
    if (headers.size() != expectedHeaders.size()) {
      System.out
          .println("ERROR: Size mismatch in spreadsheet and expected headers");
      return false;
    }

    for (int i = 0; i < headers.size(); i++) {
      if (!headers.get(i).equals(expectedHeaders.get(i))) {
        printError("column name mismatch - expected: " + expectedHeaders.get(i)
            + " was: " + headers.get(i));
        return false; // header mismatch
      }
    }
    return true;
  }

  private List<String> getExpectedHeaders() {
    if (expectedHeaders == null) {
      this.setupExpectedHeaders();
    }
    return this.expectedHeaders;
  }

  // These are the expected headers for a hubway .csv
  private void setupExpectedHeaders() {
    expectedHeaders = new ArrayList<>();
    expectedHeaders.add("\"tripduration\"");
    expectedHeaders.add("\"starttime\"");
    expectedHeaders.add("\"stoptime\"");
    expectedHeaders.add("\"start station id\"");
    expectedHeaders.add("\"start station name\"");
    expectedHeaders.add("\"start station latitude\"");
    expectedHeaders.add("\"start station longitude\"");
    expectedHeaders.add("\"end station id\"");
    expectedHeaders.add("\"end station name\"");
    expectedHeaders.add("\"end station latitude\"");
    expectedHeaders.add("\"end station longitude\"");
    expectedHeaders.add("\"bikeid\"");
    expectedHeaders.add("\"usertype\"");
    expectedHeaders.add("\"birth year\"");
    expectedHeaders.add("\"gender\"");
  }

  private void setupDb() {
    // this line loads the driver manager class, and must be
    // present for everything else to work properly
    try {
      Class.forName("org.sqlite.JDBC");
    } catch (ClassNotFoundException e) {
      System.out.println("ERROR: Class not found");
    }
    /* fullHubway changed to hubway to prevent accidents" */
    String urlToDB = "jdbc:sqlite:" + "hubway.sqlite3";
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
    try (PreparedStatement createStations = conn
        .prepareStatement("CREATE TABLE IF NOT EXISTS stations("
            + "id INTEGER NOT NULL," + "name TEXT," + "lat REAL," + "lng REAL,"
            + "PRIMARY KEY (id)," + "UNIQUE(id));")) {
      createStations.executeUpdate();
    }
    /*
     * Columns: id, duration, start_date, end_date, start_station, end_station
     */
    try (PreparedStatement createTrips = conn.prepareStatement(
        "CREATE TABLE IF NOT EXISTS trips(" + "id INTEGER NOT NULL,"
            + "duration INTEGER, "/* CHECK(duration >= 0)," */
            + "start_date TEXT," + "end_date TEXT, " + "start_station INTEGER,"
            + "end_station INTEGER," + "PRIMARY KEY (id),"
            + "FOREIGN KEY (start_station) REFERENCES stations(id) "
            + "ON DELETE CASCADE ON UPDATE CASCADE,"
            + "FOREIGN KEY (end_station) REFERENCES stations(id) "
            + "ON DELETE CASCADE ON UPDATE CASCADE);")) {
      createTrips.executeUpdate();
    }

  }

  // Note: consider inputing a map instead of a list
  private void insertTrips(List<String> row) throws SQLException {
    try (PreparedStatement insert = conn
        .prepareStatement("INSERT INTO trips VALUES (NULL, ?, datetime(?), "
            + "datetime(?), ?, ?);")) {

      String dur = row.get(Column.TRIP_DURATION.ordinal()).replace("\"", "");
      String startDate = row.get(Column.STARTTIME.ordinal()).replace("\"", "")
          .replace("/", "-");
      String endDate = row.get(Column.STOPTIME.ordinal()).replace("\"", "")
          .replace("/", "-");
      String startStn = row.get(Column.START_STATION_ID.ordinal()).replace("\"",
          "");
      String endStn = row.get(Column.END_STATION_ID.ordinal()).replace("\"",
          "");

      insert.setInt(1, Integer.parseInt(dur)); // duration
      insert.setString(2, startDate); // start_date
      insert.setString(3, endDate); // end_date
      insert.setInt(4, Integer.parseInt(startStn)); // start_station
      insert.setInt(5, Integer.parseInt(endStn)); // end_station
      insert.addBatch();
      insert.executeBatch();
    }
  }

  private void insertStations(List<String> row) throws SQLException {
    try (PreparedStatement insert = conn.prepareStatement(
        "INSERT OR IGNORE INTO stations VALUES (?, ?, ?, ?);")) {

      String idCol = row.get(Column.START_STATION_ID.ordinal()).replace("\"",
          "");
      String nameCol = row.get(Column.START_STATION_NAME.ordinal())
          .replace("\"", "");
      String latCol = row.get(Column.START_STATION_LATITUDE.ordinal())
          .replace("\"", "");
      String lngCol = row.get(Column.START_STATION_LONGITUDE.ordinal())
          .replace("\"", "");

      insert.setInt(1, Integer.parseInt(idCol)); // id
      insert.setString(2, nameCol); // name
      insert.setDouble(3, Double.parseDouble(latCol));
      insert.setDouble(4, Double.parseDouble(lngCol));
      insert.addBatch();
      insert.executeBatch();

      idCol = row.get(Column.END_STATION_ID.ordinal()).replace("\"", "");
      nameCol = row.get(Column.END_STATION_NAME.ordinal()).replace("\"", "");
      latCol = row.get(Column.END_STATION_LATITUDE.ordinal()).replace("\"", "");
      lngCol = row.get(Column.END_STATION_LONGITUDE.ordinal()).replace("\"",
          "");

      insert.setInt(1, Integer.parseInt(idCol)); // id
      insert.setString(2, nameCol); // name
      insert.setDouble(3, Double.parseDouble(latCol));
      insert.setDouble(4, Double.parseDouble(lngCol));
      insert.addBatch();
      insert.executeBatch();
    }

  }

  private void printError(String error) {
    errorMessage = error;
    System.out.println("ERROR: " + error);
  }

  /**
   * Closes DB connection.
   */
  public void close() {
    try {
      conn.close();
    } catch (SQLException s) {
      System.out.println("ERROR: Could not close hubway db connection");
    }
  }

  /**
   * @return latest error message;
   */
  public String getError() {
    return errorMessage;
  }

}
