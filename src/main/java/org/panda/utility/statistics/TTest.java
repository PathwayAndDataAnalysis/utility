package org.panda.utility.statistics;

import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.stat.inference.TestUtils;
import org.panda.utility.ArrayUtil;
import org.panda.utility.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by babur on 4/18/16.
 */
public class TTest
{
	public static double getPValOfMeanDifference(double[] x0, double[] x1)
	{
		if (x0.length < 2 || x1.length < 2) return Double.NaN;

		return TestUtils.tTest(x0, x1);
	}

	public static double getPValOfMeanEqualTo(double[] x, double mean)
	{
		if (x.length < 2) return Double.NaN;
		return TestUtils.tTest(mean, x);
	}

	public static Tuple test(double[] x0, double[] x1)
	{
		if (x0.length > 2 && x1.length > 2)
		{
			return new Tuple(TestUtils.t(x0, x1), getPValOfMeanDifference(x0, x1));
		}
		else if (x0.length == 2 && x1.length > 2)
		{
			return testFewValuesAgainstASample(x0, x1);
		}
		else if (x0.length == 1 && x1.length > 2)
		{
			return test(x0[0], x1);
		}
		else if (x1.length == 2 && x0.length > 2)
		{
			return testFewValuesAgainstASample(x1, x0);
		}
		else if (x1.length == 1 && x0.length > 2)
		{
			return test(x1[0], x0);
		}

		return new Tuple(Double.NaN, Double.NaN);
	}

	/**
	 * Tests if the given value belongs to the drawn sample.
	 */
	public static Tuple test(double x0, double[] x1)
	{
		if (x1.length < 2) return new Tuple(Double.NaN, Double.NaN);

		TDistribution dist = new TDistribution(x1.length - 1);
		double t = (x0 - Summary.mean(x1)) / Summary.stdev(x1);

		double cdf = dist.cumulativeProbability(t);
		double p = cdfToPval(cdf);
		return new Tuple(t, p);
	}

	/**
	 * Tests if the given value belongs to the drawn sample.
	 */
	public static Tuple testFewValuesAgainstASample(double[] few, double[] sample)
	{
		if (sample.length < 2) return new Tuple(Double.NaN, Double.NaN);

		TDistribution dist = new TDistribution(sample.length - 1);
		double t = (Summary.mean(few) - Summary.mean(sample)) / (Summary.stdev(sample) / Math.sqrt(few.length));

		double cdf = dist.cumulativeProbability(t);
		double p = cdfToPval(cdf);
		return new Tuple(t, p);
	}

	/**
	 * Cumulative distribution function value to 2-tailed p-value.
	 */
	public static double cdfToPval(double cdf)
	{
		return cdf < 0.5 ? cdf * 2 : (1 - cdf) * 2;
	}

	public static void main(String[] args)
	{
		Random r = new Random();
		List<Double> pvals = new ArrayList<>();

		for (int i = 0; i < 1000; i++)
		{
			List<List<Double>> list = new ArrayList<>();
			for (int j = 0; j < 2; j++)
			{
				list.add(new ArrayList<>());
			}

			for (int j = 0; j < 10; j++)
			{
//				list.get(r.nextInt(list.size())).add(r.nextGaussian());
				list.get(r.nextDouble() < 0.2 ? 0 : 1).add(r.nextDouble());
			}

			List<double[]> groups = new ArrayList<>();
			for (List<Double> doubles : list)
			{
				groups.add(ArrayUtil.toArray(doubles));
			}
			double p = getPValOfMeanDifference(groups.get(0), groups.get(1));
			if (!Double.isNaN(p)) pvals.add(p);
		}

		UniformityChecker.plot(pvals);
	}
}
