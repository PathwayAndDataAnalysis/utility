package org.panda.utility;

import java.util.HashSet;
import java.util.Set;

/**
 * Prevents repeated IDs by modifying the repeated ones.
 */
public class UniqueMaker
{
	private final Set<String> seen;

	public UniqueMaker()
	{
		seen = new HashSet<>();
	}

	public String get(String id)
	{
		if (!seen.contains(id))
		{
			seen.add(id);
			return id;
		}
		else
		{
			int i = 2;
			String s;
			do
			{
				s = id + "_" + (i++);
			} while (seen.contains(s));

			seen.add(s);
			return s;
		}
	}
}
