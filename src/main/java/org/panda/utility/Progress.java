package org.panda.utility;

/**
 * @author Ozgun Babur
 */
public class Progress
{
	/**
	 * Length of the bar.
	 */
	private int length;

	/**
	 * Total count. If this is counted, then the job is complete.
	 */
	private int totalTicks;
	private double total;

	private int counted;

	private Kronometre kron;

	private boolean showRemainingTime = true;

	public Progress(int totalTicks)
	{
		this(totalTicks, DEFAULT_LENGTH, null);
	}

	public Progress(int totalTicks, String message)
	{
		this(totalTicks, DEFAULT_LENGTH, message);
	}

	public Progress(int totalTicks, int length, String message)
	{
		this.length = length;
		this.totalTicks = totalTicks;
		this.total = totalTicks;
		this.kron = new Kronometre();
		kron.start();

		System.out.print("\n|");

		if (message != null)
		{
			System.out.print(" " + message);
			length -= message.length() + 1;
		}

		for (int i = 0; i < length; i++) System.out.print(" ");
		System.out.print("|\n ");
	}

	synchronized public void tick()
	{
		tick(null);
	}

	synchronized public void tick(String message)
	{
		counted++;

		if (counted == totalTicks)
		{
			kron.stop();
			if (showRemainingTime) printWholeProgress();
			System.out.print("  ");
			kron.print();
			return;
		}

		int p = (int) (Math.ceil((counted / total) * length) -
			Math.ceil(((counted - 1) / total) * length));

		if (showRemainingTime)
		{
			if (p > 0)
			{
				printWholeProgress();
				System.out.print(" " + Kronometre.getPrintable(estimateRemainingTime()));
				if (message != null) System.out.print(" (" + message + ")");
			}
		}
		else
		{
			if (p == 1) System.out.print(DOT);
			else if (p > 1) for (int i = 0; i < p; i++) System.out.print(DOT);
		}
	}

	private void printWholeProgress()
	{
		System.out.print("\r ");
		int p = (int) (Math.ceil((counted / total) * length));
		for (int i = 0; i < p; i++)
		{
			System.out.print(DOT);
		}
	}

	private long estimateRemainingTime()
	{
		long passed = kron.getPassedMilisec();
		double ratio = (total - counted) / counted;
		return (long) (passed * ratio);
	}
	
	private static final String DOT = "#";

	public static void main(String[] args)
	{
		for (int i = 0; i < 500; i++)
		{
			System.out.println(i + "\t" + (char)i);
		}

		Progress p = new Progress(100, 54, "A special message");

		for (int i = 0; i < 100; i++) p.tick();
	}

	public static final int DEFAULT_LENGTH = 100;
}
