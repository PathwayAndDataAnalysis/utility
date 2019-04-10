package org.panda.utility.statistics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StouffersCombinedProbability
{
	public static double combineZ(double... z)
	{
		return Summary.sum(z) / Math.sqrt(z.length);
	}

	public static double combineP(double... p)
	{
		double[] z = ZScore.percentileToZScore(p);
		double zC = combineZ(z);
		return ZScore.zScoreToPercentile(zC);
	}

	public static double[] combineP2Tailed(double[] p, int[] s)
	{
		double[] pp = new double[p.length];

		for (int i = 0; i < pp.length; i++)
		{
			pp[i] = p[i] / 2;

			if (s[i] > 0) pp[i] = 1 - pp[i];
		}

		double pC = combineP(pp);
		int sC = -1;

		if (pC > 0.5)
		{
			sC = 1;
			pC = 1 - pC;
		}

		double pC2t = pC * 2;

		return new double[]{pC2t, sC};
	}

	public static void main(String[] args)
	{
//		justCheckFewNumbers();

//		double[] r = combineP2Tailed(new double[]{0.01, 0.01}, new int[]{-1, -1});
//		System.out.println(Arrays.toString(r));

		double p = combineP(0.001, 0.2);
		System.out.println("p = " + p);


//		checkUniformity();
	}

	private static void checkUniformity()
	{
		List<Double> list = new ArrayList<>();
		int n = 2;
		double[] p = new double[n];
		for (int i = 0; i < 1000; i++)
		{
			for (int j = 0; j < n; j++)
			{
				p[j] = Math.random();
			}

			double pC = combineP(p);
			list.add(pC);
		}

		UniformityChecker.plot(list);
	}

	public static void justCheckFewNumbers()
	{
		double sabit = 0.05;

		for (double i = 0.01; i < 1; i+=0.01)
		{
			System.out.println(i + "\t" + combineP(sabit, i));
		}
		System.exit(0);
	}
}
