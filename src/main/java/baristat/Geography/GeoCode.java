package baristat.Geography;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by calebykim on 5/3/17.
 */
public class GeoCode {

  /**
   * This method gets the latitude and longitude of the desired city and state
   * as a JSONObject.
   *
   * @param city
   *          the city
   * @param state
   *          the state
   * @return the lat lon coordinates of the city and state as a JSONObject
   * @throws IOException
   *           exception
   */
  public JSONObject getCityState(String city, String state) throws IOException {
    HttpClient client = HttpClients.createDefault();
    city = city.replace(" ", "+");
    String url = "https://maps.googleapis.com/maps/api/geocode/json?address="
        + city + "," + state + "&key=AIzaSyDytR6HPmPgMBGp4eZPUSi0Wq09UojK8o0";
    HttpGet getRequest = new HttpGet(url);
    HttpResponse response = client.execute(getRequest);
    HttpEntity locEntity = response.getEntity();
    if (locEntity != null) {
      String retSrc = EntityUtils.toString(locEntity);
      JSONObject resJson = new JSONObject(retSrc);
      if (resJson.getString("status").equals("OK")) {
        JSONArray results = resJson.getJSONArray("results");
        JSONObject latlng = results.getJSONObject(0).getJSONObject("geometry")
            .getJSONObject("location");
        return latlng;
      }
    }
    return null;

  }
}
