package baristat.Square;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.squareup.connect.models.Money;
import com.squareup.connect.models.Money.CurrencyEnum;

/**
 * This class was used for testing the connection to Square by adding
 * transactions.
 *
 * @author adrianturcu
 *
 */
public class SandboxTester {

  private String accessToken;
  private String locationId;
  private String cardNonce;

  /**
   * This is the constructor for the SandboxTester class.
   *
   * @param accToken
   *          the token
   * @param locId
   *          the location id
   */
  public SandboxTester(String accToken, String locId) {
    cardNonce = "CBASEEFw2paEeo7MNCxnkoqZmzMgAQ";
    accessToken = accToken;
    locationId = locId;
  }

  /**
   * This method generates false data for testing purposes.
   *
   * @throws IOException
   *           exception
   * @throws ClientProtocolException
   *           exception
   */
  public void populateData() throws ClientProtocolException, IOException {

    HttpClient client = HttpClients.createDefault();
    HttpPost post = new HttpPost("https://connect.squareup.com/v2/locations/"
        + locationId + "/transactions");

    post.addHeader("Authorization", "Bearer " + accessToken);
    post.addHeader("Content-Type", "application/json");
    post.addHeader("Accept", "application/json");

    Gson gson = new Gson();
    Money money = new Money();

    money.setAmount((long) 495);
    money.setCurrency(CurrencyEnum.USD);

    Map<String, Object> variables = ImmutableMap.of("idempotency_key",
        UUID.randomUUID().toString(), "amount_money", money, "card_nonce",
        cardNonce, "reference_id",
        getRandomName((int) Math.random() * 4) + " LATTE");

    post.setEntity(new StringEntity(gson.toJson(variables)));

    client.execute(post);
  }

  private String getRandomName(int num) {

    switch (num) {

      case 0:
        return "ADRIANS";
      case 1:
        return "CALEBS";
      case 2:
        return "JUSTINES";
      case 3:
        return "TRISTINS";
      default:
        break;
    }

    return "SOMEONES";
  }
}
