package org.panda.utility.statistics;

import org.panda.utility.CollectionUtil;
import org.panda.utility.FileUtil;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class RankedListSignedGroupedDifferentialEnrichment
{
	public static final double REPORT_RATIO = 0.25;

	public static void reportEnrichment(List<String> rankedList1, List<String> rankedList2, Map<String, Set<Map<String, Boolean>>> featuredSets,
		int randomIter, String outFile) throws IOException
	{
		if (rankedList1.size() != rankedList2.size()) throw new IllegalArgumentException("Two ranked lists are not the same size!");
		if (new HashSet<>(rankedList1).size() != rankedList1.size()) throw new IllegalArgumentException("List 1 has repeating elements!");
		if (new HashSet<>(rankedList2).size() != rankedList2.size()) throw new IllegalArgumentException("List 2 has repeating elements!");
		HashMap<String, Boolean> emptyMap = new HashMap<>();
		for (Set<Map<String, Boolean>> set : featuredSets.values())
		{
			if (set.contains(emptyMap)) throw new IllegalArgumentException("featuresSets has a set with an empty map!");
		}

		Map<String, Double> pvals = calcEnrichmentPVals(rankedList1, rankedList2, featuredSets, randomIter);
		Map<String, Double> qVals = FDR.getQVals(pvals, null);

		List<String> setNames = pvals.keySet().stream().sorted(Comparator.comparing(pvals::get)).collect(Collectors.toList());

		BufferedWriter writer = FileUtil.newBufferedWriter(outFile);

		writer.write("Name\tDirection\tP-value\tFDR\tMembers in top " + (int) Math.round(REPORT_RATIO * 100) + "%");

		int rankThr = (int) Math.round(rankedList1.size() * REPORT_RATIO);
		Map<String, Integer> ranksDiffsMap = getRankDiffs(rankedList1, rankedList2);

		for (String name : setNames)
		{
			Set<Map<String, Boolean>> set = featuredSets.get(name);
			boolean direction = getRankSumLeft(rankedList1, set) < getRankSumLeft(rankedList2, set);

			writer.write("\n" + name + "\t" + (direction ? "+" : "-") + "\t" + pvals.get(name) + "\t" + qVals.get(name) + "\t");

			writer.write(CollectionUtil.merge(getTopMembers(ranksDiffsMap, set, direction, rankThr), " "));
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

	private static List<String> getTopMembers(Map<String, Integer> rankDiffMap, Set<Map<String, Boolean>> set,
		boolean direction, int rankThr)
	{
		rankDiffMap = getSignedRankDiffMap(rankDiffMap, set, direction);

		List<String> list = set.stream().map(Map::keySet).flatMap(Collection::stream)
			.sorted(Comparator.comparing(rankDiffMap::get)).collect(Collectors.toList());

		int i = 0;
		while ( i < list.size() && rankDiffMap.get(list.get(i)) <= rankThr) i++;

		return list.subList(0, i);
	}

	private static Map<String, Integer> getSignedRankDiffMap(Map<String, Integer> rankDiffMap,
		Set<Map<String, Boolean>> set, boolean direction)
	{
		Map<String, Integer> sRankMap = new HashMap<>();

		for (Map<String, Boolean> map : set)
		{
			map.forEach((gene, sign) ->
			{
				boolean dir = direction == sign;
				sRankMap.put(gene, dir ? rankDiffMap.get(gene) : -rankDiffMap.get(gene));
			});
		}

		return sRankMap;
	}

	public static Map<String, Double> calcEnrichmentPVals(List<String> rankedList1, List<String> rankedList2,
		Map<String, Set<Map<String, Boolean>>> featuredSets, int randomIter)
	{
		Map<String, Integer> sizesMap = featuredSets.keySet().stream().collect(Collectors.toMap(n -> n, n -> featuredSets.get(n).size()));

		RankDifferenceNullDistr nd = new RankDifferenceNullDistr(rankedList1.size(), new HashSet<>(sizesMap.values()), randomIter);

		Map<String, Double> pvals = new HashMap<>();

		for (String name : featuredSets.keySet())
		{
			Set<Map<String, Boolean>> set = featuredSets.get(name);
			pvals.put(name, nd.getPval(sizesMap.get(name),
				Math.abs(getRankSumLeft(rankedList1, set) - getRankSumLeft(rankedList2, set))));
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

	public static void main(String[] args) throws IOException
	{
		List<String> list1 = new ArrayList<>();
		for (int i = 0; i < 1000; i++)
		{
			list1.add("i-" + i);
		}

		List<String> list2 = new ArrayList<>(list1);
		Collections.shuffle(list2);

		Map<String, Set<Map<String, Boolean>>> featuredSets = new HashMap<>();
		Set<Map<String, Boolean>> set1 = new HashSet<>();
		Map<String, Boolean> m1 = new HashMap<>();
		m1.put("i-" + 0, true);
		m1.put("i-" + 1, true);
		m1.put("i-" + 2, true);
		m1.put("i-" + 3, true);
		m1.put("i-" + 999, false);
		m1.put("i-" + 998, false);
		Map<String, Boolean> m2 = new HashMap<>();
		m2.put("i-" + 4, true);
		m2.put("i-" + 5, true);
		m2.put("i-" + 997, false);
		m2.put("i-" + 996, false);
		m2.put("i-" + 995, false);
		m2.put("i-" + 994, false);
		Map<String, Boolean> m3 = new HashMap<>();
		m3.put("i-" + 6, true);
		Map<String, Boolean> m4 = new HashMap<>();
		m4.put("i-" + 7, true);
		m4.put("i-" + 993, false);
		set1.add(m1);
		set1.add(m2);
		set1.add(m3);
		set1.add(m4);

		Set<Map<String, Boolean>> set2 = new HashSet<>();
		for (Map<String, Boolean> map : set1)
		{
			Map<String, Boolean> map2 = new HashMap<>();
			map.forEach((s, b) -> map2.put(s, !b));
			set2.add(map2);
		}

		featuredSets.put("First", set1);
		featuredSets.put("Second", set2);

		reportEnrichment(list1, list2, featuredSets, 1000000, "/home/ozgunbabur/Documents/Temp/temp.tsv");
	}

}