/*
 * @(#)SAL.java
 */

package org.probe.algo;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;

import org.probe.algo.rule.LearnerParameters;
import org.probe.algo.rule.RuleGenerator;
import org.probe.stats.structures.cf.CertaintyFactor;
import org.probe.stats.structures.cf.PValueRight;

// import structures.constraints.Constraint;
import data.dataset.*;

import org.probe.stats.structures.learner.HeapPriorityQueue;
import org.probe.stats.structures.learner.PriorityQueue;
import org.probe.stats.structures.learner.RuleData;
import org.probe.stats.structures.learner.RuleDataComparator;
import org.probe.stats.structures.learner.SAL.ConjunctCounter;
import org.probe.stats.structures.learner.SAL.DataCounter;
import org.probe.stats.structures.learner.SAL.TargetValueCounter;
import org.probe.stats.structures.learner.attribute.AttributeList;
import org.probe.stats.structures.learner.attribute.LearnerAttribute;
import org.probe.stats.structures.learner.attribute.VHierarchyNode;
import org.probe.rule.Conjunct;
import org.probe.rule.Rule;
import org.probe.util.RuleList;

/**
 * Implementation of the SAL algorithm.
 * 
 * SAL isn't the simplest algorithm to understand. I've tried to make
 * explanatory comments as I go along. In addition, there are a few
 * implementation notes at the end of this file that may come in handy.
 * 
 * There are several equally good ways of simplifying the task of matching rules
 * to data. In this program, I just picked a way similar to that described in
 * Aronis and Provost's paper "Increasing the Efficiency of Data Mining
 * Algorithms with Breadth-First Marker Propagation". The real power of SAL is
 * in the way that it handles specialization.
 * 
 * To understand my implementation of SAL, it helps to know that the design of
 * this class was based heavily on the design already used with moderate success
 * in Matcher. As there was nothing inherently wrong with the structure of
 * Matcher, I decided that using it as a rough map would simplify the task of
 * implementing SAL while keeping all of the necessary details in my head.
 * 
 * Matcher.java is horribly out-of-date. It is no longer being updated.
 * 
 * TODO:
 * 
 * 1. Use prior rules for learning, rather than just applying them.
 * 
 * 2. Now SAL specializes rules by adding previously added conjuncts if more
 * than one conjunct from a particular attribute can be added. This is a small
 * waste of time, but other than that it's no big deal.
 * 
 * 3. Make most data structures non-global: pass them to the functions. This 
 * makes it easier to follow what each function modifies.
 * 
 * 4. Intermittant Beam size can grow too large if there are a sufficient number
 * of acceptable rules.
 * 
 * Updated to avoid specialization on ID fields. Updated to handle
 * value-hierarchies on the rhs.
 * 
 * @since 1.1 (old numbering)
 * @version 0.8 (new numbering)
 * @author Will Bridewell, Eric Williams
 * 
 * Moved DataComparator to separate file.
 * 
 * @version 1.2
 * @author ?
 * 
 * Formatted the code, comments and implementation notes.
 * 
 * @version 1.3
 * @author Philip Ganchev
 */

public class SAL implements RuleGenerator {
	private AttributeList attList;
	private PriorityQueue beam, newBeam;
	private int nRules;
	private ArrayList<RuleData> goodRules;
	private LearnerParameters parameters;
	private int nTrainCovered;
	private DataModel trainData;

	/**
	 * Holds all attribute-value pairs. A marker counter is an extension of
	 * Conjunct that knows which data it matches, how many times each target
	 * value was predicted by the current rule containing this MarkerCounter,
	 * which data the rule predicts correctly (TPs).
	 */
	private ConjunctCounter[] conjunctCounters;

	/**
	 * Holds information about each training datum. It contains the actual 
	 * datum, a list of all conjuncts that match it, the number of conjuncts in 
	 * the current rule that match it, and the target hierarchy values which the 
	 * datum's target value represents.
	 */
	private DataCounter[] dataCounters;

	/**
	 * Holds all target values, a count of data that match each target value,
	 * and a boolean representing whether it is a leaf node or an internal node
	 * of a value hierarchy. The SDE and MC data structures keep track of
	 * positions of TVEs in the array to avoid redundant matching.
	 */
	private TargetValueCounter[] targetValCounters;

	private CertaintyFactor pValueCalc = new PValueRight();
	private PrintStream pr = System.out;

	/**
	 * @param al
	 *			the list of Attributes in the data set
	 */
	public SAL(AttributeList al, LearnerParameters p) {
		attList = al;
		setParameters(p);
		// attList.computeInfoGains(); PG20040215

		init();

		if (parameters.verbosity > 1) {
			try {
				pr = new PrintStream("beams.out");
			} catch (java.io.FileNotFoundException x) {
				x.printStackTrace();
				return;
			}
		}
	}

	/**
	 * Prepares the rule learner for a fresh run.
	 */
	public void init() {
		nRules = 0;
		beam = null;
		conjunctCounters = null;
		targetValCounters = null;
		dataCounters = null;
		goodRules = new ArrayList<RuleData>();
	}

	public void setParameters(LearnerParameters p) {
		parameters = p;
		trainData = p.trainData;
	}

	/**
	 * Returns the number of rules examined by the last run of RL
	 * 
	 * @return the number rules examined by the last run of RL
	 */
	public int ruleCount() {
		return nRules;
	}

	public void setDataModel(DataModel trnData) {
		trainData = trnData;
	}

	/** Creates all unique single-conjunct rules and puts them on the beam */
	protected void createSingletonRules(LearnerAttribute target, PriorityQueue pBeam) {
		for (int iDatum = 0; iDatum < dataCounters.length; iDatum++) {
			// Add one to the appropriate target value in connected
			// attribute-value pairs (marker counters)
			for (Iterator it = dataCounters[iDatum].m.iterator(); it.hasNext();) {
				ConjunctCounter conjCounter = (ConjunctCounter) it.next();
				// Since there will be a tic-mark for each value in the hierarchy, there
				// will be more tic-marks than data. To avoid counting matches 
				// multiple times, see TargetValueExtension.isLeaf().
				int[] ixs = dataCounters[iDatum].getTargetIndexes();
				for (int j = 0; j < ixs.length; j++) {
					conjCounter.tv[ixs[j]]++;
				}
			}
		}

		// Create one rule per target value (class value) and per 
		// attribute-value pair (conjunct counter, previously called 
		// "marker counter").
		for (int iConj = 0; iConj < conjunctCounters.length; iConj++) {
			for (int iTargetVal = 0; iTargetVal < conjunctCounters[iConj].tv.length; iTargetVal++) {
				if (conjunctCounters[iConj].tv[iTargetVal] > 0) {
					// Add the current conjunct to a rule and fill its
					// information fields.
					Rule r = new Rule(parameters.getMaxConjuncts(),target,  
						targetValCounters[iTargetVal].getConjunct().getValue(), 
						iTargetVal);
					RuleData ruleD = new RuleData(r);
					ruleD.rule.addConjunct(conjunctCounters[iConj], 0);
					ruleD.iLastConj = conjunctCounters[iConj].getIndex();
					ruleD.nLastAttUses = 1;
					ruleD.shouldSpecialize = true;

					// This rule correctly matches (TP) all matching data that predicts 
					// the current target. This is for inductive strengthening.
					for (int iDatum = 0; iDatum < 
							conjunctCounters[iConj].matchingData.size(); iDatum++) {
						if (((DataCounter) conjunctCounters[iConj].matchingData
								.get(iDatum)).matchesTarget(iTargetVal))
							ruleD.addTpMatch(((DataCounter) 
									conjunctCounters[iConj].matchingData.get(iDatum)).instanceName);
					}

					// Calculate the true positives, etc., and the CF for the rule
					computeRuleStats(ruleD, iConj, iTargetVal);

					// Add the rule to the beam, unless it is incorrigible
					if (!isBadRule(ruleD)) {
						pBeam.enqueue(ruleD);
					} else {
					}
				}
			}
		}

		pBeam = processGoodRules(pBeam, parameters.shouldSpecializeGoodRules());
		pBeam.trimToCapacity();
	}
	
	/**
	 * Performs a beam search for rules that satisfy the constraints, from training data.
	 * 
	 * @return the list of Rules
	 */
	
	public RuleList generateRules() {
		nTrainCovered = 0;
		LearnerAttribute target = attList.getTargetAttribute();

		// Create the data structures
		conjunctCounters = createConjunctArray(target); // holds all attribute-value pairs
		//dataCounters = createDataArray(trainData, target); // holds data + extra info
		targetValCounters = createTveArray(target); // holds all target conjuncts

		 //Create links between the data and the conjuncts, filling the arrays 
		 // conjunctCounters, dataCounters, and classValCounters (previously 
		 // called mcArray, sdeArray, tvArray).
		//createLinks();

		// Initialize the counters
		//clearStructureInfo(conjunctCounters, dataCounters);
		
		beam = new HeapPriorityQueue(new RuleDataComparator(), 
				parameters.getBeamWidth());
		if (parameters.verbosity > 2) {
			System.out.print("Attributes: ");
			for (int i = 0; i < attList.size(); i++) {
				System.out.print(((org.probe.stats.structures.learner.attribute.LearnerAttribute) attList.get(i)).getName() + " ");
			}
			System.out.println();
		}
		
		beam = processGoodRules(beam, parameters.shouldSpecializeGoodRules());
		// Create singleton rules and put them on the beam. Can't use empty 
		// rules because the matching below relies on conjuncts.
		createSingletonRules(target, beam);

		/* Beam search -  specialize rules while there are still some on the beam */
		newBeam = new HeapPriorityQueue(new RuleDataComparator(), 
															parameters.getBeamWidth());	
		while (!beam.isEmpty()) {
			newBeam.clear();
			// Examine each rule on the beam for specialization
			for (int iRule = 0; iRule < beam.size(); iRule++) {
				// TODO: Before processing each rule, check if the user has decided 
				// to stop the learning. If so, process the old, sorted, pruned beam, 
				// and ignore newBeam, which is still unstable at this point.

				RuleData ruleD;
				try {
					ruleD = (RuleData) beam.get(iRule);
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
				// TODO: Do not add the old rule on the new beam. It has already
				// been saved if it is good. --PG2009			
				specializeRule(ruleD);
			}
			// "newBeam" now contains the rules to be evaluated, some of which
			// are specialized
			beam = processGoodRules(newBeam, parameters.shouldSpecializeGoodRules());
			beam.trimToCapacity();

			// Exit if all training data are covered and we care about
			// inductive strengthening.
			if (parameters.getInductiveStrengthening() > 0
					&& nTrainCovered >= trainData.numInstances() - 1) {
					// Why "-1"? --PG2009
				return getModelAndEnd(beam);
			}
			
			// Exit if all rules on the beam have been specialized.
			// Must come after the beam has been truncated
			// and pruned of rules that don't match the set parameters!
			// TODO: Replace this with a check above for whether the rule
			// should be specialized, and remove it if not. The beam will become 
			// empty if there are no more rules to
			// specialize.
			if (!hasRulesToSpecialize(beam)) {
				return getModelAndEnd(beam);
			}
		} // There are no more rules on the beam
		return getModelAndEnd(beam);
	}

	protected void specializeRule(RuleData ruleD) {
		if (!ruleD.shouldSpecialize)
			return;

		ruleD.shouldSpecialize = false;

		// If the rule already has as many conjuncts as are allowed by the "max 
		// conjuncts" parameter, do not specialize
		if (ruleD.rule.getConjunctCount() >= parameters.getMaxConjuncts())
			return;

		newBeam.enqueue(ruleD);
		
		int iTV = findTargetValIndex(ruleD.rule, targetValCounters);
		//assert (iTV>-1);

		computeMatchStats(ruleD, iTV);

		/* Find appropriate conjuncts to add to the rule. */

		// For each marker counter that has both:
		// a) An attribute index >=  the largest attribute index in this rule, 
		// b) Some number > 0 in the target value slot that corresponds
		// to the target value for this rule...
		// Iterate through the conjuncts inside the rules on the beam instead of all
		// conjuncts. This is faster when there are many attrubutes, and seems to
		// result in more accurate models in any case.
		for (int j = 0; j < beam.size(); j++) {
			RuleData rD = null;
			try {
				rD = (RuleData) beam.get(j);
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
			Rule r = rD.rule;
			Conjunct conj = r.getLhs().getConjunct(r.getLhs().size() - 1);
			int iConj = conj.getIndex();
		
			if ((conjunctCounters[iConj].tv[iTV] <= 0)
					|| ((conjunctCounters[iConj].getIndex() <= ruleD.iLastConj)))
				continue;

			/*
			 * TODO: it might be possible to optimize this code by not
			 * creating the new rule if it fails some of the constraints of
			 * staying on the beam. This would avoid object creation, rule
			 * evaluation and adding of items to the beam.
			 */

			// Create a new rule with this conjunct added and fill
			// in the proper values for the fields.
			RuleData newRuleD = new RuleData(ruleD);
			newRuleD.rule.addConjunct(conjunctCounters[iConj]);
			newRuleD.iLastConj = iConj;
			newRuleD.shouldSpecialize = true;
			newRuleD.tpMatches = conjunctCounters[iConj].newTP;
			computeRuleStats(newRuleD, iConj, iTV);

			// Add the new rule to the new beam, unless it fails some 
			// constraint that cannot be improved by specializing the rule 
			if (!isBadRule(newRuleD)) {
				newBeam.enqueue(newRuleD);
			}
		}
	}
	
	protected ArrayList<RuleData> specializeRules(ArrayList <RuleData> rules, String attName) {
		ArrayList newRules = new ArrayList();
		for (int i = 0; i < rules.size(); i++) {
			RuleData rule = rules.get(i);
			for (ConjunctCounter conjunct: conjunctCounters) {
				if (parameters.verbosity > 1)
					System.out.print(" ?=" + conjunct);
				if (conjunct.matches(attName)) {
					if (parameters.verbosity > 1)
						System.out.println("Found.");
					RuleData newRule = new RuleData(rule);
					newRule.rule.addConjunct(conjunct);
					newRule.iLastConj = conjunct.getIndex();
					newRule.nLastAttUses = 1;	// TODO: check how many times it is used
					newRules.add(newRule);	 
				}
			}
		}
		return newRules;
	}

	protected int findTargetValIndex(Rule rule, TargetValueCounter[] tVals) {
		// Find the position of this rule's target value in classValCounters 
		// (tvArray).
		for (int iTv = 0; iTv < tVals.length; iTv++) {
			// We can test for strict equality here because the rule
			// and classValCounters (tvArray) will refer to the same object.
			if (tVals[iTv].getConjunct().getValue() 
					== rule.getTargetValue()) {
				return iTv;
			}
		}
		return -1;
	}
	
	protected void computeMatchStats(RuleData ruleD, int iTV) {
		clearStructureInfo(conjunctCounters, dataCounters);
		// Mark the instances that match the rule. If the
		// rule has x conjuncts, then the instance should have x
		// matches.
		for (Enumeration conjEn = ruleD.rule.elements(); 
				conjEn.hasMoreElements();)
			for (Iterator matchIt = ((ConjunctCounter) conjEn
					.nextElement()).matchingData.iterator(); matchIt.hasNext();)
				((DataCounter) matchIt.next()).mPerRule++;
		
		// For each DataCounter (SALDataExtension) object whose
		// conjuncts all match the data, add 1 to the appropriate 
		// target value in connected marker counters.
		for (int iConj = 0; iConj < dataCounters.length; iConj++) {
			// If this datum matches the current rule
			if (dataCounters[iConj].mPerRule == ruleD.rule.getConjunctCount()) {
				// Pass markers (carrying the observed target value)
				// from this datum to all matching conjuncts
				for (int iMc = 0; iMc < dataCounters[iConj].m.size(); iMc++) {
					ConjunctCounter tmp = (ConjunctCounter) dataCounters[iConj].m.get(iMc);
					int[] ixs = dataCounters[iConj].getTargetIndexes(); 
					// All the targets that sample matches
					for (int k = 0; k < ixs.length; k++) {
						tmp.tv[ixs[k]]++;
					}
					// If new LHS & RHS matches (true pos), keep track of the 
					// datum for possible use with inductive strengthening.
					if (dataCounters[iConj].matchesTarget(iTV)) {
						tmp.newTP.add(dataCounters[iConj].instanceName);
					}
				}
			}
		}
	}
	
	protected boolean hasRulesToSpecialize(PriorityQueue beam) {
		for (int i = 0; i < beam.size(); i++) {
			try {
				if (((RuleData) beam.get(i)).shouldSpecialize) {
					return true;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	/**
	 * Finds those rules on the beam that meet the requirements of goodRule()
	 * and the inductive strength requirements, and records their coverage. If
	 * the "prune specialized" option is set, the method moves those rules from
	 * the bream to the list of good rules. Rules are examined in order from
	 * highest to lowest.
	 * 
	 * @param origBeam
	 * @return processed beam
	 */
	private PriorityQueue processGoodRules(PriorityQueue origBeam,
																boolean specializeGoodRules) {
		PriorityQueue pBeam = new HeapPriorityQueue(new RuleDataComparator(),
				parameters.getBeamWidth());

		ArrayList<RuleData> origList = new ArrayList<RuleData>();
		try {
			origList = origBeam.toSortedArrayList();
		} catch (Exception e) {
			e.printStackTrace();
		}

		RuleData ruleD;
		boolean isGood = false;

		/*
		 * Find the instances covered by the current model. This is for
		 * inductive strengthening and determining if the training set has been
		 * covered (stop condition)...
		 */

		/* TODO: optimization: can we avoid uncovering the data, but just cover 
		 * the new good rules? */
		//trainData.uncoverAll();
		nTrainCovered = 0;

		// If there are good rules that were removed from the beam, re-cover
		// the data that they match.
		/*for (int x = 0; x < goodRules.size(); x++) {
			ruleD = (RuleData) goodRules.get(x);
			if (ruleD.shouldUseIndStr) {
				nTrainCovered += ruleD.getInductiveStrength(trainData);
				ruleD.cover(trainData);
			}
		}*/
				
		// Find good rules on the beam
		for (int x = 0; x < origList.size(); x++) {
			// TODO: insert stopping ability
			
			ruleD = (RuleData) origList.get(x);
			isGood = isGoodRule(ruleD);
			
			if (ruleD.shouldUseIndStr) {
				// Count the number of data newly covered by this rule
				int curIndStr = ruleD.getInductiveStrength(trainData);
	
				// Skip this rule if it fails the inductive strengthening threshold
				if (curIndStr < parameters.getInductiveStrengthening())
					continue;
			
				// If the rule is good, cover its matching data and update the 
				// total data covered.
				if (isGood) {
					ruleD.cover(trainData);
					nTrainCovered += curIndStr;
				}
			}
			
			if (isGood)
				goodRules.add(ruleD);
			
			if (!isGood || specializeGoodRules)
				pBeam.enqueue(ruleD);
		}
		return pBeam;
	}

	/**
	 * Incrementally matches the rule to the data and calculates true positive,
	 * false positive, total negative, total positive and cf. This method is
	 * called during rule specialization.
	 * 
	 * @param rld
	 * @param iConj
	 * @param iTV
	 */
	private void computeRuleStats(RuleData rld, int iConj, int iTV) {
		Rule rule = rld.rule;

		// How many data matched the LHS of this new rule?
		// Only count the leaves or else we'll be double dipping
		int totMatches = 0;
		for (int i = 0; i < conjunctCounters[iConj].tv.length; i++) {
			if (targetValCounters[i].isLeaf()) {
				totMatches += conjunctCounters[iConj].tv[i];
			}
		}

		int tp, fp, totP, totN;
		// Conjunct matches and predicts the target value for this rule
		// LHS && RHS
		tp = conjunctCounters[iConj].tv[iTV];
		// Conjunct matches but predicts some other target value
		// LHS && !RHS
		fp = totMatches - tp;

		// Data that match the target conjunct. That is, the number of positive
		// instances, not the number of instances predicted to be positive!
		// totPos = (lhs && rhs) + (!lhs && rhs)
		totP = targetValCounters[iTV].targetCount;
		totN = trainData.numInstances() - totP;

		rule.setTrainTruePos(tp);
		rule.setTrainFalsePos(fp);
		rule.setTrainNeg(totN);
		rule.setTrainPos(totP);
		rule.setCf(CertaintyFactor.getCfArray()[parameters.getCfMethod()], 
				attList.getTargetAttribute(), trainData);
		rule.setWorth(attList.getTargetAttribute());

		nRules++;
	}

	/**
	 * Matches the rule to the training data, and re-calculate its statistics on
	 * the current training data alone. The statistics are true positive, false
	 * positive, total negative, total positive rate, and cf.
	 */
	protected void computeRuleStats(Rule rule) {
		int tp = 0, fp = 0, totP = 0, totN = 0;
		for (int y = 0; y < trainData.numInstances(); y++) {
			boolean matchL = rule.matchLhs(trainData, y);
			boolean matchR = rule.matchRhs(trainData, y);
	
			if (matchL) {
				//totP++;
				if (matchR)
					tp++;
				else
					fp++;
			}
		}
		// Set the statistics for the rule
		int iTV = findTargetValIndex(rule, targetValCounters);
		totP = targetValCounters[iTV].targetCount; // positive instances (not matches)
		totN = trainData.numInstances() - totP;	// negative instances

		rule.setTrainTruePos(tp);
		rule.setTrainFalsePos(fp);
		rule.setTrainNeg(totN);
		rule.setTrainPos(totP);
		rule.setCf(CertaintyFactor.getCfArray()[parameters.getCfMethod()], 
						attList.getTargetAttribute(), trainData);
		rule.setWorth(attList.getTargetAttribute());
		if (CertaintyFactor.getCfArray()[parameters.getCfMethod()].
											getClass().equals(pValueCalc.getClass())) {
			rule.setPValue(rule.getCf());
		} else {
			rule.setPValue(pValueCalc.getCf(rule, 
					attList.getTargetAttribute(), trainData));
		}
	}

	/**
	 * Examines those properties of a rule that can be improved by specializing
	 * it. A rule that is not "bad" (see method <code>isBadRule()</code>) and
	 * is "good" (this method) satisfies the search constraints, and so should
	 * be part of the final model.
	 * 
	 * @return true if the rule satisfies all of the following: 1. has at least
	 *		 the minimum # of conjuncts, 2. meets the CF threshold, 3. meets
	 *		 the false positive coverage requirements, 4. satisfies all other
	 *		 constraints.
	 * 
	 * @param rld
	 *			rule learner data
	 */
	private boolean isGoodRule(RuleData rld) {
		// Has at least the minimum conjuncts
		if (rld.rule.getConjunctCount() < parameters.getMinConjuncts()) {
			return false;
		}

		// Meets or exceeds the CF threshold
		if (rld.rule.getCf() < parameters.getMinCf()) {
			return false;
		}

		/*
		 * TODO: If user enters a floating point number > 1, we get a 
		 * NumberFormatException! This should be handled somehow.
		 */
		// Less than or equal to FP coverage
		if (parameters.getMaxFP() >= 0) {
			if (parameters.getMaxFP() < 1.0) {	// It is a proportion	
				if (rld.rule.getFalsePos() 
						> (rld.rule.getNeg() * parameters.getMaxFP())) {
					return false;
				}
			} else 					// It is an absolute number
				if (rld.rule.getFalsePos() 
						> (int) parameters.getMaxFP()) {
					return false;
			}
		}
		return true;
	}

	/**
	 * Examines those properties of a rule that cannot be improved by
	 * specializing it: coverage, and tp coverage. A rule that is bad should not
	 * be on the beam. A rule that is not bad and also satisfies method
	 * <code>isGoodRule()</code> satisfies the search constraints, and so
	 * should be in the final rule model.
	 * 
	 * @param rld
	 *			rule learner data
	 * @return true if the rule satisfies any of the following: (1) has no true
	 *		 positive matches (2) the minimum coverage parameter is specified
	 *		 and the rule does not have the minimum number of matches, (3) a
	 *		 coverage parameter is specified and the rule does not accurately
	 *		 predict the specified number of instances.
	 */
	private boolean isBadRule(RuleData rld) {
		Rule r = rld.rule;

		// Does not match any instances?
		if (r.getTruePos() == 0) {
			return true;
		}

		// Does it match the specified minimum number of instances?
		if (parameters.getMinCoverage() >= 0) {
			// Assume it is percentage
			if (parameters.getMinCoverage() < 1.0) {
				if ((r.getTruePos() + r.getFalsePos()) < 
						(trainData.numInstances() * parameters.getMinCoverage())) {
					return true;
				}
			// It is an absolute number
			} else if ((r.getTruePos() + r.getFalsePos()) < 
					parameters.getMinCoverage()) {
				return true;
			}
		}

		// Does it correctly match the specified minimum number of instances?
		if (parameters.getMinTP() >= 0) {
			// Ã�ï¿½ssume it is percentage
			if (parameters.getMinTP() < 1.0) {
				if (r.getTruePos() < (r.getPos() * parameters.getMinTP())) {
					return true;
				}
				// It is an absolute number
			} else if (r.getTruePos() < parameters.getMinTP()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Creates an array of all attribute-value pairs except for those describing the
	 * target attribute.
	 * 
	 * @param target
	 *			attribute to learn
	 * @return MarkerCounter matrix for SAL
	 */
	private ConjunctCounter[] createConjunctArray(LearnerAttribute target) {
		//int targetIdx = target.getIndex();
		ConjunctCounter[] mcArray = new ConjunctCounter[attList.countAttributeValues()];
		int iAttVal = 0;
		int nTargetVals = target.getHierarchy().numValues();
		for (int i = 0; i < attList.size(); i++) {
			LearnerAttribute att = (LearnerAttribute) attList.get(i);
			if (att.isOutput() || att.isIgnore() || att.isID())
				continue;
			// Create a new marker counter for each attribute value pair
			VHierarchyNode[] attVals = att.getHierarchy().getValueArray();
			for (int j = 0; j < attVals.length; j++) {
				mcArray[iAttVal] = new ConjunctCounter(att, attVals[j], nTargetVals, 
						iAttVal);
				iAttVal++;
			}
		}
		return mcArray;
	}

	/**
	 * Creates an array of all data extensions
	 * 
	 * @param dl
	 *			data list
	 * @param target
	 *			Attribute to learn
	 * @return SALDataExtension array
	 */
	private DataCounter[] createDataArray(DataModel dl,
			LearnerAttribute target) {
		int dlLength = dl.numInstances();
		DataCounter[] sdeArray = new DataCounter[dlLength];
		for (int i = 0; i < dlLength; i++) {
			try {
				sdeArray[i] = new DataCounter(dl.getInstanceName(i),
						target);
			} catch (Exception e) {
				sdeArray[i] = new DataCounter("S" + (i + 1), target);
			}
		}
		return sdeArray;
	}

	/**
	 * Creates an array of att-val pairs associated with the target attribute
	 * 
	 * @param t
	 * @return TargetValueExtension array
	 */
	private TargetValueCounter[] createTveArray(LearnerAttribute t) {
		VHierarchyNode hier = t.getHierarchy();
		TargetValueCounter[] arr = new TargetValueCounter[hier.numValues()];
		for (int i = 0; i < hier.numValues(); i++) {
			arr[i] = new TargetValueCounter(new Conjunct(t, hier.getValue(i), i));
		}

		return arr;
	}

	/**
	 * Creates links from each datum to its matching conjuncts and back. Keeps
	 * track of the datum's target index (in the array of target values)
	 */
	private void createLinks() {
		for (int i = 0; i < dataCounters.length; i++) {
			for (int j = 0; j < conjunctCounters.length; j++) {
				// If the current attribute-value pair matches the current
				// datum, add links from the a-v pair to the datum and back.
				// Note: Conjunct.matchesDatum(di) automatically checks the
				// children in a value hierarchy, so the value hierarchy will take
				// care of itself. In this case, parent nodes are treated like leaves.
				if (conjunctCounters[j].matchesDatum(trainData, i)) {
					conjunctCounters[j].matchingData.add(dataCounters[i]);
					dataCounters[i].m.add(conjunctCounters[j]);
				}
			}
			/*
			 * Associate the datum with the appropriate target-value pair
			 * located in the target value array.
			 * YELLOW FLAG: Assumes that the target isn't a set attribute.
			 */
			for (int j = 0; j < targetValCounters.length; j++) {
				if (targetValCounters[j].getConjunct().matchesDatum(trainData, i)) {
					dataCounters[i].addTargetIndex(j);
					targetValCounters[j].targetCount++;
					// Keep checking this datum as the target may be a value 
					// hierarchy.
				}
			}
		}
	}

	/**
	 * Initialize counters for a new iteration of SAL.
	 * 
	 * @param mcArray
	 * @param sdeArray
	 */
	private void clearStructureInfo(ConjunctCounter[] mcArray,
			DataCounter[] sdeArray) {
		// For each element of mcArray clearCount
		// For each element of sdeArray zero mPerRule
		for (int i = 0; i < mcArray.length; i++) {
			mcArray[i].clearCounts();
		}

		for (int i = 0; i < sdeArray.length; i++) {
			sdeArray[i].mPerRule = 0;
		}
	}

	/**
	 * Adds the the good rules from the beam to the list of good rules, and
	 * destroys the beam and the other data structures. The good rules are
	 * transformed into Rule objects and returned as a RuleList. This method is
	 * to be called when the rule learner stops.
	 * 
	 * @param inBeam
	 * @return the good rules as a RuleList
	 */
	private RuleList getModelAndEnd(PriorityQueue inBeam) {
		RuleList model = new RuleList();
		
		// Move the good rules from the beam to the list of good rules.
		// (Ignore the returned beam.)
		processGoodRules(inBeam, true);
		
		int nRetainedRules = 0;
		java.util.Set transferredAtts = new java.util.HashSet();
		
		// Convert the list of "good rules" to a rule list, by rebuilding the
		// rules using Conjunct instead of ConjunctCounter (MarkerCounter). This
		// preserves memory by removing unnecessary fields.
		for (int count = 0; count < goodRules.size(); count++) {
			RuleData ruleD = (RuleData) goodRules.get(count);
			Rule rule = new Rule(ruleD.rule.getConjunctCount(), attList
					.getTargetAttribute(), ruleD.rule.getTargetValue(),
					ruleD.rule.getRhs().getIndex());
			for (Enumeration e = ruleD.rule.elements(); e.hasMoreElements();) {
				Conjunct oldConjunct = (Conjunct) e.nextElement();
				Conjunct newConjunct = new Conjunct(attList
						.getTargetAttribute(), oldConjunct.getValue(),
						oldConjunct.getIndex());
				rule.addConjunct(newConjunct, 0);
			}
			rule.setTrainTruePos(ruleD.rule.getTruePos());
			rule.setTrainFalsePos(ruleD.rule.getFalsePos());
			rule.setTrainNeg(ruleD.rule.getNeg());
			rule.setTrainPos(ruleD.rule.getPos());
			rule.setCf(ruleD.rule.getCf());
			rule.setWorth(attList.getTargetAttribute());			
			/* **************************
			 * If p-value is made a learning parameter (that is, is used during 
			 * search, move this code to method computeRuleStats().
			 */
			if (CertaintyFactor.getCfArray()[parameters.getCfMethod()].getClass()
					.equals(pValueCalc.getClass())) {
				rule.setPValue(rule.getCf());
			} else {
				rule.setPValue(pValueCalc.getCf(rule, attList.getTargetAttribute(), trainData));
			}
			/* ***************************/
			rule.setIndex(ruleD.rule.getIndex());
			rule.isPriorRule = ruleD.rule.isPriorRule;
			if (rule.isPriorRule) {
				nRetainedRules++;
				transferredAtts.addAll(rule.getLhs().getAttributes());
			}
			model.add(rule);
		}
		
		// Destroy all the data structures to free the memory
		inBeam = null;
		targetValCounters = null;
		conjunctCounters = null;
		dataCounters = null;
		System.gc();		
		return model;
	}

	/** @param ordBeam
	 * TODO: this method should be in PriorityQueue. --PG2009
	 */
	private void printBeam(PriorityQueue pBeam) {
		ArrayList<RuleData> ordBeam = null;
		try { 
			ordBeam = pBeam.toSortedArrayList();
		} catch (Exception x) {
			x.printStackTrace();
			return;
		}
		for (int i = 0; i < ordBeam.size(); i++) {
			pr.println("Rule " + (i + 1) + ": "
					+ ((RuleData) ordBeam.get(i)).rule.toString());
		}
		pr.println("--------------\n\n");
	}

	/** PG2009 debug */
	public void printConjuncts() {
		for (int j = 0; j < conjunctCounters.length; j++) {
			System.out.print(conjunctCounters[j]);
		}
		System.out.println();
	}
}

/**
 * <h2>Notes about the implementation</h2>
 * 
 * <h3>SAL Basics</h3>
 * 
 * SAL speeds rule learning by doing all necessary matching just once. Each
 * possible conjunct (attribute-value pair) is matched to each datum exactly
 * once. Each conjunct knows every datum it matches, and every datum knows each
 * conjunct that matches it. There is a tradeoff of space for time, as we have
 * to store the history of the matches with each conjunct.
 * 
 * <h3>Data Structures Used by SAL</h3>
 * 
 * SAL uses three main data structures:
 * <ul>
 * <li><code><strong>MarkerCounter</strong></code> (stored in
 * <code>MCArray</code>) is an extension of Conjunct that knows which data it
 * matches, how many times each target value was predicted by the current rule
 * containing this MarkerCounter, which data the rule predicts correctly (TPs).
 * 
 * <li><code><strong>SALDataExtension</strong></code> (stored in
 * <code>SDEArray</code>) Contains information about a training datum. In
 * addition to the actual datum, it contains a list (m) of all MarkerCounters
 * (conjuncts) that match it, the number of conjuncts in the current rule that
 * match it, and the target hierarchy values which the datum's target value
 * represents.
 * 
 * <li><code><strong>TargetValueExtension</strong></code> stores information
 * about a target value. It is stored in <code>tveArray</code>, which covers
 * all possible target values. TVE contains the target value conjunct, a count
 * of data that match this target value, and a boolean stating whether it is a
 * leaf node or an internal node of a value hierarchy. The other two data
 * structures keep track of positions of TVEs in the array to avoid redundant
 * matching.
 * </ul>
 * 
 * This implementation creats conjuncts only once and reuses them for each rule.
 * This not only speeds up the code by reducing object creation, but it is also
 * essential to the correct operation of SAL. If more conjuncts were created,
 * then more matching would be needed.
 * 
 * <h3>SAL, the Algorithm</h3>
 * 
 * Here is a brief overview of the algorithm. It does not cover the creation of
 * the initial rules. This is a special case, but if you understand the
 * following, then you should be able to figure out the differences in about 15
 * minutes & one cup of coffee. At this time, all the MarkerCounters have been
 * mated with the SDE's and vice versa. The data structures are established and
 * we just need to use them. The initial, one-conjunct rules have been created,
 * tossed on the beam, and processed. The algorithm below is SAL + some other
 * ideas.
 * 
 * <pre>
 * 1.  Look at each rule on the beam individually.
 *		 a)  Mark the rule as specialized and put it back on the beam. This allows 
 *		 us to: 
 *				 1. Continue to specialize the rule, and 
 *				 2. Know that a rule has been specialized before.
 *						 a. If a rule has been specialized once, then all its children 
 *							have been added to the beam before.  Any future 
 *							specialization is redundant.
 *						 b. We still specialize the rule again.  It costs a little 
 *							in time and space, but since we're using beam search, this 
 *							improves our odds of seeing as many specializations as 
 *							possible. (It's likely that we could avoid respecialization
 *							w/o affecting the rules that are output.  This should be 
 *							tested before implementation.)
 *		 b)  Try to specialize the rule
 *				1. Keep track of the rule's target value, as we will need to
 *				   know it in order to identify true positive matches of the
 *				   specialization.
 *				2. Initialize the data structures needed by SAL
 *				3. Tally counters
 *						 a) for each conjunct in the current rule, 
 *						 			for all data that match that conjunct,
 *						 				pass a "+" from the conjunct to the datum.
 *						 b) for each datum that has as many "+''s
 *							as there are conjuncts in the current rule (the
 *							rule has X conjuncts, and all X match that datum)
 *								 1. Pass a "1" to the correct target value
 *									associated with each conjunct that the
 *									datum matches.  (Tell the conjunct
 *									that one of its data has an observed
 *									target value Y.)
 *								 2. If we put a "1" in the target value
 *									associated with the current rule, then
 *									keep track of it as a true positive
 *									prediction for the new rule, which is the
 *									current rule + the conjunct that we just
 *									added the "1" to. (All conjuncts in the
 *									current rule match, the new conjunct
 *									matches, and the rule predicts the correct
 *									target value.)
 *						 At this point, we haven't selected any new conjuncts to use 
 *						 in specializing this rule.  We are just setting up all possible 
 *						 conjuncts to make selection simpler later on.
 *				4. Choose conjuncts for specialization
 *						a. Only consider conjuncts whose index is larger than the last 
 *						conjunct in the current rule.  This prevents creating duplicate 
 *						rules, such as:
 *									IF (a1 = t) AND (at2 = f) THEN tar = t
 *									IF (a2 = f) AND (at1 = t) THEN tar = t
 *						b.  If we can add multiple conjuncts from one
 *							attribute (continuous values or set values), then
 *							also look at conjuncts that have an index the
 *							same as the last conjunct of the current rule.
 *							This will create duplicate rules, and may also introduce 
 *							rules that have the same conjunct repeated:
 *									  IF (at1 = T) AND (at1 = T) THEN tar = T
 * 
 *							This should be considered a bug!
 *				 5. Create the new rule from the old rule + the current
 *					 conjunct under consideration. Update the true positives count.
 *				 6. Process the new rule and add it to the beam.
 * 2.  Sort and truncate the beam
 * 3.  Process the remaining rules on the beam.
 *	  See processRules() for the details. This is also where unfit rules are pruned.
 * 4.  Check stopping conditions:
 *		 a. All training items have been covered (only if ind. str. is on).
 *		 b. All rules on the beam have been specialized at least once.
 *			 If all rules have been specialized at least once, then no more good 
 *			 rules are being generated and added to the beam, so no more good
 *			 rules can be found. 
 *		 c. The user presses "stop" -- checked in various places.
 *		 d. The beam is empty
 *				  1. This is most likely to happen when we prune good rules
 *					  from the beam, not allowing them to be specialized.
 *				  2. It can also happen if the parameters are too strict.
 * </pre>
 * 
 * In all my changes, I have kept the following principle in mind:
 * 
 * Effort should be taken to separate the algorithm from the data. No extra
 * fields should be added to any existant structures.
 * 
 * In defense of <code>MarkerCounter</code>, <code>SALDataExtension</code>,
 * <code>TargetValueExtension</code>, and <code>RuleData</code>:
 * 
 * These classes probably appear to be ad hoc hacks. In reality, I spent a good
 * deal of time deciding what should and should not go into them. However, since
 * they are private classes specifically designed to make SAL easier to
 * implement, I had fewer inhibitions about adding fields that were only vaguely
 * related (e.g. newTP). Additionally, I wasn't too meticulous about creating
 * accessor/modifier functions for each field or using meaningful names for the
 * same reason.
 * 
 * <h3>Value Hierarchy</h3>
 * 
 * There is virtually no mention of the value hierarchy in the above code. The
 * nature of SAL is such that when the initial matches are created, all of the
 * nodes in a value hierarchy are also matched appropriately by propogating
 * matches up the tree. Afterwards, we treat all nodes in the value hierarchy
 * the same.
 * 
 * The only time we have to worry about the value hierarchy is when we are
 * counting numbers. If we aren't careful, we will count a single match at both
 * the leaf level and the grouping node level. I think that I was careful. This
 * only surfaced as a problem during processRule() when I had to calculate false
 * positives.
 * 
 * 
 * <h3>Inductive Strengthening</h3>
 * 
 * This is taken care of in processRules() only. Each <code>RuleData</code>
 * object keeps track of the true positives that its rule matches. Before
 * <code>processRules()</code> is called, the beam is sorted. The rules are
 * then examined in order, and data items are considered "covered" as their
 * matching rules are seen on the beam.
 * 
 * To keep track of the true positives, an extra field had to be added to
 * <code>MarkerCounter</code>. So, as markers are being passed from the data
 * to the conjuncts, if the datum is correctly predicted by the rule + the new
 * conjunct, the MarkerCounter (conjunct) remembers. Then when the new rule is
 * created, we just use that stored list as the list of its true positives.
 * 
 * Idea:
 * 
 * If a rule covers (predicts correctly) a datum, then it owns that datum, and
 * any subsequent rule on the beam that also covers that datum does not get to
 * count that as a new positive. If a rule only covers data previously covered
 * by previous rules, then it doesn't add anything to the rule set. Since those
 * rules appear earlier on the beam, they are better than the rule in question.
 * If the inductive strengthening parameter is set to n, then each rule will
 * have to cover n data that previous rules did not cover. If it doesn't, it
 * will be pruned from the beam.
 * 
 * Inductive strengthening is processed each time the beam is processed as a
 * whole. So, if better rules come along, they can oust previous rules that had
 * been considered the best. This changes slightly when "Prune Specialized" is
 * turned on, because previously good rules cannot be ousted.
 */
// The presence of this class allows the long notes above to fold in Eclipse and other IDEs.
class X {}