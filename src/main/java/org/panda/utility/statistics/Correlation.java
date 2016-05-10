package org.panda.utility.statistics;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.panda.utility.ArrayUtil;
import org.panda.utility.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by babur on 4/18/16.
 */
public class Correlation
{
	public static double pearsonVal(double[] v1, double[] v2)
	{
		if (v1.length != v2.length) throw new IllegalArgumentException("Arrays have to be same length.");
		if (v1.length < 2) return 0;

		PearsonsCorrelation pc = new PearsonsCorrelation();
		double c = pc.correlation(v1, v2);
		return Double.isNaN(c) ? 0 : c;
	}

	public static double pearsonPval(double[] v1, double[] v2)
	{
		if (v1.length != v2.length) throw new IllegalArgumentException("Arrays have to be same length.");
		if (v1.length < 3) return 1;

		PearsonsCorrelation pc = new PearsonsCorrelation(transform(v1, v2));
		return pc.getCorrelationPValues().getColumn(0)[1];
	}

	public static Tuple pearson(double[] v1, double[] v2)
	{
		return new Tuple(pearsonVal(v1, v2), pearsonPval(v1, v2));
	}

	public static double[][] transform(double[] v1, double[] v2)
	{
		double[][] d = new double[v1.length][2];
		for (int i = 0; i < v1.length; i++)
		{
			d[i][0] = v1[i];
			d[i][1] = v2[i];
		}
		return d;
	}

	public static double spearman(double[] v1, double[] v2)
	{
		if (v1.length != v2.length) throw new IllegalArgumentException("Arrays have to be same length.");

		SpearmansCorrelation pc = new SpearmansCorrelation();
		return pc.correlation(v1, v2);
	}

	public static void main(String[] args)
	{
		checkExceptions();
		Random r = new Random();
		List<Double> pvals = new ArrayList<>();

		for (int i = 0; i < 1000; i++)
		{
			List<List<Double>> list = new ArrayList<>();
			for (int j = 0; j < 2; j++)
			{
				list.add(new ArrayList<>());
			}

			for (int j = 0; j < 100; j++)
			{
				list.get(0).add(r.nextDouble());
				list.get(1).add(r.nextDouble());
			}

			List<double[]> groups = new ArrayList<>();
			for (List<Double> doubles : list)
			{
				groups.add(ArrayUtil.toArray(doubles));
			}
			double p = pearsonPval(groups.get(0), groups.get(1));
			pvals.add(p);
		}

		UniformityChecker.plot(pvals);
	}

	private static void checkExceptions()
	{
		System.out.println("v = " + pearsonVal(new double[]{0, 1}, new double[]{3, 4}));
	}
}
