/*
 * @(#)Prediction.java 1.1 2002/01/21
 */

package structures.result;

// Java.util
import java.text.DecimalFormat;
import java.util.Enumeration;

import structures.learner.attribute.VHierarchyNode;
import rule.RuleList;

/**
 * Keeps track of the rules used to make a prediction, the prediction value and
 * the target value
 * 
 * TODO: This does not currently work with target values in the first level of
 * the hierarchy.
 * 
 * @version 1.0 2000/03/20
 * @author Jeremy Ludwig
 * 
 * Edited to make use of new code organization Changed some variable names,
 * removed public fields, added accessor methods. Fixed correctPrediction() to
 * work on value hierarchies Added data item so we keep track of which datum we
 * are predicting
 * 
 * @version 1.1 2002/01/21
 * @author Will Bridewell
 * 
 * Added instance variable <code>RuleList matchedRules</code> and method
 * <code>getMatchedRules()</code>. Replaced instance variable
 * <code>String rules</code> with a <code>RuleList usedRules</code> and the
 * type of <code>getMatchedRules()</code>.
 * 
 * @version 1.2 2002/10/30
 * @author Philip Ganchev (philip@cs.pitt.edu)
 * 
 * @version 1.3 2005/6/26
 * @author Jonathan Lustgarten Modified to handle proper way of storing
 *         predictions and the new dataset There should only be storing the
 *         Sample name, nothing else Everything else is stored in the dataset
 */
public class Prediction {
	// /** The index of the target attribute */
	// protected int targetIndex; 
	protected RuleList matchedRules;
	protected Object observedValue;
	protected VHierarchyNode predictedValue;
	protected String predictedDatum;
	public static DecimalFormat DECIMAL_FORMAT_PROBABILITY = new DecimalFormat("#.###");

	public Prediction(String d, Object target) {
		matchedRules = new RuleList();
		observedValue = target;
		predictedValue = null;
		predictedDatum = d;
	}

	public Prediction(RuleList rules, String d, Object observed, VHierarchyNode predicted) {
		// this.targetIndex = targetIndex;
		matchedRules = rules;
		observedValue = observed;
		predictedValue = predicted;
		predictedDatum = d;
	}

	public Prediction(RuleList[] rules, String d, Object observed,
			VHierarchyNode predicted) {
		matchedRules = new RuleList();
		for (int i = 0; i < rules.length; i++)
			matchedRules.addAll(rules[i]);
		observedValue = observed;
		predictedValue = predicted;
		predictedDatum = d;
	}

	public boolean isCorrect() {
		// Is there a predicted value?
		if (predictedValue == null)
			return false;

		// Is observed == predicted?
		if (predictedValue.equals(observedValue))
			return true;

		// Is observed in the subtree of the predicted?
		for (Enumeration e2 = predictedValue.preorderEnumeration(); 
					e2.hasMoreElements();) {
			if (e2.nextElement().equals(observedValue)) {
				return true;
			}
		}
		return false;
	}

	public Object getPredictedValue() {
		return	predictedValue;
	}

	public String getPredictedString() {
		return 
			predictedValue != null ? 
					predictedValue.toString() :
					"No prediction";
	}
	
	public Object getObservedValue() {
		return observedValue;
	}

	public String getPredictedDatum() {
		return predictedDatum;
	}

	public RuleList getMatchedRules() {
		return matchedRules;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append(predictedValue != null ? predictedValue.toString() 
				: "No prediction");
		// if( modelValue != null )
		// buf.append( "\t" + modelValue );
		buf.append("\t" + observedValue);
		buf.append("\nRules Matched: \n");
		buf.append(matchedRules.toString());
		return buf.toString();
	}
}