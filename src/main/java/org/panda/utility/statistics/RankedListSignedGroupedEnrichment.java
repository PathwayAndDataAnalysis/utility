package org.panda.utility.statistics;

import org.panda.utility.CollectionUtil;
import org.panda.utility.FileUtil;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class RankedListSignedGroupedEnrichment
{
	public static final double REPORT_RATIO = 0.25;

	public static void reportEnrichment(List<String> rankedList, Map<String, Set<Map<String, Boolean>>> featuredSets,
		int randomIter, String outFile) throws IOException
	{
		Map<String, Double> pvals = calcEnrichmentPVals(rankedList, featuredSets, randomIter);
		Map<String, Double> qVals = FDR.getQVals(pvals, null);

		List<String> setNames = pvals.keySet().stream().sorted(Comparator.comparing(pvals::get)).collect(Collectors.toList());

		BufferedWriter writer = FileUtil.newBufferedWriter(outFile);

		writer.write("Name\tDirection\tP-value\tFDR\tMembers in top " + (int) Math.round(REPORT_RATIO * 100) + "%");

		int rankThr = (int) Math.round(rankedList.size() * REPORT_RATIO);
		Map<String, Integer> ranksMap = getRanks(rankedList);
		int maxRank = rankedList.size() - 1;

		for (String name : setNames)
		{
			Set<Map<String, Boolean>> set = featuredSets.get(name);
			boolean direction = getRankSumLeft(rankedList, set) < getRankSumRight(rankedList, set);

			writer.write("\n" + name + "\t" + (direction ? "+" : "-") + "\t" + pvals.get(name) + "\t" + qVals.get(name) + "\t");

			writer.write(CollectionUtil.merge(getTopMembers(ranksMap, set, direction, maxRank, rankThr), " "));
		}

		writer.close();
	}

	private static Map<String, Integer> getRanks(List<String> rankedList)
	{
		Map<String, Integer> map = new HashMap<>();
		for (int i = 0; i < rankedList.size(); i++)
		{
			map.put(rankedList.get(i), i);
		}
		return map;
	}

	private static List<String> getTopMembers(Map<String, Integer> rankMap, Set<Map<String, Boolean>> set,
		boolean direction, int maxRank, int rankThr)
	{
		rankMap = getSignedRankMap(rankMap, maxRank, set, direction);

		List<String> list = set.stream().map(Map::keySet).flatMap(Collection::stream)
			.sorted(Comparator.comparing(rankMap::get)).collect(Collectors.toList());

		int i = 0;
		while ( i < list.size() && rankMap.get(list.get(i)) <= rankThr) i++;

		return list.subList(0, i);
	}

	private static Map<String, Integer> getSignedRankMap(Map<String, Integer> rankMap, int maxRank,
		Set<Map<String, Boolean>> set, boolean direction)
	{
		Map<String, Integer> sRankMap = new HashMap<>();

		for (Map<String, Boolean> map : set)
		{
			map.forEach((gene, sign) ->
			{
				boolean dir = direction == sign;
				sRankMap.put(gene, dir ? rankMap.get(gene) : maxRank - rankMap.get(gene));
			});
		}

		return sRankMap;
	}

	public static Map<String, Double> calcEnrichmentPVals(List<String> rankedList,
		Map<String, Set<Map<String, Boolean>>> featuredSets, int randomIter)
	{
		Map<String, Integer> sizesMap = featuredSets.keySet().stream().collect(Collectors.toMap(n -> n, n -> featuredSets.get(n).size()));

		RankSelectionNullDistrTwoTailed nd = new RankSelectionNullDistrTwoTailed(rankedList.size(), new HashSet<>(sizesMap.values()), randomIter);

		Map<String, Double> pvals = new HashMap<>();

		for (String name : featuredSets.keySet())
		{
			Set<Map<String, Boolean>> set = featuredSets.get(name);
			pvals.put(name, nd.getPval(sizesMap.get(name),
				Math.min(getRankSumLeft(rankedList, set), getRankSumRight(rankedList, set))));
		}

		return pvals;
	}

	private static int getRankSumLeft(List<String> list, Set<Map<String, Boolean>> set)
	{
		float sum = 0;
		for (Map<String, Boolean> map : set)
		{
			float a = 0;
			for (String ele : map.keySet())
			{
				a += map.get(ele) ? list.indexOf(ele) : list.size() - 1 - list.indexOf(ele);
			}
			sum += a / map.size();
		}
		return Math.round(sum);
	}

	private static int getRankSumRight(List<String> list, Set<Map<String, Boolean>> set)
	{
		float sum = 0;
		for (Map<String, Boolean> map : set)
		{
			float a = 0;
			for (String ele : map.keySet())
			{
				a += !map.get(ele) ? list.indexOf(ele) : list.size() - 1 - list.indexOf(ele);
			}
			sum += a / map.size();
		}
		return Math.round(sum);
	}


	public static void main(String[] args)
	{
	}

}