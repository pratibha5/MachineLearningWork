/*
 * @(#)SignalToNoise.java    1.2 2002/11/21
 */

package structures.cf;

import data.dataset.DataModel;
import structures.learner.attribute.LearnerAttribute;
import rule.Rule;

/**
 * A certainty factor based on signal-to-noise ratio,
 * 
 * <pre>
 * (TP / Pos) / (FP / Neg)
 * </pre>.
 * 
 * @version 1.0 2000/03/20
 * @author Jeremy Ludwig
 * 
 * Edited to account for new code organization Changed to use TP and FP (and
 * TotalNeg) in rules Renamed SignalToNoise (from SignaltoNoise)
 * 
 * @version 1.1 2002/01/21
 * @author Will Bridewell
 * 
 * Formatted code. Added comments.
 * 
 * @version 1.2 2002/11/21
 * @author Philip Ganchev
 */
public class SignalToNoise extends CertaintyFactor {
	public SignalToNoise() {
		super("Signal:Noise", "TP / Pos * Neg / FP");
	}

	/**
	 * The signal-to-noise ratio of <code>r</code>,
	 * 
	 * <pre>
	 * (TP / Pos) / (FP / Neg)
	 * </pre>.
	 * 
	 * @param r
	 *            the rule whose certainty factor to evaluate
	 * @return
	 * <pre>
	 * (TP / Pos) / (FP / Neg)
	 * </pre>.
	 */
	public double getCf(Rule r, LearnerAttribute trg, DataModel d) {
		int tp = r.getTruePos();
		int fp = r.getFalsePos();
		int totP = r.getPos();
		int totN = r.getNeg();

		return
			totP == 0 ? 0
			: totN == 0 ? 0
			: fp != 0 ?	tp / (double) totP / ( fp / (double) totN)
					: tp / (double) totP / (0.1 / totN);
	}
}
