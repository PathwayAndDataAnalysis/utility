package org.panda.utility.statistics;

import org.apache.commons.math3.stat.inference.OneWayAnova;
import org.panda.utility.ArrayUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * Created by babur on 4/18/16.
 */
public class Anova
{
	public static double oneWayPval(Collection<double[]> groups)
	{
		if (groups.size() < 2) return 1;
		OneWayAnova owa = new OneWayAnova();
		return owa.anovaPValue(groups);
	}

	public static double oneWayPval(int[] categ, double[] vals)
	{
		return oneWayPval(ArrayUtil.separateToCategories(categ, vals));
	}

	public static void main(String[] args)
	{
		Random r = new Random();
		List<Double> pvals = new ArrayList<>();

		for (int i = 0; i < 1000; i++)
		{
			List<List<Double>> list = new ArrayList<>();
			for (int j = 0; j < 5; j++)
			{
				list.add(new ArrayList<>());
			}

			for (int j = 0; j < 100; j++)
			{
				list.get(r.nextInt(list.size())).add(r.nextGaussian());
			}

			List<double[]> groups = new ArrayList<>();
			for (List<Double> doubles : list)
			{
				groups.add(ArrayUtil.toArray(doubles));
			}
			double p = oneWayPval(groups);
			pvals.add(p);
		}

		UniformityChecker.plot(pvals);
	}
}
