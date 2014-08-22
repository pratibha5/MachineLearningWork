/*
 * SALV2.java
 *
 * Created on November 27, 2006, 9:56 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package algo.rl.rule;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;

import parameters.LearnerParameters;
import structures.cf.CertaintyFactor;
import structures.cf.PValueRight;
import structures.constraints.Constraint;
import data.dataset.*;
import structures.learner.HeapPriorityQueue;
import structures.learner.PriorityQueue;
import structures.learner.RuleData;
import structures.learner.RuleDataComparator;
import structures.learner.SAL.ConjunctCounter;
import structures.learner.SAL.DataCounter;
import structures.learner.SAL.TargetValueCounter;
import structures.learner.attribute.AttributeList;
import structures.learner.attribute.LearnerAttribute;
import structures.learner.attribute.VHierarchyNode;
import rule.Conjunct;
import rule.Rule;
import rule.RuleList;

/**
 * This is the second version of SAL originally implemented by Will Bridewell,
 * Philip Ganchev and Eric Williams This second version has multiple benefits
 * over the original. It has rule generation seperated from rule specialization
 * The way the rules are handled have been updated. No longer does the algorithm
 * look at every single rule during rule specialization Since if you have a
 * perfect rule (no FP) you don't want to shrink its coverage since it can't get
 * any better It has multiple
 * 
 * @author Jonathan Lustgarten
 */
public class SALV2 implements RuleGenerator {
	private PriorityQueue finalRules[], firstRules[];

	private RuleList[] rules;

	private AttributeList attList;

	private int nRuleCount;

	private LearnerParameters parameter;

	private Dataset trainSet;

	/** Holds all attribute-value pairs */
	private ConjunctCounter[] mcArray;

	/** Holds data + extra info */
	private DataCounter[] sdeArray;

	/** Holds all target conjuncts */
	private TargetValueCounter[] tveArray;

	private CertaintyFactor pValueCalc = new PValueRight();

	/**
	 * Creates a new instance of SALV2
	 * 
	 * @param al
	 *            The attribute list containing all the Attribute-values
	 */
	public SALV2(AttributeList al) {
		attList = al;
		// attList.computeInfoGains(); PG20040215
		// Now the misclassification cost file can be read
		// NOTE: the file is now read at import time and the matrices are
		// primed there if all works well after 4/30/2004, this can be removed.
		// Priming here will just zap the costs that were already read.
		// attList.primeMatrix();
		init();
	}

	/**
	 * Prepares the rule learner for a fresh run.
	 */
	public void init() {
		nRuleCount = 0;
		firstRules = null;
		finalRules = null;
		mcArray = null;
		tveArray = null;
		sdeArray = null;
	}

	/**
	 * Returns the number of rules examined by the last run of RL
	 * 
	 * @return the number rules examined by the last run of RL
	 */
	public int ruleCount() {
		return nRuleCount;
	}

	/**
	 * Determines whether the rule is a valid rule (may have some FPs) but
	 * passes the positive coverage as well as minimum coverage as well as has
	 * some TPs
	 */
	private boolean isValidRule(Rule r) {
		if (r.getTruePos() == 0)
			return false;

		// Prune if rule doesn't match at least x examples
		if (parameter.getMinCoverage() >= 0) {
			// Assume it is percentage
			if (parameter.getMinCoverage() < 1.0) {
				if ((r.getTruePos() + r.getFalsePos()) < (trainSet
						.numInstances() * parameter.getMinCoverage())) {
					return false;
				}
				// It is an absolute number
			} else if ((r.getTruePos() + r.getFalsePos()) < (int) parameter
					.getMinCoverage()) {
				return false;
			}
			// return false;
		}

		// Prune if tp coverage too low - specializing won't improve coverage
		if (parameter.getMinTP() >= 0) {
			// assume it is percentage
			if (parameter.getMinTP() < 1.0) {
				if (r.getTruePos() < (r.getPos() * parameter
						.getMinTP())) {
					return false;
				}
				// It is an absolute number
			} else if (r.getTruePos() < (int) parameter
					.getMinTP()) {
				return false;
			}
		}
		return true;

	}

	/**
	 * Determines whether the rule matches what we call the perfect condition
	 * Has < FP Tolerance, has All True Positives and Passes the CF threshold
	 * Similar to isGoodRule in old SAL
	 */
	private boolean isGoodRule(Rule r) {
		if (!isValidRule(r))
			return false;

		if (r.getConjunctCount() < parameter.getMinConjuncts()) {
			return false;
		}

		// Meets or exceeds cf threshold
		if (r.getCf() < parameter.getMinCf()) {
			return false;
		}
		// Less than or equal to fp coverage
		if (parameter.getMaxFP() >= 0) {
			// Assume it is percentage
			if (parameter.getMaxFP() < 1.0) {
				if (r.getFalsePos() > (r.getNeg() * parameter
						.getMaxFP())) {
					return false;
				}
			}
			// It is an absolute number
			else if (r.getFalsePos() > (int) parameter
					.getMaxFP()) {
				return false;
			}
		}

		return (true);
	}

	/**
	 * Creates links from each datum to its matching conjuncts and back keeps
	 * track of the datum's target index (in the array of target values)
	 */
	private void createLinks() {
		for (int i = 0; i < sdeArray.length; i++) {
			for (int j = 0; j < mcArray.length; j++) {
				// If the current attribute-value pair matches the current
				// datum, add links from the a-v pair to the datum and back.
				// Note: Conjunct.matchesDatum(di) automatically checks the
				// children in a value hierarchy, so the value hierarchy will
				// take care of itself. In this case, parent nodes are
				// treated just like leaves.
				if (mcArray[j].matchesDatum(this.trainSet, i)) {
					mcArray[j].matchingData.add(sdeArray[i]);
					sdeArray[i].m.add(mcArray[j]);
				}
			}
			/*
			 * YELLOW FLAG--- Assumes that the target isn't a set of values
			 * Associate the datum with the appropriate target-value pair
			 * located in the target value array.
			 */
			for (int j = 0; j < tveArray.length; j++) {
				if (tveArray[j].getConjunct().matchesDatum(this.trainSet, i)) {
					sdeArray[i].addTargetIndex(j);
					tveArray[j].targetCount++;
					// Keep checking this datum as the target may be
					// a value hierarchy
				}
			}
		}
	}

	/**
	 * Initialize counters for each iteration of SAL.
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
	 * Returns the total number of possible unique conjuncts (att-val pairs)
	 * minus those associated with the target value.
	 * 
	 * @return the total number of possible unique conjuncts (att-val pairs)
	 *         minus those associated with the target value
	 */
	private int getNumberConjuncts() {
		int count = 0;
		for (Iterator iter = attList.iterator(); iter.hasNext();) {
			LearnerAttribute att = (LearnerAttribute) iter.next();
			if (!att.isOutput() && !att.isIgnore() && !att.isID()) {
				count += att.getHierarchy().numValues();
			}
			// System.out.println("Number of counts for possible conjuncts for
			// Attribute "+att.getName()+": "+att.getHierarchy().numValues());
			// System.out.println("Number of total conjuncts so far: "+count);
		}

		return count;
	}

	/**
	 * Creates an array of all att-val pairs except for those describing the
	 * target attribute.
	 * 
	 * @param target
	 *            attribute to learn
	 * @return MarkerCounter matrix for SAL
	 */
	private ConjunctCounter[] createMCArray(LearnerAttribute target) {
		int targetIdx = target.getIndex();
		ConjunctCounter[] mcArray = new ConjunctCounter[getNumberConjuncts()];
		int attValCounter = 0;
		int nTargetValues = target.getHierarchy().numValues();
		for (int i = 0; i < attList.size(); i++) {
			LearnerAttribute currAtt = (LearnerAttribute) attList.get(i);
			// ignore target, ID and user-ignored attributes
			if (currAtt.isOutput() || currAtt.isIgnore() || currAtt.isID())
				continue;
			// Create a new marker counter for each attribute value pair
			VHierarchyNode[] currValues = currAtt.getHierarchy()
					.getValueArray();
			for (int j = 0; j < currValues.length; j++) {
				mcArray[attValCounter] = new ConjunctCounter(currAtt,
						currValues[j], nTargetValues, attValCounter);
				attValCounter++;
			}
		}
		return mcArray;
	}

	/**
	 * Creates an array of all data extensions
	 * 
	 * @param dl
	 *            data list
	 * @param target
	 *            Attribute to learn
	 * @return SALDataExtension array
	 */
	private DataCounter[] createDataArray(Dataset dl,
			LearnerAttribute target) {
		int dlLength = dl.numInstances();
		DataCounter[] sdeArray = new DataCounter[dlLength];
		for (int i = 0; i < dlLength; i++) {
			try {
				sdeArray[i] = new DataCounter(dl.instanceName(i), target);
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
	private TargetValueCounter[] createTVEArray(LearnerAttribute t) {
		VHierarchyNode hier = t.getHierarchy();
		TargetValueCounter[] arr = new TargetValueCounter[hier.numValues()];
		for (int i = 0; i < hier.numValues(); i++) {
			arr[i] = new TargetValueCounter(new Conjunct(t, hier.getValue(i),
					i));
		}

		return arr;
	}

	/**
	 * Takes the prior rules, adds a little information necessary for SAL, and
	 * adds them to the list of good rules. They are not added to the firstRules
	 * because they are not candidates for pruning.
	 * 
	 * It is assumed that all prior rules use the same attributes that are used
	 * in the training file.
	 * 
	 * For prior rules, we have the following choices: 
	 * 1. Whether to allow them to influence learning based on the data they 
	 * cover.
	 * 
	 * 2. How to handle their statistics: 
	 * 		a. Do not modify them. 
	 * 		b. Update them based on the current data set. 
	 * 		c. Re-evaluate them based only on the current data.
	 * 
	 * 3. Whether to allow them to be pruned in favor of better rules, or to
	 * make them permanent throughout learning.
	 * 
	 * 1, 2, and 3 should be possible with the current code. You would have to
	 * keep in mind that the prior rules do not use the same conjuncts as the
	 * rules generated by SAL, so if the prior rules influence the learning
	 * process, make sure you deal with them properly (see notes on SAL's reuse
	 * of conjuncts).
	 * 
	 * 2a and 2b require the ability to maintain statistics for rules between
	 * sessions. This isn't currently implemented, but it would just involve
	 * exporting the proper information with the rule set and reading the
	 * information back in.
	 * 
	 * Assumptions that should be dealt with when working with prior rules are
	 * 1. The prior rules use the same attributes as the current training data
	 * 2. The prior rules use the same values as the current training data 3.
	 * The prior rules have the same target value that we want our learned rules
	 * to predict.
	 * 
	 * This processing of prior rules makes no assumptions. It just tries to
	 * match the lhs and rhs of the rule to each data item. To avoid dealing
	 * with these assumptions in detail, I have implemented 2, 5, and 7 from the
	 * above list of possibilities.
	 * 
	 * @see ExpertHandler for how prior rules are used after they have been used
	 *      in the training set.
	 */
	private void processPriorRules() {
		Rule curRule;
		CertaintyFactor cf = CertaintyFactor.getCfArray()[parameter
				.getCfMethod()];
		// Ensure that there are prior rules
		if (!parameter.hasPriorRules())
			return;
		// Look at each prior rule
		for (int i = 0; i < parameter.getPriorRules().size(); i++) {
			curRule = (Rule) parameter.getPriorRules().get(i);

			// Make sure we only process prior rules that have the right
			// target attribute
			if (curRule.getRhs().getIndex() == attList
					.getTargetAttribute().getIndex()) {
				// Match the rule to the training data, re-calculating its
				// statistics based on the current training data.
				int truePos, falsePos, totNeg, totPos;
				truePos = falsePos = totNeg = totPos = 0;
				for (int y = 0; y < trainSet.numInstances(); y++) {
					boolean matchL = curRule.matchLhs(trainSet, y);
					boolean matchR = curRule.matchRhs(trainSet, y);

					if (matchR) {
						totPos++;
					} else {
						totNeg++;
					}
					if (matchL && matchR) {
						truePos++;
					} else if (matchL && !matchR) {
						falsePos++;
					}
				}
				// Set the numbers for the rule
				curRule.setTrainTruePos(truePos);
				curRule.setTrainFalsePos(falsePos);
				curRule.setTrainNeg(totNeg);
				curRule.setTrainPos(totPos);
				curRule.setCf(cf, attList.getTargetAttribute(), trainSet);
				curRule.setWorth(attList.getTargetAttribute());
				if (cf.getClass().equals(pValueCalc.getClass())) {
					curRule.setPValue(curRule.getCf());
				} else {
					curRule.setPValue(pValueCalc.getCf(curRule, attList
							.getTargetAttribute(), trainSet));
				}
			}
		}
	}

	/**
	 * Calculates true positive, false positive, total negative, total positive
	 * and cf.
	 * 
	 * @param rld
	 *            The Current Rule
	 * @param conjN
	 *            Number of Conjuncts
	 * @param tvPos
	 *            The index of the target value
	 */
	private void processRule(RuleData rld, int conjN, int tvPos) {
		processRule(rld, mcArray[conjN], tvPos);
	}

	private void processRule(RuleData rld, ConjunctCounter mc, int tvPos) {
		Rule r = rld.rule;
		CertaintyFactor cf = CertaintyFactor.getCfArray()[parameter
				.getCfMethod()];
		// How many data matched the lhs of this new rule?
		// Only count the leaves or else we'll be double dipping
		int totMatches = 0;
		for (int i = 0; i < mc.tv.length; i++) {
			if (tveArray[i].isLeaf()) {
				totMatches += mc.tv[i];
			}
		}

		int trPos, faPos, totNeg, totPos;
		// Conjunct matched and predicted the target value for this rule
		// LHS && RHS
		trPos = mc.tv[tvPos];
		// Conjunct matched but predicted some other target value
		// LHS && !RHS
		faPos = totMatches - mc.tv[tvPos];

		// All things that match the target conjunct
		// totPos = lhs && rhs + !lhs && rhs
		totPos = tveArray[tvPos].targetCount;

		// Everything either matches the target conjunct or doesn't,
		// so here's the quick math.
		// totNeg = lhs && !rhs + !lhs && !rhs
		totNeg = trainSet.numInstances() - totPos;

		r.setTrainTruePos(trPos);
		r.setTrainFalsePos(faPos);
		r.setTrainNeg(totNeg);
		r.setTrainPos(totPos);
		r.setCf(cf, attList.getTargetAttribute(), trainSet);
		r.setWorth(attList.getTargetAttribute());
		/***********************************************************************
		 * YELLOW FLAG ********* This is a HUGE drain on performance. We could
		 * move this to the end and only calculate P-Value when we have the
		 * final rule set, but if it becomes a parameter for learning, then we
		 * need the calculation for every rule.
		 */
		if (cf.getClass().equals(pValueCalc.getClass())) {
			r.setPValue(r.getCf());
		} else {
			r.setPValue(pValueCalc.getCf(r, attList.getTargetAttribute(),
					trainSet));
		}
		/* *********************************************** */
		nRuleCount++;
	}

	public void setDataset(Dataset trnData) {
		trainSet = trnData;
	}

	public void setParameters(LearnerParameters p) {
		parameter = p;
		trainSet = parameter.trainData;
	}

	/**
	 * Master Learning Function (controls rule gen and rule special)
	 * 
	 * @param trainData
	 *            The Training Data to learn from
	 * @param p
	 *            The Learning paramters to use
	 */
	public RuleList generateRules() {
		LearnerAttribute target = attList.getTargetAttribute();

		/* Create the data structures */

		mcArray = createMCArray(target); // holds all attribute-value pairs
		sdeArray = createDataArray(trainSet, target); // holds data + extra
		// info
		tveArray = createTVEArray(target); // holds all target conjuncts

		/*
		 * Create links between the data and the conjuncts, filling mcArray,
		 * sdeArray, tvArray and targetClassCount.
		 */
		createLinks();

		processPriorRules();

		// Create the firstRules for firstRules search
		firstRules = new HeapPriorityQueue[parameter.getMaxConjuncts()];
		finalRules = new HeapPriorityQueue[parameter.getMaxConjuncts()
				- parameter.getMinConjuncts() + 1];
		for (int i = 0; i < parameter.getMaxConjuncts(); i++) {
			firstRules[i] = new HeapPriorityQueue(new RuleDataComparator(),
					parameter.getBeamWidth());
			if (i < parameter.getMaxConjuncts() - parameter.getMinConjuncts() + 1)
				finalRules[i] = new HeapPriorityQueue(new RuleDataComparator(),
						parameter.getBeamWidth());
		}
		// Initialize for an iteration of SAL
		clearStructureInfo(mcArray, sdeArray);

		/* Create single-conjunct rules */

		// Start at the following step because the initial rules have 0
		// conjuncts on the LHS. Therefore, all data matches all rules.
		// For each datum (SALDataExtension object)
		for (int nDatum = 0; nDatum < sdeArray.length; nDatum++) {
			// Add one to the appropriate target value in connected
			// attribute-value pairs (marker counters)
			for (Iterator it = sdeArray[nDatum].m.iterator(); it.hasNext();) {
				ConjunctCounter markerCounter = (ConjunctCounter) it.next();
				// Since there will be a tic-mark for each value in the
				// hierarchy, there will be more tic-marks than data.
				// To avoid counting matches multiple times, see the
				// isLeaf() section of TargetValueExtension.
				int[] idxs = sdeArray[nDatum].getTargetIndexes();
				for (int j = 0; j < idxs.length; j++) {
					markerCounter.tv[idxs[j]]++;
				}
				// System.out.println("Marker counter:
				// "+markerCounter.toString());
			}
		}

		// Create one rule per target value for each attribute-value pair
		// (marker counter).
		for (int nMarker = 0; nMarker < mcArray.length; nMarker++) {
			for (int nTargetVal = 0; nTargetVal < mcArray[nMarker].tv.length; nTargetVal++) {
				if (mcArray[nMarker].tv[nTargetVal] > 0) {
					Rule tempRule = new Rule(parameter.getMaxConjuncts(), target,
							tveArray[nTargetVal].getConjunct().getValue(),
							nTargetVal);

					RuleData newRLD = new RuleData(tempRule);
					// Add current conjunct to rule and set up the
					// information fields.
					newRLD.rule.addConjunct(mcArray[nMarker], nMarker);
					newRLD.iLastConj = mcArray[nMarker].getIndex();
					newRLD.nLastAttUses = 1;
					newRLD.shouldSpecialize = true;
					// if(mcArray[nMarker].getAttribute().getName().equals("zn8808.4236")||mcArray[nMarker].getAttribute().getName().equals("zn5922.2604")||mcArray[nMarker].getAttribute().getName().equals("zn5709.2074"))
					// System.out.println("Testing this rule");
					// This rule correctly matches (TP) all matching data
					// that predicts the currently examined target. This
					// is for inductive strengthening.
					for (int nDatum = 0; nDatum < mcArray[nMarker].matchingData
							.size(); nDatum++) {
						if (((DataCounter) mcArray[nMarker].matchingData
								.get(nDatum)).matchesTarget(nTargetVal))
							newRLD
									.addTpMatch(((DataCounter) mcArray[nMarker].matchingData
											.get(nDatum)).instanceName);
					}
					// Fill in the # information for the rule
					processRule(newRLD, nMarker, nTargetVal);
					// System.out.println("Rule generated:
					// "+tempRule.toString());
					// Add the rule to the firstRules
					// Don't bother adding incorrigible rules to the firstRules
					if (isGoodRule(newRLD.rule)) {
						finalRules[0].enqueue(newRLD);
					} else if (isValidRule(newRLD.rule)) {
						firstRules[0].enqueue(newRLD);
					}
				}
			}
		}
		if (parameter.getMinConjuncts() == 1) {
			rules[0] = processBeam(finalRules[0]);
			finalRules[0] = null;
		}
		specializeRules();
		return combineBeams();
	}

	/**
	 * This is where rule specialization occurs. This form has been changed
	 * since now it is a stepwise Instead of looking at all the rules with
	 * possible conjunct formation the rules are first tried to match two
	 * conjuncts, those that produce "perfect" rules are removed and stored on
	 * the final two conjunct rule firstRules all other rules that don't pass
	 * the statistics are shoved to the next firstRules (if it exists -
	 * otherwise rule thrown away)
	 */
	private boolean specializeRules() {
		// specialRules = new HeapPriorityQueue(new
		// RuleDataComparator(),parameter.getBeamWidth()*6);
		// This gores through each beam adding the current rule to the
		for (int nRule = 0; nRule < firstRules[0].size(); nRule++) {
			RuleData ruleToAdd = new RuleData(new Rule());
			try {
				ruleToAdd = (RuleData) firstRules[0].get(nRule);
			} catch (Exception e) {
				e.printStackTrace();
			}
			ConjunctCounter mc = (ConjunctCounter) ruleToAdd.rule.getLhs()
					.getConjunct(ruleToAdd.rule.getLhs().size() - 1);
			for (int b = firstRules.length - 2; b >= 0; b--) {
				for (int r = 0; r < firstRules[b].size(); r++) {
					// Get the rule for possible specialization (in partially
					// sorted order)
					RuleData curRLD = new RuleData(new Rule());
					try {
						curRLD = (RuleData) firstRules[b].get(r);
					} catch (Exception e) {
						e.printStackTrace();
					}
					curRLD.shouldSpecialize = false;
					if (ruleToAdd.iLastConj < curRLD.iLastConj)// If rules
						// attribute is
						// greater
						continue;
					// What is the position of this rule's target value in
					// tvArray?
					int tvPos = -1;
					for (int nTargetVal = 0; nTargetVal < tveArray.length; nTargetVal++) {
						// test for strict equality here is okay as the rule
						// and tvArray will possess the same object.
						if (tveArray[nTargetVal].getConjunct().getValue() == curRLD.rule
								.getTargetValue()) {
							tvPos = nTargetVal;
							break;
						}
					}
					// YELLOW FLAG -- if tvPos still equals -1, we have a
					// problem, but I don't see how that can happen.
					// assert (tvPos>-1);

					// Specialize rule by adding conjuncts, but only if the
					// number of
					// conjuncts doesn't already exceed that specified by the
					// max_conjuncts parameter.

					clearStructureInfo(mcArray, sdeArray);
					// For each mc in current rule
					// add one to each appropriate sde's mPerRule
					for (Enumeration conjEn = curRLD.rule.elements(); conjEn
							.hasMoreElements();)
						for (Iterator matchIt = ((ConjunctCounter) conjEn
								.nextElement()).matchingData.iterator(); matchIt
								.hasNext();)
							((DataCounter) matchIt.next()).mPerRule++;
					// For each SALDataExtension object with num conjuncts
					// (therefore the lhs of the rule matches), add one to the
					// appropriate target value in connected marker counters.
					for (int j = 0; j < sdeArray.length; j++) {
						// If this datum matches the current rule
						if (sdeArray[j].mPerRule == curRLD.rule
								.getConjunctCount()) {
							// Pass markers (carrying the observed target value)
							// from this datum to all matching conjuncts
							for (int mcIdx = 0; mcIdx < sdeArray[j].m.size(); mcIdx++) {
								ConjunctCounter tmp = (ConjunctCounter) sdeArray[j].m
										.get(mcIdx);
								int[] idxs = sdeArray[j].getTargetIndexes();
								for (int k = 0; k < idxs.length; k++)
									tmp.tv[idxs[k]]++;
								// If new LHS & RHS matches (true pos), keep
								// track of the datum for possible use with
								// inductive strengthening.
								if (sdeArray[j].matchesTarget(tvPos)) {
									tmp.newTP.add(sdeArray[j].instanceName);
								}
							}
						}
					}
					// PriorityQueue rulesNotUsed = new HeapPriorityQueue(new
					// RuleDataComparator(),firstRules[0].size());

					// For each rule on the firstRules
					// Before processing each rule, make sure the user hasn't
					// decided to call it quits. If the user decides to quit,
					// act as if the specialRules hasn't been created and
					// process
					// the old, sorted, pruned firstRules (specialRules is still
					// unstable).
					// SAL has determined the match statistics for the current
					// rule (without adding any conjuncts

					// Mark rule as specialized (either it will be specialized,
					// or it can no longer be specialized)

					// This is where there is a huge departure from the original
					// SAL
					// The original one goes through every possible conjunct and
					// adds it to the rule to see if it works.
					// This is wasteful and allows for one att-val pair which is
					// powerful to bring up any weak
					// Instead now it will go through all the beams and see if
					// adding the current single rule helps

					// Create a new rule with this attribute added
					// and see how things look. Also fill in the
					// proper values for the fields.
					RuleData newRLD = new RuleData(curRLD);

					newRLD.rule.addConjunct(mc, 0);
					if (newRLD.iLastConj == ruleToAdd.iLastConj) {
						newRLD.nLastAttUses++;
					} else {
						newRLD.iLastConj = ruleToAdd.iLastConj;
					}

					newRLD.shouldSpecialize = true;
					// Attach list of true positive matches to the rule
					newRLD.tpMatches = mc.newTP;
					processRule(newRLD, mc, tvPos);
					// Don't bother adding incorrigible rules to the
					// firstRules
					boolean isGood = isGoodRule(newRLD.rule);
					if (isGood)
						finalRules[b + 1].enqueue(newRLD);
					else if ((isGood && parameter.shouldSpecializeGoodRules())
							|| (!isGood && isValidRule(newRLD.rule))) {
						firstRules[b + 1].enqueue(newRLD);
						// rulesNotUsed.enqueue(ruleToAdd);
					}
				}// End where we decide to add current conjunct to rule
				// firstRules[0] = rulesNotUsed; // This trims the possible
				// conjuncts down -
				// it removes the conjunct if it was used in a "Good" rule
			}
		} // End where we look for new conjuncts to add

		processBeamsFromUserPreference();
		return true;
	}

	private RuleList processBeam(PriorityQueue pq) {
		ArrayList<RuleData> tRuleList;
		try {
			tRuleList = pq.toSortedArrayList();
		} catch (Exception e) {
			System.out.println("Cannot sort array.");
			return new RuleList();
		}
		int indStr = 0;
		ArrayList<RuleData> strRules = new ArrayList<RuleData>();
		for (Iterator i = tRuleList.iterator(); i.hasNext();) {
			RuleData newRld = (RuleData) i.next();
			int inNRL = newRld.getInductiveStrength(trainSet);
			if (inNRL >= parameter.getInductiveStrengthening()) {
				newRld.cover(trainSet);
				indStr += inNRL;
				strRules.add(newRld);
			}
		}
		RuleList curModel = new RuleList();
		for (int count = 0; count < strRules.size(); count++) {
			RuleData curRLD = (RuleData) strRules.get(count);
			Rule newRule = new Rule(curRLD.rule.getConjunctCount(), attList
					.getTargetAttribute(), curRLD.rule.getTargetValue(),
					curRLD.rule.getRhs().getIndex());
			for (Enumeration e = curRLD.rule.elements(); e.hasMoreElements();) {
				Conjunct oldConjunct = (Conjunct) e.nextElement();
				Conjunct newConjunct = new Conjunct(attList
						.getTargetAttribute(), oldConjunct.getValue(),
						oldConjunct.getIndex());
				newRule.addConjunct(newConjunct, 0);
			}
			newRule.setTrainTruePos(curRLD.rule.getTruePos());
			newRule.setTrainFalsePos(curRLD.rule.getFalsePos());
			newRule.setTrainNeg(curRLD.rule.getNeg());
			newRule.setTrainPos(curRLD.rule.getPos());
			newRule.setCf(curRLD.rule.getCf());
			newRule.setWorth(attList.getTargetAttribute());
			newRule.setPValue(curRLD.rule.getPValue());
			newRule.setIndex(curRLD.rule.getIndex());
			curModel.add(newRule);
		}
		return curModel;
	}

	@SuppressWarnings("unused")
	private RuleList combineBeams() {
		HeapPriorityQueue finalBeam = new HeapPriorityQueue(
				new RuleDataComparator(), parameter.getBeamWidth());
		for (int i = 0; i < finalRules.length; i++) {
			try {
				for (int j = 0; j < finalRules[i].size(); j++) {
					finalBeam.enqueue(finalRules[i].get(j));
				}
			} catch (Exception e) {
				continue;
			}
		}
		return processBeam(finalBeam);
	}

	private void processBeamsFromUserPreference() {
		for (int i = 0; i < rules.length; i++) {
			trainSet.uncoverAll();
			rules[i] = processBeam(finalRules[parameter.getMinConjuncts() + i]);
		}
	}

	/**
	 * Returns all the rules generated by this model
	 * 
	 * @return the array of rules
	 */
	public RuleList getRules() {
		RuleList nRls = new RuleList();
		if (rules != null) {
			for (int i = 0; i < rules.length; i++)
				nRls.addAll(rules[i]);
		}
		return nRls;
	}

	public RuleList[] getRuleLists() {
		return rules;
	}
}
