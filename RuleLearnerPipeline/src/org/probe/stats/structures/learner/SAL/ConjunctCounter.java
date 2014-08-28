package org.probe.stats.structures.learner.SAL;

import java.util.ArrayList;

import org.probe.data.dataset.*;
import org.probe.stats.structures.learner.attribute.LearnerAttribute;
import org.probe.stats.structures.learner.attribute.VHierarchyNode;
import org.probe.rule.Conjunct;

/**
 * An extension of Conjunct used in Rules during learning using SAL. After the
 * learning, all marker counters in rules can be replaced with standard
 * conjuncts. The extra object creation may hamper performance if the rule set
 * is large.
 * 
 * <p>
 * Title: JavaRL
 * </p>
 * <p>
 * Description: rule induction for knowledge discovery
 * </p>
 * <p>
 * Copyright: Copyright (c) 2002
 * </p>
 * <p>
 * Company: Intelligent Systems Laboratory at University of Pittsburgh
 * </p>
 * 
 * @author Jeremy Ludwig, Will Bridewell, Eric Williams
 * @version 0.8 (new version numbering system)
 */

public class ConjunctCounter extends Conjunct {
	/** Contains a count, for each target value, of how many data we marked */
	public int[] tv;

	/**
	 * List of all data that contain this att-val pair. It must be expandable
	 * since we don't know how many data match.
	 */
	public ArrayList<DataCounter> matchingData;

	/**
	 * Used to track data uniquely covered (as true positives) by a rule that
	 * has just added this conjunct. Used to calculate inductive strengthening
	 * of a rule.
	 */
	public ArrayList<String> newTP;

	public ArrayList<String> matchedTrainData;

	/**
	 * Create a MarkerCounter for attribute a and value v that can be matched to
	 * nTargetValues target values.
	 * 
	 * @param a
	 *            The LearnerAttribute which contains information about the
	 *            attribute
	 * @param v
	 *            The Hierarchy node or the value of the attribute
	 * @param nTargetValues
	 *            The number of target variables
	 */
	public ConjunctCounter(LearnerAttribute a, VHierarchyNode v,
			int nTargetValues, int mkInd) {
		super(a, v, mkInd);
		matchingData = new ArrayList<DataCounter>();
		tv = new int[nTargetValues];
		newTP = new ArrayList<String>();
		matchedTrainData = new ArrayList<String>();
	}

	/**
	 * Clears target counts and also clears newTP.
	 */
	public void clearCounts() {
		for (int i = 0; i < tv.length; i++)
			tv[i] = 0;
		// This gives the programmer (i.e. you) the option of copying
		// or cloning newTP. newTP.clear() would create the need to
		// clone the list if the data must be retained.
		// Warning: this may create noticeable slowdown between
		// iterations of SAL.
		newTP = new ArrayList<String>();
	}
}
