/*
 * @(#)EvaluationOnly.java  0.9 2002/10/28
 *
 * <h1>JavaRL</h1>
 * A rule induction system for knowledge discovery
 * Copyright (c) 2002
 * Intelligent Systems Laboratory at University of Pittsburgh
 */

package structures.cr;

import data.dataset.*;
import structures.learner.attribute.LearnerAttribute;
import rule.RuleList;

/**
 * An evidence gatherer that compares the predictions of other evidence
 * gatherers. Those are BestPValue, MinWeightedVoting, MostFeaturesCovered,
 * MostSpecificSingleBest, SingleBest, WeightedVoting.
 * 
 * @version 0.9
 * @author Jeremy Ludwig, Will Bridewell, Eric Williams, Philip Ganchev
 *         (philip@cs.pitt.edu)
 * @see BestPValue
 * @see MinWeightedVoting
 * @see MostFeaturesCovered
 * @see MostSpecificSingleBest
 * @see SingleBest
 * @see WeightedVoting
 */
public class EvaluationOnly extends ConflictResolver {
	private BestPValue bpv;

	private MinWeightedVoting mwv;

	private MostFeaturesCovered mfc;

	private MostSpecificSingleBest mssb;

	private SingleBest sb;

	private WeightedVoting wv;

	// File out;
	// FileWriter fw;
	// PrintWriter pw;

	public EvaluationOnly() {
		super("Compare EGs",
				"A dummy EG that uses all EGs in turn and prints out their outputs");
		mwv = new MinWeightedVoting();
		mfc = new MostFeaturesCovered();
		mssb = new MostSpecificSingleBest();
		sb = new SingleBest();
		wv = new WeightedVoting();
		bpv = new BestPValue();
		tarIndex = -1;
		// out = new File("results.tsv");
		// fw = new FileWriter(out);
		// pw = new PrintWriter(fw, true);
	}

	public void setTarget(LearnerAttribute a) {
		bpv.setTarget(a);
		wv.setTarget(a);
		mwv.setTarget(a);
		sb.setTarget(a);
		mssb.setTarget(a);
		mfc.setTarget(a);
	}

	public int predict(RuleList r, Dataset d) {
		ConflictResolver[] egs = { bpv, wv, mwv, sb, mssb, mfc };
		// ConflictResolver[] egs
		// = (ConflictResolver[]) getEGArray(frame).toArray();
		// ClassCastException
		// Object[] egs = getEGArray(frame).toArray();
		// NullPointerException at WeightedVoting.predict(), line that
		// includes target.getHierarchy().numValues()

		// Compare predictions of all combinations of evidence gatherers
		for (int i = 0; i < egs.length; i++) {
			// System.out.println(egs[i]);
			for (int j = 0; j < i; j++) {
				System.out.print(egs[j] + "\t");
				if (egs[i].getClass() == this.getClass())
					continue;
				int p1 = -1, p2 = -1;
				p1 = egs[i].predict(r, d);
				p2 = egs[j].predict(r, d);
				if (p1 == p2) {
					if (p1 == tarIndex) {
						System.out.print("BOTH");
					} else {
						System.out.print("NEITHER");
					}
				} else {
					if (p1 == tarIndex) {
						System.out.print(egs[i]);
					} else if (p2 == tarIndex) {
						System.out.print(egs[j]);
					} else {
						System.out.print("NEITHER");
					}
				}
				System.out.print("\t");
			}
		}

		// pw.print("\n");
		// fw.close();

		// Why return BPV info?? -PG
		int bpvPred = bpv.predict(r, d);
		usedRules = bpv.getUsedRules();
		return bpvPred;
	}
}
