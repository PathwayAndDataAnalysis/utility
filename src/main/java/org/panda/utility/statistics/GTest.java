package org.panda.utility.statistics;

import org.apache.commons.math3.stat.inference.ChiSquareTest;
import org.panda.utility.ArrayUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by babur on 4/18/16.
 */
public class GTest extends ChiSquare
{
	/**
	 * The contingency table has to be length 2 in the first dimension, and this this should represent present/absent.
	 * The second dimension can be 2 or more in length, representing tested groups.
	 */
	public static double testDependence(long[][] cnts)
	{
		if (cnts == null) return Double.NaN;

		int d1 = cnts.length;
		if (d1 != 2) throw new IllegalArgumentException("Contingency table first dimension has to be 2 and " +
			"has to represent absent/present");

		int d2 = cnts[0].length;
		if (d2 < 2) return 1;

		long[] sumD1 = new long[d1];
		long[] sumD2 = new long[d2];
		double allSum = 0;
		for (int i = 0; i < d1; i++)
		{
			for (int j = 0; j < d2; j++)
			{
				sumD1[i] += cnts[i][j];
				sumD2[j] += cnts[i][j];
				allSum += cnts[i][j];
			}
		}

		double[] expRat = new double[d1];
		for (int i = 0; i < d1; i++)
		{
			expRat[i] = sumD1[i] / allSum;
		}

		double g = 0;

		for (int k = 0; k < d2; k++)
		{
			for (int i = 0; i < d1; i++)
			{
				if (cnts[i][k] > 0)
				{
					g += cnts[i][k] * Math.log(cnts[i][k] / (sumD2[k] * expRat[i]));
				}
			}
		}

		g *= 2;

		return ChiSquare.pValue(g, d2 - 1);
	}

	public static double testDependence(int[] cat, boolean[] control, boolean[] test)
	{
		return testDependence(ArrayUtil.convertCategorySubsetsToContingencyTables(cat, control, test));
	}


	public static double testDependence(int[] cat1, int[] cat2)
	{
		long[][] con = ArrayUtil.convertCategoriesToContingencyTable(cat1, cat2);
		return testDependence(reconfigure(con));
	}

	public static long[][] reconfigure(long[][] cont)
	{
		if (cont.length == 2 && cont[0].length > 1) return cont;
		if (cont.length > 2 && cont[0].length == 2) return ArrayUtil.transpose(cont);
		return null;
	}



	public static void main(String[] args)
	{
		int n = 2;
		int m = 2;

		List<Double> pvals = new ArrayList<>();

		for (int j = 0; j < 1000; j++)
		{
			long[][] l = new long[n][m];

			Random r = new Random();

			for (int i = 0; i < 15; i++)
			{
				l[r.nextInt(n)][r.nextInt(m)]++;
			}

			double p = testDependence(l);
			if (!Double.isNaN(p)) pvals.add(p);
		}

		UniformityChecker.plot(pvals);
	}
}
