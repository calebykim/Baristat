package baristat.API;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import org.joda.time.DateTime;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import baristat.Dates.Dates;
import baristat.Dates.UnitTime;

/**
 * This is the product profile class.
 *
 * @author jbreuch
 */
public class ProductProfile implements Product, Comparable<ProductProfile> {

  private String name;
  private Double cost;
  private Multiset<DateTime> numSoldPerUnit;
  private Map<DateTime, Integer> numSoldPerUnitMap;
  private int rank;
  private int totalNumSold;

  /**
   * Constructs a ProductProfile with the name and cost passed in.
   *
   * @param name
   *          the name of the product
   * @param cost
   *          the cost of the product
   */
  public ProductProfile(String name, Double cost) {
    this.name = name;
    this.cost = cost;
    this.numSoldPerUnit = HashMultiset.create();
    this.numSoldPerUnitMap = new TreeMap<>();
    this.totalNumSold = 0;
    this.rank = 0;
  }

  /**
   * Sets rank of product in set of products.
   *
   *
   * @param newRank
   *          rank
   * @return rank the rank
   */
  public int setRank(int newRank) {
    this.rank = newRank;
    return rank;
  }

  /**
   * Sets number of products sold.
   *
   * @param numSold
   *          the number of product sold
   * @return numSold
   *
   */
  public int setTotalNumberSold(int numSold) {
    this.totalNumSold = numSold;
    return numSold;
  }

  /**
   * Sets number of products sold.
   *
   * @param numSoldMap
   *          the number of product sold
   *
   */
  public void setTotalNumberSoldPerUnitTime(Multiset<DateTime> numSoldMap) {
    this.numSoldPerUnit = numSoldMap;
  }

  /**
   * Sets number of products sold in the map.
   *
   * @param numSoldMap
   *          the number of product sold
   * @param start
   *          start of range
   * @param end
   *          end of range
   * @param ut
   *          UnitTime of range
   *
   */
  public void setTotalNumberSoldPerUnitTimeMap(Multiset<DateTime> numSoldMap,
      DateTime start, DateTime end, UnitTime ut) {
    this.numSoldPerUnit = numSoldMap;

    if (ut.compareTo(UnitTime.HOUR) == 0) {
      end = Dates.incrementDate(end, UnitTime.DAY);
    }

    for (DateTime dt : numSoldMap.elementSet()) {
      numSoldPerUnitMap.put(dt, numSoldMap.count(dt));
    }

    DateTime curr = start;

    while (curr.compareTo(end) <= 0) {
      if (!numSoldPerUnitMap.keySet().contains(curr)) {
        numSoldPerUnitMap.put(curr, 0);
      }
      curr = Dates.incrementDate(curr, ut);
    }
  }

  /**
   * Gets the TreeMap indicating the number of items sold.
   *
   * @return numSoldPerUnitMap, the map
   */

  public Map<DateTime, Integer> getNumSoldTreeMap() {
    return numSoldPerUnitMap;
  }

  /**
   * Gets the Multiset indicating the number of items sold.
   *
   * @return numSoldPerUnit, the Multiset
   */

  public Multiset<DateTime> getNumSoldMap() {
    return numSoldPerUnit;
  }

  /**
   * Returns quantity sold for the product.
   *
   * @return quantity sold for the product.
   */
  public int getTotalNumSold() {
    return totalNumSold;
  }

  /**
   * Returns quantity sold for the product.
   *
   * @param dt
   *          datetime to query
   * @return quantity sold for the product.
   */
  public int getTotalNumOnDate(DateTime dt) {
    return this.numSoldPerUnit.count(dt);
  }

  /**
   * Returns rank for the product.
   *
   * @return rank for the product.
   */
  public int getRank() {
    return rank;
  }

  /**
   * Returns name of the Product.
   *
   * @return name of the Product
   */
  @Override
  public String getName() {
    return name;
  }

  /**
   * Returns cost of the Product.
   *
   * @return cost of the Product
   */
  @Override
  public Double getCost() {
    return cost;
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.name);
  }

  // @SuppressWarnings("unchecked")
  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof ProductProfile)) {
      return false;
    }
    ProductProfile other = (ProductProfile) o;
    return (other.name.equals(this.getName()));
  }

  @Override
  public int compareTo(ProductProfile otherProd) {

    double otherTotalPrice = otherProd.getTotalNumSold() * otherProd.getCost();
    double thisTotalPrice = this.getTotalNumSold() * this.getCost();

    // ascending order
    return Double.compare(otherTotalPrice, thisTotalPrice);

    // descending order
    // return compareQuantity - this.quantity;

  }

}
