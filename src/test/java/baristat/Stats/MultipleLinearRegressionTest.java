package baristat.Stats;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import Jama.Matrix;

/**
 * <<<<<<< HEAD Tests for Anova and MLR.
 *
 * @author Tristin Falk
 *
 */
public class MultipleLinearRegressionTest {

  /**
   * This checks if the getBetaValues function returns the correct values.
   */
  @Test
  public void testGetBeta() {
    final double[][] xArray = {{1, 70, .4}, {1, 71, .7}, {1, 72, .8},
        {1, 73, .2}, {1, 74, .5}, {1, 75, .09}, {1, 76, .04}, {1, 77, .74},
        {1, 78, .98}, {1, 79, .96}};

    final double[] yArray = {100, 120, 140, 160, 180, 200, 220, 240, 260, 280};

    Matrix xMatrix = new Matrix(xArray);
    Matrix yMatrix = new Matrix(yArray, yArray.length);

    MultipleLinearRegression mlr = new MultipleLinearRegression(xMatrix,
        yMatrix);

    assertEquals(mlr.getBetaValue(0), -1300, .001);
    assertEquals(mlr.getBetaValue(1), 20, .001);
    assertEquals(mlr.getBetaValue(2), 0, .001);
  }

  /**
   * This checks if the r squared value is reasonable.
   */
  @Test
  public void testRSquared() {
    final double[][] xArray = {{1, 70, .4}, {1, 71, .7}, {1, 72, .8},
        {1, 73, .2}, {1, 74, .5}, {1, 75, .09}, {1, 76, .04}, {1, 77, .74},
        {1, 78, .98}, {1, 79, .96}};

    final double[] yArray = {100, 120, 140, 160, 180, 200, 220, 240, 260, 280};

    Matrix xMatrix = new Matrix(xArray);
    Matrix yMatrix = new Matrix(yArray, yArray.length);

    Anova anova = new Anova(xMatrix, yMatrix);
    assertEquals(1, anova.getRSquared(), .001);

  }

  /**
   * This tests if a two D array is treated properly.
   */
  @Test
  public void testTwoDArray() {
    final double[][] xArray = {{1, 70}, {1, 71}, {1, 72}, {1, 73}, {1, 74},
        {1, 75}, {1, 76}, {1, 77}, {1, 78}, {1, 79}};

    final double[] yArray = {100, 120, 140, 160, 180, 200, 220, 240, 260, 280};

    Matrix xMatrix = new Matrix(xArray);
    Matrix yMatrix = new Matrix(yArray, yArray.length);

    Anova anova = new Anova(xMatrix, yMatrix);
    assertEquals(1, anova.getRSquared(), .001);

  }

  /**
   * Simple test.
   */
  @Test
  public void matrixTest() {
    double[][] mArr = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};
    Matrix m = new Matrix(mArr);

    double[][] mTransArr = {{1, 4, 7}, {2, 5, 8}, {3, 6, 9}};

    Matrix mTrans = new Matrix(mTransArr);
    Matrix mTransCalc = m.transpose();

    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 3; j++) {
        assert (mTrans.get(i, j) == mTransCalc.get(i, j));
      }
    }
  }

  /**
   * Huge test - the expected answers are from using excel multiple linear
   * regression software. This test pretty much proves things work correctly.
   *
   * Test on basketball data.
   *
   * X1: height in feet
   *
   * X2: weight in lbs
   *
   * X3: percentage of successful FGs
   *
   * X4: percentage of successful
   *
   * Free Throws Y1: average points scored per game
   */
  @Test
  public void basketballDataTest() {
    final double[][] xArray = {{1, 6.8, 225, 0.442, 0.672},
        {1, 6.3, 180, 0.435, 0.797}, {1, 6.4, 190, 0.456, 0.761},
        {1, 6.2, 180, 0.416, 0.651}, {1, 6.9, 205, 0.449, 0.9},
        {1, 6.4, 225, 0.431, 0.78}, {1, 6.3, 185, 0.487, 0.771},
        {1, 6.8, 235, 0.469, 0.75}, {1, 6.9, 235, 0.435, 0.818},
        {1, 6.7, 210, 0.48, 0.825}, {1, 6.9, 245, 0.516, 0.632},
        {1, 6.9, 245, 0.493, 0.757}, {1, 6.3, 185, 0.374, 0.709},
        {1, 6.1, 185, 0.424, 0.782}, {1, 6.2, 180, 0.441, 0.775},
        {1, 6.8, 220, 0.503, 0.88}, {1, 6.5, 194, 0.503, 0.833},
        {1, 7.6, 225, 0.425, 0.571}, {1, 6.3, 210, 0.371, 0.816},
        {1, 7.1, 240, 0.504, 0.714}, {1, 6.8, 225, 0.4, 0.765},
        {1, 7.3, 263, 0.482, 0.655}, {1, 6.4, 210, 0.475, 0.244},
        {1, 6.8, 235, 0.428, 0.728}, {1, 7.2, 230, 0.559, 0.721},
        {1, 6.4, 190, 0.441, 0.757}, {1, 6.6, 220, 0.492, 0.747},
        {1, 6.8, 210, 0.402, 0.739}, {1, 6.1, 180, 0.415, 0.713},
        {1, 6.5, 235, 0.492, 0.742}, {1, 6.4, 185, 0.484, 0.861},
        {1, 6, 175, 0.387, 0.721}, {1, 6, 192, 0.436, 0.785},
        {1, 7.3, 263, 0.482, 0.655}, {1, 6.1, 180, 0.34, 0.821},
        {1, 6.7, 240, 0.516, 0.728}, {1, 6.4, 210, 0.475, 0.846},
        {1, 5.8, 160, 0.412, 0.813}, {1, 6.9, 230, 0.411, 0.595},
        {1, 7, 245, 0.407, 0.573}, {1, 7.3, 228, 0.445, 0.726},
        {1, 5.9, 155, 0.291, 0.707}, {1, 6.2, 200, 0.449, 0.804},
        {1, 6.8, 235, 0.546, 0.784}, {1, 7, 235, 0.48, 0.744},
        {1, 5.9, 105, 0.359, 0.839}, {1, 6.1, 180, 0.528, 0.79},
        {1, 5.7, 185, 0.352, 0.701}, {1, 7.1, 245, 0.414, 0.778},
        {1, 5.8, 180, 0.425, 0.872}, {1, 7.4, 240, 0.599, 0.713},
        {1, 6.8, 225, 0.482, 0.701}, {1, 6.8, 215, 0.457, 0.734},
        {1, 7, 230, 0.435, 0.764}};

    final double[] yArray = {9.2, 11.7, 15.8, 8.6, 23.2, 27.4, 9.3, 16, 4.7,
        12.5, 20.1, 9.1, 8.1, 8.6, 20.3, 25, 19.2, 3.3, 11.2, 10.5, 10.1, 7.2,
        13.6, 9, 24.6, 12.6, 5.6, 8.7, 7.7, 24.1, 11.7, 7.7, 9.6, 7.2, 12.3,
        8.9, 13.6, 11.2, 2.8, 3.2, 9.4, 11.9, 15.4, 7.4, 18.9, 7.9, 12.2, 11,
        2.8, 11.8, 17.1, 11.6, 5.8, 8.3

    };

    Matrix xMatrix = new Matrix(xArray);
    Matrix yMatrix = new Matrix(yArray, yArray.length);

    Anova anova = new Anova(xMatrix, yMatrix);

    // test DoF
    assertEquals(4, anova.getDoF(StatType.MODEL));
    assertEquals(49, anova.getDoF(StatType.ERROR));
    assertEquals(53, anova.getDoF(StatType.TOTAL));

    // test SS M/E/T
    assertEquals(409.933593, anova.getSS(StatType.MODEL), .0001);
    assertEquals(1434.531777, anova.getSS(StatType.ERROR), .0001);
    assertEquals(1844.46537, anova.getSS(StatType.TOTAL), .0001);

    // test MS M/E/T
    assertEquals(102.4833982, anova.getMS(StatType.MODEL), .0001);
    assertEquals(29.27615, anova.getMS(StatType.ERROR), .0001);
    assertEquals(34.8012334, anova.getMS(StatType.TOTAL), .0001);

    // test R2 adjR2 SE
    assertEquals(.222250, anova.getRSquared(), .0001);
    assertEquals(5.4107447, anova.getStandardError(), .0001);
    assertEquals(.158760, anova.getAdjustedRSquared(), .0001);

    // test beta values
    final double[] expectedBetas = {4.148706706, -3.690499083, 0.009458458,
        47.94019916, 11.37101926};

    int i = 0;
    for (double calculatedBeta : anova.getBeta()) {
      assertEquals(expectedBetas[i], calculatedBeta, .0001);
      i++;
    }

  }

}
