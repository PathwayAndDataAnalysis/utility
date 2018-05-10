package org.panda.utility;

/**
 * Created by babur on 4/18/16.
 */
public class FormatUtil
{
	public static double roundToSignificantDigits(double v, int digits)
	{
		if (v == 0) return v;
		if (v < 0) return -roundToSignificantDigits(-v, digits);

		double x = v;

		int a = 0;
		while (x < 1)
		{
			x *= 10;
			a++;
		}

		if (a == 0)
		{
			while (x > 1)
			{
				x /= 10;
				a--;
			}
		}

		int shift = a + digits - (a > 0 ? 1 : 0);

		double c = 1;

		if (shift > 0)
		{
			for (int i = 0; i < shift; i++) c *= 10;
		}
		else if (shift < 0)
		{
			for (int i = 0; i < -shift; i++) c /= 10;
		}

		double result = Math.round(v * c) / c;
		if (a < 0) result = Math.round(result);
		return result;
	}

	/**
	 * Converts a positive integer to a string with leading zeros.
	 * @param num the number
	 * @param max the maximum number in series
	 * @return the number represented as a string with leading zeros for appropriate alignment
	 */
	public static String posIntToString(int num, int max)
	{
		int space = numOfDigits(max) - numOfDigits(num);
		return zeros(space) + num;
	}

	private static String zeros(int howMany)
	{
		String s = "";
		for (int i = 0; i < howMany; i++)
		{
			s += "0";
		}
		return s;
	}

	private static int numOfDigits(int x)
	{
		int d = 1;

		while (x > 9)
		{
			x /= 10;
			d++;
		}

		return d;
	}
}
