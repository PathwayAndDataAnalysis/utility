package org.panda.utility.statistics;

import org.apache.commons.math3.distribution.HypergeometricDistribution;
import org.panda.utility.Kronometre;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

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

//		return calcPositiveDepPval(size - selected - featuredOverall + featuredSelected,
//			featuredOverall - featuredSelected, selected - featuredSelected, featuredSelected);

		HypergeometricDistribution hd = new HypergeometricDistribution(size, featuredOverall, selected);
		return hd.upperCumulativeProbability(featuredSelected);
	}

	public static double calcDeficiencyPval(int size, int featuredOverall, int selected, int featuredSelected)
	{
		assert selected <= size;
		assert featuredSelected <= selected;
		assert featuredSelected <= featuredOverall;

//		return calcNegativeDepPval(size - selected - featuredOverall + featuredSelected,
//			featuredOverall - featuredSelected, selected - featuredSelected, featuredSelected);

		HypergeometricDistribution hd = new HypergeometricDistribution(size, featuredOverall, selected);
		return hd.cumulativeProbability(featuredSelected);
	}

	public static double calcDeficiencyPval(boolean[] alt1, boolean[] alt2)
	{
		int[] cnts = alterationsToCounts(alt1, alt2);
		return calcDeficiencyPval(cnts[0], cnts[1], cnts[2], cnts[3]);
	}

	public static int[] alterationsToCounts(boolean[] alt1, boolean[] alt2)
	{
		if (alt1.length != alt2.length)
			throw new IllegalArgumentException("Array sizes unequal. It is " + alt1.length + " versus " + alt2.length);

		int[] cnts = new int[4];
		cnts[0] = alt1.length;

		for (int i = 0; i < alt1.length; i++)
		{
			if (alt1[i])
			{
				cnts[1]++;
				if (alt2[i]) cnts[3]++;
			}
			if (alt2[i]) cnts[2]++;
		}
		return cnts;
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
//		System.out.println(calcEnrichmentPval(100, 30, 20, 14));
//		System.out.println(ChiSquare.testEnrichment(100, 30, 20, 14));

//		int a = 21, b = 20, c = 34, d = 13;
//		System.out.println(calcPositiveDepPval(a, b, c, d));
//		System.out.println(calcNegativeDepPval(a, b, c, d));

		commonsTest();
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

	public static void commonsTest()
	{
		Random rand = new Random();

		Kronometre kron = new Kronometre();
		kron.start();

		double sum = 0;
		for (int i = 0; i < 10000; i++)
		{
			int populationSize = 1000;
			int featured = rand.nextInt(populationSize);
			int selected = rand.nextInt(populationSize);
			int overlap = rand.nextInt(Math.min(featured, selected) + 1);

//			HypergeometricDistribution hd = new HypergeometricDistribution(populationSize, featured, selected);
//			double p = hd.upperCumulativeProbability(overlap);

			double p = calcEnrichmentPval(populationSize, featured, selected, overlap);

			sum += p;
		}

		System.out.println("sum = " + sum);

		kron.stop();
		kron.print();
	}
}
