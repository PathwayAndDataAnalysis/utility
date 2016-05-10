package org.panda.utility.statistics;

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
		double chi = 0;

		for (double pval : pvals)
		{
			chi += -2 * Math.log(pval);
		}

		return ChiSquare.pValue(chi, 2 * pvals.length);
	}

	public static void main(String[] args)
	{
		System.out.println(pValue(2.8E-4, 0.02));
//		System.out.println(pValue(1E-16, 0.4));
	}
}
