package org.panda.utility.statistics;

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

			double mean = Summary.meanOfDoubles(dist);
			double sd = Summary.stdev(dist.toArray(new Double[dist.size()]));

			Double actual = sample.get(id);

			z.put(id, (actual - mean) / sd);
		}

		return z;
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

			z.put(id, (actual - mean) / sd);
		}

		return z;
	}
}
