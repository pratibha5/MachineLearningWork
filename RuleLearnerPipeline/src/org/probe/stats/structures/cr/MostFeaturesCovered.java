/*
 * @(#)MostFeaturesCovered.java    1.1 2002/04/11
 */

package structures.cr;

import java.util.ArrayList;

import data.dataset.*;
import structures.learner.attribute.LearnerAttribute;
import rule.*;


/**
 * An evidence gatherer that predicts the test datum's class as the class for
 * which the matching rules cover the graeatest number of attributes (features).
 * This evidence gatherer first determines how many features are covered by
 * rules predicting each class. Then, whichever class is predicted by more
 * features wins. In case of a tie, it uses MinimumWeightedVoting on rules from
 * tied target classes as the evidence gathering method.
 * 
 * The current implementation is a bit of a kludge, but it's a proof of concept.
 * 
 * @see MinimumWeightedVoting
 * 
 * @version 1.0 2002/04/11
 * @author Eric Williams
 * 
 * Compacted and formatted code and comments. Now uses
 * <code>RuleList usedRules</code>
 * 
 * @version 1.1 2002/10/28
 * @author Philip Ganchev (philip@cs.pitt.edu)
 * 
 * Now uses the dataset created by Jonathan Lustgarten
 * @version 1.2 2006/05/10
 * @author Jonathan Lustgarten (jll@cbmi.pitt.edu)
 */
public class MostFeaturesCovered extends ConflictResolver {
	public MostFeaturesCovered() {
		super("Most Features Covered",
				"Class predicted by the rules that together match the "
						+ "greatest number of the test case's features.  "
						+ "Minimum Weighted Voting is used to beak a tie.");
	}

	public int predict(RuleList rules, DataModel d) {
		int nReturn = -1;
		// return nReturn;
		// Find out how many target classes there are
		// int numTargetValues = target.getHierarchy().numValues();

		// Find out how many features are represented
		/*
		 * int highestAttribIndex = 0; int highestTargetValueIndex = 0; Rule
		 * tempRule;
		 * 
		 * int numRules = 0; for(int rlsI = 0;rlsI<rules.length;rlsI++)
		 * numRules += rules[rlsI].size(); int[] numConjuncts = new
		 * int[numRules]; int attribIndex; int[] predictedValueIndex = new
		 * int[numRules]; LearnerAttribute tempAttribute, tempAttribute2;
		 * Conjunction leftSide; // PG ArrayList attributes = new ArrayList();
		 * int size; for(int nRsI=0;nRsI<rules.length;nRsI++) { for (int i = 0;
		 * i < numRules; i++) { // Get the rule tempRule = (Rule)
		 * rules[nRsI].get(i); // Get target index predictedValueIndex[i] =
		 * tempRule.getPredictedValueIndex(); // Compare to highest target index
		 * if (predictedValueIndex[i] > highestTargetValueIndex) {
		 * highestTargetValueIndex = predictedValueIndex[i]; } // How many
		 * conjuncts in lefthand side? numConjuncts[i] =
		 * tempRule.getConjunctCount(); // Get lefthand side of rule //leftSide =
		 * new Conjunct[numConjuncts[i]]; PG //leftSide = tempRule.getLhs(); PG
		 * leftSide = tempRule.getLhs(); // PG // Look at attribute indexes for
		 * (int j = 0; j < numConjuncts[i]; j++) { // Extract attribute from
		 * conjunct //tempAttribute = leftSide[j].getAttribute(); PG
		 * tempAttribute = d.attribute(leftSide.getConjunct(j).getAttribute()); //
		 * PG // Get attribute index attribIndex = tempAttribute.getIndex();
		 * //attributes[attribIndex] = tempAttribute.getInformationGain(); //
		 * Compare to highest attribute index if (attribIndex >
		 * highestAttribIndex) { highestAttribIndex = attribIndex; }
		 * tempAttribute2 = new
		 * LearnerAttribute(tempAttribute.getName(),attribIndex); size =
		 * attributes.size(); tempAttribute2.setInfoGain(
		 * tempAttribute.getInfoGain() ); attributes.add(tempAttribute2);
		 * 
		 * //attributes.add( // attribIndex, // new
		 * Double(tempAttribute.getInformationGain()) //); } } } /* We now know
		 * the highest attribute index needed we can now contruct an attribute
		 * matrix x-coordinate is rule index y-coordinate is attribute index
		 * z-ccordinate is target class we can use local index for rules instead
		 * of absolute index
		 * 
		 * 
		 * int[][][] markerMatrix = new
		 * int[numRules][highestAttribIndex+1][highestTargetValueIndex+1]; //
		 * Initialize with zeros for (int i = 0; i < numRules; i++) { for (int j =
		 * 0; j <= highestAttribIndex; j++) { for (int k = 0; k <=
		 * highestTargetValueIndex; k++) { markerMatrix[i][j][k] = 0; } } }
		 * 
		 * //double[] attributes = new double[highestAttribIndex+1];
		 * 
		 * /*for (int i=0;i<highestAttribIndex;i++) { attributes[i] = 0; } //
		 * Set 1 if attribute present for(int nRsI=0;nRsI<rules.length;nRsI++) {
		 * for (int i = 0; i < numRules; i++) { tempRule = (Rule)
		 * rules[nRsI].get(i); //leftSide = tempRule.getLhs(); leftSide =
		 * tempRule.getLhs(); for (int j = 0; j < numConjuncts[i]; j++) {
		 * //tempAttribute = leftSide[j].getAttribute(); PG tempAttribute =
		 * leftSide.getConjunct(j).getAttribute(); // PG attribIndex =
		 * tempAttribute.getIndex(); //attributes[attribIndex] = tempAttribute; /*
		 * if (attributes[attribIndex]==0) { attributes[attribIndex] =
		 * tempAttribute.getInformationGain(); } // For this rule and this
		 * target class, this attribute // is present
		 * markerMatrix[i][attribIndex][predictedValueIndex[i]] = 1; } } } // At
		 * this point, the marker matrix has a 1 wherever an attribute is //
		 * present. Now we can sum across rules. The result is a 2d array of //
		 * class vs attribute.
		 * 
		 * int[][] maskMatrix = new
		 * int[highestTargetValueIndex+1][highestAttribIndex+1]; // Initialize
		 * for (int i = 0; i <= highestTargetValueIndex; i++) { for (int j = 0;
		 * j <= highestAttribIndex; j++) { maskMatrix[i][j] = 0; } }
		 * 
		 * double[] infogains = new double[highestAttribIndex+1];
		 * 
		 * for (int i = 0; i <= highestAttribIndex; i++) { infogains[i] = 0; }
		 * 
		 * double[] coverage = new double[highestTargetValueIndex+1];
		 * 
		 * for (int i = 0; i <= highestTargetValueIndex; i++) { coverage[i] = 0;
		 * for (int j = 0; j <= highestAttribIndex; j++) { for (int k = 0; k <
		 * numRules; k++) { //maskMatrix[i][j] += tempRule.getCf(); if
		 * (markerMatrix[k][j][i] == 1) { maskMatrix[i][j] = 1; }
		 * //maskMatrix[i][j] += markerMatrix[k][j][i]; } } } // At this point,
		 * the mask matrix tells us which attributes are // represented at least
		 * once for each target class. Now we'd like to // know how many
		 * attributes predict each target class for comparison.
		 * 
		 * Double tempDouble = new Double(0);
		 * 
		 * for (int i = 0; i < attributes.size(); i++) { tempAttribute =
		 * (LearnerAttribute) attributes.get(i);
		 * infogains[tempAttribute.getIndex()] =
		 * Math.abs(tempAttribute.getInfoGain()); }
		 * 
		 * for (int i = 0; i <= highestTargetValueIndex; i++) { for (int j=0; j <=
		 * highestAttribIndex;j++) { //tempDouble = (Double) attributes.get(j);
		 * coverage[i] += infogains[j] * maskMatrix[i][j]; } } // We now know
		 * how many attributes are coveraged for each class // prediction. Now
		 * to look at the highest coverage. ArrayList bestTargets = new
		 * ArrayList(); Integer tempInt = new Integer(0); int x = 0;
		 * 
		 * bestTargets.add(tempInt);
		 * 
		 * for (int i = 1; i <= highestTargetValueIndex; i++) { tempInt =
		 * (Integer) bestTargets.get(0); x = tempInt.intValue(); if (coverage[i] >
		 * coverage[x]) { bestTargets.clear(); bestTargets.add(new Integer(i)); }
		 * else if (coverage[i] == coverage[x]) { bestTargets.add(new
		 * Integer(i)); } } // 'bestTargets' contains a list of target class
		 * indexes that have most // attribute coverage. If the list is larger
		 * than 1, resort to minimum // weighted voting for those classes.
		 * Otherwise, predict the single // most covered target class. RuleList
		 * bestRules = new RuleList(); Integer tempInteger; for(int nRsI=0;nRsI<rules.length;nRsI++) {
		 * for (int i = 0; i < bestTargets.size(); i++) { for (int j = 0; j <
		 * numRules; j++) { tempInteger = (Integer) bestTargets.get(i); if
		 * (predictedValueIndex[j] == tempInteger.intValue()) {
		 * bestRules.add(rules[nRsI].get(j)); } } } } if (bestTargets.size() >
		 * 1) { // use Min Weighted Voting rules MinWeightedVoting mwv = new
		 * MinWeightedVoting(); mwv.setTarget(target); RuleList[] nrlist = new
		 * RuleList[1]; nrlist[0] = bestRules; nReturn = mwv.predict(nrlist, d);
		 * usedRules = mwv.getUsedRules(); } else { // use the best rules
		 * usedRules = new RuleList(bestRules); nReturn = ((Integer)
		 * bestTargets.get(0)).intValue(); }
		 */
		return nReturn;
	}
}
