package org.panda.utility.statistics;

import org.panda.utility.CollectionUtil;
import org.panda.utility.FileUtil;
import org.panda.utility.Progress;
import org.panda.utility.Tuple;

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

	public static void writeActivityScoreMatrix(Map<String, List<String>> rankedLists,
												Map<String, Map<String, Boolean>> featuredSets,
												int minTarg, int randomIter, String outFile) throws IOException
	{
		Map<String, Map<String, Tuple>> pvalsMap = calcEnrichmentPValsMultiRankedList(rankedLists, featuredSets, minTarg, randomIter);
		printSignificantCellCounts(pvalsMap);

		List<String> setNames = featuredSets.keySet().stream().sorted().collect(Collectors.toList());
		List<String> cellNames = rankedLists.keySet().stream().sorted().collect(Collectors.toList());

		BufferedWriter writer = FileUtil.newBufferedWriter(outFile);

		writer.write("Name");
		cellNames.forEach(name -> FileUtil.tab_write(name, writer));

		Progress prg = new Progress(setNames.size(), "Calculating activity scores");
		for (String setName : setNames)
		{
			writer.write("\n" + setName);

			for (String cellName : cellNames)
			{
				Tuple tup = pvalsMap.get(cellName).get(setName);

				if (tup == null)
				{
					FileUtil.tab_write("0", writer);
				}
				else
				{
					double score = -Math.log(tup.p);
					if (tup.v < 0) score = -score;

					FileUtil.tab_write(score, writer);
				}
			}

			prg.tick();
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

	public static Map<String, Map<String, Tuple>> calcEnrichmentPValsMultiRankedList(
		Map<String, List<String>> rankedLists, Map<String, Map<String, Boolean>> featuredSetsRaw,
		int minTarg, int randomIter)
	{
		Map<String, Map<String, Tuple>> pvalsMap = new HashMap<>();

		Progress prg = new Progress(rankedLists.size(), "Calculating enrichment p-values");
		rankedLists.forEach((cellName, rankedList) ->
		{
			Map<String, Tuple> pvals = new HashMap<>();

			Map<String, Map<String, Boolean>> featuredSets = convertPriors(featuredSetsRaw, rankedList, minTarg);
			Set<Integer> sizes = featuredSets.values().stream().map(Map::size).collect(Collectors.toSet());
			RankSelectionNullDistrTwoTailed nd = new RankSelectionNullDistrTwoTailed(rankedLists.values().iterator().next().size(), sizes, randomIter);

			for (String name : featuredSets.keySet())
			{
				Map<String, Boolean> set = featuredSets.get(name);
				int rankSumLeft = getRankSumLeft(rankedList, set);
				int rankSumRight = getRankSumRight(rankedList, set);
				double v = rankSumLeft < rankSumRight ? 1 : -1;
				pvals.put(name, new Tuple(v, nd.getPval(set.size(), Math.min(rankSumLeft, rankSumRight))));
			}

			pvalsMap.put(cellName, pvals);
			prg.tick();
		});

		return pvalsMap;
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

	private static <T> boolean allRankingsHaveSameGenes(Map<String, List<T>> rankedLists)
	{
		Set<T> set = new HashSet<>(rankedLists.values().iterator().next());
		for (List<T> list : rankedLists.values())
		{
			Set<T> anotherSet = new HashSet<>(list);
			if (!set.equals(anotherSet)) return false;
		}
		return true;
	}

	private static void printSignificantCellCounts(Map<String, Map<String, Tuple>> pvalsMap)
	{
		List<String> cellNames = new ArrayList<>(pvalsMap.keySet());
		List<String> tfNames = pvalsMap.values().stream().map(Map::keySet).flatMap(Collection::stream).distinct().collect(Collectors.toList());
		System.out.println("tfNames.size() = " + tfNames.size());

		Map<String, Integer> sizes = new HashMap<>();

		Progress prg = new Progress(tfNames.size(), "Calculating...");
		for (String tf : tfNames)
		{
			Map<String, Double> pMap = new HashMap<>();
			for (String cell : cellNames)
			{
				Tuple tup = pvalsMap.get(cell).get(tf);
				if (tup != null) pMap.put(cell, tup.p);
			}
			if (!pMap.isEmpty())
			{
				List<String> selected = FDR.select(pMap, null, 0.1);
				sizes.put(tf, selected.size());
			}
			else sizes.put(tf, 0);

			prg.tick();
		}

		tfNames.sort((o1, o2) -> sizes.get(o2).compareTo(sizes.get(o1)));

		tfNames.forEach(tf -> System.out.println(sizes.get(tf) + "\t" + tf));
	}

	private static void printSignificantCellCounts(String transposedFilename)
	{
		Map<String, Map<String, Tuple>> pvalsMap = new HashMap<>();

		String[] header = FileUtil.readHeader(transposedFilename);
		FileUtil.linesTabbedSkip1(transposedFilename).forEach(t ->
		{
			String cell = t[0];
			HashMap<String, Tuple> pMap = new HashMap<>();
			pvalsMap.put(cell, pMap);
			for (int i = 1; i < t.length; i++)
			{
				if (Double.parseDouble(t[i]) != 0)
				{
					double p = Math.pow(Math.E, -Math.abs(Double.parseDouble(t[i])));
					if (p == 1)
					{
						System.out.println();
					}
					pMap.put(header[i], new Tuple(0, p));
				}
			}
		});

		printSignificantCellCounts(pvalsMap);
	}

	public static Map<String, Map<String, Boolean>> convertPriors(Map<String, Map<String, Boolean>> priors, List<String> consider, int minTarg)
	{
		Map<String, Map<String, Boolean>> converted = new HashMap<>();

		for (String tf : priors.keySet())
		{
			Map<String, Boolean> targets = new HashMap<>();
			converted.put(tf, targets);

			for (String target : priors.get(tf).keySet())
			{
				if (consider.contains(target))
				{
					boolean sign = priors.get(tf).get(target);
					targets.put(target, sign);
				}
			}
		}

		Set<String> remove = new HashSet<>();
		converted.keySet().stream().filter(tf -> converted.get(tf).size() < minTarg).forEach(remove::add);
		remove.forEach(converted::remove);

		return converted;
	}


	public static void main(String[] args)
	{
		printSignificantCellCounts("/home/ozgunbabur/Data/Josh/take3/tf_scores_t.tsv");
	}

}