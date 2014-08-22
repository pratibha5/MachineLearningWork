/*
 * @(#)FreqBasedNormalized.java    1.2 2002/11/21
 */

package structures.cf;

import data.dataset.Dataset;
import structures.learner.attribute.LearnerAttribute;
import rule.Rule;

/**
 * Certainty factor based on frequency of correct classification, normalized for
 * asymmetric distributions, evaluated as
 * 
 * <pre>
 * TP/(TP + (FP * TotalP / TotalN)
 * </pre>
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

public class PPVNormalized extends CertaintyFactor {
	public PPVNormalized() {
		super("PPV-normalized",
				"TP / (TP + FP * Pos/Neg)");
	}

	/**
	 * Return the PPV normalized for a skewed distribution of positive and negative examples.
	 * 
	 * <pre>
	 * TP / (TP + FP * P / N)
	 * </pre>
	 * 
	 * @return
	 * <pre>
	 * TP / (TP + FP * P / N)
	 * </pre>
	 */
	public double getCf(Rule r, LearnerAttribute trg, Dataset d) {
		int tp = r.getTruePos();
		int fp = r.getFalsePos();
		int totP = r.getPos();
		int totN = r.getNeg();

		return 
			fp == 0 ? 1 
			: tp + fp == 0 ? 0 
			: tp / (tp + (fp * totP / (double) totN));
	}
}
