/*
 * RulePrediction.java
 *
 * Created on July 13, 2006, 12:03 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package structures.result;

import java.text.DecimalFormat;

import structures.learner.attribute.*;
import rule.*;

/**
 * 
 * @author Jonathan Lustgarten
 */
public class RulePrediction extends Prediction {
	private RuleList usedRules;
	private double certainty;
	public static DecimalFormat dec = new DecimalFormat("#.###");
	
	/**
	 *  Creates a new instance of RulePrediction 
	 */
	public RulePrediction(String d, Object target) {
		super(d, target);
		usedRules = new RuleList();
		certainty = 0;
	}

	/**
	 * @deprecated
	 * @param matchedRules
	 * @param d
	 * @param observed
	 * @param predicted
	 * @param usedRules
	 */
	public RulePrediction(RuleList matchedRules, String d, Object observed,
			VHierarchyNode predicted, RuleList usedRules) {
		super(matchedRules, d, observed, predicted);
		this.usedRules = usedRules;
		certainty = 0;
	}

	/**
	 * @deprecated
	 * @param matchedRules
	 * @param d
	 * @param observed
	 * @param predicted
	 * @param usedRules
	 */
	public RulePrediction(RuleList[] matchedRules, String d, Object observed,
			VHierarchyNode predicted, RuleList usedRules) {
		super(matchedRules, d, observed, predicted);
		this.usedRules = usedRules;
		certainty = 0;
	}

	public RulePrediction(RuleList matchedRules, String d, Object observed,
			VHierarchyNode pred, RuleList usdRules, double conf) {
		super(matchedRules, d, observed, pred);
		usedRules = usdRules;
		certainty = conf;
		for (int i = 0; i < usedRules.size(); i++) {
			Rule r = usedRules.get(i);
			if (isCorrect())
				r.setTestTruePos(r.getTestTruePos() + 1);
			else
				r.setTestFalsePos(r.getTestFalsePos() + 1);
		}
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append(predictedDatum + "\t");
		buf.append(predictedValue != null ? predictedValue.toString()
				: "No prediction");

		// if( modelValue != null )
		// buf.append( "\t" + modelValue );

		buf.append("\t" + observedValue);
		buf.append("\t" + matchedRules.getIndecesString());
		buf.append("\t" + usedRules.getIndecesString());
		buf.append("\t" + dec.format(certainty));
		return buf.toString();
	}

	public RuleList getUsedRules() {
		return usedRules;
	}

	public double getPredictedValueCertainty() {
		return certainty;
	}
}