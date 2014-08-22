/*
 * @(#)LaplaceExtended.java    1.2 2002/11/21
 */

package structures.cf;

import data.dataset.Dataset;
import structures.learner.attribute.LearnerAttribute;
import rule.Rule;

/**
 * A certainty factor based on the Laplace Estimate, but which takes class
 * distribution into account.
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
 * Formatted code. Added comments.
 * 
 * @version 1.2 2002/11/21
 * @author Philip Ganchev
 */
public class LaplaceExtended extends CertaintyFactor {
	public LaplaceExtended() {
		super(
				"Laplace + class distribution",
				"(TP + k * m) / (TP +FP + K), where k = 1 + #classes, m = Pos / (Pos + Neg)");
	}

	/**
	 * @param r
	 *            the rule whose certainty factor value to calculate
	 * @return
	 * <pre>
	 * (TP + k * m) / (TP + FP + K)
	 * </pre>, where
	 * 
	 * <pre>
	 *      k = 1 + # target values
	 *      m = Pos / (Pos + Neg)
	 * </pre>.
	 */
	public double getCf(Rule r, LearnerAttribute trg, Dataset d) {
		double k; /* Variance parameter on prior distr. of rule accuracies */
		int tp = r.getTruePos();
		int fp = r.getFalsePos();
		int totP = r.getPos();
		int totN = r.getNeg();

		/* Positive class prior */
		double m = (totP + totN) > 0 ? ((double) totP) / (totP + totN) : 0;

		// YELLOW FLAG -- No Good Reason???!!! -- From C code
		k = trg.getHierarchy().numValues() + 1; /* No good reason */
		return (tp + k * m) / (tp + fp + k);
	}
}
