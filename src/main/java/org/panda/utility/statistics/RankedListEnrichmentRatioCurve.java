package org.panda.utility.statistics;

import java.util.List;
import java.util.Set;

/**
 * This class is for evaluating the enrichment in ranked lists, to see how it changes as we go down the list and to
 * compare two ranked lists based on their enrichment curves.
 *
 * @author Ozgun Babur
 */
public class RankedListEnrichmentRatioCurve
{
	public static <T> double[] get(List<T> rankedList, Set<T> featured)
	{
		double[] rat = new double[rankedList.size()];

		int featCnt = 0;

		for (int i = 0; i < rat.length; i++)
		{
			if (featured.contains(rankedList.get(i))) featCnt++;

			rat[i] = featCnt / (double) (i + 1);
		}

		return rat;
	}
}
