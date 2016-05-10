package org.panda.utility.statistics;

import java.io.*;
import java.util.*;

/**
 * @author Ozgun Babur
 */
public class KaplanMeier
{
	public static void generate(OutputStream out, String[] names, List<double[]> valsList) throws IOException
	{
		if (names.length != valsList.size())
			throw new IllegalArgumentException("Name and valsList sizes should be equal");

		for (double[] vals : valsList)
		{
			Arrays.sort(vals);
		}

		int[] i = new int[names.length];
		double[] y = new double[names.length];

		for (int j = 0; j < i.length; j++)
		{
			i[j] = 0;
			y[j] = 1;
		}

		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));

		writer.write("Time");
		for (String name : names)
		{
			writer.write("\t" + name);
		}

		double x = 0;

		writer.write("\n" + x);
		for (int j = 0; j < i.length; j++)
		{
			writer.write("\t" + y[j]);
		}

		do
		{
			x = min(i, valsList);
			if (x == Double.MAX_VALUE) break;

			writer.write("\n" + x);
			for (int j = 0; j < i.length; j++)
			{
				writer.write("\t" + y[j]);
			}

			if (!advance(i, valsList, x)) break;

			// update ratios
			for (int j = 0; j < i.length; j++)
			{
				y[j] = 1 - (i[j] / (double) valsList.get(j).length);
			}

			writer.write("\n" + x);
			for (int j = 0; j < i.length; j++)
			{
				writer.write("\t" + y[j]);
			}
		}
		while (true);

		writer.close();

	}

	private static boolean advance(int[] i, List<double[]> valsList, double min)
	{
		boolean changed = false;
		for (int j = 0; j < i.length; j++)
		{
			int index = newInd(valsList.get(j), i[j], min);
			if (index != i[j])
			{
				i[j] = index;
				changed = true;
			}
		}
		return changed;
	}

	private static double min(int[] i, List<double[]> valsList)
	{
		double min = Double.MAX_VALUE;

		for (int j = 0; j < i.length; j++)
		{
			if (valsList.get(j).length == i[j]) continue;
			if (valsList.get(j)[i[j]] < min) min = valsList.get(j)[i[j]];
		}

		return min;
	}

	private static int newInd(double[] vals, int currentInd, double currentMin)
	{
		int i = currentInd;
		for (; i < vals.length; i++)
		{
			if (vals[i] > currentMin) break;
		}
		return i;
	}
}
