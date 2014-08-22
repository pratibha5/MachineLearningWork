/*
 * @(#)MostSpecificSingleBest.java    1.1 2002/03/29
 */

package structures.cr;

import data.dataset.*;
import rule.*;

/**
 * An evidence gatherer that uses the rule with the greatest number of conjuncts
 * among all rules with the highest certainty factor, to predict the test datum.
 * 
 * @version 1.0 2002/03/29
 * @author Eric Williams
 * @see SingleBest
 * @see SingleBestSpecific
 * 
 * Makes use of version 1.1 code organization by Will Bridewell
 * 
 * Compacted and formatted code. Now uses <code>RuleList usedRules</code>
 * 
 * @version 1.1 2002/10/28
 * @author Philip Ganchev (philip@cs.pitt.edu)
 */
public class MostSpecificSingleBest extends ConflictResolver {
	public MostSpecificSingleBest() {
		super("Most specific AND highest CF rule",
				"Class predicted by the rules with the highest number of conjuncts "
						+ "among rules with the highest CF.");
	}

	public int predict(RuleList rules, Dataset d) {
		int nReturn = -1;

		Rule r, singleBest = (Rule) rules.get(0);
		for (int y = 0; y < rules.size(); y++) {
			r = (Rule) rules.get(y);
			if (r.getConjunctCount() > singleBest.getConjunctCount()) {
				singleBest = r;
			} else if (r.getConjunctCount() == singleBest.getConjunctCount()) {
				if (r.getWorth() > singleBest.getWorth()) {
					singleBest = r;
				} else if (r.getWorth() == singleBest.getWorth()
						&& r.getPValue() > singleBest.getPValue()) {
					singleBest = r;
				}
			}
		}
		certainty = singleBest.getCf();
		(usedRules = new RuleList()).add(singleBest);
		nReturn = singleBest.getPredictedValueIndex();
		return nReturn;
	}
}
