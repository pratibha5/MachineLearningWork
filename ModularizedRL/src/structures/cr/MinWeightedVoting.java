/*
 * @(#)MinWeightedVoting.java    1.1 2002/01/21
 */

package structures.cr;

import data.dataset.*;
import structures.learner.*;
import structures.learner.attribute.*;
import rule.*;

/**
 * An evidence gatherer that predicts the class of the test datum based on the
 * predictions of matching rules, weighted as in <code>WeightedVoting</code>.
 * It first determines the minimum number <i>k</i> of rules voting for any
 * target class, then <i>k</i> best rules predicting each class are used for
 * weighted voting.
 * 
 * @version 1.0 2000/03/20
 * @author Jeremy Ludwig
 * @see WeightedVoting
 * @see SingleBest
 * 
 * Edited to make use of new code organization
 * 
 * @version 1.1 2002/01/21
 * @author Will Bridewell
 * 
 * Compacted and formatted code and comments. Now uses <code>RuleList
 * usedRules</code>.
 * Corrected class JavaDoc comment.
 * 
 * @version 1.2 2002/10/28
 * @author Philip Ganchev (philip@cs.pitt.edu)
 */
public class MinWeightedVoting extends ConflictResolver {
	private WeightedVoting wv;

	public MinWeightedVoting() {
		super(
				"Minimum weighted voting",
				"Class predicted by the vote of k rules of each class, "
						+ "weighted by their CF, where k is the minimum number of "
						+ "rules voting for any class");
		wv = new WeightedVoting();
	}

	public int predict(RuleList rules, Dataset d) {
		int[] nCount = new int[target.getHierarchy().numValues()];
		PriorityQueue[] beam = new HeapPriorityQueue[nCount.length];
		RuleList minRules;
		int nMin;

		// Initialize the counts
		for (int i = 0; i < nCount.length; i++) {
			nCount[i] = 0;
		}

		// Count rules for each target
		for (int i = 0; i < rules.size(); i++) {
			nCount[((Rule) rules.get(i)).getPredictedValueIndex()]++;
		}
		// Find the minimum index
		nMin = nCount[0];
		for (int i = 1; i < nCount.length; i++) {
			if ((nCount[i] < nMin || nMin == 0) && nCount[i] != 0) {
				nMin = nCount[i];
			}
		}

		// Initialize the rule beams
		for (int i = 0; i < beam.length; i++) {
			beam[i] = new HeapPriorityQueue(new RuleComparator(), nMin);
		}
		// Put rules into rule beams per target
		for (int i = 0; i < rules.size(); i++) {
			beam[((Rule) rules.get(i)).getPredictedValueIndex()].enqueue(rules
					.get(i));
		}
		// Trim the rule beams
		for (int i = 0; i < beam.length; i++)
			beam[i].trimToCapacity();

		// Put the rules back into a single list
		minRules = new RuleList(nMin * beam.length);

		for (int i = 0; i < beam.length; i++) {
			if (!beam[i].isEmpty()) {
				for (int j = 0; j < nMin; j++) {
					try {
						minRules.add((Rule) beam[i].get(j));
					} catch (Exception x) {
						x.printStackTrace();
					}
				}
			}
		}
		wv.setTarget(target);
		int nReturn = wv.predict(minRules, d);
		certainty = wv.certainty;
		usedRules = wv.getUsedRules();
		return nReturn;
	}
}