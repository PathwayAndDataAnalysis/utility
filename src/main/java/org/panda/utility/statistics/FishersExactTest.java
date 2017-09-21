package org.panda.utility.statistics;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Consider below two-by-two table of counts of two traits.
 *
 *      A-  A+
 *     --------
 * B- | a | b |
 *    ---------
 * B+ | c | d |
 *    ---------
 *
 * Fisher's exact test calculates the exact probability of observing this pattern if traits A and B
 * were independent. This class provides two one-tailed tests, one for positive dependency and one
 * for negative dependency.
 *
 * @author Ozgun Babur
 */
public class FishersExactTest
{
	public static double calcNegativeDepPval(int a, int b, int c, int d)
	{
		double pval = 0;

		do {
			FactorialSolver f = new FactorialSolver(
				new ArrayList<>(Arrays.asList(a+b, c+d, a+c, b+d)),
				new ArrayList<>(Arrays.asList(a, b, c, d, a+b+c+d)));
			pval += f.solve();

			a--;
			b++;
			c++;
			d--;
		}
		while(d >= 0 && a >= 0);
		return pval;
	}

	public static double calcPositiveDepPval(int a, int b, int c, int d)
	{
		double pval = 0;

		do {
			FactorialSolver f = new FactorialSolver(
				new ArrayList<>(Arrays.asList(a+b, c+d, a+c, b+d)),
				new ArrayList<>(Arrays.asList(a, b, c, d, a+b+c+d)));
			pval += f.solve();

			a++;
			b--;
			c--;
			d++;
		}
		while(b >= 0 && c >= 0);
		return pval;
	}

	public static double calcEnrichmentPval(int size, int featuredOverall, int selected,
		int featuredSelected)
	{
		assert selected <= size;
		assert featuredSelected <= selected;
		assert featuredSelected <= featuredOverall;

		return calcPositiveDepPval(size - selected - featuredOverall + featuredSelected,
			featuredOverall - featuredSelected, selected - featuredSelected, featuredSelected);
	}

	public static double calcImprovishmentPval(int size, int featuredOverall, int selected,
		int featuredSelected)
	{
		assert selected <= size;
		assert featuredSelected <= selected;
		assert featuredSelected <= featuredOverall;

		return calcNegativeDepPval(size - selected - featuredOverall + featuredSelected,
			featuredOverall - featuredSelected, selected - featuredSelected, featuredSelected);
	}

	public static double getPvalOfMeanDiff_discretizeToTwo(double[] x0, double[] x1)
	{
		double[] d = new double[x0.length + x1.length];
		System.arraycopy(x0, 0, d, 0, x0.length);
		System.arraycopy(x1, 0, d, x0.length, x1.length);
		double median = Summary.median(d);

		int x0Low = 0;
		int x0High = 0;
		int x1Low = 0;
		int x1High = 0;

		for (double v : x0) if (v < median) x0Low++; else x0High++;
		for (double v : x1) if (v < median) x1Low++; else x1High++;

		double p1 = calcPositiveDepPval(x0Low, x0High, x1Low, x1High);
		double p2 = calcNegativeDepPval(x0Low, x0High, x1Low, x1High);

		double p = Math.min(p1, p2) * 2;
		return p;
	}


	public static void main(String[] args)
	{
		System.out.println(calcEnrichmentPval(213, 61, 14, 44));
	}

	public static double calcCoocPval(boolean[] b1, boolean[] b2)
	{
		if (b1.length != b2.length) throw new IllegalArgumentException("Array lengths have to be equal.");

		int a = 0;
		int b = 0;
		int c = 0;
		int d = 0;

		for (int i = 0; i < b1.length; i++)
		{
			if (b1[i])
			{
				if (b2[i]) d++;
				else b++;
			}
			else if (b2[i]) c++;
			else a++;
		}

		return calcPositiveDepPval(a, b, c, d);
	}
}
