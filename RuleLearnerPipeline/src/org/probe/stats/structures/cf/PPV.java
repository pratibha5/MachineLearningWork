/*
 * @(#)FreqBased.java    1.2 2002/11/21
 */

package structures.cf;

import data.dataset.DataModel;
import structures.learner.attribute.LearnerAttribute;
import rule.*;

/**
 * A certainty factor based on Positive Predictive Value,
 * 
 * <pre>
 * TP / (FP + TP)
 * </pre>. That is, the frequency of correct classification evaluated.
 * 
 * @version 1.0 2000/03/20
 * @author Jeremy Ludwig
 * 
 * Edited to account for new code organization Changed to use TP and FP in rules
 * 
 * @version 1.1 2002/01/21
 * @author Will Bridewell
 * 
 * Formatted code. Added comments.
 * 
 * @version 1.2 2002/11/21
 * @author Philip Ganchev
 */

public class PPV extends CertaintyFactor {
	public PPV() {
		super("PPV", "TP / Pos");
	}

	/**
	 * Returns the positive predictive value of <code>r</code>.
	 * 
	 * @param r
	 *            the rule whose certainty factor value to compute
	 * @return
	 * <pre>
	 * TP / (TP + FP)
	 * </pre>
	 */
	public double getCf(Rule r, LearnerAttribute trg, DataModel d) {
		int tp = r.getTruePos();
		int fp = r.getFalsePos();
		//int negs = r.getTrainTotalNeg();
		return ((tp + fp) == 0) ? 0 : ((double) tp) / ((double) (tp + fp));
	}
}