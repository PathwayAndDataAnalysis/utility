package org.panda.utility.statistics;

import org.panda.utility.Tuple;
import org.panda.utility.statistics.trendline.PolyTrendLine;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This class selects the subset of the given results to maintain a certain false discovery rate.
 * @author Ozgun Babur
 */
public class FDR
{
	public static <T> List<T> select(final Map<T, Double> results, double fdrThr,
		List<Double> randomized, int randMultiplier)
	{
		if (results.isEmpty()) return Collections.emptyList();
		if (randomized.isEmpty()) return new ArrayList<>(results.keySet());

		Collections.sort(randomized);

		List<T> keys = new ArrayList<>(results.keySet());

		Collections.sort(keys, (o1, o2) -> results.get(o1).compareTo(results.get(o2)));

		int ranIndex = -1;
		int maxIndex = -1;

		for (int i = 0; i < keys.size(); i++)
		{
			T key = keys.get(i);
			double pval = results.get(key);

			while(ranIndex < randomized.size() - 1 && randomized.get(ranIndex + 1) <= pval)
			{
				ranIndex++;
			}

			double noise = (ranIndex + 1) / (double) randMultiplier;

			if (noise / (i+1) <= fdrThr) maxIndex = i;
		}

		if (maxIndex < 0) return Collections.emptyList();
		else return new ArrayList<>(keys.subList(0, maxIndex+1));
	}

	public static <T> Map<T, Double> getQVals(final Map<T, Double> results, List<Double> randomized, int randMultiplier)
	{
		if (results.isEmpty()) return Collections.emptyMap();

		Collections.sort(randomized);

		List<T> keys = new ArrayList<>(results.keySet());

		Collections.sort(keys, (o1, o2) -> results.get(o1).compareTo(results.get(o2)));

		int ranIndex = -1;
		Map<T, Double> qvals = new HashMap<>();

		for (int i = 0; i < keys.size(); i++)
		{
			T key = keys.get(i);
			double pval = results.get(key);

			while(ranIndex < randomized.size() - 1 && randomized.get(ranIndex + 1) <= pval)
			{
				ranIndex++;
			}

			double noise = (ranIndex + 1) / (double) randMultiplier;

			double fdr = noise / (i+1);
			qvals.put(key, fdr);
		}

		return qvals;
	}

	/**
	 * @param results
	 * @param fdrThr
	 * @return
	 */
	public static <T> List<T> select(Map<T, Double> results, Map<T, Double> limits,
		final Map<T, Double> priorityScores, double fdrThr)
	{
		List<T> items = new ArrayList<>(priorityScores.keySet());
		Collections.sort(items, (o1, o2) -> priorityScores.get(o2).compareTo(priorityScores.get(o1)));

		if (limits == null) limits = defaultLimits(results);

		int priorityIndex = -1;
		int maxResult = 0;
		int selectedPriority = -1;
		List<T> result = null;

		do
		{
			while(priorityIndex < items.size() - 1 && (priorityIndex < 0 ||
				priorityScores.get(items.get(priorityIndex)).equals(
					priorityScores.get(items.get(priorityIndex + 1)))))
			{
				priorityIndex++;
			}

			Map<T, Double> resultsTemp = new HashMap<>();
			Map<T, Double> limitsTemp = new HashMap<>();

			for (int i = 0; i <= priorityIndex; i++)
			{
				T key = items.get(i);
				resultsTemp.put(key, results.get(key));
				limitsTemp.put(key, limits.get(key));
			}

			List<T> selected = select(resultsTemp, limitsTemp, fdrThr);

			if (selected.size() >= maxResult)
			{
				maxResult = selected.size();
				selectedPriority = priorityIndex;
				result = selected;
			}

			priorityIndex++;
		}
		while(priorityIndex < items.size() - 1);

		System.out.println("selectedPriority = " + selectedPriority);

		if (result == null) return Collections.emptyList();
		return result;
	}

	public static <T> List<T> selectUsingHalfRange(final Map<T, Double> results, double fdrThr)
	{
		long halfCnt = results.values().stream().filter(v -> v < 0.5).count();
		long cnt = halfCnt * 2;

		List<T> keys = new ArrayList<>(results.keySet());

		keys.sort(Comparator.comparing(results::get));

		int maxIndex = -1;
		for (int i = 0; results.get(keys.get(i)) < 0.5; i++)
		{
			T key = keys.get(i);
			double pval = results.get(key);

			double noise = pval * cnt;

			if (noise / (i + 1) <= fdrThr) maxIndex = i;
		}

		if (maxIndex < 0) return Collections.emptyList();
		else return new ArrayList<>(keys.subList(0, maxIndex + 1));
	}

	public static <T> List<T> selectBH(final Map<T, Double> results, double fdrThr)
	{
		return select(results, null, fdrThr);
	}

	public static <T> List<T> select(final Map<T, Double> results, Map<T, Double> limits, double fdrThr)
	{
		if (results.isEmpty()) return Collections.emptyList();

		if (limits == null) limits = defaultLimits(results);

		List<Double> limitList = new ArrayList<>(limits.values());
		Collections.sort(limitList);

		List<T> keys = new ArrayList<>(results.keySet());

		keys.sort(Comparator.comparing(results::get));

		int limIndex = 0;
		double limPv = limitList.get(0);
		int maxIndex = -1;

		for (int i = 0; i < keys.size(); i++)
		{
			T key = keys.get(i);
			double pval = results.get(key);

			while(limPv <= pval && limIndex < limitList.size()-1)
			{
				limPv = limitList.get(++limIndex);
			}

			double noise = pval * (limIndex + 1);

			if (noise / (i + 1) <= fdrThr) maxIndex = i;
		}

		if (maxIndex < 0) return Collections.emptyList();
		else return new ArrayList<>(keys.subList(0, maxIndex + 1));
	}

	private static <T> Map<T, Double> defaultLimits(Map<T, Double> results)
	{
		Map<T, Double> limits = new HashMap<>(results);

		for (T key : new HashSet<>(limits.keySet()))
		{
			limits.put(key, 0D);
		}
		return limits;
	}

	public static <T> List<T> selectWithPvalThreshold(final Map<T, Double> pvals, double pvalThr)
	{
		List<T> keys = new ArrayList<>(pvals.keySet());
		Collections.sort(keys, (o1, o2) -> new Double(pvals.get(o1)).compareTo(new Double(pvals.get(o2))));

		int cut = 0;
		while (pvals.get(keys.get(cut)) <= pvalThr && cut < keys.size()) cut++;

		return keys.subList(0, cut);
	}

	/**
	 * @param results
	 * @return
	 */
	public static <T> Map<T, Double> getQVals(final Map<T, Double> results,
		Map<T, Double> limits)
	{
		Map<T, Double> qvals = new HashMap<>();

		if (limits == null) limits = defaultLimits(results);

		List<Double> limitList = new ArrayList<>(limits.values());
		Collections.sort(limitList);

		List<T> keys = new ArrayList<>(results.keySet());

		Collections.sort(keys, (o1, o2) -> results.get(o1).compareTo(results.get(o2)));

		int limIndex = 0;
		double limPv = limitList.get(0);

		for (int i = 0; i < keys.size(); i++)
		{
			T key = keys.get(i);
			double pval = results.get(key);

			while(limPv <= pval && limIndex < limitList.size()-1)
			{
				limPv = limitList.get(++limIndex);
			}

			double noise = pval * (limIndex + 1);

			double fdr = noise / (i + 1);
			qvals.put(key, fdr);
		}
		return qvals;
	}

	public static <T> double getPValueThreshold(final Map<T, Double> results,
		Map<T, Double> limits, double fdrThr)
	{
		if (results.isEmpty()) return 0;

		Map<T, Double> qvals = getQVals(results, limits);

		double maxP = 0;
		for (T t : results.keySet())
		{
			double p = results.get(t);
			double q = qvals.get(t);

			if (p > maxP && q <= fdrThr) maxP = p;
		}

		return maxP;
	}

	public static <T> int[] getResultSizesUsingPolyCurve(final Map<T, Double> results, double[] thrs)
	{
		int[] sizes = new int[thrs.length];
		for (int i = 0; i < thrs.length; i++)
		{
			sizes[i] = selectUsingPolyCurve(results, thrs[i]).size();
		}
		return sizes;
	}

	/**
	 * @param results
	 * @param fdrThr
	 * @return
	 */
	public static <T> List<T> selectUsingPolyCurve(final Map<T, Double> results, double fdrThr)
	{
		if (results.isEmpty()) return Collections.emptyList();

		List<T> keys = new ArrayList<>(results.keySet());

		Collections.sort(keys, (o1, o2) -> results.get(o1).compareTo(results.get(o2)));

		double noiseSize = estimateNoiseVolume(results);

		int maxIndex = -1;

		for (int i = 0; i < keys.size(); i++)
		{
			T key = keys.get(i);
			double pval = results.get(key);

			double noise = pval * noiseSize;

			if (noise / (i + 1) <= fdrThr) maxIndex = i;
		}

		if (maxIndex < 0) return Collections.emptyList();
		else return new ArrayList<>(keys.subList(0, maxIndex+1));
	}

	public static <T> double estimateNoiseVolume(Map<T, Double> pvals)
	{
		double[][] f = getNoiseEstimatesForDifferentLambda(pvals);

		PolyTrendLine trendLine = new PolyTrendLine(3);
		trendLine.setValues(f[1], f[0]);

		double noiseRatio = trendLine.predict(f[0][f[0].length - 1]);

//		System.out.println("\n\nplot");
//		for (int i = 0; i < f[0].length; i++)
//		{
//			System.out.println(f[0][i] + "\t" + f[1][i] + "\t" + trendLine.predict(f[0][i]));
//		}
//		System.out.println();

		return noiseRatio * pvals.size();
	}

	public static <T> double[][] getNoiseEstimatesForDifferentLambda(Map<T, Double> pvals)
	{
		List<Double> vals = new ArrayList<>();

		int[] cnt = new int[99];

		for (Double val : pvals.values())
		{
			for (int i = 0; i < cnt.length; i++)
			{
				if (val > ((i + 1) * 0.01)) cnt[i]++;
			}
		}

		double m = pvals.size();

		for (int i = 0; i < cnt.length; i++)
		{
			double x = cnt[i] / (m * (1D - ((i + 1) * 0.01)));

			if (x > 0) vals.add(x);
			else break;
		}

		double[][] v = new double[2][vals.size()];

		for (int i = 0; i < v[0].length; i++)
		{
			v[0][i] = (i + 1) * 0.01;
			v[1][i] = vals.get(i);
		}

		return v;
	}

	public static <T> double decideBestFDR(final  Map<T,  Double>  results, List<Double>  randomized,
		int randMultiplier)
	{
		double bestFDR = -1;
		double maxScore = 0;

		System.out.println("\nFDR\tResult size\tExpected true positives\ttp-fp");
		for (int i = 1; i <= 50; i++)
		{
			double fdr = i / 100D;
			List<T> select = select(results, fdr, randomized, randMultiplier);
			double tp = select.size() * (1 - fdr);
			double fp = select.size() * fdr;
			double score = tp - fp;
			if (score > maxScore)
			{
				maxScore = score;
				bestFDR = fdr;
			}

			System.out.println(fdr + "\t" + select.size() + "\t" + ((int) Math.round(tp)) + "\t" +
				((int) Math.round(tp - fp)));
		}
		System.out.println();

		return bestFDR;
	}

	public static <T> double decideBestFDR_BH(final Map<T,  Double>  results, Map<T,  Double>  limits)
	{
		double bestFDR = -1;
		double maxScore = 0;

//		System.out.println("\nFDR\tResult size\tExpected true positives\ttp-fp");
		for (int i = 1; i <= 50; i++)
		{
			double fdr = i / 100D;
			List<T> select = select(results, limits, fdr);
			double tp = select.size() * (1 - fdr);
			double fp = select.size() * fdr;
			double score = tp - fp;
			if (score > maxScore)
			{
				maxScore = score;
				bestFDR = fdr;
			}

//			System.out.println(fdr + "\t" + select.size() + "\t" + ((int) Math.round(tp)) + "\t" + ((int) Math.round(tp - fp)));
		}
//		System.out.println();

		return bestFDR;
	}

	public static <T> Map<T, Double> extractPval(Map<T, Tuple> map)
	{
		return map.keySet().stream().collect(Collectors.toMap(Function.identity(), t -> map.get(t).p));
	}
}
