package org.panda.utility.statistics.trendline;

import java.util.Arrays;

/**
 * http://stackoverflow.com/questions/17592139/trend-lines-regression-curve-fitting-java-library
 */
public class PolyTrendLine extends OLSTrendLine {
	final int degree;
	public PolyTrendLine(int degree) {
		if (degree < 0) throw new IllegalArgumentException("The degree of the polynomial must not be negative");
		this.degree = degree;
	}
	protected double[] xVector(double x) { // {1, x, x*x, x*x*x, ...}
		double[] poly = new double[degree+1];
		double xi=1;
		for(int i=0; i<=degree; i++) {
			poly[i]=xi;
			xi*=x;
		}
		return poly;
	}
	@Override
	protected boolean logY() {return false;}

	public double[] getCoefficients()
	{
		return coef.getColumn(0);
	}

	public static void main(String[] args)
	{
		PolyTrendLine tl = new PolyTrendLine(1);
		tl.setValues(new double[]{5,7,9,11,13,15}, new double[]{2,3,4,5,6,7});
		System.out.println("tl.predict(20) = " + tl.predict(20));
		System.out.println(Arrays.toString(tl.getCoefficients()));
	}
}