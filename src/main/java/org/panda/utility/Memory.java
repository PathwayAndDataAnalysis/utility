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
		if (!set.contains(s))
		{
			System.out.println(s);
		}

		return seenBefore(s);
	}

	public static boolean seenBefore(String s)
	{
		tc.addTerm(s);
		return set.add(s);
	}
}
