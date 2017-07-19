package baristat.Square;

/**
 * This class is responsible for querying Square for various information, using
 * a merchant's access token and location id.
 *
 * @author adrianturcu
 *
 */
public class SquareReader {

  private static String connectHost = "https://connect.squareup.com";
  private String accessToken;
  private String locationId;

  /**
   * This is the constructor for the SquareReader class.
   *
   * @param accessToken
   *          the access token
   * @param locationId
   *          the location id
   */
  public SquareReader(String accessToken, String locationId) {

    this.accessToken = accessToken;
    this.locationId = locationId;

  }
}
