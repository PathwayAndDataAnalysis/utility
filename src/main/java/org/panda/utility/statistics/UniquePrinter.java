package org.panda.utility.statistics;

import java.util.HashSet;
import java.util.Set;

/**
 * This is a debug utility class to print certain things only one during the program execution.
 */
public class UniquePrinter
{
	private Set<String> printed;

	public UniquePrinter()
	{
		this.printed = new HashSet<>();
	}

	public void print(String prefix, String unique)
	{
		if (!printed.contains(unique))
		{
			System.out.println(prefix + unique);
			printed.add(unique);
		}
	}
}
