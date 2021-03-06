/* 
 * @(#)CertaintyFactor.java    1.2 2002/11/21
 */

package structures.cf;

import java.util.ArrayList;

import data.dataset.Dataset;
import structures.learner.attribute.LearnerAttribute;
import rule.Rule;


/**
 * A base class for various certainty factor functions.
 * 
 * @version 1.0 2000/03/20
 * @author Jeremy Ludwig
 * 
 * Edited to make use of new code organization and rename of SignalToNoise
 * 
 * @version 1.1 2002/01/21
 * @author Will Bridewell
 * 
 * Formatted code and comments. Added comments. Renamed getCFArray() to
 * getCfArray() as per Java conventions.
 * 
 * @version 1.2
 * @author Philip Ganchev
 */

public abstract class CertaintyFactor {
	protected String name;

	protected String description;

	protected LearnerAttribute target;

	/**
	 * @param name
	 *            the name of the new certainty factor
	 */
	public CertaintyFactor(String name, String description) {
		this.name = name;
		this.description = description;
	}

	/**
	 * @return the name of this certainty factor
	 */
	public String toString() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	/**
	 * Sets the target attribute for this certainty factor.
	 * 
	 * @param a
	 *            the attribute to set as the new target of this certainty
	 *            factor
	 */
	public void setTarget(LearnerAttribute a) {
		target = a;
	}

	/**
	 * Returns the value of this certainty factor function for 'r'.
	 * 
	 * @param r
	 *            the rule whose certainty factor value to compute
	 * @param d
	 *            TODO
	 * @return 'r''s certainty factor
	 */
	public abstract double getCf(Rule r, LearnerAttribute trg, Dataset d);

	/**
	 * Returns a list of useful subclasses of this class.
	 * 
	 * @return a list of useful subclasses of 'CertaintyFactor'
	 */
	public static CertaintyFactor[] getCfArray() {
		CertaintyFactor[] cfFunctions = new CertaintyFactor[7];
		cfFunctions[0] = new LikelihoodRatio();
		cfFunctions[1] = new PPV();
		cfFunctions[2] = new PPVYates();
		cfFunctions[3] = new PPVNormalized();
		// cfFunctions[3] = new SignalToNoise();
		cfFunctions[4] = new LaplaceEstimate();
		cfFunctions[5] = new LaplaceExtended();	
		cfFunctions[6] = new LaplaceDepth();
		// cfFunctions[3] = new Jaccard();
		// cfFunctions[0] = new FMeasure();
		
		// TODO: add p-value after, we figure out how to deal with
		// the cutoff. Unlike other CFs, smaller p-values are better.
		// This may actually exist in tandem with some other CF, so until we
		// really know what's going on, don't muddle things.
		//
		// cfFunctions[0] = new PValueRight());

		return cfFunctions;
	}
}