package baristat.Weather;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import baristat.API.APIClient;

/**
 * This the testing suite for the weather client.
 *
 * @author jbreuch
 */
public class APIClientTest {

  /**
   * This tests if the APIClient is constructed properly.
   */
  @Test
  public void testConstruction() {
    assertNotNull(new APIClient(null, null));
  }
}
