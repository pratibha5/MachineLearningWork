/*
 * @(#)SingleBest.java    1.3 2002/01/21
 */

package structures.cr;

import data.dataset.*;
import rule.*;

/**
 * An evidence gatherer that uses the first, single rule with the highest
 * certainty factor to classify the test datum.
 * 
 * @author Jeremy Ludwig
 * @version 1.0 2000/03/20
 * 
 * Edited to make use of new code organization.
 * 
 * @author Will Bridewell
 * @version 1.1 2002/01/21
 * 
 * Edited to use more clearly defined notion of "best" (using CF).
 * 
 * @version 1.2 2002/03/29
 * @author Eric Williams
 * 
 * Compacted and formatted code and comments. Now uses
 * <code>RuleList usedRules</code>
 * 
 * @author Philip Ganchev (philip@cs.pitt.edu)
 * @version 1.3 2002/10/28
 */
public class SingleBest extends ConflictResolver {
	public SingleBest() {
		super("Highest CF rule",
				"Class predicted by the rule with the highest CF among matcing rules");
	}

	public int predict(RuleList rules, DataModel d) {
		int nReturn = -1;
		Rule r, singleBest = (Rule) rules.get(0);
		for (int y = 0; y < rules.size(); y++) {
			r = (Rule) rules.get(y);
			if (r.getWorth() > singleBest.getWorth())
				singleBest = r;
			else if (r.getWorth() == singleBest.getWorth()
					&& r.getTruePos() > singleBest.getTruePos())
				singleBest = r;
		}
		certainty = singleBest.getCf();
		nReturn = singleBest.getPredictedValueIndex();
		(usedRules = new RuleList()).add(singleBest);
		return nReturn;
	}
}
