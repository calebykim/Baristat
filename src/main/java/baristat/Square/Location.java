package baristat.Square;

/**
 * This is the location class that serves as a vehicle for which information
 * about a location is sent to the front end.
 *
 * @author adrianturcu
 *
 */
public class Location {

  private String locationId;
  private String businessName;
  private String city;
  private String state;

  /**
   * This is the constructor for the Location class.
   */
  public Location() {
    locationId = "";
    businessName = "";
    city = "";
    state = "";
  }

  /**
   * This method sets the location id of the location.
   *
   * @param locId
   *          the location id
   */
  public void setLocId(String locId) {
    locationId = locId;
  }

  /**
   * This method sets the name of the business.
   *
   * @param business
   *          the business
   */
  public void setBusinessName(String business) {
    businessName = business;
  }

  /**
   * This method sets the name of the city.
   *
   * @param cit
   *          the city
   */
  public void setCity(String cit) {
    city = cit;
  }

  /**
   * This method sets the name of the state.
   *
   * @param stat
   *          the state
   */
  public void setState(String stat) {
    state = stat;
  }

  /**
   * This method returns the location id.
   *
   * @return the location id
   */
  public String getLocId() {
    return locationId;
  }

  /**
   * This method returns the business name.
   *
   * @return the business
   */
  public String getBusinessName() {
    return businessName;
  }

  /**
   * This method returns the location id.
   *
   * @return the location id
   */
  public String getCity() {
    return city;
  }

  /**
   * This method returns the location id.
   *
   * @return the location id
   */
  public String getState() {
    return state;
  }
}
