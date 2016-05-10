package org.panda.utility;

/**
 * Class to represent value - p-value pairs. For instance it can be correlation - correlation-p-value tuple.
 *
 * Created by babur on 4/25/16.
 */
public class Tuple
{
	/**
	 * The value.
	 */
	public double v;

	/**
	 * The p-value
	 */
	public double p;

	public Tuple(double v, double p)
	{
		this.v = v;
		this.p = p;
	}
}
