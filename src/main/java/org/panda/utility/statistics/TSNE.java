package org.panda.utility.statistics;

import com.jujutsu.tsne.FastTSne;
import com.jujutsu.tsne.SimpleTSne;
import com.jujutsu.tsne.TSne;
import com.jujutsu.tsne.barneshut.BHTSne;
import com.jujutsu.tsne.barneshut.BarnesHutTSne;
import com.jujutsu.tsne.barneshut.ParallelBHTsne;
import com.jujutsu.tsne.barneshut.TSneConfig;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.Covariance;
import org.panda.utility.ArrayUtil;

import java.util.*;

/**
 * Principal Component Analysis
 *
 * @author Ozgun Babur
 */
public class TSNE
{
	public static Map<String, double[]> run(Map<String, double[]> rows)
	{
		return run(rows, rows.size() / 6);
	}

	public static Map<String, double[]> run(Map<String, double[]> rows, double perplexity)
	{
		return run(rows, perplexity, 0.5,  1000, rows.values().iterator().next().length);
	}

	public static Map<String, double[]> run(Map<String, double[]> rows, double perplexity, double theta, int maxIter,
		int initialDims)
	{
		double[][] m = new double[rows.size()][];
		int index = 0;
		for (String rowName : rows.keySet())
		{
			m[index++] = rows.get(rowName);
		}

		TSne tsne = theta > 0 ? new BHTSne() : new FastTSne();

		m = tsne.tsne(new TSneConfig(
			m, 2, initialDims, perplexity, maxIter, initialDims != m[0].length, theta, true, false));

		Map<String, double[]> result = new TreeMap<>();
		int i = 0;
		for (String rowName : rows.keySet())
		{
			result.put(rowName, m[i++]);
		}

		return result;
	}



	public static void main(String[] args)
	{
		Map<String, double[]> map = new TreeMap<>();
		Random r = new Random();

		Set<String> s1 = new HashSet<>();
		Set<String> s2 = new HashSet<>();

		for (int i = 0; i < 100; i++)
		{
			map.put("A" + i, new double[]{r.nextDouble(), r.nextDouble(), r.nextDouble(), r.nextDouble(), r.nextDouble() + 10, r.nextDouble() + 10, r.nextDouble() + 10, r.nextDouble() + 10, r.nextDouble() + 10});
			s1.add("A" + i);
		}

		for (int i = 0; i < 100; i++)
		{
			map.put("B" + i, new double[]{r.nextDouble() + 10, r.nextDouble() + 10, r.nextDouble() + 10, r.nextDouble() + 10, r.nextDouble() + 10, r.nextDouble(), r.nextDouble(), r.nextDouble(), r.nextDouble()});
			s2.add("B" + i);
		}

//		Map<String, double[]> result = run(map);
//		for (String name : result.keySet())
//		{
//			double[] ev = result.get(name);
//			System.out.println(name + "\t" + ev[0] + "\t" + ev[1]);
//		}

		TSNEPlot.plot("Test", map, s1, s2);
	}
}
