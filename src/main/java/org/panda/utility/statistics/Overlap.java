package org.panda.utility.statistics;

import java.util.*;

/**
 * @author Ozgun Babur
 */
public class Overlap
{
	private static final Map<String, Double> cache = new HashMap<String, Double>();
	public static boolean useCache = true;
	public static final String SEP = ".";
	public static int cacheUsed = 0;
	public static int cacheNotUsed = 0;

	/**
	 * Calculates the p-value for getting o or less overlaps by chance.
	 *
	 * @param n sample size
	 * @param a array of alteration counts
	 * @param o overlap
	 * @return
	 */
	public static double calcMutexPval(int n, int o, int... a)
	{
		String key = null;
		if (useCache)
		{
			key = n + SEP + o + SEP + Arrays.toString(a);
			if (cache.containsKey(key))
			{
				cacheUsed++;
				return cache.get(key);
			}
		}

		// Make sure that all parameters are non-negative

		if (n < 0 || o < 0) throw new IllegalArgumentException(
			"All parameters should be non-negative. n="+n+" o="+o);

		for (int aa : a)
		{
			if (aa < 0) throw new IllegalArgumentException(
				"All parameters should be non-negative. a contains " + aa);
		}
		// Make sure that n >= a >= b >= o


		if (o > Summary.min(a)) throw new IllegalArgumentException("o cannot be more than min(a)");
		if (n < Summary.max(a)) throw new IllegalArgumentException("n cannot be less than max(a)");


		int minO = Math.max(0, Summary.sum(a) - ((a.length - 1) * n));
		if (o < minO) throw new IllegalArgumentException("o cannot be lower than max(0, sum(a)-((length(a)-1) * n))");

		if (n == 0) return 1;

		double[] p = calcOverlapPvals(n, minO, o, a);
		double result = Summary.sum(p);

		if (useCache)
		{
			cacheNotUsed++;
			cache.put(key, result);
		}

		return result;
	}

	/**
	 * Calculates the p-value for getting o or less overlaps by chance.
	 *
	 * @param n sample size
	 * @param a array of alteration counts
	 * @param o overlap
	 * @return
	 */
	public static double calcCoocPval(int n, int o, int... a)
	{
		// Make sure that all parameters are non-negative

		if (n < 0 || o < 0) throw new IllegalArgumentException(
			"All parameters should be non-negative. n="+n+" o="+o);

		for (int aa : a)
		{
			if (aa < 0) throw new IllegalArgumentException(
				"All parameters should be non-negative. a contains " + aa);
		}
		// Make sure that n >= a >= b >= o


		if (o > Summary.min(a)) throw new IllegalArgumentException("o cannot be more than min(a)");
		if (n < Summary.max(a)) throw new IllegalArgumentException("n cannot be less than max(a)");


		int minO = Math.max(0, Summary.sum(a) - ((a.length - 1) * n));
		if (o < minO) throw new IllegalArgumentException("o cannot be lower than max(0, sum(a)-((length(a)-1) * n))");

		int maxO = Summary.min(a);
		if (o > maxO) throw new IllegalArgumentException("o cannot be higher than min(a)");

		if (n == 0) return 1;

		double[] p = calcOverlapPvals(n, o, maxO, a);
		return Summary.sum(p);
	}

	/**
	 * Calculates all overlap pvals in the given range.
	 * @param n sample size
	 * @param a alt counts
	 * @param from lowest overlap
	 * @param to highest overlap
	 * @return all p-values in the given range
	 */
	public static double[] calcOverlapPvals(int n, int from, int to, int... a)
	{
		if (a.length < 2) throw new IllegalArgumentException(
			"Array a should have length at least 2. length(a) = " + a.length);

		// Make sure that all parameters are non-negative

		if (n < 0 || from < 0 || to < 0) throw new IllegalArgumentException(
			"All parameters should be non-negative. n="+n+" from="+from+" to="+to);

		for (int aa : a)
		{
			if (aa < 0) throw new IllegalArgumentException(
				"All parameters should be non-negative. a contains " + aa);
		}

		// Make sure that n >= a >= b >= o

		if (from > to) throw new IllegalArgumentException("from cannot be bigger than to");
		if (to > Summary.min(a)) throw new IllegalArgumentException("to cannot be more than min(a)");
		if (Summary.max(a) > n) throw new IllegalArgumentException("max(a) cannot be greater than sample size");
		if (n == 0) throw new IllegalArgumentException("n should be a positive number");

		int minO = Math.max(0, Summary.sum(a) - ((a.length - 1) * n));
		if (from < minO) throw new IllegalArgumentException("from cannot be lower than max(0, sum(a)-((length(a)-1) * n))");

		double[] pval = new double[to - from + 1];

		if (a.length == 2)
		{
			int e = (int) (Summary.mult(a) / Math.pow(n, a.length - 1));
			if (e < from) e = from;
			else if (e > to) e = to;

			pval[e - from] = calcProb(n, a[0], a[1], e);

			for (int i = e - 1; i >= from; i--)
			{
				pval[i - from] = pval[i - from + 1] * getMultiplierToDecreaseO(n, a[0], a[1], i+1);
			}
			for (int i = e + 1; i <= to; i++)
			{
				pval[i - from] = pval[i - from - 1] * getMultiplierToIncreaseO(n, a[0], a[1], i-1);
			}

			return pval;
		}

		// else

		int[] at = new int[a.length - 1];
		System.arraycopy(a, 0, at, 0, at.length);

		int f = Math.max(Summary.sum(at) - ((at.length - 1) * n), minO);
		int t = Math.min(Summary.min(at), n + to - a[a.length - 1]);
		double[] p1 = calcOverlapPvals(n, f, t, at);

		for (int i = 0; i < pval.length; i++)
		{
			pval[i] = calcProb(n, i + from, p1, f, a);
		}
		return pval;
	}

	/**
	 * Calculates all overlap pvals in the given range of a.
	 * @param n n
	 * @param o overlap
	 * @param b b
	 * @param from lowest a
	 * @param to highest a
	 * @return all p-values in the given range
	 */
	public static double[] calcOverlapPvalsForDifferingA(int n, int b, int o, int from, int to)
	{
		// Make sure that all parameters are non-negative

		if (n < 0 || o < 0 || b < 0 || from < 0 || to < 0) throw new IllegalArgumentException(
			"All parameters should be non-negative. n="+n+" o="+o+" b="+b+" from="+from+" to="+to);

		// Make sure that n >= a >= b >= o

		if (from > to) throw new IllegalArgumentException("from cannot be bigger than to");
		if (o > from) throw new IllegalArgumentException("o cannot be more than from");
		if (from > n) throw new IllegalArgumentException("from cannot be greater than sample size");
		if (to > n) throw new IllegalArgumentException("to cannot be greater than sample size");
		if (b > n) throw new IllegalArgumentException("b cannot be greater than sample size");
		if (o < b-(n-from)) throw new IllegalArgumentException("o cannot be lower than b-(n-from)");
		if (o < b-(n-to)) throw new IllegalArgumentException("o cannot be lower than b-(n-to)");
		if (o > b) throw new IllegalArgumentException("o cannot be greater than b");
		if (n == 0) throw new IllegalArgumentException("n should be a positive number");

		double[] pval = new double[to - from + 1];

		int e = (int) ((n * o) / (double) b);
		if (e < from) e = from;
		else if (e > to) e = to;

		pval[e - from] = calcProb(n, e, b, o);

		for (int i = e - 1; i >= from; i--)
		{
			pval[i - from] = pval[i - from + 1] * getMultiplierToDecreaseA(n, i+1, b, o);
		}
		for (int i = e + 1; i <= to; i++)
		{
			pval[i - from] = pval[i - from - 1] * getMultiplierToIncreaseA(n, i-1, b, o);
		}

		return pval;
	}

	/**
	 * Calculated the probability that sets a and b have exactly x overlaps.
	 * @param n
	 * @param a
	 * @param b
	 * @param x
	 * @return
	 */
	protected static double calcProb(int n, int a, int b, int x)
	{
		if ((a + b - n) > x) return 0;
		if (x > a || x > b) return 0;

		String key = null;
		if (useCache)
		{
			key = n + SEP + x + SEP + Math.max(a, b) + SEP + Math.min(a, b);
			if (cache.containsKey(key))
			{
//				System.out.println(".");
				return cache.get(key);
			}

		}

		FactorialSolver s = new FactorialSolver(
			new ArrayList<>(Arrays.asList(a, b, (n - a), (n - b))),
			new ArrayList<>(Arrays.asList(n, x, (a - x), (b - x), (n - a - b + x))));

		double p = s.solve();

		if (useCache) cache.put(key, p);

		return p;
	}

	/**
	 * Calculated the probability that sets a, b, and c have exactly x overlaps.
	 * @param n
	 * @param a
	 * @param x
	 * @return
	 */
	protected static double calcProb(int n, int x, double[] p1, int startOv, int... a)
	{
		int minX = Math.max(Summary.sum(a) - ((a.length - 1) * n) , 0);
		int maxX = Summary.min(a);
		if (minX > x || x > maxX) return 0;

		int from = Math.max(Summary.sumButLast(a) - ((a.length - 2) * n), x);
		assert from >= startOv;
		int to = Math.min(Summary.minButLast(a), n + x - a[a.length - 1]);
		double[] p2 = calcOverlapPvalsForDifferingA(n, a[a.length - 1], x, from, to);

		double pval = 0;

		for (int i = 0; i < p2.length; i++)
		{
			if (i + from - startOv > p1.length)
			{
				System.out.println();
			}
			pval += p1[i + from - startOv] * p2[i];
		}
		return pval;
	}

	private static double getMultiplierToIncreaseO(int n, int a, int b, int o)
	{
		return  ((a - o) * (b - o)) / (double) ((o + 1) * (n - a - b + o + 1));
	}

	private static double getMultiplierToDecreaseO(int n, int a, int b, int o)
	{
		return  (o * (n - a - b + o)) / (double) ((a - o + 1) * (b - o + 1));
	}

	private static double getMultiplierToIncreaseA(int n, int a, int b, int o)
	{
		return ((a + 1) * (n -a -b + o)) / (double) ((n - a) * (a - o + 1));
	}

	private static double getMultiplierToDecreaseA(int n, int a, int b, int o)
	{
		return ((n - a + 1) * (a - o)) / (double) (a * (n - a - b + o + 1));
	}

	public static double calcMutexPvalOfSubset(boolean[] use, boolean[]... alt)
	{
		int[] a = new int[alt.length];
		int[] no = getCounts(use, a, alt);
		return calcMutexPval(no[0], no[1], a);
	}

	public static double calcMutexPval(boolean[]... alt)
	{
		return calcMutexPvalOfSubset(null, alt);
	}

	public static double calcCoocPvalOfSubset(boolean[] use, boolean[]... alt)
	{
		int[] a = new int[alt.length];
		int[] no = getCounts(use, a, alt);
		return calcCoocPval(no[0], no[1], a);
	}

	public static List<Integer> getCounts(boolean[]... alt)
	{
		int[] a = new int[alt.length];
		int[] no = getCounts(null, a, alt);
		List<Integer> cnts = new ArrayList<Integer>();
		cnts.add(no[0]);
		cnts.add(no[1]);
		for (int i : a) cnts.add(i);
		return cnts;
	}

	public static double calcCoocPval(boolean[]... alt)
	{
		return calcCoocPvalOfSubset(null, alt);
	}

	private static int[] getCounts(boolean[] use, int[] altCntToFill, boolean[]... alt)
	{
		assert altCntToFill.length == alt.length;
		int samples = alt[0].length;
		assert use == null || use.length == samples;
		for (int i = 0; i < alt.length; i++)
		{
			assert alt[i].length == samples;
			altCntToFill[i] = 0;
		}

		int n = 0;
		int overlap = 0;

		for (int i = 0; i < samples; i++)
		{
			if (use != null && !use[i]) continue;

			n++;

			int hit = 0;
			for (int j = 0; j < alt.length; j++)
			{
				if (alt[j][i])
				{
					altCntToFill[j]++;
					hit++;
				}
			}
			if (hit == alt.length) overlap++;
		}
		return new int[]{n, overlap};
	}

	public static void main(String[] args) throws InterruptedException
	{
		int n = 199;
		int a = 58;
		int b = 58;
		int o = 23;

		System.out.println(calcCoocPval(n, o, a, b));
	}
}
