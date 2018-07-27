package org.panda.utility.statistics;

import org.panda.utility.ArrayUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ozgun Babur
 */
public class ZScore
{
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
}
