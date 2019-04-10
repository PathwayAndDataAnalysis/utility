package org.panda.utility.statistics;

import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class KolmogorovSmirnov
{
	/**
	 * Checks if the given positions on the genome are distributed uniformly between the given range.
	 * @param positions
	 * @param min inclusive
	 * @param max inclusive
	 * @return p-value
	 */
	public static double testUniformityOfLocations(List<Integer> positions, int min, int max)
	{
		UniformRealDistribution dist = new UniformRealDistribution(min, max + 1);
		double[] p = new double[positions.size()];
		int i = 0;
		for (Integer pos : positions)
		{
			p[i++] = pos;
		}

		KolmogorovSmirnovTest test = new KolmogorovSmirnovTest();
		return test.kolmogorovSmirnovTest(dist, p);
	}

	public static void main(String[] args)
	{
		List<Double> pvals = new ArrayList<>();
		Random r = new Random();

		for (int i = 0; i < 10000; i++)
		{
			List<Integer> pos = new ArrayList<>();

			for (int j = 0; j < 20; j++)
			{
				pos.add(r.nextInt(100));
			}

			double p = testUniformityOfLocations(pos, pos.stream().min(Integer::compareTo).get(), pos.stream().max(Integer::compareTo).get());
			pvals.add(p);
		}

		UniformityChecker.plot(pvals);
	}
}
