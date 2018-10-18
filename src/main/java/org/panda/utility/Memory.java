package org.panda.utility;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Ozgun Babur
 */
public class Memory
{
	public static TermCounter tc = new TermCounter();
	static Set<String> set = new HashSet<>();

	public static boolean printIfNew(String s)
	{
		return printIfNew(s, "");
	}

	public static boolean printIfNew(String s, String prefix)
	{
		if (!set.contains(s))
		{
			System.out.println(prefix + s);
		}

		return seenBefore(s);
	}

	public static boolean seenBefore(String s)
	{
		tc.addTerm(s);
		return set.add(s);
	}
}
