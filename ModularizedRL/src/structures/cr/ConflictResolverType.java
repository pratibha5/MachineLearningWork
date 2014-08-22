/*
 * ConflictResolverType.java
 *
 * Created on September 24, 2005, 3:50 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package structures.cr;

import java.io.*;

/**
 * This class is used to store all the different types of EvidenceGatherers or
 * Conflict Resolutors
 * 
 * @author Jonathan Lustgarten
 */
public enum ConflictResolverType implements Serializable {
	/**
	 * 
	 */
	BestPValue(new BestPValue()),
	/**
	 * 
	 */
	EvaluationOnly(new EvaluationOnly()),
	/**
	 * 
	 */
	MinWeightedVoting(new MinWeightedVoting()),
	/**
	 * 
	 */
	MostFeaturesCovered(new MostFeaturesCovered()),
	/**
	 * 
	 */
	MostSpecificSingleBest(new MostSpecificSingleBest()),
	/**
	 * 
	 */
	NearestNeighbor(new NearestNeighbor()),
	/**
	 * 
	 */
	SingleBest(new SingleBest()),
	/**
	 * 
	 */
	SingleBestSpecific(new SingleBestSpecific()),
	/**
	 * 
	 */
	WeightedVoting(new WeightedVoting()),
	/**
	 * 
	 */
	CombineCertaintyFactor(new CombineCF());
	private ConflictResolver cr;

	ConflictResolverType(ConflictResolver conR) {
		cr = conR;
	}

	ConflictResolver getFunction() {
		return cr;
	}
}
