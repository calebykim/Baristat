package baristat.Square;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.joda.time.DateTime;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

import baristat.Dates.Dates;
import spark.Request;

/**
 * This class is responsible for creating and updating a table that holds the
 * Square users interacting with Baristat. The table has 3 columns: a merchant
 * ID, a unique ID (which we generate and encrypt based on the merchant ID), and
 * an access token, which will be updated every time the user authenticates.
 *
 * @author adrianturcu
 */
public class SquareUsers {

  private static Connection conn;

  /**
   * This is the constructor for the SquareUsers class.
   */
  public SquareUsers() {
    this.setupDb();
    try {
      this.setupTables();
    } catch (SQLException e) {
      e.printStackTrace();
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
    String urlToDB = "jdbc:sqlite:" + "locUser.sqlite3";
    try {
      conn = DriverManager.getConnection(urlToDB);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    // these two lines tell the database to enforce foreign
    // keys during operations, and should be present
    Statement stat = null;
    try {
      stat = conn.createStatement();
      stat.executeUpdate("PRAGMA foreign_keys = ON;");
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private void setupTables() throws SQLException {

    PreparedStatement creation = conn
        .prepareStatement("CREATE TABLE IF NOT EXISTS locations("
            + "locationID TEXT," + "city TEXT," + "state TEXT,"
            + "dateUpdated TEXT," + "constant REAL," + "temperature REAL,"
            + "precipitation REAL," + "isJan REAL," + "isFeb REAL,"
            + "isMar REAL," + "isApr REAL," + "isMay REAL," + "isJun REAL,"
            + "isJul REAL," + "isAug REAL," + "isSep REAL," + "isOct REAL,"
            + "isNov REAL," + "isMon REAL," + "isTue REAL," + "isWed REAL,"
            + "isThu REAL," + "isFri REAL," + "isSat REAL," + "isHoliday REAL,"
            + "PRIMARY KEY (locationID)," + "UNIQUE (locationID));");
    creation.executeUpdate();
    creation.close();

    creation = conn.prepareStatement(
        "CREATE TABLE IF NOT EXISTS items(" + "id INTEGER NOT NULL,"
            + "locationID TEXT," + "item TEXT," + "dateUpdated TEXT,"
            + "constant REAL," + "temperature REAL," + "precipitation REAL,"
            + "isJan REAL," + "isFeb REAL," + "isMar REAL," + "isApr REAL,"
            + "isMay REAL," + "isJun REAL," + "isJul REAL," + "isAug REAL,"
            + "isSep REAL," + "isOct REAL," + "isNov REAL," + "isMon REAL,"
            + "isTue REAL," + "isWed REAL," + "isThu REAL," + "isFri REAL,"
            + "isSat REAL," + "isHoliday REAL," + "PRIMARY KEY (id),"
            + "FOREIGN KEY (locationID) REFERENCES locations (locationID) "
            + "ON DELETE CASCADE ON UPDATE CASCADE, " + "UNIQUE (id));");
    creation.executeUpdate();
    creation.close();

    creation = conn.prepareStatement("CREATE TABLE IF NOT EXISTS users("
        + "id INTEGER NOT NULL, " + "uniqueID TEXT," + "merchantID TEXT,"
        + "accessToken TEXT," + "locationID " + "TEXT, PRIMARY KEY (id),"
        + "FOREIGN KEY (locationID) REFERENCES locations (locationID) "
        + "ON DELETE CASCADE ON UPDATE CASCADE, UNIQUE (id));");
    creation.executeUpdate();
    creation.close();
  }

  /**
   * This method is responsible for inserting a new user or updating an existing
   * user's information.
   *
   * @param uniqueID
   *          the unique id
   * @param merchantID
   *          the merchant id
   * @param accessToken
   *          the access token
   * @param locId
   *          the locationId
   * @param city
   *          the city
   * @param state
   *          the state
   * @throws SQLException
   *           exception
   */
  public void insertOrReplace(String uniqueID, String merchantID,
      String accessToken, String locId, String city, String state)
      throws SQLException {

    PreparedStatement insert = conn.prepareStatement(
        "INSERT OR IGNORE INTO locations VALUES (?, ?, ?, ?, ?, ?, ?, ?, "
            + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
    insert.setString(1, locId);
    insert.setString(2, city);
    insert.setString(3, state);
    insert.setString(4, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
        .format(DateTime.now().minusMonths(1).toDate()));

    for (int i = 5; i < 26; i++) {
      insert.setDouble(i, 0.0);
    }

    insert.addBatch();
    insert.executeBatch();
    insert.close();

    insert = conn.prepareStatement(
        "INSERT OR IGNORE INTO users VALUES (NULL, ?, ?, ?, ?);");
    insert.setString(1, uniqueID);
    insert.setString(2, merchantID);
    insert.setString(3, accessToken);
    insert.setString(4, locId);

    insert.addBatch();
    insert.executeBatch();
    insert.close();

    PreparedStatement update = conn.prepareStatement(
        "UPDATE users SET accessToken= ? WHERE merchantID= ?");

    update.setString(1, accessToken);
    update.setString(2, merchantID);
    update.addBatch();
    update.executeBatch();
    update.close();
  }

  /**
   * This method adds an item and its model.
   *
   * @throws SQLException
   */

  private void insertItem(String locationId, String item,
      List<Double> betaValues) throws SQLException {
    PreparedStatement insert = conn.prepareStatement(
        "INSERT OR IGNORE INTO items VALUES (NULL, ?, ?, ?, ?, ?, ?, ?, "
            + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
    insert.setString(1, locationId);
    insert.setString(2, item);
    insert.setString(3,
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()));

    for (int i = 4; i < 25; i++) {
      insert.setDouble(i, betaValues.get(i - 4));
    }

    insert.addBatch();
    insert.executeBatch();
    insert.close();
  }

  /**
   * This method replaces the model for an item if it is expired.
   *
   * @throws SQLException
   */

  private void replaceItem(String locationId, String item,
      List<Double> betaValues) throws SQLException {
    PreparedStatement update = conn.prepareStatement(
        "UPDATE items SET dateUpdated=?, constant=?, temperature=?, "
            + "precipitation=?, isJan=?, isFeb=?, isMar=?, isApr=?, "
            + "isMay=?, isJun=?, isJul=?, isAug=?, isSep=?, isOct=?, isNov=?, "
            + "isMon=?, isTue=?, isWed=?, isThu=?, isFri=?, isSat=?, "
            + "isHoliday=? WHERE locationID=? AND item=?;");
    update.setString(1,
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()));

    for (int i = 2; i < 23; i++) {
      update.setDouble(i, betaValues.get(i - 2));
    }

    update.setString(23, locationId);
    update.setString(24, item);

    update.addBatch();
    update.executeBatch();
    update.close();
  }

  /**
   * This method inserts items into the database appropriately.
   *
   * @param locationId
   *          the location id
   * @param item
   *          the item
   * @param betaValues
   *          the beta values for the model
   * @throws SQLException
   *           exception
   */
  public void insertOrReplaceItem(String locationId, String item,
      Iterator<Double> betaValues) throws SQLException {
    List<Double> betaValuesList = new ArrayList<>();

    while (betaValues.hasNext()) {
      betaValuesList.add(betaValues.next());
    }
    insertItem(locationId, item, betaValuesList);
    replaceItem(locationId, item, betaValuesList);
  }

  /**
   * This method associates a location id with a model.
   *
   * @param locId
   *          the locationId
   * @param betaValues
   *          the beta values for the model
   * @throws SQLException
   *           exception
   */

  public void addModelToLocation(String locId, List<Double> betaValues)
      throws SQLException {

    PreparedStatement update = conn.prepareStatement(
        "UPDATE locations SET dateUpdated=?, constant=?, temperature=?, "
            + "precipitation=?, isJan=?, isFeb=?, isMar=?, isApr=?, isMay=?, "
            + "isJun=?, isJul=?, isAug=?, isSep=?, isOct=?, isNov=?, isMon=?, "
            + "isTue=?, isWed=?, isThu=?, isFri=?, isSat=?, isHoliday=? "
            + "WHERE locationID=?");

    update.setString(1,
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()));

    for (int i = 2; i < 23; i++) {
      update.setDouble(i, betaValues.get(i - 2));
    }
    update.setString(23, locId);

    update.addBatch();
    update.executeBatch();
    update.close();

  }

  /**
   * Checks if the uniqueId from the session is a valid session Id of an
   * existent user.
   *
   * @param uniqueId
   *          uniqueId provided from session.
   * @return true if existent else false.
   */
  public boolean isCurrentUser(String uniqueId) {
    boolean exists = false;
    try {
      String query = "SELECT 1 FROM users WHERE uniqueID = ? LIMIT 1";
      PreparedStatement prep = conn.prepareStatement(query);
      prep.setString(1, uniqueId);
      ResultSet rs = prep.executeQuery();
      while (rs.next()) {
        if (rs.getInt(1) == 1) {
          exists = true;
        }
      }
      rs.close();
      prep.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return exists;
  }

  /**
   * Gets the merchant ID given a uniqueID.
   *
   * @param request
   *          request with unique id used to find merchant id
   * @return true if existent else false.
   */
  public String getMerchantId(Request request) {
    String ret = null;
    assert !request.session().attributes().isEmpty();
    String uniqueId = request.session().attribute("uniqueId");

    try {
      String query = "SELECT merchantID FROM users WHERE uniqueID = ? LIMIT 1";
      PreparedStatement prep = conn.prepareStatement(query);
      prep.setString(1, uniqueId);
      ResultSet rs = prep.executeQuery();
      while (rs.next()) {
        ret = rs.getString(1);
      }
      rs.close();
      prep.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    assert ret != null;
    return ret;
  }

  /**
   * This method gets the access token given the unique id from the request.
   *
   * @param request
   *          the request
   * @return the access token
   */
  public String getAccessToken(Request request) {
    String ret = "";
    assert !request.session().attributes().isEmpty();
    String uniqueId = request.session().attribute("uniqueId");
    try {
      String query = "SELECT accessToken FROM users WHERE " + "uniqueID = ?";
      PreparedStatement prep = conn.prepareStatement(query);
      prep.setString(1, uniqueId);
      ResultSet rs = prep.executeQuery();
      while (rs.next()) {
        ret = rs.getString(1);
      }
      rs.close();
      prep.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    assert ret != null;
    return ret;
  }

  /**
   * This method checks if a model is expired by seeing if the last time it was
   * generated was more than a week ago.
   *
   * @param locId
   *          the location Id
   * @return whether or not the model is expired
   * @throws SQLException
   *           exception
   * @throws ParseException
   *           exception
   */
  public boolean modelIsExpired(String locId)
      throws SQLException, ParseException {
    Date dateUpdated = null;
    String query = "SELECT dateUpdated FROM locations WHERE locationID=?";
    PreparedStatement prep = conn.prepareStatement(query);
    prep.setString(1, locId);
    ResultSet rs = prep.executeQuery();
    if (rs.next()) {
      String dateString = rs.getString(1);
      dateUpdated = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
          .parse(dateString);
    } else {
      return true;
    }

    rs.close();
    prep.close();

    if (dateUpdated == null) {
      return true;
    }

    DateTime dtUpdated = new DateTime(dateUpdated);
    DateTime now = DateTime.now();
    if (Dates.getDatesBetween(dtUpdated, now).size() > 7) {
      return true;
    }
    return false;

  }

  /**
   * This method returns the beta values for a model given a location id.
   *
   * @param locationId
   *          the location id
   * @return the list of beta values for the model
   * @throws SQLException
   *           exception
   */
  public List<Double> getModelFromDb(String locationId) throws SQLException {
    List<Double> betaValues = new ArrayList<Double>();

    String query = "SELECT constant, temperature, "
        + "precipitation, isJan, isFeb, isMar, isApr, isMay, "
        + "isJun, isJul, isAug, isSep, isOct, isNov, isMon, "
        + "isTue, isWed, isThu, isFri, isSat, isHoliday "
        + "FROM locations WHERE locationID=?";

    PreparedStatement prep = conn.prepareStatement(query);
    prep.setString(1, locationId);
    ResultSet rs = prep.executeQuery();

    while (rs.next()) {
      for (int i = 1; i < 22; i++) {
        betaValues.add(rs.getDouble(i));
      }
    }

    rs.close();
    prep.close();

    return betaValues;

  }

  /**
   * This method checks if the item models for a location need to be updated.
   *
   * @param locId
   *          the locationId
   * @return whether or not the models need to be updated
   * @throws SQLException
   *           exception
   * @throws ParseException
   *           exception
   */
  public boolean itemModelsNeedUpdate(String locId)
      throws SQLException, ParseException {
    Date dateUpdated = new Date();
    String query = "SELECT dateUpdated FROM items WHERE locationID=? LIMIT 1";
    PreparedStatement prep = conn.prepareStatement(query);
    prep.setString(1, locId);
    ResultSet rs = prep.executeQuery();

    if (rs.next()) {
      String dateString = rs.getString(1);
      dateUpdated = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
          .parse(dateString);
    } else {
      return true;
    }

    rs.close();
    prep.close();

    DateTime dtUpdated = new DateTime(dateUpdated);
    DateTime now = DateTime.now();
    if (Dates.getDatesBetween(dtUpdated, now).size() > 7) {
      return true;
    }
    return false;

  }

  /**
   * This method returns all of the beta values for the item models at a certain
   * location.
   *
   * @param locationId
   *          the location id
   * @return a Multimap of beta values
   * @throws SQLException
   *           exception
   */
  public Multimap<String, Double> getItemModels(String locationId)
      throws SQLException {

    Multimap<String, Double> itemModels = LinkedListMultimap.create();
    String query = "SELECT item, constant, temperature, "
        + "precipitation, isJan, isFeb, isMar, isApr, isMay, "
        + "isJun, isJul, isAug, isSep, isOct, isNov, isMon, "
        + "isTue, isWed, isThu, isFri, isSat, isHoliday "
        + "FROM items WHERE locationID=?";

    PreparedStatement prep = conn.prepareStatement(query);
    prep.setString(1, locationId);
    ResultSet rs = prep.executeQuery();

    while (rs.next()) {
      String itemName = rs.getString(1);
      for (int i = 2; i < 23; i++) {
        itemModels.put(itemName, rs.getDouble(i));
      }
    }

    rs.close();
    prep.close();

    return itemModels;

  }

  /**
   * This method returns the city and the state of a location.
   *
   * @param locationId
   *          the location id
   * @return a string array containing the city and state
   * @throws SQLException
   *           exception
   */
  public String[] getCityState(String locationId) throws SQLException {
    String[] cityState = new String[2];

    String query = "SELECT city, state FROM locations WHERE locationID=?";
    PreparedStatement prep = conn.prepareStatement(query);
    prep.setString(1, locationId);
    ResultSet rs = prep.executeQuery();

    while (rs.next()) {
      cityState[0] = rs.getString(1);
      cityState[1] = rs.getString(2);
    }

    rs.close();
    prep.close();

    return cityState;
  }

}
