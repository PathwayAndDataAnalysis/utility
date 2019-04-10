package org.panda.utility;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.*;

public class ROCTest
{
	@Test
	public void getAUC()
	{
		assertEquals(ROC.getAUC(generateList(0, 100), new HashSet<>(generateList(0, 50))), 1, 0);

		assertEquals(ROC.getAUC(generateList(0, 100), new HashSet<>(generateList(50, 100))), 0, 0);
	}

	private List<String> generateList(int startNum, int endNum)
	{
		List<String> list = new ArrayList<>();
		for (int i = startNum; i <= endNum; i++)
		{
			list.add("item-" + i);
		}
		return list;
	}
}