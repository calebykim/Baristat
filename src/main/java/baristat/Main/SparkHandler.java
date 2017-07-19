package baristat.Main;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;

import baristat.API.Client;
import baristat.API.ProductProfile;
import baristat.Dates.Dates;
import baristat.Dates.UnitTime;
import baristat.Hubway.HubwayClient;
import baristat.Square.Location;
import baristat.Square.OAuthHandler;
import baristat.Square.SquareClient;
import baristat.Square.SquareUsers;
import freemarker.template.Configuration;
import spark.ExceptionHandler;
import spark.ModelAndView;
import spark.QueryParamsMap;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;
import spark.TemplateViewRoute;
import spark.template.freemarker.FreeMarkerEngine;

/**
 * This class is responsible for communicating with the Spark server.
 *
 * @author adrianturcu
 */
public class SparkHandler {

  private static OAuthHandler oAuth;
  private static SquareUsers userDB;
  private static final Gson GSON = new Gson();

  /**
   * This is the constructor for the SparkHandler class.
   */
  public SparkHandler() {
    userDB = new SquareUsers();
    oAuth = new OAuthHandler();
  }

  /**
   * This method runs the spark server.
   *
   * @param port
   *          the port number
   */
  public void runServer(int port) {

    Spark.port(port);
    Spark.externalStaticFileLocation("src/main/resources/static");
    Spark.exception(Exception.class, new ExceptionPrinter());

    FreeMarkerEngine freeMarker = createEngine();

    // Setup Spark Routes
    Spark.get("/login", new AuthorizeHandler(), freeMarker);
    Spark.get("/callback", new CallbackHandler(), freeMarker);
    Spark.post("/sales", new TotalSalesHandler());
    Spark.post("/temperature", new TempHandler());
    Spark.post("/precipitation", new PrecipHandler());
    Spark.post("/location", new LocationHandler());
    Spark.post("/trends", new TrendHandler());
    Spark.post("/overview", new OverviewHandler());
    Spark.post("/change", new ChangeLocationHandler());
    Spark.post("/items", new ItemHandler());
    Spark.post("/logout", new LogoutHandler());
  }

  private static FreeMarkerEngine createEngine() {
    Configuration config = new Configuration();
    File templates = new File("src/main/resources/spark/template/freemarker");
    try {
      config.setDirectoryForTemplateLoading(templates);
    } catch (IOException ioe) {
      System.out.printf("ERROR: Unable use %s for template loading.%n",
          templates);
      System.exit(1);
    }
    return new FreeMarkerEngine(config);
  }

  /**
   * This sets up the login page that routes to the Square authentication page.
   *
   * @author adrianturcu
   */
  private static class AuthorizeHandler implements TemplateViewRoute {

    @Override
    public ModelAndView handle(Request req, Response res) throws Exception {

      return oAuth.authorize();
    }

  }

  /**
   * This handler logs the user out.
   */
  private class LogoutHandler implements Route {
    @Override
    public String handle(Request req, Response res) throws Exception {
      req.session().removeAttribute("uniqueId");
      req.session().removeAttribute("location");
      req.session().invalidate();

      return "";
    }
  }

  /**
   * This sets up the login page that routes to the Square authentication page.
   *
   * @author adrianturcu
   */
  private static class CallbackHandler implements TemplateViewRoute {

    @Override
    public ModelAndView handle(Request req, Response res) throws Exception {

      return oAuth.callback(req, res, userDB);
    }

  }

  /**
   * This class is responsible for changing the current location id stored in
   * the session. This occurs when the user selects another location from the
   * settings list.
   *
   * @author adrianturcu
   *
   */
  private static class ChangeLocationHandler implements Route {
    @Override
    public String handle(Request req, Response res) throws Exception {

      QueryParamsMap qm = req.queryMap();

      String locationId = qm.value("loc");

      req.session().attribute("location", locationId);

      return "success";
    }
  }

  /**
   * This class is responsible for sending a list of locations, as well as the
   * current location, to the front end.
   *
   * @author adrianturcu
   *
   */
  private static class LocationHandler implements Route {
    @Override
    public String handle(Request req, Response res) throws Exception {

      List<Location> locList = new ArrayList<>();
      HttpClient locClient = HttpClients.createDefault();
      HttpGet locGet = new HttpGet("https://connect.squareup.com/v2/locations");

      String accessToken = userDB.getAccessToken(req);
      locGet.addHeader("Authorization", "Bearer " + accessToken);
      locGet.addHeader("Content-Type", "application/json");
      locGet.addHeader("Accept", "application/json");

      HttpResponse locRes = locClient.execute(locGet);
      HttpEntity locEntity = locRes.getEntity();
      JSONObject resJson = null;
      if (locEntity != null) {
        String retSrc = EntityUtils.toString(locEntity);
        resJson = new JSONObject(retSrc);
        JSONArray locations = resJson.getJSONArray("locations");
        for (int i = 0; i < locations.length(); i++) {

          Location currLoc = new Location();
          currLoc.setLocId(locations.getJSONObject(i).getString("id"));
          currLoc.setBusinessName(locations.getJSONObject(i).getString("name"));
          currLoc.setCity(locations.getJSONObject(i).getJSONObject("address")
              .getString("locality"));
          currLoc.setState(locations.getJSONObject(i).getJSONObject("address")
              .getString("administrative_district_level_1"));

          locList.add(currLoc);
        }
      }

      Map<String, Object> variables = ImmutableMap.of("currLoc",
          req.session().attribute("location"), "locations", locList);

      return GSON.toJson(variables);

    }
  }

  /**
   * This method uses DataHandler to make calls to Square to pull information
   * about total sales in a given date range.
   *
   * @author jbreuch
   */
  private static class TotalSalesHandler implements Route {

    @Override
    public String handle(Request req, Response res) {

      QueryParamsMap qm = req.queryMap();
      String startDateString = qm.value("start");
      String endDateString = qm.value("end");
      String unitString = qm.value("unit");

      DateTime start = Dates.parseStringToDateTime(startDateString);
      DateTime end = Dates.parseStringToDateTime(endDateString);
      DateTime middle = null;

      UnitTime unit = getUnitTime(unitString);

      String currMerchant = userDB.getMerchantId(req);

      Client client = null;

      String accessToken = userDB.getAccessToken(req);
      String locationId = req.session().attribute("location");

      if (currMerchant.compareTo("7M8GW6D1APRND") == 0) {
        middle = Dates.findSplitDate(start, end, true);
        client = new HubwayClient(start, middle, end, userDB, locationId);
      } else {
        middle = Dates.findSplitDate(start, end, false);
        client = new SquareClient(accessToken, locationId, start, middle, end,
            userDB);
      }

      Map<DateTime, Double> pastTotalSalesByUnitTime = client
          .getTotalSalesInDateRange(unit);

      Map<DateTime, Double> predictedTotalSalesByUnitTime = client
          .predictTotalSalesInDateRange(unit);

      Map<String, Object> variables = ImmutableMap.of("past_sales",
          pastTotalSalesByUnitTime, "predicted_sales",
          predictedTotalSalesByUnitTime, "error", "");
      return GSON.toJson(variables);

    }
  }

  /**
   * This method is responsible for sending information to the front end about
   * temperature values in a specified date range.
   */

  private static class TempHandler implements Route {

    @Override
    public String handle(Request req, Response res) throws Exception {
      QueryParamsMap qm = req.queryMap();
      String startDateString = qm.value("start");
      String endDateString = qm.value("end");
      String unitString = qm.value("unit");

      DateTime start = Dates.parseStringToDateTime(startDateString);
      DateTime end = Dates.parseStringToDateTime(endDateString);
      DateTime middle = null;

      UnitTime unit = getUnitTime(unitString);

      String currMerchant = userDB.getMerchantId(req);

      Client client = null;

      String accessToken = userDB.getAccessToken(req);
      String locationId = req.session().attribute("location");

      Map<DateTime, Double> tempByUnitTime = null;

      if (currMerchant.compareTo("7M8GW6D1APRND") == 0) {
        middle = Dates.findSplitDate(start, end, true);
        client = new HubwayClient(start, middle, end, userDB, locationId);
      } else {
        middle = Dates.findSplitDate(start, end, false);
        client = new SquareClient(accessToken, locationId, start, middle, end,
            userDB);
      }

      tempByUnitTime = client.getTemperatureInDateRange(unit);

      Map<String, Object> variables = ImmutableMap.of("temperature",
          tempByUnitTime, "error", "");

      return GSON.toJson(variables);
    }

  }

  /**
   * This method is responsible for sending information to the front end about
   * precipitation values in a specified date range.
   */

  private static class PrecipHandler implements Route {

    @Override
    public String handle(Request req, Response res) throws Exception {
      QueryParamsMap qm = req.queryMap();
      String startDateString = qm.value("start");
      String endDateString = qm.value("end");
      String unitString = qm.value("unit");

      DateTime start = Dates.parseStringToDateTime(startDateString);
      DateTime end = Dates.parseStringToDateTime(endDateString);
      DateTime middle = null;

      UnitTime unit = getUnitTime(unitString);

      String currMerchant = userDB.getMerchantId(req);

      Client client = null;

      String accessToken = userDB.getAccessToken(req);
      String locationId = req.session().attribute("location");

      Map<DateTime, Double> precipByUnitTime = null;

      if (currMerchant.compareTo("7M8GW6D1APRND") == 0) {
        middle = Dates.findSplitDate(start, end, true);
        client = new HubwayClient(start, middle, end, userDB, locationId);
      } else {
        middle = Dates.findSplitDate(start, end, false);
        client = new SquareClient(accessToken, locationId, start, middle, end,
            userDB);
      }

      precipByUnitTime = client.getPrecipitationInDateRange(unit);

      Map<String, Object> variables = ImmutableMap.of("precipitation",
          precipByUnitTime, "error", "");

      return GSON.toJson(variables);
    }

  }

  /**
   * This is the Spark handler for stats for a specific item .
   *
   * @author jbreuch
   */
  private static class ItemHandler implements Route {

    @Override
    public String handle(Request req, Response res) throws Exception {

      QueryParamsMap qm = req.queryMap();
      String startDateString = qm.value("start");
      String endDateString = qm.value("end");
      String unitString = qm.value("unit");

      DateTime start = Dates.parseStringToDateTime(startDateString);
      DateTime end = Dates.parseStringToDateTime(endDateString);
      DateTime middle = null;

      UnitTime unit = getUnitTime(unitString);

      String currMerchant = userDB.getMerchantId(req);

      Client client = null;

      String accessToken = userDB.getAccessToken(req);
      String locationId = req.session().attribute("location");

      if (currMerchant.compareTo("7M8GW6D1APRND") == 0) {
        middle = Dates.findSplitDate(start, end, true);
        client = new HubwayClient(start, middle, end, userDB, locationId);
      } else {
        middle = Dates.findSplitDate(start, end, false);
        client = new SquareClient(accessToken, locationId, start, middle, end,
            userDB);
      }

      List<ProductProfile> pastItems = client.getItemsInDateRange(unit);

      List<ProductProfile> futureItems = client.predictItemsInDateRange(unit);

      Map<String, Object> variables = ImmutableMap.of("pastItems", pastItems,
          "futureItems", futureItems, "error", "");
      return GSON.toJson(variables);

    }

  }

  /**
   * This class is responsible for calculating trends (best day, worst day,
   * average visitors per day).
   */

  private static class TrendHandler implements Route {

    @Override
    public String handle(Request req, Response res) throws Exception {

      QueryParamsMap qm = req.queryMap();

      String timeSpan = qm.value("timeSpan");

      DateTime end = null;
      DateTime start = null;
      DateTime middle = null;

      String currMerchant = userDB.getMerchantId(req);

      Client client = null;

      String accessToken = userDB.getAccessToken(req);
      String locationId = req.session().attribute("location");

      if (currMerchant.compareTo("7M8GW6D1APRND") == 0) {
        end = new DateTime(2017, 02, 28, 0, 0);

      } else {
        end = Dates.roundToNearestDay(DateTime.now());
      }

      switch (timeSpan) {
        case "week":
          start = end.minusDays(7);
          break;
        case "month":
          start = end.minusDays(28);
          break;
        default:
          start = end.minusDays(7);
          break;
      }

      if (currMerchant.compareTo("7M8GW6D1APRND") == 0) {
        middle = Dates.findSplitDate(start, end, true);
        client = new HubwayClient(start, middle, end, userDB, locationId);
      } else {
        middle = Dates.findSplitDate(start, end, false);
        client = new SquareClient(accessToken, locationId, start, middle, end,
            userDB);
      }

      return client.getTrendBlurbs();
    }

  }

  /**
   * This returns the overview blurb with a series of quick predictions for next
   * week.
   */

  private static class OverviewHandler implements Route {

    @Override
    public String handle(Request req, Response res) throws Exception {

      String currMerchant = userDB.getMerchantId(req);

      Client client = null;

      String accessToken = userDB.getAccessToken(req);
      String locationId = req.session().attribute("location");

      if (currMerchant.compareTo("7M8GW6D1APRND") == 0) {
        client = new HubwayClient(null, null, null, userDB, locationId);

      } else {
        client = new SquareClient(accessToken, locationId, null, null, null,
            userDB);
      }

      return client.getNextWeekBlurbs();
    }

  }

  /**
   * Display an error page when an exception occurs in the server.
   */
  private static class ExceptionPrinter implements ExceptionHandler {
    @Override
    public void handle(Exception e, Request req, Response res) {
      res.status(500);
      StringWriter stacktrace = new StringWriter();
      try (PrintWriter pw = new PrintWriter(stacktrace)) {
        pw.println("<pre>");
        e.printStackTrace(pw);
        pw.println("</pre>");
      }
      res.body(stacktrace.toString());
    }

  }

  private static UnitTime getUnitTime(String unitString) {
    UnitTime unit;
    switch (unitString) {
      case "hour":
        unit = UnitTime.HOUR;
        break;
      default:
        unit = UnitTime.DAY;
    }
    return unit;
  }

}
