package org.panda.utility;

import java.util.*;
import java.util.stream.Collectors;

public class RunUtil
{
	public static void run(List<Runnable> runnables, int cores)
	{
		Kronometre kron = new Kronometre();
		kron.start();

		Set<Thread> runningThreads = new HashSet<>();
		List<Thread> queue = runnables.stream().map(Thread::new).collect(Collectors.toCollection(LinkedList::new));

		while (!queue.isEmpty())
		{
			if (runningThreads.size() < cores)
			{
				Thread t = queue.remove(0);
				t.start();
				runningThreads.add(t);
			}
			else
			{
				try
				{
					Thread.sleep(5000);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}

				Set<Thread> alives = runningThreads.stream().filter(Thread::isAlive).collect(Collectors.toSet());
				runningThreads.retainAll(alives);
			}
		}

		System.out.println("\nRunUtil finished");
		kron.print();
	}

	/**
	 * For testing.
	 */
	public static void main(String[] args)
	{
		List<Runnable> runnables = new ArrayList<>();
		for (int i = 0; i < 100; i++)
		{
			Integer a = i;
			runnables.add(new Runnable()
			{
				@Override
				public void run()
				{
					System.out.println("Runnable " + a + " started");
					double x = 0;
					int limit = (int) (Math.random() * 100000000);
					for (int j = 0; j < limit; j++)
					{
						x = Math.random();
					}
					System.out.println("Runnable " + a + " finished");
				}
			});
		}
		run(runnables, 10);
	}
}
