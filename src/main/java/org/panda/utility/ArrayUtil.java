package org.panda.utility;

import java.util.*;
import java.util.stream.IntStream;

/**
 * Created by babur on 4/18/16.
 */
public class ArrayUtil
{
	public static final int ABSENT_INT = Integer.MIN_VALUE;

	public static double mean(double[] v)
	{
		double s = 0;
		int c = 0;
		for (double aV : v)
		{
			if (!Double.isNaN(aV))
			{
				s += aV;
				c++;
			}
		}
		return s / c;
	}

	public static double geometricMean(double[] v)
	{
		double s = 1;
		int c = 0;
		for (double aV : v)
		{
			if (!Double.isNaN(aV))
			{
				s *= aV;
				c++;
			}
		}
		return Math.pow(s, c);
	}

	public static double mean(double[] v, boolean[] use)
	{
		double s = 0;
		int c = 0;
		for (int i = 0; i < use.length; i++)
		{
			if (use[i] && !Double.isNaN(v[i]))
			{
				s += v[i];
				c++;
			}
		}
		return s / c;
	}

	public static double mean(int[] v)
	{
		if (v.length == 1) return v[0];

		return sum(v) / (double) v.length;
	}

	public static double mean(int[] v, boolean[] use)
	{
		double s = 0;
		int c = 0;
		for (int i = 0; i < use.length; i++)
		{
			if (use[i] && v[i] != ABSENT_INT)
			{
				s += v[i];
				c++;
			}
		}
		return s / c;
	}

	public static double sum(double[] v)
	{
		double s = 0;
		for (double v1 : v)
		{
			s += v1;
		}
		return s;
	}

	public static int sum(int[] v)
	{
		int s = 0;
		for (int v1 : v)
		{
			s += v1;
		}
		return s;
	}

	public static double diffOfMeans(double[] x0, double[] x1)
	{
		return mean(x1) - mean(x0);
	}


	/**
	 * Separates the given double array into groups based on the integer categories. This is useful to prepare the
	 * dataset to ANOVA test.
	 * @param categ
	 * @param vals
	 * @return
	 */
	public static List<double[]> separateToCategories(int[] categ, double[] vals)
	{
		if (categ.length != vals.length) throw new IllegalArgumentException("Array sizes must be the same. categ" +
			".legth = " + categ.length + ", vals.length = " + vals.length);

		Map<Integer, Set<Integer>> categoryIndexMap = new HashMap<>();

		for (int i = 0; i < categ.length; i++)
		{
			if (categ[i] == ABSENT_INT) continue;
			if (Double.isNaN(vals[i])) continue;

			if (!categoryIndexMap.containsKey(categ[i])) categoryIndexMap.put(categ[i], new HashSet<>());
			categoryIndexMap.get(categ[i]).add(i);
		}

		List<Integer> indices = new ArrayList<>(categoryIndexMap.keySet());
		Collections.sort(indices);

		List<double[]> list = new ArrayList<>();
		for (Integer index : indices)
		{
			if (categoryIndexMap.get(index).size() < 3) continue;
			double[] v = new double[categoryIndexMap.get(index).size()];
			int i = 0;
			for (Integer valInd : categoryIndexMap.get(index))
			{
				v[i++] = vals[valInd];
			}
			list.add(v);
		}

		return list;
	}

	/**
	 * Gets the index that if the groups are separated from that index by making the index the first group of the right
	 * group, then the separation is kind of balanced.
	 * @param grouped
	 * @return
	 */
	public static int getBalancedSeparatingIndex(List<double[]> grouped)
	{
		int leftInd = 0;
		int rightInd = grouped.size() - 1;

		int leftSize = grouped.get(leftInd).length;
		int rightSize = grouped.get(rightInd).length;

		while (rightInd - leftInd > 1)
		{
			if (leftSize < rightSize)
			{
				leftSize += grouped.get(++leftInd).length;
			}
			else
			{
				rightSize += grouped.get(--rightInd).length;
			}
		}

		return rightInd;
	}

	public static double[][] separateToBalancedTwo(List<double[]> groups)
	{
		int index = getBalancedSeparatingIndex(groups);
		int leftSize = 0;
		int rightSize = 0;
		for (int i = 0; i < groups.size(); i++)
		{
			int size = groups.get(i).length;
			if (i < index) leftSize += size;
			else rightSize += size;
		}
		double[][] v = new double[][]{new double[leftSize], new double[rightSize]};

		int fromIndex = 0;
		for (int i = 0; i < groups.size(); i++)
		{
			System.arraycopy(groups.get(i), 0, v[i < index ? 0 : 1], fromIndex, groups.get(i).length);
			if (i == index) fromIndex = 0;
		}
		return v;
	}

	public static long[][] convertCategoriesToContingencyTable(int[] c1, int[] c2)
	{
		List<Integer> list1 = getOrderedCategories(c1);
		List<Integer> list2 = getOrderedCategories(c2);

		long[][] t = new long[list1.size()][list2.size()];
		for (long[] longs : t) Arrays.fill(longs, 0);

		for (int i = 0; i < c1.length; i++)
		{
			if (c1[i] == ABSENT_INT || c2[i] == ABSENT_INT) continue;

			t[list1.indexOf(c1[i])][list2.indexOf(c2[i])]++;
		}
		return t;
	}

	public static long[][] convertCategorySubsetsToContingencyTables(int[] c, boolean[] control, boolean[] test)
	{
		if (c.length != control.length || c.length != test.length)
		{
			throw new IllegalArgumentException("Array lengths has to be equal.");
		}

		List<Integer> list = getOrderedCategories(c);
		long[][] t = new long[2][list.size()];

		for (int i = 0; i < c.length; i++)
		{
			if (control[i] || test[i])
			{
				t[test[i] ? 1 : 0][list.indexOf(c[i])]++;
			}
		}
		return t;
	}

	public static List<Integer> getOrderedCategories(int[] cat)
	{
		List<Integer> list = new ArrayList<>();
		for (int i : cat)
		{
			if (i != ABSENT_INT && !list.contains(i)) list.add(i);
		}
		Collections.sort(list);
		return list;
	}

	public static double[] toDouble(int[] array)
	{
		double[] v = new double[array.length];
		for (int i = 0; i < v.length; i++)
		{
			v[i] = array[i] == ABSENT_INT ? Double.NaN : array[i];
		}
		return v;
	}

	public static double[] toArray(List<Double> list)
	{
		double[] v = new double[list.size()];
		for (int i = 0; i < v.length; i++)
		{
			v[i] = list.get(i);
		}
		return v;
	}

	public static double[][] trimNaNs(double[] v1, double[] v2)
	{
		List<Integer> indices = new ArrayList<>();
		for (int i = 0; i < v1.length; i++)
		{
			if (!Double.isNaN(v1[i]) && !Double.isNaN(v2[i])) indices.add(i);
		}
		double[][] v = new double[2][indices.size()];

		int i = 0;
		for (Integer index : indices)
		{
			v[0][i] = v1[index];
			v[1][i++] = v2[index];
		}

		return v;
	}

	public static void ORWith(boolean[] toChange, boolean[] toAdd)
	{
		if (toChange.length != toAdd.length) throw new IllegalArgumentException(
			"Array sizes have to be equal.");

		for (int i = 0; i < toAdd.length; i++)
		{
			if (toAdd[i]) toChange[i] = true;
		}
	}

	public static double[] subset(double[] vals, boolean[] select)
	{
		if (vals.length != select.length) throw new IllegalArgumentException("Parameter array lengths must be equal.");

		return IntStream.range(0, vals.length)
			.filter(i -> select[i] && !Double.isNaN(vals[i]))
			.mapToDouble(i -> vals[i]).toArray();
	}

	public static int[] subset(int[] vals, boolean[] select)
	{
		if (vals.length != select.length) throw new IllegalArgumentException("Parameter array lengths must be equal.");

		return IntStream.range(0, vals.length)
			.filter(i -> select[i] && vals[i] != ABSENT_INT)
			.map(i -> vals[i]).toArray();
	}

	public static int countValue(boolean[] b, boolean val)
	{
		int cnt = 0;
		for (boolean v : b)
		{
			if (v == val) cnt++;
		}
		return cnt;
	}

	public static int countValue(int[] arr, int val)
	{
		int cnt = 0;
		for (int v : arr)
		{
			if (v == val) cnt++;
		}
		return cnt;
	}

	public static int countValues(String[] arr, String... query)
	{
		return (int) Arrays.stream(arr).filter(Arrays.asList(query)::contains).count();
	}

	public static String getString(String delim, Object... o)
	{
		if (o.length == 0) return "";

		StringBuilder sb = new StringBuilder(o[0].toString());

		IntStream.range(1, o.length).forEach(i -> sb.append(delim).append(o[i]));

		return sb.toString();
	}

	public static int indexOf(String[] array, String... query)
	{
		for (int i = 0; i < array.length; i++)
		{
			for (String q : query)
			{
				if (array[i].equals(q)) return i;
			}
		}
		return -1;
	}

	public static boolean[] negate(boolean[] b)
	{
		boolean[] n = new boolean[b.length];
		for (int i = 0; i < b.length; i++)
		{
			n[i] = !b[i];
		}
		return n;
	}

	public static void shuffle(boolean[] b, Random r)
	{
		int ind;
		for (int i = b.length - 1; i > 0; i--)
		{
			ind = r.nextInt(i + 1);
			if (b[ind] != b[i])
			{
				b[ind] = b[i];
				b[i] = !b[i];
			}
		}
	}

	public static boolean isUniform(boolean[] b)
	{
		if (b.length < 2) return true;
		for (int i = 1; i < b.length; i++)
		{
			if (b[i] != b[0]) return false;
		}
		return true;
	}

	public static int[] convertToBasicIntArray(List<Integer> list)
	{
		int[] arr = new int[list.size()];
		for (int i = 0; i < arr.length; i++)
		{
			arr[i] = list.get(i);
		}
		return arr;
	}
}
