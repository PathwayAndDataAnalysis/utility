package org.panda.utility.statistics;

import org.apache.commons.math3.analysis.function.Gaussian;

import java.util.Random;

/**
 * @author Ozgun Babur
 */
public class KernelDensityEstimation implements Function
{
	private double[] vals;
	private double delta;
	private Gaussian g;
	private double oneOverNDelta;

	public KernelDensityEstimation(double[] vals)
	{
		this.vals = vals;
		g = new Gaussian();
		initDeltaToDefault();
		oneOverNDelta = 1D / (vals.length * delta);
	}

	public double initDeltaToDefault()
	{
		double stdev = Summary.stdev(vals);
		delta = (1.06 / Math.pow(vals.length, 0.2)) * stdev;
		return delta;
	}

	public void setDelta(double delta)
	{
		this.delta = delta;
	}

	public double[] getVals()
	{
		return vals;
	}

	public double getDelta()
	{
		return delta;
	}

	public double value(double x)
	{
		double e = 0;

		for (double val : vals)
		{
			e += g.value((x - val) / delta);
		}

		e *= oneOverNDelta;
		return e;
	}

	public static void main(String[] args)
	{
		double[] m = new double[]{-5, 1, 5};

		Random r = new Random();
		int n = 1000;
		double[] vals = new double[n];
		for (int i = 0; i < n; i++)
		{
			int gInd = r.nextInt(m.length);
			vals[i] = r.nextGaussian() + m[gInd];
		}

		KernelDensityEstimation kde = new KernelDensityEstimation(vals);

		KernelDensityPlot kdp = new KernelDensityPlot(kde);
		kdp.label("Zero", 0);
		kdp.label("One", 1);
		kdp.label("Two", 2);
		kdp.label("Three", 3);
		kdp.label("Minus one", -1);
		kdp.label("Minus two", -2);
		kdp.label("Minus 3", -3);
		kdp.setVisible(true);
	}
}
