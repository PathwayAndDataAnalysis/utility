package org.panda.utility.statistics;

import java.util.*;

/**
 * @author Ozgun Babur
 */
public class Frequency
{
	Map<Comparable, Integer> cnt;

	public Frequency()
	{
		cnt = new HashMap<>();
	}

	public void count(Comparable o)
	{
		if (cnt.containsKey(o)) cnt.put(o, cnt.get(o) + 1);
		else cnt.put(o, 1);
	}

	public void count(double[] v)
	{
		for (double v1 : v)
		{
			count(v1);
		}
	}

	public void print()
	{
		List<Comparable> list = new ArrayList<Comparable>(cnt.keySet());
		Collections.sort(list);
		for (Comparable c : list)
		{
			System.out.println(c + "\t" + cnt.get(c));
		}
	}

}
