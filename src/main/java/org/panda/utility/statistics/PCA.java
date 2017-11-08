package org.panda.utility.statistics;

import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.stat.correlation.Covariance;
import org.panda.utility.ArrayUtil;

import java.util.*;

/**
 * Principal Component Analysis
 *
 * @author Ozgun Babur
 */
public class PCA
{
	public static Map<String, double[]> run(Map<String, double[]> rows)
	{
		double[][] m = new double[rows.size()][];
		int index = 0;
		for (String rowName : rows.keySet())
		{
			m[index++] = rows.get(rowName);
		}

		//create real matrix
		RealMatrix realMatrix = MatrixUtils.createRealMatrix(m);

		//create covariance matrix of points, then find eigen vectors
		//see https://stats.stackexchange.com/questions/2691/making-sense-of-principal-component-analysis-eigenvectors-eigenvalues

		Covariance covariance = new Covariance(realMatrix);
		RealMatrix covarianceMatrix = covariance.getCovarianceMatrix();
		EigenDecomposition ed = new EigenDecomposition(covarianceMatrix, 0);

		int cols = m[0].length;
		Map<String, double[]> result = new HashMap<>();
		for (String rowName : rows.keySet())
		{
			double[] vals = rows.get(rowName);
			double[] n = new double[cols];
			for (int i = 0; i < cols; i++)
			{
				n[i] = ArrayUtil.vectorMultiply(vals, ed.getEigenvector(i).toArray());
			}
			result.put(rowName, n);
		}

		return result;
	}



	public static void main(String[] args)
	{
		Map<String, double[]> map = new HashMap<>();
		Random r = new Random();
		map.put("A", new double[]{r.nextDouble(), r.nextDouble(), r.nextDouble(), r.nextDouble(), r.nextDouble() + 10, r.nextDouble() + 10, r.nextDouble() + 10, r.nextDouble() + 10, r.nextDouble() + 10});
		map.put("B", new double[]{r.nextDouble(), r.nextDouble(), r.nextDouble(), r.nextDouble(), r.nextDouble() + 10, r.nextDouble() + 10, r.nextDouble() + 10, r.nextDouble() + 10, r.nextDouble() + 10});
		map.put("C", new double[]{r.nextDouble(), r.nextDouble(), r.nextDouble(), r.nextDouble(), r.nextDouble() + 10, r.nextDouble() + 10, r.nextDouble() + 10, r.nextDouble() + 10, r.nextDouble() + 10});
		map.put("D", new double[]{r.nextDouble() + 10, r.nextDouble() + 10, r.nextDouble() + 10, r.nextDouble() + 10, r.nextDouble() + 10, r.nextDouble(), r.nextDouble(), r.nextDouble(), r.nextDouble()});
		map.put("E", new double[]{r.nextDouble() + 10, r.nextDouble() + 10, r.nextDouble() + 10, r.nextDouble() + 10, r.nextDouble() + 10, r.nextDouble(), r.nextDouble(), r.nextDouble(), r.nextDouble()});
		map.put("F", new double[]{r.nextDouble() + 10, r.nextDouble() + 10, r.nextDouble() + 10, r.nextDouble() + 10, r.nextDouble() + 10, r.nextDouble(), r.nextDouble(), r.nextDouble(), r.nextDouble()});

		Map<String, double[]> result = run(map);
		for (String name : result.keySet())
		{
			double[] ev = result.get(name);
			System.out.println(name + "\t" + ev[0] + "\t" + ev[1]);
		}
	}
}
