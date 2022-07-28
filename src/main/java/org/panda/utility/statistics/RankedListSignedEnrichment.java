package org.panda.utility.statistics;

import org.panda.utility.CollectionUtil;
import org.panda.utility.FileUtil;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class RankedListSignedEnrichment
{
	public static final double REPORT_RATIO = 0.25;

	public static <T> void reportEnrichment(List<T> rankedList, Map<String, Map<T, Boolean>> featuredSets, int randomIter, String outFile) throws IOException
	{
		if (new HashSet<>(rankedList).size() != rankedList.size()) throw new IllegalArgumentException("rankedList has repeating elements!");

		Map<String, Double> pvals = calcEnrichmentPVals(rankedList, featuredSets, randomIter);
		Map<String, Double> qVals = FDR.getQVals(pvals, null);

		List<String> setNames = pvals.keySet().stream().sorted(Comparator.comparing(pvals::get)).collect(Collectors.toList());

		BufferedWriter writer = FileUtil.newBufferedWriter(outFile);

		writer.write("Name\tDirection\tP-value\tFDR\tMembers in top " + (int) Math.round(REPORT_RATIO * 100) + "%");

		int rankThr = (int) Math.round(rankedList.size() * REPORT_RATIO);
		Map<T, Integer> ranksMap = getRanks(rankedList);
		int maxRank = rankedList.size() - 1;

		for (String name : setNames)
		{
			Map<T, Boolean> set = featuredSets.get(name);
			boolean direction = getRankSumLeft(rankedList, set) < getRankSumRight(rankedList, set);

			writer.write("\n" + name + "\t" + (direction ? "+" : "-") + "\t" + pvals.get(name) + "\t" + qVals.get(name) + "\t");

			writer.write(CollectionUtil.merge(getTopMembers(ranksMap, set, direction, maxRank, rankThr), " "));
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

	private static <T> List<T> getTopMembers(Map<T, Integer> rankMap, Map<T, Boolean> set, boolean direction, int maxRank, int rankThr)
	{
		rankMap = getSignedRankMap(rankMap, maxRank, set, direction);

		List<T> list = new ArrayList<>(set.keySet());
		list.sort(Comparator.comparing(rankMap::get));

		int i = 0;
		while ( i < list.size() && rankMap.get(list.get(i)) <= rankThr) i++;

		return list.subList(0, i);
	}

	private static <T> Map<T, Integer> getSignedRankMap(Map<T, Integer> rankMap, int maxRank, Map<T, Boolean> set, boolean direction)
	{
		Map<T, Integer> sRankMap = new HashMap<>();

		set.forEach((gene, sign) ->
		{
			boolean dir = direction == sign;
			sRankMap.put(gene, dir ? rankMap.get(gene) : maxRank - rankMap.get(gene));
		});

		return sRankMap;
	}

	public static <T> Map<String, Double> calcEnrichmentPVals(List<T> rankedList, Map<String, Map<T, Boolean>> featuredSets, int randomIter)
	{
		Set<Integer> sizes = featuredSets.values().stream().map(Map::size).collect(Collectors.toSet());
		RankSelectionNullDistrTwoTailed nd = new RankSelectionNullDistrTwoTailed(rankedList.size(), sizes, randomIter);

		Map<String, Double> pvals = new HashMap<>();

		for (String name : featuredSets.keySet())
		{
			Map<T, Boolean> set = featuredSets.get(name);
			pvals.put(name,
				nd.getPval(set.size(), Math.min(getRankSumLeft(rankedList, set), getRankSumRight(rankedList, set))));
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