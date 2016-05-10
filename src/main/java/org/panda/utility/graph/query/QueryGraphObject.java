package org.panda.utility.graph.query;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ozgun Babur
 */
public class QueryGraphObject
{
	private Map<Object, Object> labelMap;

	/**
	 * Puts the specified label.
	 */
	public void putLabel(Object label)
	{
		if (labelMap == null) labelMap = new HashMap<>();
		labelMap.put(label, null);
	}

	/**
	 * Updates the value of the label. Or creates if does not exist.
	 */
	public void putLabel(Object label, Object value)
	{
		if (labelMap == null) labelMap = new HashMap<>();
		labelMap.put(label, value);
	}

	/**
	 * Checks if the specified label type exists on the node.
	 */
	public boolean hasLabel(Object label)
	{
		return labelMap != null && labelMap.containsKey(label);
	}

	/**
	 * Checks if the specified type label exists and its value matches to the
	 * second parameter.
	 */
	public boolean hasLabel(Object label, Object value)
	{
		return labelMap != null && labelMap.get(label) != null && labelMap.get(label).equals(value);
	}

	/**
	 * Gets the associated label on the object specified with the key.
	 */
	public Object getLabel(Object label)
	{
		if (labelMap == null) return null;
		return labelMap.get(label);
	}

	/**
	 * Removes the label specified with the parameter key.
	 */
	public void removeLabel(Object label)
	{
		if (labelMap == null) return;
		labelMap.remove(label);
	}


}
