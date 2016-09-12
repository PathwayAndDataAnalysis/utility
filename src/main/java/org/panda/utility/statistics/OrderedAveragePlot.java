package org.panda.utility.statistics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Ozgun Babur
 */
public class OrderedAveragePlot
{
	private List<Double> numbers;

	public OrderedAveragePlot()
	{
		this(Collections.emptySet());
	}

	public OrderedAveragePlot(Collection<Double> num)
	{
		numbers = new ArrayList<>(num);
	}

	public OrderedAveragePlot(Collection<Integer> num, boolean dummyParameter)
	{
		numbers = new ArrayList<>();
		for (Integer i : num)
		{
			numbers.add((double) i);
		}
	}

	public void add(double num)
	{
		this.numbers.add(num);
	}

	public void plot(int bins)
	{
		Collections.sort(numbers);
		System.out.println(numbers.size() + " numbers present");
		double range = numbers.size() / (double) bins;
		System.out.println("bin size = " + range);
		for (int i = 0; i < bins; i++)
		{
			List<Double> sub = numbers.subList((int) Math.round(i * range), (int) Math.round((i + 1) * range));
			double av = Summary.meanOfDoubles(sub);
			System.out.println((i + 1) + "\t" + av + "\t" + Math.log(av));
		}
	}
}
