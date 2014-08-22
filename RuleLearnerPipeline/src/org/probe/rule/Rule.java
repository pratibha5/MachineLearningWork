/*
 * @(#)Rule.java    1.2 2002/11/04
 */

package org.probe.rule;

import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.probe.stats.structures.cf.CertaintyFactor;

import data.dataset.*;

import org.probe.stats.structures.learner.attribute.*;
//import structures.learner.rule.*;
//import structures.model.BayesModel;
import org.probe.stats.structures.result.Prediction;
import org.probe.stats.structures.result.RulePrediction;
import org.probe.util.RuleList;

/**
 * Basic rule class
 * 
 * @version 1.0 2000/03/20
 * @author Jeremy Ludwig
 * 
 * Method addConjunct() now lets the rule grow in size when it runs out of room.
 * Edited to make use of new code organization. Rule now stores True Positives,
 * False Positives and (Total Negatives)/Positives in a clearer manner. Several
 * fields needed only for learning purposes were removed and attached to the
 * learning algorithm.
 * 
 * Added p-value field and merged p-Value code by Yasir Khalifa (02/25/02)
 * 
 * @version 1.1 2002/01/21
 * @author Will Bridewell
 * 
 * Formatted code and comments, added comments. Changed instance variable
 * <code>lhs</code> to a Conjunction object. Conjunction extends ArrayList
 * despite the warning below about speed. It can easily be changed to array but
 * should stay an object.
 * 
 * @version 1.2 2002/11/04
 * @author Philip Gachev (philip@cs.pitt.edu)
 */
public class Rule {
	/** The left hand side of this rule */
	protected Conjunction lhs;

	/** The right hand side of this rule */
	protected Conjunct rhs;
	// An array brings marked speed improvements over Vector or ArrayList
	// private Conjunct[] lhs;

	/** Number of training instances that match the LHS and RHS of this rule */
	protected int trainTruePos;

	/** Number of training instances that match the LHS but not the RHS of this rule */
	protected int trainFalsePos;

	/** Number of training instances that have the same class as this rule predicts */
	protected int trainPos;

	/** Number of training instances that have a different class than this rule predicts */
	protected int trainNeg;
	
	protected int testTruePos;

	protected int testFalsePos;

	/** The certainty factor of this rule. */
	protected double dCf;

	/** Index of this rule in a rule set. */
	protected int index;

	/** P-value of this rule. */
	protected double pValue;

	// TODO: document!
	protected double cost;

	// TODO: document!
	protected double worth;

	protected double k2Score;

	//protected BayesModel bm;

	public boolean isPriorRule;
	
	/**
	 * Format for printing decimal numbers. The tenths digit is printed, and up
	 * to two additional digits if they are nonzero.
	 */
	protected final static String DECIMAL_FORMAT = "0.0##";

	/**
	 * Creates a rule with initial space for 3 conjuncts.
	 */
	public Rule() {
		this(3);
	}

	/**
	 * Creates a rule with initial space for conjunctCount conjuncts.
	 */
	public Rule(int nConjuncts) {
		lhs = new Conjunction(nConjuncts);
		rhs = new Conjunct();
		index = -1;
		cost = 1;
	}

	public Rule(int conjunctCount, LearnerAttribute a, VHierarchyNode v,
			int markerInd) {
		lhs = new Conjunction(conjunctCount);

		// If the target attribute or value are null, then create a dummy RHS
		rhs = (a != null && v != null) ? new Conjunct(a, v, markerInd)
				: new Conjunct();
		index = -1;
		cost = 1;
		worth = 1;
	}
 
	public Rule(Scanner scanner) throws IOException {		
		lhs = new Conjunction(scanner);
		scanner.next("=>");
		rhs = new Conjunct(scanner);
	}
	
	public void setIndex(int i) {
		index = i;
	}

	public int getIndex() {
		return index;
	}

	public void setRhs(LearnerAttribute a, VHierarchyNode v, int markerInd) {
		rhs.setAttribute(a);
		rhs.setValue(v, markerInd);
	}

	public void setRhs(Conjunct c) {
		rhs = c;
	}

	public Conjunct getRhs() {
		return rhs;
	}

	public Conjunction getLhs() {
		return lhs;
	}

	/**
	 * Returns the index of the predicted value.
	 * 
	 * @return the index of the value predicted by this rule
	 */
	public int getPredictedValueIndex() {
		return rhs.getValueIndex(rhs.getValue());
	}

	public VHierarchyNode getTargetValue() {
		return rhs.getValue();
	}

	public String getTargetAttribute() {
		return rhs.getAttributeName();
	}

	public void setTrainTruePos(int n) {
		trainTruePos = n;
	}

	public int getTruePos() {
		return trainTruePos;
	}

	public void setTrainFalsePos(int n) {
		trainFalsePos = n;
	}

	public int getFalsePos() {
		return trainFalsePos;
		//return trainFalsePos;
	}

	public void setTrainPos(int n) {
		trainPos = n;
	}

	public int getPos() {
		return trainPos;
		//return trainPos;
	}

	public void setTrainNeg(int n) {
		trainNeg = n;
	}

	public int getNeg() {
		return trainNeg;
		//return trainNeg;
	}
	
	public void setTestTruePos(int n) {
		testTruePos = n;
	}

	public int getTestTruePos() {
		return testTruePos;
	}

	public void setTestFalsePos(int n) {
		testFalsePos = n;
	}

	public int getTestFalsePos() {
		return testFalsePos;
	}

	public int getConjunctCount() {
		return lhs.size();
	}

	public void setCf(CertaintyFactor cFactor, LearnerAttribute trg, DataModel trn) {
		dCf = cFactor.getCf(this, trg, trn);
	}

	public void setCf(double cFactor) {
		dCf = cFactor;
	}

	public void setPValue(double p) {
		pValue = p;
	}

	public double getPValue() {
		return pValue;
	}

	public double getCf() {
		return dCf;
	}

	/**
	 * Sets the average cost, calulates worth and sets worth of this rule. This
	 * rule's worth is defined as <code>worth = certainty-factor / cost</code>.
	 * 
	 * @author by Eric Williams
	 * @since 2002/07/22
	 */
	public void setWorth(LearnerAttribute trg) {
		// Get the attribute from the RHS
		LearnerAttribute ruleAttrib = trg;

		// Get the value from the RHS
		Object ruleValue = getTargetValue();

		// Get the index for the rule's value
		int valueIndex = ruleAttrib.getHierarchy().getValueIndex(ruleValue);

		// Calculate avg cost
		// removed: this is performed in getAvgCost automatically and has
		// been made a private method (no real use for it outside the
		// class as it returns no values and is called whenever you might want
		// to use the average cost information 4/17/04
		// If you read the above and everything's working okay, zap this line.
		// ruleAttrib.calculateAvgCost();

		// Get the avg cost for the class
		cost = ruleAttrib.getAvgCost(valueIndex);

		// Divide the CF by the cost to get worth
		worth = dCf / cost;
	}

	public double getWorth() {
		return worth;
	}

	public boolean matchLhs(DataModel d, int instIx) {
		return lhs.matchesDatum(d, instIx);
	}

	public boolean matchRhs(DataModel d, int instIx) {
		return rhs.matchesDatum(d, instIx);
	}

	public void addConjunct(Conjunct c) {
		lhs.addConjunct(c);
	}
	
	public void addConjunct(Conjunct c, double newCF) {
		lhs.addConjunct(c);
		dCf = dCf + (1 - dCf) * newCF;
	}
	
	public void addConjunct(LearnerAttribute t, VHierarchyNode v,
			int markerInd, double newCF) {
		addConjunct(new Conjunct(t, v, markerInd), newCF);
	}

	public String nameString() {
		StringBuffer buf = new StringBuffer();

		if (index > -1) {
			buf.append(index + ". ");
		}
		buf.append(lhs);
		if (rhs != null && rhs.getAttributeName() != null) {
			buf.append(" ==> ");
			buf.append(rhs.toString());
		}

		return buf.toString();
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		if (isPriorRule)
			buf.append("p");
		buf.append(nameString());
		DecimalFormat df = new DecimalFormat(DECIMAL_FORMAT);
		buf.append("\r\n\tCF=" + df.format(dCf));
		//buf.append(", Av.cost=" + df.format(cost));
		buf.append(", CF/cost=" + df.format(worth));
		buf.append(", P=" + df.format(pValue));
		buf.append(", TP=" + trainTruePos);
		buf.append(", FP=" + trainFalsePos);
		//buf.append(", Pos=" + trainPos);
		//buf.append(", Neg=" + trainNeg);
		buf.append(", TP_in_model=" + testTruePos);
		buf.append(", FP_in_model=" + testFalsePos);
		return buf.toString();
	}

	public Enumeration elements() {
		return new RuleEnum();
	}

	class RuleEnum implements Enumeration {
		int currentItem = 0;

		public boolean hasMoreElements() {
			return currentItem < lhs.size();
		}

		public Object nextElement() {
			if (!hasMoreElements()) {
				throw new NoSuchElementException();
			}
			return lhs.get(currentItem++);
		}
	}

	public ArrayList getPredictedData(ArrayList predictions) {
		ArrayList l = new ArrayList();
		Prediction p;
		for (int i = 0; i < predictions.size(); i++) {
			p = (Prediction) predictions.get(i);
			if (p instanceof RulePrediction) {
				if (((RulePrediction) p).getUsedRules().indexOf(this) >= 0) {
					l.add(p.getPredictedDatum());
				}
			} else
				l.add(p.getPredictedDatum());
		}
		return l;
	}

	public ArrayList getMatchedData(ArrayList predictions) {
		ArrayList l = new ArrayList();
		Prediction p;
		for (int i = 0; i < predictions.size(); i++) {
			p = (Prediction) predictions.get(i);
			RuleList rls = p.getMatchedRules();
			if (rls.indexOf(this) != -1)
				l.add(p.getPredictedDatum());
		}
		return l;
	}

	/**
	 * Returns a shallow copy of this rule. That is, the new rule's instance
	 * fields <code>lhs</code> and <code>rhs</code> refer to the same
	 * objects as this rule's.
	 * 
	 * TODO: if possible, replace references to this method
	 * with method Rule(Rule). --PG2009
	 */
	public Object clone() {
		Rule r = new Rule(lhs.size());
		r.lhs = (Conjunction) lhs.clone();
		r.rhs = rhs;
		r.trainTruePos = trainTruePos;
		r.trainFalsePos = trainFalsePos;
		r.trainPos = trainPos;
		r.trainNeg = trainNeg;
		r.dCf = dCf;
		r.index = index;
		r.pValue = pValue;
		r.cost = cost;
		r.worth = worth;
		r.k2Score = k2Score;
		r.isPriorRule = isPriorRule;
		return r;
	}
}
