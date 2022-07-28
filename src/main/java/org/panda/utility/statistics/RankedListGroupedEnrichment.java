package org.panda.utility.statistics;

import org.panda.utility.CollectionUtil;
import org.panda.utility.FileUtil;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class RankedListGroupedEnrichment
{
	public static final double REPORT_RATIO = 0.25;

	public static <T> void reportEnrichment(List<T> rankedList, Map<String, Set<Set<T>>> featuredSets, int randomIter, String outFile) throws IOException
	{
		if (new HashSet<>(rankedList).size() != rankedList.size()) throw new IllegalArgumentException("rankedList has repeating elements!");

		Map<String, Double> pvals = calcEnrichmentPVals(rankedList, featuredSets, randomIter);
		Map<String, Double> qVals = FDR.getQVals(pvals, null);

		List<String> setNames = pvals.keySet().stream().sorted(Comparator.comparing(pvals::get)).collect(Collectors.toList());

		BufferedWriter writer = FileUtil.newBufferedWriter(outFile);

		writer.write("Name\tP-value\tFDR\tMembers in top " + (int) Math.round(REPORT_RATIO * 100) + "%");

		int rankThr = (int) Math.round(rankedList.size() * REPORT_RATIO);
		Map<T, Integer> ranksMap = getRanks(rankedList);

		for (String name : setNames)
		{
			writer.write("\n" + name + "\t" + pvals.get(name) + "\t" + qVals.get(name) + "\t");

			writer.write(CollectionUtil.merge(getTopMembers(ranksMap, featuredSets.get(name), rankThr), " "));
		}

		writer.close();
	}

	private static <T> Map<T, Integer> getRanks(List<T> rankedList)
	{
		Map<T, Integer> map = new HashMap<>();
		for (int i = 0; i < rankedList.size(); i++)
		{
			map.put(rankedList.get(i), i);
		}
		return map;
	}

	private static <T> List<T> getTopMembers(Map<T, Integer> rankMap, Set<Set<T>> set, int rankThr)
	{
		List<T> list = new ArrayList<>();
		for (Set<T> group : set)
		{
			list.addAll(group);
		}

		list.sort(Comparator.comparing(rankMap::get));

		int i = 0;
		while ( i < list.size() && rankMap.get(list.get(i)) <= rankThr) i++;

		return list.subList(0, i);
	}

	public static <T> Map<String, Double> calcEnrichmentPVals(List<T> rankedList, Map<String, Set<Set<T>>> featuredSets, int randomIter)
	{
		Set<Integer> sizes = featuredSets.values().stream().map(Set::size).collect(Collectors.toSet());
		RankSelectionNullDistrOneTailed nd = new RankSelectionNullDistrOneTailed(rankedList.size(), sizes, randomIter);

		Map<String, Double> pvals = new HashMap<>();

		for (String name : featuredSets.keySet())
		{
			Set<Set<T>> set = featuredSets.get(name);
			pvals.put(name, nd.getPval(set.size(), getRankSum(rankedList, set)));
		}

		return pvals;
	}

	private static <T> int getRankSum(List<T> list, Set<Set<T>> set)
	{
		double sum = 0;
		for (Set<T> group : set)
		{
			double grpSum = 0;
			for (T ele : group)
			{
				grpSum += list.indexOf(ele);
			}
			sum += grpSum / group.size();
		}
		return (int) Math.round(sum);
	}


	public static void main(String[] args)
	{
	}

}