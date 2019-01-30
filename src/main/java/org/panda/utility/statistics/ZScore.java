package org.panda.utility.statistics;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.special.Erf;
import org.panda.utility.ArrayUtil;

import java.util.*;

/**
 * @author Ozgun Babur
 */
public class ZScore
{
	public static final double SQRT2 = Math.sqrt(2);

	public static Map<String, Double> get(Map<String, List<Double>> distMap, Map<String, Double> sample)
	{
		Map<String, Double> z = new HashMap<>();

		for (String id : sample.keySet())
		{
			List<Double> dist = distMap.get(id);
			if (dist == null) continue;

			double zval = getZVal(sample.get(id), dist);

			z.put(id, zval);
		}

		return z;
	}

	public static double getZVal(double value, List<Double> dist)
	{
		double mean = Summary.meanOfDoubles(dist);
		double sd = Summary.stdev(dist.toArray(new Double[dist.size()]));

		return (value - mean) / sd;
	}

	public static Map<String, Double> get(Map<String, double[]> distMap, Map<String, Double> sample, Object passNull)
	{
		Map<String, Double> z = new HashMap<>();

		for (String id : sample.keySet())
		{
			double[] dist = distMap.get(id);
			if (dist == null) continue;

			double mean = Summary.mean(dist);
			double sd = Summary.stdev(dist);

			Double actual = sample.get(id);

			double zval = (actual - mean) / sd;
			z.put(id, zval);
		}

		return z;
	}

	/**
	 * Converts the provided array into z-scores. May return null.
	 * @param original the array
	 * @return z-scores
	 */
	public static double[] get(double[] original)
	{
		double[] arr = ArrayUtil.trimNaNs(original);

		if (arr.length < 3) return null;

		double sd = Summary.stdev(arr);

		if (sd == 0 || Double.isNaN(sd)) return null;

		double mean = Summary.mean(arr);

		double[] zarr = new double[original.length];

		for (int i = 0; i < zarr.length; i++)
		{
			if (Double.isNaN(original[i])) zarr[i] = Double.NaN;
			else
			{
				zarr[i] = (original[i] - mean) / sd;
			}
		}
		return zarr;
	}

	public static double zScoreToPercentile(double z)
	{
		NormalDistribution dist = new NormalDistribution();
		return dist.cumulativeProbability(z);
	}

	public static double[] zScoreToPercentile(double[] z)
	{
		double[] p = new double[z.length];

		for (int i = 0; i < p.length; i++)
		{
			p[i] = zScoreToPercentile(z[i]);
		}

		return p;
	}

	public static double percentileToZScore(double p)
	{
		return - SQRT2 * Erf.erfcInv(2 * p);
	}

	public static double[] percentileToZScore(double[] p)
	{
		double[] z = new double[p.length];

		for (int i = 0; i < z.length; i++)
		{
			z[i] = percentileToZScore(p[i]);
		}

		return z;
	}

	public static void main(String[] args)
	{
//		System.out.println(zScoreToPercentile(-3));
//		System.out.println(percentileToZScore(0.001));
//		System.out.println(percentileToZScore(1 - 0.03));
//		checkUniformity();
		plotPtoZ();
	}

	private static void checkUniformity()
	{
		Random r = new Random();
		int n = 1000;
		double[] v = new double[n];
		List<Double> list = new ArrayList<>();

		for (int i = 0; i < n; i++)
		{
			v[i] = r.nextGaussian();
		}

		double[] z = get(v);

		for (int i = 0; i < n; i++)
		{
			double p = zScoreToPercentile(z[i]);
			list.add(p);
		}
		UniformityChecker.plot(list);
	}

	private static void plotPtoZ()
	{
		for (double i = 0.01; i < 1; i+=0.01)
		{
			System.out.println(-Math.log(i) + "\t" + percentileToZScore(i));
		}
	}
}
