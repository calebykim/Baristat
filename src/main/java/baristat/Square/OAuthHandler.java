package baristat.Square;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;

import com.google.common.collect.ImmutableMap;

import spark.ModelAndView;
import spark.QueryParamsMap;
import spark.Request;
import spark.Response;

/**
 * This class is responsible for navigating to the authentication page and
 * getting the merchant's id and access token.
 *
 * @author adrianturcu
 */
public class OAuthHandler {

  // The application ID and secret for Baristat
  private static String appId = "sq0idp-mBvVYFYTnRUUEnXzxF90Xg";

  private static String appSecret = "sq0csp-Lm7Wqd1zOO0A56l3gRPucv"
      + "S71KPIdWyypgEGWd_XzEk";

  // The base URL for every Connect API request
  private static String connectHost = "https://connect.squareup.com";

  // private static Map<String, String> merchantToToken;

  private String accessToken;

  private String locationId;

  private String merchantId;

  private String uniqueId;

  private String business;

  /**
   * This is the constructor for the OAuthHandler class.
   */
  public OAuthHandler() {
    // merchantToToken = new ConcurrentHashMap<String, String>();
    accessToken = "";
    merchantId = "";
    uniqueId = "";
  }

  /**
   * This method helps set up the initial page from where authorization can
   * occur.
   *
   * @return ModelAndView of the page.
   */
  public ModelAndView authorize() {
    Map<String, Object> variables = ImmutableMap.of("title", "Baristat");
    return new ModelAndView(variables, "landing.ftl");
  }

  /**
   * Stores access token and merchant id, proceeds to dashboard.
   *
   * @param request
   *          the request made
   * @param response
   *          the response made
   * @param db
   *          the database of Square users.
   * @return ModelAndView of dashboard.
   * @throws IOException
   *           io exception thrown
   * @throws ClientProtocolException
   *           client protocol exception thrown
   */
  public ModelAndView callback(Request request, Response response,
      SquareUsers db) throws ClientProtocolException, IOException {

    if (checkSessionId(request, db)) {
      Map<String, Object> variables = ImmutableMap.of("title", "Baristat");
      return new ModelAndView(variables, "home.ftl");
    }

    String authorization = verifyAuthorization(request);
    if (authorization == null) {
      Map<String, Object> variables = ImmutableMap.of("title", "Baristat");
      response.redirect("/login");
      return new ModelAndView(variables, "landing.ftl");
    }

    requestAccessToken(authorization);
    requestMerchantData(request, response, db);
    // request.session().attribute("uniqueId", uniqueId);

    Map<String, Object> variables = ImmutableMap.of("title", "Baristat",
        "uniqueId", uniqueId, "business", business);

    return new ModelAndView(variables, "home.ftl");

  }

  private boolean checkSessionId(Request request, SquareUsers db) {
    String sessionUserId = request.session().attribute("uniqueId");
    return sessionUserId != null && db.isCurrentUser(sessionUserId);
  }

  private void requestMerchantData(Request request, Response response,
      SquareUsers db) throws IOException {
    // get location id
    HttpClient locClient = HttpClients.createDefault();
    HttpGet locGet = new HttpGet(connectHost + "/v2/locations");

    locGet.addHeader("Authorization", "Bearer " + accessToken);
    locGet.addHeader("Content-Type", "application/json");
    locGet.addHeader("Accept", "application/json");

    HttpResponse locRes = locClient.execute(locGet);
    HttpEntity locEntity = locRes.getEntity();
    if (locEntity != null) {
      String retSrc = EntityUtils.toString(locEntity);
      JSONObject resJson = new JSONObject(retSrc);
      if (!resJson.has("locations")) {
        response.redirect("/login");
      } else {
        JSONArray locations = resJson.getJSONArray("locations");
        for (int i = 0; i < locations.length(); i++) {
          locationId = locations.getJSONObject(i).getString("id");
          business = locations.getJSONObject(i).getString("name");
          String city = locations.getJSONObject(i).getJSONObject("address")
              .getString("locality");
          String state = locations.getJSONObject(i).getJSONObject("address")
              .getString("administrative_district_level_1");

          try {
            db.insertOrReplace(uniqueId, merchantId, accessToken, locationId,
                city, state);
            request.session().attribute("uniqueId", uniqueId);
            request.session().attribute("location", locationId);
          } catch (SQLException e) {
            e.printStackTrace();
          }
        }
      }

    }

  }

  private String verifyAuthorization(Request request) {
    QueryParamsMap qm = request.queryMap();
    Map<String, String[]> map = qm.toMap();
    String authorizationCode = null;
    for (String key : map.keySet()) {
      if (key.equals("code")) {
        authorizationCode = qm.value(key);
        break;
      }
    }
    return authorizationCode;
  }

  private void requestAccessToken(String authorizationCode) throws IOException {
    HttpClient client = HttpClients.createDefault();
    HttpPost post = new HttpPost(connectHost + "/oauth2/token");

    List<NameValuePair> requestParams = new ArrayList<>(3);
    requestParams.add(new BasicNameValuePair("client_id", appId));
    requestParams.add(new BasicNameValuePair("client_secret", appSecret));
    requestParams.add(new BasicNameValuePair("code", authorizationCode));

    post.setEntity(new UrlEncodedFormEntity(requestParams, "UTF-8"));

    HttpResponse res = client.execute(post);
    HttpEntity entity = res.getEntity();
    if (entity != null) {
      String retSrc = EntityUtils.toString(entity);
      JSONObject obj = new JSONObject(retSrc);
      if (obj.has("access_token")) {
        accessToken = obj.getString("access_token");
        merchantId = obj.getString("merchant_id");
        uniqueId = BCrypt.hashpw(merchantId, BCrypt.gensalt());
      }
    }
  }

  /**
   * This method returns the merchant's access token.
   *
   * @return the access token
   */

  public String getAccessToken() {
    return accessToken;
  }

  /**
   * This method returns the merchant's location id.
   *
   * @return the location id
   */
  public String getLocationId() {
    return locationId;
  }

}
