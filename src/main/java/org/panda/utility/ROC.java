package org.panda.utility;

import java.io.PrintStream;
import java.util.List;
import java.util.Set;

public class ROC
{
	public static double getAUC(List<String> results, Set<String> relevant)
	{
		double[][] plot = getPlot(results, relevant);

		double area = 0;
		int sideSteps = 0;

		for (int i = 1; i < plot[0].length; i++)
		{
			if (plot[0][i] != plot[0][i-1])
			{
				area += plot[1][i];
				sideSteps++;
			}
		}

		// Normalize total area to 1
		area /= sideSteps;

		return area;
	}

	public static void printPlotForGoogleSheets(Set<String> relevant, PrintStream out, List<String>... results)
	{
		String delim = "\t";
		for (List<String> result : results)
		{
			printPlot(result, relevant, delim, out);
			delim += "\t";
		}
		out.println(0 + delim + 0);
		out.println(1 + delim + 1);
	}

	public static void printPlot(List<String> results, Set<String> relevant, PrintStream out)
	{
		printPlot(results, relevant, "\t", out);
	}

	public static void printPlot(List<String> results, Set<String> relevant, String delim, PrintStream out)
	{
		double[][] plot = getPlot(results, relevant);

		for (int i = 0; i < plot[0].length; i++)
		{
			out.println(plot[0][i] + delim + plot[1][i]);
		}
	}

	public static double[][] getPlot(List<String> results, Set<String> relevant)
	{
		double totalRelevant = CollectionUtil.countOverlap(results, relevant);
		double totalIrrelevant = results.size() - totalRelevant;

		double[][] r = new double[2][];
		r[0] = new double[results.size() + 1];
		r[1] = new double[results.size() + 1];

		r[0][0] = 0;
		r[1][0] = 0;

		int cntRel = 0;
		int cntIrr = 0;

		for (int i = 1; i <= results.size(); i++)
		{
			if (relevant.contains(results.get(i - 1))) cntRel++;
			else cntIrr++;

			r[0][i] = cntIrr / totalIrrelevant;
			r[1][i] = cntRel / totalRelevant;
		}

		return r;
	}
}
