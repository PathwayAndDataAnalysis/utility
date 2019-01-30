package org.panda.utility;

import org.panda.utility.statistics.FishersCombinedProbability;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class to represent value - p-value pairs. For instance it can be correlation - correlation-p-value tuple.
 *
 * Created by babur on 4/25/16.
 */
public class Tuple
{
	/**
	 * The value.
	 */
	public double v;

	/**
	 * The p-value
	 */
	public double p;

	public static final Tuple NaN = new Tuple();

	public Tuple()
	{
		this.v = Double.NaN;
		this.p = Double.NaN;
	}

	public Tuple(double v, double p)
	{
		this.v = v;
		this.p = p;
	}

	@Override
	public String toString()
	{
		return "v = " + v + ", p = " + p;
	}

	public boolean isNaN()
	{
		return Double.isNaN(p);
	}

	public Tuple getCombined(Tuple... tup)
	{
		double min = 2;
		double v = 0;

		List<Tuple> list = new ArrayList<>(tup.length + 1);
		if (!isNaN()) list.add(this);
		Arrays.stream(tup).filter(t -> !t.isNaN()).forEach(list::add);

		if (list.isEmpty()) return new Tuple();

		for (int i = 0; i < list.size(); i++)
		{
			if (min > list.get(i).p)
			{
				min = list.get(i).p;
				v = list.get(i).v;
			}
		}

		if (list.size() == 1) return list.get(0);

		double[] pp = new double[list.size()];
		for (int i = 0; i < pp.length; i++)
		{
			pp[i] = list.get(i).p;
		}

		return new Tuple(v, FishersCombinedProbability.combine(pp));
	}
}
