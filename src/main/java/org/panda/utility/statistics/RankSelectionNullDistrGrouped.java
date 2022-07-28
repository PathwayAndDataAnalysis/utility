package org.panda.utility.statistics;

import org.panda.utility.Progress;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * This class generates a simulated null distribution for the total rank of randomly selected items from a ranked list.
 * It is a two-tailed distribution, meaning ranks are calculated wrt both left end and right end, and the smaller one is
 * selected.
 *
 * @deprecated use RankSelectionNullDistrTwo Tailed instead
 */
public class RankSelectionNullDistrGrouped
{
	protected Map<Map<Integer, Integer>, int[]> cumCntMap;
	protected double randomizationCount;
	protected Random rand;

	public RankSelectionNullDistrGrouped(int listSize, Set<Map<Integer, Integer>> selectionSizes, int randomizationCount)
	{
		this.randomizationCount = randomizationCount;
		this.rand = new Random();
		init(listSize, selectionSizes, randomizationCount);
	}

	protected void init(int listSize, Set<Map<Integer, Integer>> selectionSizes, int randomizationCount)
	{
		int[] list = new int[listSize];
		for (int i = 0; i < listSize; i++)
		{
			list[i] = i;
		}

		// Calculate flattened sizes for each grouped selection
		Map<Map<Integer, Integer>, Integer> plainSizeMap = new HashMap<>();
		int maxFlatSize = 0;
		for (Map<Integer, Integer> group : selectionSizes)
		{
			int sum = 0;
			for (Integer size : group.keySet())
			{
				sum += size * group.get(size);
			}
			plainSizeMap.put(group, sum);

			if (maxFlatSize < sum) maxFlatSize = sum;
		}


		int[] rPerm = new int[maxFlatSize];
		int maxRank = listSize - 1;

		Map<Map<Integer, Integer>, Map<Integer, Integer>> countsMap = new HashMap<>();
		selectionSizes.forEach(s -> countsMap.put(s, new HashMap<>()));

		Progress prg = new Progress(randomizationCount, "Generating null distribution");
		for (int i = 0; i < randomizationCount; i++, prg.tick())
		{
			rPermutation(list, rPerm);

			for (Map<Integer, Integer> group : countsMap.keySet())
			{
				int units = group.values().stream().reduce(Integer::sum).get();
				int sumL = getRankSum(rPerm, group);
				int sumR = (maxRank * units) - sumL;

				assert sumR >= 0 && sumR <= maxRank : "sumR = " + sumR;

				int minSum = Math.min(sumL, sumR);

				Map<Integer, Integer> counts = countsMap.get(group);
				counts.put(minSum, counts.getOrDefault(minSum, 0) + 1);
			}
		}

		// convert counts to cumulative counts
		calculateCumulativeCounts(countsMap);
	}

	protected void rPermutation(int[] list, int[] perm)
	{
		int n = list.length;
		for (int i = 0; i < perm.length; i++)
		{
			int rInd = rand.nextInt(n - i) + i;
			perm[i] = list[rInd];
			list[rInd] = list[i];
			list[i] = perm[i];
		}
	}

	protected void calculateCumulativeCounts(Map<Map<Integer, Integer>, Map<Integer, Integer>> countsMap)
	{
		cumCntMap = new HashMap<>();
		countsMap.forEach((s, map) ->
		{
			int maxRankSum = map.keySet().stream().max(Integer::compareTo).get();
			int[] cumArray = new int[maxRankSum + 1];
			cumCntMap.put(s, cumArray);

			int cum = 0;
			for (int i = 0; i <= maxRankSum; i++)
			{
				cum += map.getOrDefault(i, 0);
				cumArray[i] = cum;
			}
		});
	}


	private int getRankSum(int[] rPerm, Map<Integer, Integer> group)
	{
		float sum = 0;
		int index = 0;
		for (Integer size : group.keySet())
		{
			for (int repeat = 0; repeat < group.get(size); repeat++)
			{
				float a = 0;

				for (int i = index; i < index + size; i++)
				{
					a += rPerm[i];
				}

				sum += a / size;
				index += size;
			}
		}
		return Math.round(sum);
	}

	public double getPval(Map<Integer, Integer> groupSize, int rankSum)
	{
		int[] cum = cumCntMap.get(groupSize);
		if (rankSum >= cum.length) return 1;
		else return cum[rankSum] / randomizationCount;
	}
}
