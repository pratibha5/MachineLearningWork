/*
 * @(#)WeightedVoting.java    1.1 02/01/21
 */

package structures.cr;

import data.dataset.*;
import rule.*;

/**
 * An evidence gatherer that predicts the class that has the highest weight,
 * where the weight is the sum of certainty factors of the rules predicting that 
 * class, and predicts the highest-weighted class. If there is a tie, predict class 0.
 * 
 * @version 1.0 00/03/20
 * @author Jeremy Ludwig
 * 
 * Edited to make use of new code organization
 * 
 * @version 1.1 02/01/21
 * @author Will Bridewell
 */
public class WeightedVoting extends ConflictResolver {
	public WeightedVoting() {
		super("Weighted voting",
				"Class predicted by a vote of matching rules, "
						+ "weighed by the sum of their CFs.");
	}

	public int predict(RuleList rules, DataModel d) {
		Rule r;
		if (rules.size() == 0) {
			certainty = 0;
			usedRules = new RuleList();
			return -1;
		}
		// Find the weighted vote for each class
		double[] voteWeights = new double[target.getHierarchy().numValues()];

		for (int y = 0; y < rules.size(); y++) {
			r = (Rule) rules.get(y);
			int i = r.getPredictedValueIndex();
			if (i > -1)
				voteWeights[i] += r.getWorth();
		}

		// Find the max weighted vote and its class
		double max = voteWeights[0]; // Defaults to class 0
		int iPredClass = 0;
		for (int y = 1; y < voteWeights.length; y++) {
			if (voteWeights[y] > max) {
				max = voteWeights[y];
				iPredClass = y;
			}
		}
		certainty = max;
		// Collect the rules into the RuleList usedRulesList
		usedRules = new RuleList();
		for (int y = 0; y < rules.size(); y++) {
			r = (Rule) rules.get(y);
			if (iPredClass == r.getPredictedValueIndex()) {
				usedRules.add(r);
			}
		}
		return iPredClass;
	}
}