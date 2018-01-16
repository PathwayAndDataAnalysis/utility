package org.panda.utility.statistics;

import java.util.ArrayList;
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
	public static double pValue(double... pvals)
	{
		if (pvals == null || pvals.length == 0) return Double.NaN;

		double chi = 0;

		for (double pval : pvals)
		{
			chi += -2 * Math.log(pval);
		}

		return ChiSquare.pValue(chi, 2 * pvals.length);
	}

	public static void main(String[] args)
	{
		Random r = new Random();
		List<Double> list = new ArrayList<>();
		for (int i = 0; i < 1000; i++)
		{
			double[] arr = new double[r.nextInt(10) + 1];
			for (int j = 0; j < arr.length; j++)
			{
				arr[j] = r.nextDouble();
			}
			list.add(pValue(arr));
		}

		UniformityChecker.plot(list);
	}
}
