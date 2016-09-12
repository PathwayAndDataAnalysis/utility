package org.panda.utility;

import org.apache.commons.collections4.list.TreeList;

import java.util.*;

/**
 * Keeps top of a growing list. Does not support removing an element from the list as only top portion is stored.
 *
 * @author Ozgun Babur
 */
public class TopOfList<T>
{
	private TreeList<T> list;
	Comparator<T> com;
	private int capacity;

	public TopOfList(Comparator<T> com, int capacity)
	{
		this.list = new TreeList<>();
		this.com = com;
		this.capacity = capacity;
	}

	public void add(T ele)
	{
		if (list.size() == capacity)
		{
			if (com.compare(ele, list.get(list.size() - 1)) < 0)
			{
				list.remove(list.size() - 1);
				list.add(findInsertionIndex(ele, 0, list.size()), ele);
			}
		}
		else list.add(findInsertionIndex(ele, 0, list.size()), ele);
	}

	public T get(int index)
	{
		return list.get(index);
	}

	@Override
	public String toString()
	{
		return list.toString();
	}

	public int size()
	{
		return list.size();
	}

	private int findInsertionIndex(T ele, int left, int right)
	{
		if (left == right) return left;

		int mid = (left + right) / 2;

		int c = com.compare(ele, list.get(mid));

		if (c == 0) return mid;
		else if (c < 0) return findInsertionIndex(ele, left, mid);
		else return findInsertionIndex(ele, mid + 1, right);
	}
}
