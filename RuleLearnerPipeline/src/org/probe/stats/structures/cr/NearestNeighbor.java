/*
 * @(#)NearestNeighbor.java    1.1 2002/01/21
 */

package structures.cr;

import data.dataset.DataModel;
import rule.RuleList;

/**
 * Goes through the list of rules and looks at the data items which each rule
 * covers. Finds the nearest neighbor of this set of rules to the given data
 * item. Uses the closest neighbor to pick the rule to use in classification.
 * 
 * If two rules are equally similar to the given datum, this voting scheme
 * prefers rule that covers fewer training data items.
 * 
 * Currently this is not a standard option - it runs very slow.
 * 
 * @version 1.0 2000/03/20
 * @author Jeremy Ludwig
 * 
 * Edited to make use of new code organization
 * 
 * @version 1.1 2002/01/21
 * @author Will Bridewell
 */
public class NearestNeighbor extends ConflictResolver {
	public NearestNeighbor() {
		super(
				"Nearest neighbor",
				"Class predicted by the rule which matches "
						+ "the closest datum to this datum, among all matching rules.  "
						+ "Distance is the number of attributes where the two data disagree.");
	}

	@Override
	public int predict(RuleList combrls, DataModel d) {
		return -1;
	}

	/*
	 * public int predict( ArrayList rules, Datum d ) { int nReturn = -1;
	 * 
	 * Rule r, singleBest = (Rule) rules.get(0); double dSim =
	 * similarity(singleBest, d); int nCov =
	 * (singleBest.getRhsMatchList()).size(); double dTemp; int nCurMatchCount;
	 * 
	 * for (int y = 1; y < rules.size(); y++) { r = (Rule) rules.get(y); dTemp =
	 * similarity(r, d); nCurMatchCount = r.getRhsMatchList().size(); if (dTemp >
	 * dSim || ((dTemp == dSim) && (nCurMatchCount < nCov))) { singleBest = r;
	 * dSim = dTemp; nCov = nCurMatchCount; } }
	 * 
	 * usedRules = new StringBuffer(); usedRules.append( singleBest.getIndex() );
	 * nReturn = singleBest.getPredictedValueIndex();
	 * 
	 * System.out.println("matched: " + dSim); return nReturn; }
	 */
	/**
	 * Among the training data that match rule 'r', finds the datum with the
	 * least distance to the datum 'testDatum', and returns the distance.
	 * Distance is the number of attributes where two data disagree.
	 * 
	 * @return the distance of that training datum matching the given rule,
	 *         which is closest to the given datum.
	 */
	/*
	 * public double similarity(Rule r, Datum testDatum) { DataList matches =
	 * r.getRhsMatchList(); // the training data that match this rule Datum
	 * matchDatum; int similarity = -1; int nCurDatumSimilarity; int datumSize =
	 * d.getDataLength(); // the number of attributes int nValueA, nValueB;
	 * AttributeList attList = frame.getAttributeKeeper().getAttributeList();
	 * 
	 * for (int nCurMatch = 0; nCurMatch < matches.size(); nCurMatch++) {
	 * nCurMatchCount = 0; matchDatum = matches.getA(nCurMatch);
	 * 
	 * for (int nAtt = 0; nAtt < datumSize; nAtt++) { if
	 * (!attList.getA(nAtt).ignore() && !attList.getA(nAtt).output()) { // Does
	 * the datum's value match the rule's value for this attribute? nValueA =
	 * attList.getA(nAtt) .getValueIndex(testDatum.getValue(nAtt)); nValueB =
	 * attList.getA(nAtt) .getValueIndex(matchDatum.getValue(nAtt)); if (nValueA ==
	 * nValueB) { nCurDatumSimilarity++; } // Give up on this datum if it has no
	 * chance to beat the most similar one if ((nCurDatumSimilarity + (datumSize -
	 * nAtt)) <= simlarity) { break; } } }
	 * 
	 * if (nCurMatchCount > simlarity) { similarity = nCurMatchCount; } } //
	 * Return the highest similarity return similarity; }
	 */
}