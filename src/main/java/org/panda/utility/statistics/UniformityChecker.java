package org.panda.utility.statistics;

import java.util.Collections;
import java.util.List;

/**
 * Created by babur on 4/18/16.
 */
public class UniformityChecker
{
	public static void plot(List<Double> vals)
	{
		plot(vals, 0, 1);
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
}
