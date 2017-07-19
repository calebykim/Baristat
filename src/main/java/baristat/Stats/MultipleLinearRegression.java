package baristat.Stats;

import Jama.Matrix;

/**
 *
 * Performs a multiple linear regression on inputs. This class serves as a
 * simple wrapper for when classes will only want beta values. It hides Anova
 * functionality.
 *
 * @author Tristin Falk
 *
 */

public class MultipleLinearRegression {
  private Anova anova;

  /**
   *
   * @param x
   *          X matrix for Anova (predictor variables)
   * @param y
   *          Y matrix for Anova (variable to predict)
   */
  public MultipleLinearRegression(Matrix x, Matrix y) {
    this.anova = new Anova(x, y);
  }

  /**
   *
   * @param j
   *          jth beta value wanted
   * @return corresponding beta
   */
  public double getBetaValue(int j) {
    return this.anova.getBeta().get(j);
  }

}
