package org.panda.utility.statistics;

import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.stat.inference.TestUtils;
import org.panda.utility.ArrayUtil;
import org.panda.utility.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * Created by babur on 4/18/16.
 */
public class TTest
{
	public static double getPValOfMeanDifference(double[] control, double[] test)
	{
		if (control.length < 2 || test.length < 2) return Double.NaN;

		return TestUtils.tTest(control, test);

//		double t = t(x0, x1);
//		return pval(t, length(x0) + length(x1) - 2);
	}

	public static double t(double[] x1, double[] x2)
	{
		double M1 = ArrayUtil.mean(x1);
		double M2 = ArrayUtil.mean(x2);
		double N1 = length(x1);
		double N2 = length(x2);

		return (M2 - M1) /
			Math.sqrt(((sumOfSquares(x1, M1) + sumOfSquares(x2, M2)) / (N1 + N2 - 2)) * ((1 / N1) + (1 / N2)));
	}

	private static double sumOfSquares(double[] x, double mean)
	{
		double sum = 0;
		for (int i = 0; i < x.length; i++)
		{
			if (!Double.isNaN(x[i]))
			{
				double v = x[i] - mean;
				v *= v;
				sum += v;
			}
		}
		return sum;
	}

	private static double variance(double[] x, double mean)
	{
		double var = 0;
		int cnt = 0;

		for (double v : x)
		{
			if (!Double.isNaN(v))
			{
				double term = v - mean;
				var += term * term;
				cnt++;
			}
		}

		return cnt == 0 ? Double.NaN : var / (cnt - 1);
	}

	private static int length(double[] x)
	{
		return (int) IntStream.range(0, x.length).filter(i -> !Double.isNaN(x[i])).count();
	}


	public static double pval(double t, int df)
	{
		TDistribution distribution = new TDistribution(df);
		return 2.0 * distribution.cumulativeProbability(-Math.abs(t));
	}

	public static double getPValOfMeanEqualTo(double[] x, double mean)
	{
		if (x.length < 2) return Double.NaN;
		return TestUtils.tTest(mean, x);
	}

	public static Tuple testPaired(double[] control, double[] test)
	{
		if (control.length > 2 && test.length > 2)
		{
			return new Tuple(TestUtils.pairedT(test, control), TestUtils.pairedTTest(test, control));
		}

		return new Tuple(Double.NaN, Double.NaN);
	}
	public static Tuple test(double[] control, double[] test)
	{
		if (control.length > 2 && test.length > 2)
		{
			return new Tuple(TestUtils.t(test, control), getPValOfMeanDifference(test, control));
		}
		else if (control.length == 2 && test.length > 2)
		{
			Tuple tup = testFewValuesAgainstASample(test, control);
			tup.v *= -1;
			return tup;
		}
		else if (control.length == 1 && test.length > 2)
		{
			Tuple tup = test(test, control[0]);
			tup.v *= -1;
			return tup;
		}
		else if (test.length == 2 && control.length > 2)
		{
			return testFewValuesAgainstASample(control, test);
		}
		else if (test.length == 1 && control.length > 2)
		{
			return test(control, test[0]);
		}

		return new Tuple(Double.NaN, Double.NaN);
	}

	/**
	 * Tests if the given value belongs to the drawn sample.
	 */
	public static Tuple test(double[] control, double test)
	{
		if (control.length < 2) return new Tuple(Double.NaN, Double.NaN);

		TDistribution dist = new TDistribution(control.length - 1);
		double t = (test - Summary.mean(control)) / Summary.stdev(control);

		double cdf = dist.cumulativeProbability(t);
		double p = cdfToPval(cdf);
		return new Tuple(t, p);
	}

	/**
	 * Tests if the given value belongs to the drawn sample.
	 */
	public static Tuple testFewValuesAgainstASample(double[] control, double[] fewTest)
	{
		if (control.length < 2) return new Tuple(Double.NaN, Double.NaN);

		TDistribution dist = new TDistribution(control.length - 1);
		double t = (Summary.mean(fewTest) - Summary.mean(control)) / (Summary.stdev(control) / Math.sqrt(fewTest.length));

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
		checkPaired();


//		Random r = new Random();
//		List<Double> pvals = new ArrayList<>();
//
//		for (int i = 0; i < 1000; i++)
//		{
//			List<List<Double>> list = new ArrayList<>();
//			for (int j = 0; j < 2; j++)
//			{
//				list.add(new ArrayList<>());
//			}
//
//			for (int j = 0; j < 10; j++)
//			{
//				list.get(r.nextInt(list.size())).add(r.nextGaussian());
////				list.get(r.nextDouble() < 0.2 ? 0 : 1).add(r.nextDouble());
//			}
//
//			List<double[]> groups = new ArrayList<>();
//			for (List<Double> doubles : list)
//			{
//				groups.add(ArrayUtil.toArray(doubles));
//			}
//			double p = getPValOfMeanDifference(groups.get(0), groups.get(1));
//			if (!Double.isNaN(p)) pvals.add(p);
//		}
//
//		UniformityChecker.plot(pvals);
	}

	public static void checkPaired()
	{
		double[] v0 = new double[]{6001.3, 7627.7, 5639.4, 7628.5};
		double[] v1 = new double[]{24.5, 27.2, 25.2, 26.5};

		for (int i = 0; i < v0.length; i++)
		{
			v0[i] = Math.log(v0[i]);
			v1[i] = Math.log(v1[i]);
		}

		System.out.println("TestUtils.t(v0, v1) = " + TestUtils.t(v1, v0));
		System.out.println("TestUtils.ttest(v0, v1) = " + TestUtils.tTest(v0, v1));
	}
}
