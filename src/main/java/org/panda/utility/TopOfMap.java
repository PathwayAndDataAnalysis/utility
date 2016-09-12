package org.panda.utility;

import org.apache.commons.collections4.list.TreeList;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Keeps top of a growing list. Does not support removing an element from the list as only top portion is stored.
 *
 * @author Ozgun Babur
 */
public class TopOfMap<K, E>
{
	private TreeList<K> list;
	Comparator<K> com;
	private int capacity;
	private Map<K, E> map;

	public TopOfMap(Comparator<E> com, int capacity)
	{
		this.list = new TreeList<>();
		this.capacity = capacity;
		this.map = new HashMap<>();
		this.com = (o1, o2) -> com.compare(map.get(o1), map.get(o2));
	}

	public void put(K key, E ele)
	{
		if (map.containsKey(key)) throw new IllegalArgumentException("Map already has this key: " + key);

		map.put(key, ele);
		if (list.size() == capacity)
		{
			if (com.compare(key, list.get(list.size() - 1)) < 0)
			{
				K removedKey = list.remove(list.size() - 1);
				map.remove(removedKey);
				list.add(findInsertionIndex(key, 0, list.size()), key);
			}
			else map.remove(key);
		}
		else list.add(findInsertionIndex(key, 0, list.size()), key);
	}

	public E get(K key)
	{
		return map.get(key);
	}

	public Set<K> keySet()
	{
		return map.keySet();
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Key\tValue");
		for (K key : list)
		{
			sb.append("\n").append(key).append("\t").append(map.get(key));
		}
		return sb.toString();
	}

	public int size()
	{
		return list.size();
	}

	private int findInsertionIndex(K key, int left, int right)
	{
		if (left == right) return left;

		int mid = (left + right) / 2;

		int c = com.compare(key, list.get(mid));

		if (c == 0) return mid;
		else if (c < 0) return findInsertionIndex(key, left, mid);
		else return findInsertionIndex(key, mid + 1, right);
	}
}
