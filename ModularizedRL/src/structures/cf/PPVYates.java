/*
 * @(#)FreqBasedYates.java    1.2 2002/11/21
 */

package structures.cf;

import data.dataset.Dataset;
import structures.learner.attribute.LearnerAttribute;
import rule.Rule;

/**
 * A certainty factor based on Quinlan (1987), normalized for asymmetric
 * distributions:
 * 
 * <pre>
 * (TP + .05) / (TP + FP) or (TP - .05) / (TP + FP)
 * </pre>,
 * 
 * depending on whether TP > FP or TP < FP, respectively
 * 
 * 
 * @version 1.0 2000/03/20
 * @author Jeremy Ludwig
 * 
 * Edited to account for new code organization Changed to use TP and FP (and
 * TotalNeg) in rules
 * 
 * @version 1.1 2002/01/21
 * @author Will Bridewell
 * 
 * Formatted code and comments.
 * 
 * @version 1.2 2002/11/21
 * @author Philip Ganchev
 */
public class PPVYates extends CertaintyFactor {
	public PPVYates() {
		super(
				"PPV-Yates",
				"((TP + .05) / P), if TP > FP; ((TP - .05) / P), if FP < TP");
	}

	/**
	 * Returns the value of this certainty factor function for <code>r</code>
	 * evaluated as
	 * 
	 * <pre>
	 * (TP + .05) / (TP + FP) or (TP - .05) / (TP + FP)
	 * </pre>
	 * 
	 * This CF function is from Quinlan 1987, and was used later in the C++
	 * version of RL.
	 * 
	 * @param r
	 *            the rule whose sertainty factor to evaluate
	 * @return
	 * 
	 * <pre>
	 * (TP + .05) / (TP + FP) or (TP - .05) / (TP + FP)
	 * </pre>
	 */
	public double getCf(Rule r, LearnerAttribute trg, Dataset d) {
		int tp = r.getTruePos();
		int fp = r.getFalsePos();

		return tp + fp == 0 ? 0 
				: tp > fp ? (tp + 0.05) / (tp + fp)
				: tp < fp ? (tp - 0.05) / (tp + fp) 
							:  tp / ((double) (tp + fp));
	}
}
