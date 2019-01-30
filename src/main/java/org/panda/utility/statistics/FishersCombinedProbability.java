package org.panda.utility.statistics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Aggregates independent p-values using Fisher's combined probability test.
 * @author Ozgun Babur
 */
public class FishersCombinedProbability
{
	/**
	 * Calculates the combined p-value for the given independent p-values.
	 * @param pvals p-values to combine
	 * @return aggregate p-value
	 */
	public static double combine(double... pvals)
	{
		if (pvals == null || pvals.length == 0) return Double.NaN;

		double chi = 0;

		for (double pval : pvals)
		{
			chi += -2 * Math.log(pval);
		}

		return ChiSquare.pValue(chi, 2 * pvals.length);
	}

	/**
	 * This is not a good idea. If two tailed test is needed, then use Stouffer's combined probability instead.
	 */
	public static double[] combine2Tailed(double[] p2t, int[] sign)
	{
		if (p2t.length != sign.length)
		{
			throw new IllegalArgumentException("Input arrays has to be equal size. p2t.length = " + p2t.length +
				", sign.length = " + sign.length);
		}

		double[] p = new double[p2t.length];
		for (int j = 0; j < p.length; j++)
		{
			p[j] = p2t[j] / 2;
			if (sign[j] > 0) p[j] = 1 - p[j];
		}

		double pC = combine(p);
		int s = -1;

		if (pC > 0.5)
		{
			pC = 1 - pC;
			s = 1;
		}

		double pC2t = 2 * pC;

		return new double[]{pC2t, s};
	}

	public static void main(String[] args)
	{
		justCheck();
		graphIt();
		testTails();
		Random r = new Random();
		List<Double> list = new ArrayList<>();
		for (int i = 0; i < 1000; i++)
		{
			double[] arr = new double[r.nextInt(10) + 1];
			for (int j = 0; j < arr.length; j++)
			{
				arr[j] = r.nextDouble();
			}
			list.add(combine(arr));
		}

		UniformityChecker.plot(list);
	}

	private static void testTails()
	{
		Random r = new Random();
		List<Double> list = new ArrayList<>();
		int n = 2;
		int[] signCnt = new int[2];
		for (int i = 0; i < 1000; i++)
		{
			double[] p2t = new double[n];
			int[] s = new int[n];

			for (int j = 0; j < n; j++)
			{
				p2t[j] = r.nextDouble();
				s[j] = r.nextDouble() < 0.5 ? -1 : 1;
			}

			double[] result = combine2Tailed(p2t, s);
			double pC2t = result[0];
			double sign = result[1];
			signCnt[sign < 0 ? 0 : 1]++;

			for (int j = 0; j < s.length; j++)
			{
				s[j] = -s[j];
			}
			double[] ra = combine2Tailed(p2t, s);

			if (ra[0] < pC2t) pC2t = ra[0];

			System.out.println(pC2t + "\t" + ra[0]);

			list.add(pC2t);
		}

		System.out.println(Arrays.toString(signCnt));

//		UniformityChecker.plot(list);

		System.exit(0);
	}

	public static void justCheck()
	{
		System.out.println(combine(0.001, 0.999));
		System.exit(0);
	}
	public static void graphIt()
	{
		double sabit = 0.05;

		for (double i = 0.01; i < 1; i+=0.01)
		{
			System.out.println(i + "\t" + combine(sabit, i));
		}

		System.exit(0);
	}
}
