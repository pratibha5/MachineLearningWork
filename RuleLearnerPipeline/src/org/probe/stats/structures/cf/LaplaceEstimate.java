/*
 * @(#)LaplaceEstimate.java    1.2 2002/11/21
 */

package org.probe.stats.structures.cf;

import data.dataset.DataModel;
import org.probe.stats.structures.learner.attribute.LearnerAttribute;
import org.probe.rule.Rule;

/**
 * A certainty factor based on the Laplace Estimate:
 * 
 * <pre>
 * (TP + 1) / (TP + FP + number_of_target_values + 1)
 * </pre>.
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
 * Added comments.
 * 
 * @version 1.2
 * @author Philip Ganchev
 */
public class LaplaceEstimate extends CertaintyFactor {
	public LaplaceEstimate() {
		super("Laplace accuracy estimate",
				"(TP + 1) / (TP + FP + k), where k = 1 + #classes)");
	}

	/**
	 * Returns the laplace estimate for <code>r</code>.
	 * 
	 * @param r
	 *            the rule whose certainty factor value to compute
	 * @return 
	 * <pre>
	 * (TP + 1) / (TP + FP + k)
	 * </pre>,
	 * where <pre>k = 1 + # target values</pre>
	 */
	public double getCf(Rule r, LearnerAttribute trg, DataModel d) {
		final int tp = r.getTruePos();

		return ((double) tp + 1)
				/ (tp + r.getFalsePos() + trg.getHierarchy().numValues() + 1);
	}
}
