package org.panda.utility;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class ChartColorsList
{
	public static final List<Color> colors = new ArrayList<>();

	static
	{
		colors.add(new Color(26, 68, 129));
		colors.add(new Color(236, 82, 44));
		colors.add(new Color(248, 211, 79));
		colors.add(new Color(102, 154, 54));
		colors.add(new Color(115, 27, 35));
		colors.add(new Color(146, 200, 250));
		colors.add(new Color(51, 63, 16));
		colors.add(new Color(179, 205, 64));
		colors.add(new Color(70, 34, 107));
		colors.add(new Color(241, 154, 59));
		colors.add(new Color(181, 46, 30));
		colors.add(new Color(55, 130, 202));
	}

	public static int size()
	{
		return colors.size();
	}

	public static Color get(int index)
	{
		int i = index % size();
		return colors.get(i);
	}

	public static String getString(int index)
	{
		Color c = get(index);
		return c.getRed() + " " + c.getGreen() + " " + c.getBlue();
	}
}
