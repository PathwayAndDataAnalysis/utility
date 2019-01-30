package org.panda.utility;

import java.util.*;

/**
 * Created by babur on 4/18/16.
 */
public class CollectionUtil
{
	public static List<String> getSortedList(final Map<String, Double> map)
	{
		List<String> list = new ArrayList<String>(map.keySet());
		Collections.sort(list, (o1, o2) -> {
			Double v1 = map.get(o1);
			Double v2 = map.get(o2);

			if (v1.equals(v2)) return o1.compareTo(o2);
			return v1.compareTo(v2);
		});

		return list;
	}

	public static boolean intersects(Collection<?> col1, Collection<?> col2)
	{
		return col1.stream().anyMatch(col2::contains);
	}

	public static boolean intersectionEmpty(Collection<?> col1, Collection<?> col2)
	{
		return col1.stream().noneMatch(col2::contains);
	}

	public static int countOverlap(Collection<?> col1, Collection<?> col2)
	{
		return (int) col1.stream().filter(col2::contains).count();
	}

	public static int countDiffOfFirst(Collection<?> col1, Collection<?> col2)
	{
		return (int) col1.stream().filter(e -> !col2.contains(e)).count();
	}

	public static int countUnion(Set<?> set1, Set<?> set2)
	{
		return countDiffOfFirst(set1, set2) + set2.size();
	}

	public static double getJaccardSimilarity(Set<?> set1, Set<?> set2)
	{
		return countOverlap(set1, set2) / (double) countUnion(set1, set2);
	}

	public static <T extends Comparable> void printVennSets(Collection<T> col1, Collection<T> col2, Map<T, T> mapFrom2To1)
	{
		Set<T> set2 = new HashSet<>();
		for (T t : col2)
		{
			if (mapFrom2To1.containsKey(t)) set2.add(mapFrom2To1.get(t));
			else set2.add(t);
		}

		printVennSets(col1, set2);
	}

	public static <T extends Comparable> void printVennSets(Collection<T>... col)
	{
		int[] cnt = getVennCounts(col);
		Set<T>[] venn = getVennSets(col);
		String[] name = getSetNamesArray(col.length);

		for (int i = 0; i < cnt.length; i++)
		{
			List<T> list = new ArrayList<>(venn[i]);
			Collections.sort(list);

			System.out.print(name[i] + "\t" + cnt[i] + "\t" + list);

			if (i < col.length)
			{
				System.out.print("\t" + FormatUtil.roundToSignificantDigits(
					(cnt[i] / (double) col[i].size()) * 100, 3));
			}

			System.out.println();
		}
	}

	public static <T extends Comparable> void printVennCounts(Collection<T>... col)
	{
		int[] cnt = getVennCounts(col);
		String[] name = getSetNamesArray(col.length);

		for (int i = 0; i < cnt.length; i++)
		{
			System.out.print(name[i] + "\t" + cnt[i]);

			if (i < col.length)
			{
				System.out.print("\t" + FormatUtil.roundToSignificantDigits(
					(cnt[i] / (double) col[i].size()) * 100, 3));
			}

			System.out.println();
		}
	}

	public static void printNameMapping(String... names)
	{
		String[] nms = getSetNamesArray(names.length);

		for (int i = 0; i < names.length; i++)
		{
			System.out.println(nms[i] + "\t" + names[i]);
		}
	}

	private static String addPrefixSpaces(String s, int desiredLength)
	{
		while (s.length() < desiredLength) s = " " + s;
		return s;
	}

	public static <T> int[] getVennCounts(Collection<T>... col)
	{
		Set<T>[] venn = getVennSets(col);
		int[] cnt = new int[venn.length];
		for (int i = 0; i < cnt.length; i++)
		{
			cnt[i] = venn[i].size();
		}
		return cnt;
	}

	public static <T> Set<T>[] getVennSets(Collection<T>... col)
	{
		int size = col.length;
		Set<T>[] set = new Set[size];

		for (int i = 0; i < size; i++)
		{
			set[i] = new HashSet<T>(col[i]);
		}

		Set<T>[] venn = new Set[(int) (Math.pow(2, size) - 1)];
		String[] bs = generateBinaryStrings(size);

		int x = 0;

		for (String s : bs)
		{
			Set<Set<T>> intersectSets = new HashSet<Set<T>>();
			Set<Set<T>> subtractSets = new HashSet<Set<T>>();

			for (int k = 0; k < size; k++)
			{
				if (s.length() < k + 1 || s.charAt(s.length() - 1 - k) == '0')
				{
					subtractSets.add(set[k]);
				}
				else intersectSets.add(set[k]);
			}

			boolean first = true;
			Set<T> select = new HashSet<T>();
			for (Set<T> inset : intersectSets)
			{
				if (first)
				{
					select.addAll(inset);
					first = false;
				}
				else select.retainAll(inset);
			}
			for (Set<T> subset : subtractSets)
			{
				select.removeAll(subset);
			}

			venn[x++] = select;
		}
		return venn;
	}

	private static String[] generateBinaryStrings(int n)
	{
		List<String> list = new ArrayList<String>();
		for (int i = 1; i < Math.pow(2, n); i++)
		{
			list.add(Integer.toBinaryString(i).intern());
		}

		Collections.sort(list, (o1, o2) -> {
			int c1 = count1inBinaryString(o1);
			int c2 = count1inBinaryString(o2);

			if (c1 != c2) return new Integer(c1).compareTo(c2);

			for (int i = Math.min(o1.length(), o2.length()) - 1; i >= 0 ; i--)
			{
				boolean b1 = o1.charAt(i) == '1';
				boolean b2 = o2.charAt(i) == '1';

				if (b1 != b2)
				{
					return b1 ? -1 : 1;
				}
			}
			return 0;
		});

		return list.toArray(new String[list.size()]);
	}

	private static int count1inBinaryString(String s)
	{
		int cnt = 0;
		for (int i = 0; i < s.length(); i++)
		{
			if (s.charAt(i) == '1') cnt++;
		}
		return cnt;
	}

	public static String[] getSetNamesArray(int n)
	{
		String[] bin = generateBinaryStrings(n);
		String[] names = new String[bin.length];

		int x = 0;
		for (String s : bin)
		{
			String name = "";
			for (int i = 0; i < s.length(); i++)
			{
				if (s.charAt(s.length() - 1 - i) == '1') name += (char) (i + 65);
			}
			names[x++] = name;
		}

		return names;
	}

	public static String merge(Collection col, String delim)
	{
		StringBuilder sb = new StringBuilder();
		Iterator iter = col.iterator();
		while (iter.hasNext())
		{
			sb.append(iter.next());
			if (iter.hasNext()) sb.append(delim);
		}
		return sb.toString();
	}

	public static <T> Set<T> getIntersection(Collection<T>... col)
	{
		Set<T> set = new HashSet<>(col[0]);
		for (int i = 1; i < col.length; i++)
		{
			set.retainAll(col[i]);
		}
		return set;
	}

	public static <T> void removeIntersection(Collection<T>... col)
	{
		Set<T> intersection = getIntersection(col);
		for (Collection<T> c : col)
		{
			c.removeAll(intersection);
		}
	}

	public static <T> Set<T> getUnion(Collection<T>... col)
	{
		Set<T> set = new HashSet<>();
		for (Collection<T> aCol : col)
		{
			set.addAll(aCol);
		}
		return set;
	}

	public static int maxIntInList(List<Integer> list)
	{
		List<Integer> m = new ArrayList<>(1);
		list.stream().max(Integer::compare).ifPresent(m::add);
		return m.isEmpty() ? -Integer.MAX_VALUE : m.iterator().next();
	}

	/**
	 * Gets a new set, which is A - B.
	 */
	public static <T> Set<T> diff(Set<T> A, Set<T> B)
	{
		Set<T> d = new HashSet<>(A);
		d.removeAll(B);
		return d;
	}

	public static int countValues(List<String> list, String... query)
	{
		return (int) list.stream().filter(Arrays.asList(query)::contains).count();
	}

}
