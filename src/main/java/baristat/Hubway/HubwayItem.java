package baristat.Hubway;

import baristat.API.ProductProfile;

/**
 * Class to model a mock "item". Items are created by assigning them a start and
 * end station Id. If a trip has a start station id of the item, then it is
 * considered a sale for that item.
 *
 * @author Tristin Falk
 *
 */
public class HubwayItem extends ProductProfile {
  private int startStnId;
  private int endStnId;

  /**
   *
   * @param name
   *          name of item
   * @param startStnId
   *          start station id
   * @param endStnId
   *          end station id
   * @param cost
   *          cost of item
   */
  public HubwayItem(String name, int startStnId, int endStnId, double cost) {
    super(name, cost);
    this.startStnId = startStnId;
    this.endStnId = endStnId;
  }

  /**
   *
   * @return start stn id
   */
  public int getStartStnId() {
    return startStnId;
  }

  /**
   *
   * @return end stn id
   */
  public int getEndStnId() {
    return endStnId;
  }

}
