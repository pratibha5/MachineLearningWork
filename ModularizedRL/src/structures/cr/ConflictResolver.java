/*
 * @(#)ConflictResolver.java 1.2 2002/10/28
 */

package structures.cr;

//import java.util.ArrayList;
//import corefiles.structures.data.dataset.attribute.*;
//import corefiles.structures.cr.*;
import data.dataset.*;
import structures.learner.attribute.*;
import rule.*;

/**
 * An abstract base class for all evidence gathering classes which make use a
 * list of rules to predict the target class of a given data item.
 * 
 * @version 1.0 2000/03/20
 * @author Jeremy Ludwig
 * 
 * Edited to make use of new code organization
 * 
 * @version 1.1 2002/01/21
 * @author Will Bridewell
 * 
 * Now uses <code>RuleList</code> instead of <code>String</code>
 * <code>usedRules</code>.
 * <code>getEGArray() now does not include <code>EvaluationOnly</code>
 *
 * @version 1.2 2002/10/28
 * @author Philip Ganchev (philip@cs.pitt.edu)
 *
 * Changed name and adapted file to use new dataset and new format
 * @version 1.3 2005/9/25
 * @author Jonathan Lustgarten
 */
public abstract class ConflictResolver {
	protected String name;
	protected String description;

	/** The target attribute */
	protected LearnerAttribute target;

	/** The index of the target attribute */
	protected int tarIndex;

	/** The rules used in the last prediction */
	protected RuleList usedRules;
	protected double certainty;
	protected boolean enabled;

	/**
	 * Creates an evidence gatherer named by <code>name</code>. The name is
	 * used for GUI display purposes.
	 */
	public ConflictResolver(String name, String description) {
		this.name = name;
		this.description = description;
		usedRules = new RuleList();
		certainty = 0;
		enabled = true;
	}

	/**
	 * Records the index of the target attribute for comparison with other
	 * evidence gathering methods.
	 * 
	 * @param index
	 *            the index of the target attribute
	 */
	public void setTargetValueIndex(int index) {
		tarIndex = index;
	}

	/**
	 * Returns a string representation of this evidence gatherer.
	 * 
	 * @return the name of this evidence gatherer
	 */
	public String toString() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	/**
	 * Sets the target of this evidence gatherer.
	 * 
	 * @param target
	 *            the attribute to set as the target attribute
	 */
	public void setTarget(LearnerAttribute target) {
		this.target = target;
	}

	/**
	 * Predicts the class of <code>d</code> using <code>rules</code>.
	 * 
	 * @param combrls
	 *            the rules to use in the prediction
	 * @param d
	 *            the datum to predict
	 * @return the predicted class of the target attribute
	 */
	public abstract int predict(RuleList combrls, Dataset d);

	/**
	 * Returns the rules used in the last prediction.
	 * 
	 * @return a list of the rules used in the last preditcion
	 */
	public RuleList getUsedRules() {
		return usedRules;
	}

	public double getCertaintyValue() {
		return certainty;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean end) {
		enabled = end;
	}

	/**
	 * Returns the possible evidence gathering methods.
	 * 
	 * @return a list of the possible evidence gathering methods
	 */
	public static ConflictResolver[] getCRArray() {
		ConflictResolver[] crs = new ConflictResolver[8];
		crs[4] = new SingleBest();
		crs[3] = new BestPValue();
		crs[0] = new WeightedVoting();
		crs[5] = new MinWeightedVoting();
		crs[1] = new MaximumLikelihoodRatio();
		// crs[1] = new MostFeaturesCovered();
		crs[6] = new SingleBestSpecific();
		crs[7] = new MostSpecificSingleBest();
		crs[2] = new CombineCF();
		return crs;
	}
}