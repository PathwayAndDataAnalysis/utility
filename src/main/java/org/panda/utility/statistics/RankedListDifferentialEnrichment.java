package org.panda.utility.statistics;

import org.panda.utility.CollectionUtil;
import org.panda.utility.FileUtil;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class RankedListDifferentialEnrichment
{
	public static final double REPORT_RATIO = 0.25;

	public static <T> void reportEnrichment(List<T> rankedList1, List<T> rankedList2, Map<String, Set<T>> featuredSets, int randomIter, String outFile) throws IOException
	{
		if (rankedList1.size() != rankedList2.size()) throw new IllegalArgumentException("Two ranked lists are not the same size!");
		if (new HashSet<>(rankedList1).size() != rankedList1.size()) throw new IllegalArgumentException("List 1 has repeating elements!");
		if (new HashSet<>(rankedList2).size() != rankedList2.size()) throw new IllegalArgumentException("List 2 has repeating elements!");

		Map<String, Double> pvals = calcEnrichmentPVals(rankedList1, rankedList2, featuredSets, randomIter);
		Map<String, Double> qVals = FDR.getQVals(pvals, null);

		List<String> setNames = pvals.keySet().stream().sorted(Comparator.comparing(pvals::get)).collect(Collectors.toList());

		BufferedWriter writer = FileUtil.newBufferedWriter(outFile);

		writer.write("Name\tDirection\tP-value\tFDR\tRank changers more than " + (int) Math.round(REPORT_RATIO * 100) + "%");

		int rankThr = (int) Math.round(rankedList1.size() * REPORT_RATIO);
		Map<T, Integer> rankDiffsMap = getRankDiffs(rankedList1,rankedList2);

		for (String name : setNames)
		{
			boolean direction = getRankSumDiff(rankedList1, rankedList2, featuredSets.get(name)) < 0;

			writer.write("\n" + name + "\t" + (direction ? "+" : "-") + "\t" + pvals.get(name) + "\t" + qVals.get(name) + "\t");
			writer.write(CollectionUtil.merge(getTopMembers(rankDiffsMap, featuredSets.get(name), direction, rankThr), " "));
		}

		writer.close();
	}

	private static <T> Map<T, Integer> getRankDiffs(List<T> rankedList1, List<T> rankedList2)
	{
		Map<T, Integer> map = new HashMap<>();
		for (int i = 0; i < rankedList1.size(); i++)
		{
			T ele = rankedList1.get(i);
			int rank2 = rankedList2.indexOf(ele);
			map.put(ele, i - rank2);
		}
		return map;
	}

	private static <T> List<T> getTopMembers(Map<T, Integer> rankDiffMap, Set<T> set, boolean direction, int minJump)
	{
		List<T> list = new ArrayList<>(set);
		list.sort(Comparator.comparing(rankDiffMap::get));

		if (direction) // we are interested in decreased ranks
		{
			int i = 0;
			while ( i < list.size() && rankDiffMap.get(list.get(i)) <= -minJump) i++;
			return list.subList(0, i);
		}
		else // we are interested in increased ranks
		{
			int i = list.size() - 1;
			while ( i >= 0 && rankDiffMap.get(list.get(i)) >= minJump) i--;
			return list.subList(i+1, list.size());
		}
	}

	public static <T> Map<String, Double> calcEnrichmentPVals(List<T> rankedList1, List<T> rankedList2, Map<String, Set<T>> featuredSets, int randomIter)
	{
		Set<Integer> sizes = featuredSets.values().stream().map(Set::size).collect(Collectors.toSet());
		RankDifferenceNullDistr nd = new RankDifferenceNullDistr(rankedList1.size(), sizes, randomIter);

		Map<String, Double> pvals = new HashMap<>();

		for (String name : featuredSets.keySet())
		{
			Set<T> set = featuredSets.get(name);
			pvals.put(name, nd.getPval(set.size(), Math.abs(getRankSumDiff(rankedList1, rankedList2, set))));
		}

		return pvals;
	}

	private static <T> int getRankSumDiff(List<T> list1, List<T> list2, Set<T> set)
	{
		int sum1 = 0;
		int sum2 = 0;

		for (T ele : set)
		{
			sum1 += list1.indexOf(ele);
			sum2 += list2.indexOf(ele);
		}
		return sum1 - sum2;
	}


	public static void main(String[] args)
	{
	}

}