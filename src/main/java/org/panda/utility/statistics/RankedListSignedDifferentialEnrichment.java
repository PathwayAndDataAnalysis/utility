package org.panda.utility.statistics;

import org.panda.utility.CollectionUtil;
import org.panda.utility.FileUtil;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class RankedListSignedDifferentialEnrichment
{
	public static final double REPORT_RATIO = 0.25;

	public static <T> void reportEnrichment(List<T> rankedList1, List<T> rankedList2, Map<String, Map<T, Boolean>> featuredSets, int randomIter, String outFile) throws IOException
	{
		if (rankedList1.size() != rankedList2.size()) throw new IllegalArgumentException("Two ranked lists are not the same size!");
		if (new HashSet<>(rankedList1).size() != rankedList1.size()) throw new IllegalArgumentException("List 1 has repeating elements!");
		if (new HashSet<>(rankedList2).size() != rankedList2.size()) throw new IllegalArgumentException("List 2 has repeating elements!");

		Map<String, Double> pvals = calcEnrichmentPVals(rankedList1, rankedList2, featuredSets, randomIter);
		Map<String, Double> qVals = FDR.getQVals(pvals, null);

		List<String> setNames = pvals.keySet().stream().sorted(Comparator.comparing(pvals::get)).collect(Collectors.toList());

		BufferedWriter writer = FileUtil.newBufferedWriter(outFile);

		writer.write("Name\tDirection\tP-value\tFDR\tRank changed more than " + (int) Math.round(REPORT_RATIO * 100) + "%");

		int rankThr = (int) Math.round(rankedList1.size() * REPORT_RATIO);
		Map<T, Integer> rankDiffsMap = getRankDiffs(rankedList1, rankedList2);

		for (String name : setNames)
		{
			Map<T, Boolean> set = featuredSets.get(name);
			boolean direction = getRankSumLeft(rankedList1, set) < getRankSumLeft(rankedList2, set);

			writer.write("\n" + name + "\t" + (direction ? "+" : "-") + "\t" + pvals.get(name) + "\t" + qVals.get(name) + "\t");

			writer.write(CollectionUtil.merge(getTopMembers(rankDiffsMap, set, direction, rankThr), " "));
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

	private static <T> List<T> getTopMembers(Map<T, Integer> rankDiffsMap, Map<T, Boolean> set, boolean direction, int rankThr)
	{
		rankDiffsMap = getSignedRankDiffMap(rankDiffsMap, set, direction);

		List<T> list = new ArrayList<>(set.keySet());
		list.sort(Comparator.comparing(rankDiffsMap::get));

		int i = 0;
		while ( i < list.size() && rankDiffsMap.get(list.get(i)) <= rankThr) i++;

		return list.subList(0, i);
	}

	private static <T> Map<T, Integer> getSignedRankDiffMap(Map<T, Integer> rankDiffMap, Map<T, Boolean> set, boolean direction)
	{
		Map<T, Integer> sRankMap = new HashMap<>();

		set.forEach((gene, sign) ->
		{
			boolean dir = direction == sign;
			sRankMap.put(gene, dir ? rankDiffMap.get(gene) : -rankDiffMap.get(gene));
		});

		return sRankMap;
	}

	public static <T> Map<String, Double> calcEnrichmentPVals(List<T> rankedList1, List<T> rankedList2, Map<String, Map<T, Boolean>> featuredSets, int randomIter)
	{
		Set<Integer> sizes = featuredSets.values().stream().map(Map::size).collect(Collectors.toSet());
		RankDifferenceNullDistr nd = new RankDifferenceNullDistr(rankedList1.size(), sizes, randomIter);

		Map<String, Double> pvals = new HashMap<>();

		for (String name : featuredSets.keySet())
		{
			Map<T, Boolean> set = featuredSets.get(name);
			pvals.put(name,
				nd.getPval(set.size(), Math.abs(getRankSumLeft(rankedList1, set) - getRankSumLeft(rankedList2, set))));
		}

		return pvals;
	}

	private static <T> int getRankSumLeft(List<T> list, Map<T, Boolean> set)
	{
		int sum = 0;
		for (T ele : set.keySet())
		{
			sum += set.get(ele) ? list.indexOf(ele) : list.size() - 1 - list.indexOf(ele);
		}
		return sum;
	}

	private static <T> int getRankSumRight(List<T> list, Map<T, Boolean> set)
	{
		int sum = 0;
		for (T ele : set.keySet())
		{
			sum += !set.get(ele) ? list.indexOf(ele) : list.size() - 1 - list.indexOf(ele);
		}
		return sum;
	}


	public static void main(String[] args)
	{
	}

}