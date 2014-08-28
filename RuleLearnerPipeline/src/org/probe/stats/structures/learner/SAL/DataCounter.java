/* @(#)SALDataExtension.java    v0.8
 * Title: JavaRL
 * Description: rule induction for knowledge discovery
 * Copyright: Copyright (c) 2002
 * Intelligent Systems Laboratory at University of Pittsburgh
 */
package org.probe.stats.structures.learner.SAL;

import java.util.ArrayList;

import org.probe.data.dataset.*;
import org.probe.stats.structures.learner.attribute.LearnerAttribute;

/**
 * Contains a datum and extra information needed for learning.
 * 
 * @author Jeremy Ludwig, Will Bridewell, Eric Williams
 * @version 0.8
 */

public class DataCounter {
	/** This data extension's underlyng datum */
	public String instanceName;

	/**
	 * List of marker counters (conjuncts) associated with att-val pairs that are 
	 * in this datum.
	 */
	public ArrayList<ConjunctCounter> m;

	/**
	 * tIdx[i] should be true if SAL.tvArray[i] matches this datum. Value
	 * Hierarchies make the size unpredictable.
	 */
	protected boolean[] tIdx;

	/**
	 * The number of conjuncts in the current rule that match this datum.
	 * Multiple matching values are possible due to value hierarchies.
	 */
	public int mPerRule;
	
	/**
	 * Constructs a data extension for datum <code>di</code> with the target
	 * attribute <code>target</code> for this run of RL.
	 * 
	 * @param di
	 *            the datum to be associated with this data extension.
	 * @param target
	 *            the target attribute to be associated with this data
	 *            extension.
	 */
	public DataCounter(String di, LearnerAttribute target) {
		instanceName = di;
		m = new ArrayList<ConjunctCounter>();
		tIdx = new boolean[target.getHierarchy().getValueArray().length];
		for (int i = 0; i < tIdx.length; i++)
			tIdx[i] = false;
		mPerRule = 0;
	}

	/**
	 * Add the index of a matching target value -- see TargetValueExtension
	 */
	public void addTargetIndex(int idx) {
		tIdx[idx] = true;
	}

	/**
	 * Returns an array of all indexes into tveArray that match this datum.
	 * There may be more than one because of value hierarchies.
	 * 
	 * @return integer array with target indexes
	 */
	public int[] getTargetIndexes() {
		int i, j;
		int nIdxs = 0;

		for (i = 0; i < tIdx.length; i++)
			if (tIdx[i])
				nIdxs++;
		int[] tmp = new int[nIdxs];
		j = 0;
		for (i = 0; i < tIdx.length; i++)
			if (tIdx[i])
				tmp[j++] = i;
		return tmp;
	}

	/**
	 * Takes a target index into tveArray and says whether or not this datum
	 * matches it.
	 * 
	 * @param index
	 *            the index to be matched.
	 * @return <code>true</code> if <code>index</code> matches the index fo
	 *         the target atribute.
	 */
	public boolean matchesTarget(int index) {
		return (index < 0 || index >= tIdx.length) ? false : tIdx[index];
	}
}
