package org.panda.utility.statistics;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.panda.utility.CollectionUtil;
import org.panda.utility.FileUtil;
import org.panda.utility.Progress;
import org.panda.utility.Tuple;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class RankedListSignedEnrichmentAnalytical
{
	public static final double REPORT_RATIO = 0.25;

	public static <T> void reportEnrichment(List<T> rankedList, Map<String, Map<T, Boolean>> featuredSets, String outFile) throws IOException
	{
		if (new HashSet<>(rankedList).size() != rankedList.size()) throw new IllegalArgumentException("rankedList has repeating elements!");

		Map<String, Tuple> pvalsAndDirs = calcEnrichmentPValsAndDirections(rankedList, featuredSets);
		Map<String, Double> pvals = pvalsAndDirs.keySet().stream().collect(Collectors.toMap(s -> s, s -> pvalsAndDirs.get(s).p));
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
			boolean direction = pvalsAndDirs.get(name).v > 0;

			writer.write("\n" + name + "\t" + (direction ? "+" : "-") + "\t" + pvals.get(name) + "\t" + qVals.get(name) + "\t");

			writer.write(CollectionUtil.merge(getTopMembers(ranksMap, set, direction, maxRank, rankThr), " "));
		}

		writer.close();
	}

	public static void writeActivityScoreMatrix(Map<String, List<String>> rankedLists,
												Map<String, Map<String, Boolean>> featuredSets,
												int minTarg, String outFile) throws IOException
	{
		Map<String, Map<String, Tuple>> pvalsMap = calcEnrichmentPValsMultiRankedList(rankedLists, featuredSets, minTarg);

		List<String> setNames = featuredSets.keySet().stream().sorted().collect(Collectors.toList());
		List<String> cellNames = rankedLists.keySet().stream().sorted().collect(Collectors.toList());

		BufferedWriter writer = FileUtil.newBufferedWriter(outFile);

		writer.write("Name");
		cellNames.forEach(name -> FileUtil.tab_write(name, writer));

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

	public static <T> Map<String, Tuple> calcEnrichmentPValsAndDirections(List<T> rankedList, Map<String, Map<T, Boolean>> featuredSets)
	{
		Map<String, Tuple> pvals = new HashMap<>();
		Map<T, Double> nranks = getNormalizedRanks(rankedList);
		int n = nranks.size();

		for (String name : featuredSets.keySet())
		{
			Map<T, Boolean> set = featuredSets.get(name);
			double[] meanRankAndSize = getMeanRankAndSize(nranks, set);
			double mr = meanRankAndSize[0];

			int dir = mr <= 0.5 ? 1 : -1;
			if (mr > 0.5) mr = 1 - mr;

			double k = meanRankAndSize[1];

			double sd = Math.sqrt(((n+1) * (n-k)) / (12 * k)) / n;

			NormalDistribution ndist = new NormalDistribution(0.5, sd);

			// Two-tailed test, we search for both activation and inactivation
			double pval = 2 * ndist.cumulativeProbability(mr);
			pvals.put(name, new Tuple(dir, pval));
		}

		return pvals;
	}

	public static <T> Map<T, Double> getNormalizedRanks(List<T> rankedlist)
	{
		Map<T, Double> nranks = new HashMap<>();
		int listSize = rankedlist.size();
		for (int i = 0; i < listSize; i++)
		{
			nranks.put(rankedlist.get(i), (i + 0.5) / listSize);
		}
		return nranks;
	}

	public static <T> double[] getMeanRankAndSize(Map<T, Double> nranks, Map<T, Boolean> featuredSet)
	{
		double mr = 0;
		int size = 0;

		for (T item : featuredSet.keySet())
		{
			if (nranks.containsKey(item))
			{
				boolean up = featuredSet.get(item);
				double rank = nranks.get(item);
				mr += up ? rank : 1 - rank;
				size++;
			}
		}
		mr /= size;
		return new double[]{mr, size};
	}

	public static Map<String, Map<String, Tuple>> calcEnrichmentPValsMultiRankedList(
		Map<String, List<String>> rankedLists, Map<String, Map<String, Boolean>> featuredSetsRaw,
		int minTarg)
	{
		Map<String, Map<String, Tuple>> pvalsMap = new HashMap<>();

		Progress prg = new Progress(rankedLists.size(), "Calculating enrichment p-values");
		rankedLists.keySet().parallelStream().forEach(cellName ->
		{
			List<String> rankedList = rankedLists.get(cellName);
			Map<String, Map<String, Boolean>> featuredSets = convertPriors(featuredSetsRaw, rankedList, minTarg);


			Map<String, Tuple> pvalsAndDirs = calcEnrichmentPValsAndDirections(rankedList, featuredSets);

			synchronized (pvalsMap)
			{
				pvalsMap.put(cellName, pvalsAndDirs);
			}
			prg.tick();
		});

		return pvalsMap;
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
}