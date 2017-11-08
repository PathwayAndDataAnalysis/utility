package org.panda.utility.statistics;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

/**
 * Prints a table that can be used to generate a plot for visualizing how the given p-values are different than a
 * uniform distribution. The method is more accurate when there are a lot of p-values.
 */
public class UniformityChecker
{
	public static void plot(List<Double> vals)
	{
		plot(vals, 0, 1);
	}

	public static void plot(List<Double> vals, String filename) throws IOException
	{
		plot(vals, 0, 1, filename);
	}

	public static void plot(List<Double> vals, double min, double max)
	{
		Collections.sort(vals);
		System.out.println("Expected\tExpected\tObserved");
		double dif = max - min;

		for (int i = 0; i < vals.size(); i++)
		{
			double expected = min + (dif * i / (double) vals.size());
			System.out.println(expected + "\t" + expected + "\t" + vals.get(i));
		}
	}

	public static void plot(List<Double> vals, double min, double max, String filename) throws IOException
	{
		BufferedWriter writer = Files.newBufferedWriter(Paths.get(filename));
		Collections.sort(vals);
		writer.write("Expected\tExpected\tObserved");
		double dif = max - min;

		for (int i = 0; i < vals.size(); i++)
		{
			double expected = min + (dif * i / (double) vals.size());
			writer.write("\n" + expected + "\t" + expected + "\t" + vals.get(i));
		}
		writer.close();
	}
}
