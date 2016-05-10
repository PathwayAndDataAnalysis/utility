package org.panda.utility.statistics;

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

//		try
		{
			return TestUtils.tTest(mean, x);
		}
//		catch (MathException e)
//		{
//			e.printStackTrace();
//			throw new RuntimeException(e);
//		}
	}

	public static Tuple test(double[] x0, double[] x1)
	{
		double change = Summary.calcChangeOfMean(x0, x1);
		double pval = getPValOfMeanDifference(x0, x1);
		return new Tuple(change, pval);
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
