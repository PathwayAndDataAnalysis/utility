package org.panda.utility.statistics;

import org.apache.commons.math3.stat.inference.ChiSquareTest;
import org.panda.utility.ArrayUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by babur on 4/18/16.
 */
public class ChiSquare
{
	public static double testEnrichment(int size, int featuredOverall, int selected, int featuredSelected)
	{
		double p = testDependence(size, featuredOverall, selected, featuredSelected) / 2;
		if (featuredOverall / (double) size > featuredSelected / (double) selected) p = 1 - p;
		return p;
	}

	public static double testExclusivity(int size, int featuredOverall, int selected, int featuredSelected)
	{
		double p = testDependence(size, featuredOverall, selected, featuredSelected) / 2;
		if (featuredOverall / (double) size < featuredSelected / (double) selected) p = 1 - p;
		return p;
	}

	public static double testDependence(int size, int featuredOverall, int selected, int featuredSelected)
	{
		return testDependence(convertOverlapToContingencyTable(size, featuredOverall, selected, featuredSelected));
	}

	public static long[][] convertOverlapToContingencyTable(int size, int featuredOverall, int selected,
		int featuredSelected)
	{
		long[][] cnts = new long[2][2];
		cnts[0][0] = size - selected - featuredOverall + featuredSelected;
		cnts[0][1] = featuredOverall - featuredSelected;
		cnts[1][0] = selected - featuredSelected;
		cnts[1][1] = featuredSelected;
		return cnts;
	}

	public static int[] convertContingencyTableToOverlap(long[][] cnts)
	{
		int featuredSelected = (int) cnts[1][1];
		int selected = (int) (cnts[1][1] + cnts[1][0]);
		int featuredOverall = (int) (cnts[1][1] + cnts[0][1]);
		int size = (int) (cnts[0][0] + cnts[0][1] + cnts[1][0] + cnts[1][1]);
		return new int[]{size, featuredOverall, selected, featuredSelected};
	}

	public static double testDependence(long[][] cnts)
	{
		if (cnts.length < 2 || cnts[0].length < 2) return 1;

		ChiSquareTest cst = new ChiSquareTest();
		return cst.chiSquareTest(cnts);
	}

	public static double testDependence(int[] cat1, int[] cat2)
	{
		return testDependence(ArrayUtil.convertCategoriesToContingencyTable(cat1, cat2));
	}

	public static double testDependence(int[] cat, boolean[] control, boolean[] test)
	{
		return testDependence(ArrayUtil.convertCategorySubsetsToContingencyTables(cat, control, test));
	}

	/**
	 * Calculates the p-value of the given chi-square value with the given degrees of freedom.
	 * @param x chi value
	 * @param n degrees of freedom
	 * @return p-value
	 */
	public static double pValue(double x, double n)
	{
		if(n==1 && x>1000)
		{
			return 0;
		}
		if(x>1000 || n>1000)
		{
			double q = pValue((x - n) * (x - n) / (2 * n), 1) / 2;

			if(x>n)
			{
				return q;
			}
			else
			{
				return 1-q;
			}
		}
		double p = Math.exp(-0.5 * x);
		if((n % 2) == 1)
		{
			p = p * Math.sqrt(2 * x / Math.PI);
		}

		double k = n;

		while(k >= 2)
		{
			p = p * x / k;
			k = k - 2;
		}
		double t = p;
		double a = n;
		while(t > 0.0000000001 * p)
		{
			a = a + 2;
			t = t * x / a;
			p = p + t;
		}

		return 1 - p;
	}


	public static void main(String[] args)
	{
		System.out.println(testDependence(new long[][]{{55, 1621-50}, {16, 516-16}}));
	}

	public static void checkUniformity()
	{
		int n = 2;
		int m = 2;

		List<Double> pvals = new ArrayList<>();

		for (int j = 0; j < 1000; j++)
		{
			long[][] l = new long[n][m];

			Random r = new Random();

			for (int i = 0; i < 100; i++)
			{
				l[r.nextInt(n)][r.nextInt(m)]++;
			}

			int[] c = convertContingencyTableToOverlap(l);
			double p = testExclusivity(c[0], c[1], c[2], c[3]);
//			double p = testDependence(l);
			if (!Double.isNaN(p)) pvals.add(p);
		}

		UniformityChecker.plot(pvals);
	}
}
