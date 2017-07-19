package baristat.API;

/**
 * This class offers an interface for a product profile.
 *
 * @author Tristin Falk
 *
 */

public interface Product {

  /**
   * Returns name of the Product.
   *
   * @return name of the Product
   */
  String getName();

  /**
   * Returns cost of the Product.
   *
   * @return cost of the Product
   */
  Double getCost();

}
