package org.panda.utility.statistics;

import org.apache.commons.math3.distribution.BinomialDistribution;
import org.junit.Test;

import static org.junit.Assert.*;

public class BinomialTest
{
	@Test
	public void testApache()
	{
		BinomialDistribution d = new BinomialDistribution(20, 0.5);

		for (int i = 0; i <= 20; i++)
		{
			System.out.println(i + " = " + d.probability(i));
		}

	}
}