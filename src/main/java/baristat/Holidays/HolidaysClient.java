package baristat.Holidays;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;

import baristat.API.APIClient;

/**
 * This is the HolidaysClient class that is responsible for making calls to
 * holidaysapi.com to get information about holidays.
 *
 * @author adrianturcu
 *
 */
public class HolidaysClient {
  private APIClient apiRequest;
  private static final String KEY = "key=37f15270-26d0-4ae9-b9cf-d15bf7bebb76";

  /**
   * Constructs a holidays client to interact with holidaysapi.com.
   */
  public HolidaysClient() {
    this.apiRequest = new APIClient("https://holidayapi.com/v1/holidays?", KEY);
  }

  /**
   * This method gets the holiday on a certain date.
   *
   * @param date
   *          the date
   * @return the holiday on the date.
   */
  public Holiday getHolidayByDate(DateTime date) {
    if (date == null) {
      throw new IllegalArgumentException("ERROR: Parameters can't be null");
    }

    String response = apiRequest
        .get(String.format("country=US&month=%d&day=%d&year=%d",
            date.getMonthOfYear(), date.getDayOfMonth(), date.getYear()));

    JSONObject jsonObject = new JSONObject(response);
    if (jsonObject.getInt("status") != 200) {
      return null;
    }
    JSONArray parsedResponse = jsonObject.getJSONArray("holidays");
    if (parsedResponse.length() > 0) {
      JSONObject found = parsedResponse.getJSONObject(0);
      Holiday foundHoliday = new HolidayProfile(found.getString("name"), date);
      return foundHoliday;
    }
    return null;
  }

}
