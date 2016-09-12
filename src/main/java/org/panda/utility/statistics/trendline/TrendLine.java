package org.panda.utility.statistics.trendline;

/**
 * http://stackoverflow.com/questions/17592139/trend-lines-regression-curve-fitting-java-library
 */
public interface TrendLine
{
	public void setValues(double[] y, double[] x); // y ~ f(x)
	public double predict(double x); // get a predicted y for a given x
}
