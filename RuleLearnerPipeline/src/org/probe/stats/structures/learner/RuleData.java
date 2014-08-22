/*
 * @(#) RuleLearnerData.java    2002/08/09 v 1.0
 */

package org.probe.stats.structures.learner;

import java.util.ArrayList;

import data.dataset.*;
import org.probe.rule.Rule;

/**
 * Stores extra information about rules that is mostly useful during learning.
 * 
 * 
 * Similar to the class in good ol' Matcher. Here, the field "tpMatches" stores
 * TrPos not already covered. matches and rhsMatches are no longer needed.
 * 
 * @version 1.0 2002/08/09
 */
public class RuleData {
	/** The rule whose information this class stores */
	public Rule rule;

	/**
	 * Matching data for this class's rule's using the sample name lhs and rhs
	 */
	public ArrayList<String> tpMatches;

	/**
	 * Number of times the last added attribute has been used in this rule
	 */
	public int nLastAttUses;

	/** Index of the last added attribute */
	public int iLastConj;

	/**
	 * Should specializations of this rule be placed on the beam? Set this to false
	 * if the rule has already been specialized or if there is no chance that 
	 * specializations will meet the constraints.
	 */
	public boolean shouldSpecialize;

	public ArrayList<String> trainDataMatches;

	public int numberCreated;

	/**
	 * Should the data that this rule matches be considered covered for inductive
	 * strengthening during learning? This can be set to false for prior rules, so 
	 * that prior rules do not alter the search for new rules. 
	 */
	public boolean shouldUseIndStr = true;
	
	/**
	 * Construtcs a store for information about <code>r</code>
	 */
	public RuleData(Rule r) {
		rule = r;
		iLastConj = -1;
		nLastAttUses = 1;
		tpMatches = new ArrayList<String>();
		shouldSpecialize = true;
		trainDataMatches = new ArrayList<String>();
	}

	/**
	 * Copy all of <code>rld</code> except for specialized status list of
	 * tpMatches
	 */
	public RuleData(RuleData rld) {
		rule = (Rule) rld.rule.clone();
		iLastConj = rld.iLastConj;
		nLastAttUses = rld.nLastAttUses;
		tpMatches = new ArrayList<String>();
		shouldSpecialize = true;
		trainDataMatches = new ArrayList<String>();
	}

	/**
	 * Clears matching data for this rule. Also resets the true positives count
	 * and the false positives count.
	 */
	public void resetMatchLists() {
		tpMatches = new ArrayList<String>();
		rule.setTrainTruePos(0);
		rule.setTrainFalsePos(0);
		trainDataMatches = new ArrayList<String>();
	}

	/**
	 * Adds <code>d</code> as a true positive match for this rule
	 */
	public void addTpMatch(String d) {
		tpMatches.add(d);
	}

	public void addTrainMatch(String d) {
		trainDataMatches.add(d);
	}

	/**
	 * Returns the list of all true positive matches for this rule.
	 */
	public ArrayList<String> getTPMatches() {
		return tpMatches;
	}

	public ArrayList<String> getTrainMatches() {
		return trainDataMatches;
	}

	/**
	 * Returns the number of data newly covered by this rule. That is, data 
	 * that are not covered by prevoius rules, namely for which 
	 * <code>DataModel.isCovered(i)</code> is not ture. 
	 * This method must only be called before <code>cover()</code>, 
	 * otherwise the result is meaningless.
	 */
	public int getInductiveStrength(DataModel ds) {
		int nIndStr = 0;
		for (int x = 0; x < ds.numInstances(); x++) {
			if (rule.matchLhs(ds, x)) {
				try {
					if (!(ds.isCovered(x)))
						nIndStr++;
				} catch (InstanceNotFoundException e) {
					e.printStackTrace();
				}
			}
		}

		return nIndStr;
	}

	/**
	 * Mark this rule's matching data as covered. Once a rule is covered, its
	 * inductive strengthening cannot be calculated. Call this method <i>after</i>
	 * getIndStr() and not before.
	 */
	public void cover(DataModel ds) {
		for (int x = 0; x < ds.numInstances(); x++) {
			if (rule.matchLhs(ds, x)) {
				try {
					if (!(ds.isCovered(x)))
						ds.cover(x);
				} catch (InstanceNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
		/*
		 * for (int x = 0; x < tpMatches.size(); x++) {
		 * if(!ds.getDatum(tpMatches.get(x)).isCovered())
		 * ds.getDatum(tpMatches.get(x)).changeCovered(); }
		 */
	}

	/**
	 * Prints the rule.
	 */
	public String toString() {
		return rule.toString();
	}
}
