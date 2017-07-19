package baristat.Geography;

import java.io.IOException;

import org.junit.Test;

/**
 * Created by calebykim on 5/3/17.
 */
public class GeoCodeTest {
  /**
   * This tests if the GeoCode class gets the correct city and state latitude
   * and longitude.
   *
   * @throws IOException
   *           exception
   */
  @Test
  public void testGeo() throws IOException {
    GeoCode geo = new GeoCode();
    geo.getCityState("los angeles", "CA");
    geo.getCityState("los Angeles", "CA");
    geo.getCityState("los+Angeles", "CA");

  }

}
