/*
 * @(#)SingleBestSpecific.java    1.2 2002/01/21
 */

package structures.cr;

import data.dataset.*;
import rule.*;

/**
 * An evidence gatherer that uses the single best rule with the greatest number
 * of conjuncts to classify the test datum.
 * 
 * @version 1.0 2000/03/20
 * @author Jeremy Ludwig
 * @see SingleBest
 * 
 * Edited to make use of new code organization
 * 
 * @version 1.1 2002/01/21
 * @author Will Bridewell
 * 
 * Compacted and formatted code and comments. Now uses
 * <code>RuleList usedRules</code>
 * 
 * @version 1.2 2002/10/29
 * @author Philip Ganchev (philip@cs.pitt.edu)
 */
public class SingleBestSpecific extends ConflictResolver {
	public SingleBestSpecific() {
		super(
				"Most specific OR highest-CF rule",
				"Class predicted by the matching rule with the greatest number of conjuncts.  "
						+ "If there is more than one, the rule with the highest CF is used.");
	}

	public int predict(RuleList rules, DataModel d) {
		Rule r, singleBest = (Rule) rules.get(0);
		for (int y = 0; y < rules.size(); y++) {
			r = (Rule) rules.get(y);
			if (r.getWorth() > singleBest.getWorth()
					|| r.getConjunctCount() > singleBest.getConjunctCount()) {
				singleBest = r;
			}
		}
		certainty = singleBest.getCf();
		(usedRules = new RuleList()).add(singleBest);
		return singleBest.getPredictedValueIndex();
	}
}
