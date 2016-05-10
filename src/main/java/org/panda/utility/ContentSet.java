package org.panda.utility;

import java.util.Collection;
import java.util.HashSet;

/**
 * This class is for identifying a set only with its contents. It is useful when sets with identical contents need to be
 * considered equal.
 *
 * Warning: The content of this set should not be modified after adding it to a HashSet or HashMap, since its hashCode
 * changes by content modifications. If that it violated, then the Set or Map operations may become unpredictable.
 *
 * @author Ozgun Babur
 */
public class ContentSet<E> extends HashSet<E>
{
	public ContentSet()
	{
		super();
	}

	public ContentSet(Collection<? extends E> c)
	{
		super(c);
	}

	@Override
	public int hashCode()
	{
		int h = 0;
		for (E e : this)
		{
			h += e.hashCode();
		}
		return h;
	}

	@Override
	public boolean equals(Object o)
	{
		return o instanceof ContentSet && ((ContentSet) o).size() == size() && ((ContentSet) o).containsAll(this);
	}
}
