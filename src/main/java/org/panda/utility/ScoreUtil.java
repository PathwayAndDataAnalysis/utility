package org.panda.utility;

import org.panda.utility.statistics.Summary;

import java.util.*;

/**
 * @author Ozgun Babur
 */
public class ScoreUtil
{
	private List<Double> scores;
	private double max;
	private double min;
	protected int multiFactor;

	public ScoreUtil()
	{
		this(1);
	}

	public ScoreUtil(int multiFactor)
	{
		this.multiFactor = multiFactor;
		scores = new ArrayList<Double>();
	}

	public void addSCore(double d)
	{
		if (scores.isEmpty())
		{
			min = d;
			max = d;
		}

		scores.add(d);

		if (d > max) max = d;
		else if (d < min) min = d;
	}

	public void unite(ScoreUtil sc)
	{
		this.scores.addAll(sc.scores);
		this.multiFactor += sc.multiFactor;
		if (this.max < sc.max) this.max = sc.max;
		if (this.min > sc.min) this.min = sc.min;
	}

	public int countOverThr(double thr)
	{
		int cnt = 0;
		for (Double score : scores)
		{
			if (score >= thr) cnt++;
		}
		return cnt;
	}

	public static final double EPS = 0.00000001;

	/**
	 * Performs a logarithmic search for the given FDR.
	 * @param real scores for the non-random case(s)
	 * @param fdr target FDR
	 * @return threshold value for the target FDR
	 */
	public double getThresholdForFDR(ScoreUtil real, double fdr)
	{
		if (getFDRForThr(real, real.max) > fdr) return real.max + EPS;

		double highFDR = getFDRForThr(real, this.min);
		if (highFDR <= fdr) return this.min;

		List<Double> sorted = getSortedValues();
		double minVal = this.max + EPS;

		for (Double val : sorted)
		{
			double FDR = getFDRForThr(real, val);

			if (FDR <= fdr)
			{
				if (val < minVal) minVal = val;
			}
		}

		return minVal;
	}

	public List<Double> getSortedValues()
	{
		Set<Double> set = new HashSet<Double>(scores);
		List<Double> list = new ArrayList<Double>(set);
		Collections.sort(list);
		Collections.reverse(list);
		return list;
	}

	public double getFDRForThr(ScoreUtil real, double thr)
	{
		int ranCnt = countOverThr(thr);
		int norCnt = real.countOverThr(thr);

		if (norCnt == 0) return 0;

		double ranNormalized = ranCnt / (double) multiFactor;
		double norNormalized = norCnt / (double) real.multiFactor;

		return ranNormalized / norNormalized;
	}

	public double getTotal()
	{
		return Summary.sum(scores);
	}
}
