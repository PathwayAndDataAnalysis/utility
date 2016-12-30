package org.panda.utility.statistics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Experimental class to asses how a signal is separated from the noise.
 *
 * @author Ozgun Babur
 */
public class SignalToNoiseDistance
{
	/**
	 * Returns the difference between means after a -log transformation of signal and noise p-values.
	 */
	public static <T> double calculate(Map<T, Double> pvalsSignal, Map<T, Double> pvalsNoise)
	{
		List<Double> signal = new ArrayList<>(pvalsSignal.values().size());
		List<Double> noise = new ArrayList<>(pvalsNoise.values().size());

		for (Double val : pvalsSignal.values())
		{
			signal.add(-Math.log(val));
		}
		for (Double val : pvalsNoise.values())
		{
			noise.add(-Math.log(val));
		}
		double meanSignal = Summary.mean(signal.toArray(new Double[signal.size()]));
		double meanNoise = Summary.mean(noise.toArray(new Double[noise.size()]));

		double sdSignal = Summary.stdev(signal.toArray(new Double[signal.size()]));
		double sdNoise = Summary.stdev(noise.toArray(new Double[noise.size()]));

		return (meanSignal - meanNoise) / (sdSignal + sdNoise);
	}
}
