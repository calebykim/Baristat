package baristat.Stats;

import java.util.ArrayList;
import java.util.List;

import Jama.Matrix;

/**
 * This class calculates and stores ANOVA values for a multiple regression
 * analysis. In particular the beta matrix will be used for predictions. The
 * other methods are for developers.
 *
 * @author Tristin Falk
 *
 */
public class Anova {
  private List<Integer> dof;
  private List<Double> sumSquares;
  private List<Double> meanSquares;
  private Matrix x;
  private Matrix y;
  private Matrix beta;

  /**
   * Constructor.
   *
   * @param x
   *          Takes in a X Matrix, which should contain all predictor variable
   *          past data.
   * @param y
   *          Matrix containing all past data for the thing you are trying to
   *          predict
   */
  public Anova(Matrix x, Matrix y) {
    this.x = x;
    this.y = y;
    this.setupSumSquares();
    this.setupDoF();
    this.setupMeanSquares();
  }

  private void setupSumSquares() {
    this.sumSquares = new ArrayList<>();
    // Sum of Squares Total
    double yBar = 0.0;
    int yLen = y.getRowDimension();
    for (int i = 0; i < yLen; i++) {
      yBar += y.get(i, 0);
    }
    yBar = yBar / yLen;

    double sumSquaresTotal = 0.0;
    for (int i = 0; i < yLen; i++) {
      double diff = y.get(i, 0) - yBar;
      sumSquaresTotal += diff * diff;
    }

    // Sum of Squares Error
    // ensure beta is filled
    this.getBeta();
    Matrix error = x.times(beta).minus(y);
    double sumSquaresError = error.norm2() * error.norm2();

    // Sum of Squares Model
    double sumSquaresModel = sumSquaresTotal - sumSquaresError;

    sumSquares.add(sumSquaresModel);
    sumSquares.add(sumSquaresError);
    sumSquares.add(sumSquaresTotal);
  }

  private void setupDoF() {
    dof = new ArrayList<>();
    int n = x.getRowDimension();
    int p = x.getColumnDimension();
    dof.add(p - 1); // p-1 dof
    dof.add(n - p); // n-p-1 dof
    dof.add(n - 1); // n-1 dof
  }

  // divide sumSquares by corresponding DoF
  // Need to make sure this works
  private void setupMeanSquares() {
    meanSquares = new ArrayList<>();
    meanSquares.add(sumSquares.get(StatType.MODEL.ordinal())
        / dof.get(StatType.MODEL.ordinal()));
    meanSquares.add(sumSquares.get(StatType.ERROR.ordinal())
        / dof.get(StatType.ERROR.ordinal()));
    meanSquares.add(sumSquares.get(StatType.TOTAL.ordinal())
        / dof.get(StatType.TOTAL.ordinal()));
  }

  // Calculates beta using
  // b = (X'X)^(−1)X'Y
  private void setupBeta() {
    Matrix ls = x.transpose().times(x).inverse();
    Matrix rs = x.transpose().times(y);
    beta = ls.times(rs);
  }

  /**
   * @return computes and returns R-Squared
   */
  public double getRSquared() {
    return sumSquares.get(StatType.MODEL.ordinal())
        / sumSquares.get(StatType.TOTAL.ordinal());
  }

  /**
   * @return computes and returns adjusted R-Squared
   */
  public double getAdjustedRSquared() {
    return 1.0 - getMS(StatType.ERROR) / getMS(StatType.TOTAL);
  }

  /**
   * @return computes and returns standard error
   */
  public double getStandardError() {
    return Math.sqrt(meanSquares.get(StatType.ERROR.ordinal()));
  }

  /**
   * @param type
   *          of stat
   * @return the Degrees of Freedom of given stat type
   */
  public int getDoF(StatType type) {
    return dof.get(type.ordinal());
  }

  /**
   * @param type
   *          of stat
   * @return the Sum of Squares of given stat type
   */
  public double getSS(StatType type) {
    return sumSquares.get(type.ordinal());
  }

  /**
   * @param type
   *          of stat
   * @return the Mean Squares of Freedom of given stat type
   */
  public double getMS(StatType type) {
    return meanSquares.get(type.ordinal());
  }

  /**
   * @return A List of doubles that represent the beta values for the predictor
   *         variables
   */
  public List<Double> getBeta() {
    if (beta == null) {
      this.setupBeta();
    }
    return RegressionTools.arrayToList(beta.getColumnPackedCopy());
  }

}
