package org.panda.utility;

import org.panda.utility.statistics.Histogram;

import java.text.DecimalFormat;
import java.util.*;

/**
 * This class is used for discovering the structure of a collection of terms in large datasets.
 * It simply counts the added terms and displays terms and their counts when asked.
 * @author Ozgun Babur
 *         Date: Apr 10, 2008
 *         Time: 12:01:16 PM
 */
public class TermCounter
{
	private Map<String, Item> termMap;
	private int samples;
	private Map<String, Integer> globalCounts;

	public TermCounter()
	{
		termMap = new HashMap<String, Item>();
		samples = 0;
	}

	public void setGlobalCounts(Map<String, Integer> globalCounts)
	{
		this.globalCounts = globalCounts;
	}

	public void addTerm(String term)
	{
		if (!termMap.containsKey(term))
		{
			termMap.put(term, new Item(term));
		}

		termMap.get(term).increment();
		samples++;
	}

	public boolean contains(String term)
	{
		return termMap.containsKey(term);
	}

	public Map<String, Integer> getTermCounts()
	{
		Map<String, Integer> map = new HashMap<String, Integer>();
		for (String key : termMap.keySet())
		{
			Item item = termMap.get(key);

			map.put(item.term, item.count);
		}
		return map;
	}

	public List<String> getMostFrequentTerms(int minCount)
	{
		List<String> terms = new ArrayList<String>();
		List<Item> items = new ArrayList<Item>();

		for (Item item : termMap.values())
		{
			if (item.count >= minCount) items.add(item);
		}
		Collections.sort(items);
		for (Item item : items)
		{
			terms.add(item.term);
		}
		return terms;
	}

	public String getMostFrequentTerm()
	{
		List<Item> items = new ArrayList<Item>(termMap.values());
		Collections.sort(items);
		if (items.isEmpty()) return null;
		else return items.get(0).term;
	}

	public String toString()
	{
		return toString(-1);
	}

	private static final DecimalFormat fmt = new DecimalFormat("0.000");

	public String toString(int minCnt)
	{
		String s = "Total terms = " + termMap.size() + ", samples = " + samples + "\n";
		Item[] items = termMap.values().toArray(new Item[termMap.values().size()]);
		Arrays.sort(items);

		for (Item item : items)
		{
			if (item.count < minCnt) break;

			s += item.toString();

			if (globalCounts != null && globalCounts.containsKey(item.term))
			{
				s += "\t" + fmt.format(item.count / (double) globalCounts.get(item.term));
			}

			s += "\n";
		}

		return s;
	}

	public void printFreqDistr()
	{
		Histogram h = new Histogram(1);

		for (Item item : termMap.values())
		{
			h.count(item.count);
		}

		h.printDensity();
	}

	public void print()
	{
		System.out.println(this);
	}

	public void print(int minCnt)
	{
		System.out.println(this.toString(minCnt));
	}

	private class Item implements Comparable
	{
		String term;
		Integer count;

		private Item(String term)
		{
			this.term = term;
			this.count = 0;
		}

		public int compareTo(Object o)
		{
			if (!(o instanceof Item)) return 0;

			Item other = (Item) o;
			return other.count.compareTo(count);
		}

		void increment()
		{
			count = count + 1;
		}

		public String toString()
		{
//			return count + (count >= 1000 ? "\t" : "\t\t") + term;
			return count + "\t" + term;
		}
	}

	public Map<String, Integer> getCountMap()
	{
		Map<String, Integer> map = new HashMap<>();
		for (Item item : termMap.values())
		{
			map.put(item.term, item.count);
		}
		return map;
	}
}
