package baristat.API;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import freemarker.core.ParseException;

/**
 * This is the Main class.
 *
 * @author aturcu
 */
public class APIClient {

  private final String url;
  private final String key;

  /**
   * Constructs an APIClient.
   *
   * @param url
   *          the api domain to call
   * @param key
   *          must take in the full path (ie: apikey=XXXX not just XXX)
   */
  public APIClient(String url, String key) {
    this.url = url;
    this.key = key;
  }

  /**
   * Gets a response from an API endpoint.
   *
   * @param endpoint
   *          the endpoint
   * @return the response
   */
  public String get(String endpoint) {
    try {
      URL newURL = new URL(
          String.format("%s%s&%s", this.url, endpoint, this.key));
      if (key.length() == 0) {
        newURL = new URL(String.format("%s%s", this.url, endpoint));
      }

      try (BufferedReader reader = new BufferedReader(
          new InputStreamReader(newURL.openStream(), "UTF-8"))) {
        final StringBuffer json = new StringBuffer();
        String line;

        while ((line = reader.readLine()) != null) {
          json.append(String.format("%s%n", line));
        }
        reader.close();
        return json.toString();
      }
    } catch (final ParseException e) {
      System.out.println(e);
    } catch (final IOException e) {
      System.out.println(e);
    }
    return null;
  }
}
