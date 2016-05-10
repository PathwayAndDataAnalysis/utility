package org.panda.utility;

import java.awt.*;
import java.util.Arrays;

/**
 * Created by babur on 3/9/16.
 */
public class ValToColor
{
	double[] keyValues;
	Color[] keyColors;

	public ValToColor(double[] keyValues, Color[] keyColors)
	{
		if (keyValues.length != keyColors.length)
		{
			throw new IllegalArgumentException("keyValues and keyColors have to be the same size.");
		}
		for (int i = 1; i < keyValues.length; i++)
		{
			if (keyValues[i] <= keyValues[i - 1]) throw new IllegalArgumentException("Key values have to increase " +
				"monotonically. Key values = " + Arrays.toString(keyValues));
		}

		this.keyValues = keyValues;
		this.keyColors = keyColors;
	}

	public String getColorInString(double v)
	{
		Color c = getColor(v);
		return c.getRed() + " " + c.getGreen() + " " + c.getBlue();
	}

	public Color getColor(double v)
	{
		if (v <= keyValues[0]) return keyColors[0];
		if (v >= keyValues[keyValues.length - 1]) return keyColors[keyColors.length - 1];

		for (int i = 0; i < keyValues.length; i++)
		{
			if (v == keyValues[i]) return keyColors[i];
		}

		double[] val = getValueBounds(v);
		Color[] col = getColorBounds(v);

		return new Color(
			getColorComponent(v, val[0], val[1], col[0].getRed(), col[1].getRed()),
			getColorComponent(v, val[0], val[1], col[0].getGreen(), col[1].getGreen()),
			getColorComponent(v, val[0], val[1], col[0].getBlue(), col[1].getBlue()));
	}

	private int getColorComponent(double val, double lowVal, double highVal, int lowColor, int highColor)
	{
		double valDif = highVal - lowVal;

		double changeExtend = (val - lowVal) / valDif;

		double colDif = highColor - lowColor;

		return (int) Math.round(lowColor + (colDif * changeExtend));
	}

	private double[] getValueBounds(double v)
	{
		for (int i = 0; i < keyValues.length - 1; i++)
		{
			if (v > keyValues[i] && v < keyValues[i + 1]) return new double[]{keyValues[i], keyValues[i + 1]};
		}
		throw new RuntimeException("Code should not reach here!");
	}

	private Color[] getColorBounds(double v)
	{
		for (int i = 0; i < keyValues.length - 1; i++)
		{
			if (v > keyValues[i] && v < keyValues[i + 1]) return new Color[]{keyColors[i], keyColors[i + 1]};
		}
		throw new RuntimeException("Code should not reach here!");
	}
}
