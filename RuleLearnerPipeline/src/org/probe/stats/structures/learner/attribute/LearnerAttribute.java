/*
 * @(#)Attribute.java    1.1 2002/01/21
 */

package org.probe.stats.structures.learner.attribute;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.probe.rule.Rule;
import org.probe.util.RuleList;
import org.probe.stats.structures.result.Prediction;
import org.probe.stats.structures.result.RulePrediction;

/**
 * Attribute represents one variable in the data. Each attribute has some
 * associated information: name, type, use, tags, number of times it can be used
 * in a rule and information gain. It also has a hierarchy of values. Each datum
 * has a value for this attribute which is a node in this attribute's value
 * hierarchy.
 * 
 * @version 1.0 2000/03/20
 * @author Jeremy Ludwig
 * @see structures.learner.attribute.VHierarchy
 * @see structures.learner.attribute.AttributeList
 * 
 * Edited to make use of new code organization. Replaced direct use/creation of
 * type/use/compare strings with static finals. Added ID use. Removed data[] in
 * favor of separate fields. Added comments.
 * 
 * @version 1.1 2002/01/21
 * @author Will Bridewell
 * 
 * Added method setName(String), used to remove a leading "=>" of a target
 * attribute. The get and set methods should all be replaced by public fields.
 * @version 2004/02/05
 * @author Philip Ganchev
 * 
 * @author Jonathan Lustgarten
 * @version 2005/6/26 Modified the name and added an enumerated type Uses much
 *          more compact way and no longer have all these spurious references
 *          that occurred in previous versions
 */
public class LearnerAttribute {

	// Attribute data
	/** The hierarchy of values this attribute may take */
	private VHierarchyNode hierarchy;

	/** The name of this attribute */
	private String name;

	/**
	 * The type of this attribute, such as discrete, continuous and set.
	 * 
	 * @see #DISCRETE
	 * @see #CONTINUOUS
	 * @see #SET
	 * @see INVALID
	 * @see #DISCRETE_TSTR
	 * @see #CONTINUOUS_TSTR
	 * @see #INVALID_TSTR
	 */
	private LearnerAttributeType type;

	/**
	 * The use of this attribute, such as input, output, identification or
	 * ignored.
	 * 
	 * @see #INPUT
	 * @see #OUTPUT
	 * @see #IGNORE
	 * @see #ID
	 * @see INVALID
	 * @see #INPUT_USTR
	 * @see #OUTPUT_USTR
	 * @see #ID_USTR
	 * @see INVALID_USTR
	 */
	private int use;

	private String tags; // XXX document!

	/** Number of times this attribute can appear in a rule */
	private int usesCount;

	/** This attribute's information gain */
	private double infoGain;

	/** This attribute's index in the set of attributes */
	private int index;

	private double costMatrix[][]; // XXX document!

	private double avgCost[]; // XXX document!

	private int matrixSize; // XXX document!

	// Use constants

	/**
	 * Denotes the input attribute use. Attributes of this type are used for
	 * induction.
	 * 
	 * @see #use
	 */
	public static final int INPUT = 0;

	/**
	 * Denotes the output attribute use. This is the attribute RL tries to
	 * predict. There can be only one output attribute when RL performs
	 * induction. Attribute of this type are also called target attributes.
	 * 
	 * @see #use
	 */
	public static final int OUTPUT = 1;

	/**
	 * Denotes the ignored attribute use. Attributes of this use are not used
	 * for induction.
	 * 
	 * @see #use
	 */
	public static final int IGNORE = 2;

	/**
	 * Denotes the identification attribute use. Attributes of this type are not
	 * used for induction.
	 * 
	 * @see #use
	 */
	public static final int ID = 3;

	/**
	 * Describes the input attribute use.
	 * 
	 * @see #use
	 */
	public static final String INPUT_USTR = "input";

	/**
	 * Describes the output attribute use.
	 * 
	 * @see #use
	 */
	public static final String OUTPUT_USTR = "output";

	/**
	 * Describes the ignored attribute use.
	 * 
	 * @see #use
	 */
	public static final String IGNORE_USTR = "ignore";

	/**
	 * Describes the identification attribute use.
	 * 
	 * @see #use
	 */
	public static final String ID_USTR = "identification";

	/**
	 * Describes the invalid attribute use.
	 * 
	 * @see #use
	 */
	public static final String INVALID_USTR = "invalid use";

	/**
	 * Constructs a discrete input attribute named <code>name</code> with an
	 * empty tags list, and which can appear only once in a rule.
	 * 
	 * @param name
	 *            the name of this attribute
	 * @param index
	 *            the index of this attibute in an attribute set
	 */
	public LearnerAttribute(String name, int index) {
		this.name = name;
		type = LearnerAttributeType.Discrete;
		use = INPUT;
		tags = "";
		usesCount = 1; // Number of times this attribute can appear in a rule
		infoGain = 0;
		hierarchy = new HNode(name, "ROOT");
		this.index = index;
	}

	public LearnerAttribute(String name, int index, LearnerAttributeType a) {
		this.name = name;
		type = a;
		use = INPUT;
		tags = "";
		usesCount = 1; // Number of times this attribute can appear in a rule
		infoGain = 0;
		hierarchy = new HNode(name, "ROOT");
		this.index = index;
	}

	/**
	 * Fills the attribute's row of cost matrix with <code>1</code>s, which
	 * represents a uniform cost.
	 * 
	 * @author Eric Williams
	 * @since 2002/07/22
	 */
	public void primeMatrix() {
		matrixSize = hierarchy.numValues();
		costMatrix = new double[matrixSize][matrixSize];
		avgCost = new double[matrixSize];

		for (int i = 0; i < matrixSize; i++) {
			for (int j = 0; j < matrixSize; j++) {
				costMatrix[i][j] = 1.0;
			}
			avgCost[i] = 1.0;
		}
	}

	/**
	 * Calculates the average misclassification cost.
	 * 
	 * @author Eric Williams
	 * @since 2002/07/22
	 */
	private void calculateAvgCost() {
		double sum;

		for (int i = 0; i < matrixSize; i++) {
			sum = 0;
			for (int j = 0; j < matrixSize; j++) {
				sum = sum + costMatrix[i][j];
			}
			avgCost[i] = sum / matrixSize;
		}
	}

	/**
	 * Calulates and returns the average cost for a target class.
	 * 
	 * @param valueIndex
	 *            the index of the value in the value hierarchy
	 * @return the average cost of this attribute
	 * @author Eric Williams
	 * @since 2002/07/22
	 */
	public double getAvgCost(int valueIndex) {
		// If the costs haven't been set before, go ahead and prime the matrix.
		if (avgCost == null) {
			primeMatrix();
		}
		double newCost;
		calculateAvgCost();
		newCost = avgCost[valueIndex];
		return newCost;
	}

	/**
	 * Sets the misclassication cost for one predicted/actual pair.
	 * 
	 * @param classPred
	 * @param classAct
	 * @param cost
	 * @author Eric Williams
	 * @since 2002/07/22
	 */
	public void setCost(String classPred, String classAct, double cost) {
		int predIndex, actIndex, counter = 0;

		predIndex = hierarchy.getValueIndex(classPred);

		actIndex = hierarchy.getValueIndex(classAct);

		costMatrix[predIndex][actIndex] = cost;
	}

	public boolean isIgnore() {
		return use == IGNORE;
	}

	public boolean isID() {
		return use == ID;
	}

	public boolean isOutput() {
		return use == OUTPUT;
	}

	public void setInfoGain(double d) {
		infoGain = d;
	}

	public double getInfoGain() {
		return infoGain;
	}

	public int getNumberUses() {
		return usesCount;
	}

	public int getIndex() {
		return index;
	}

	public String getName() {
		return name;
	}

	public void setName(String s) {
		this.name = s;
	}

	public LearnerAttributeType getType() {
		return type;
	}

	public String getTypeString() {
		return type.getTypeTxt();
	}

	public String getUseString() {
		return use == INPUT ? INPUT_USTR : use == OUTPUT ? OUTPUT_USTR
				: use == IGNORE ? IGNORE_USTR : use == ID ? ID_USTR
						: INVALID_USTR;
	}

	public String getCompString() {
		return type.getDescripTxt();
	}

	public VHierarchyNode getHierarchy() {
		return hierarchy;
	}

	public void setHierarchy(VHierarchyNode v) {
		if (v == null) 
			throw new AssertionError();
		hierarchy = v;
	}

	public ArrayList getTagList() {
		ArrayList list = new ArrayList();

		StringTokenizer tok = new StringTokenizer(tags, ",:; ");

		while (tok.hasMoreElements()) {
			String newStr = new String(tok.nextElement().toString());
			if (!list.contains(newStr))
				list.add(newStr);
		}

		return list;
	}

	public String getTagString() {
		return tags;
	}

	// AttributeList may want to alter the index of the attribute. This is
	// necessary when we've read in attribute descriptions from an external
	// source. Attribute order in the descriptor file may be different from
	// that in the data file, and we want the index to match w/ the data file.

	// This sits at default (package-level) visibility because it makes sense
	// for the AttributeList to control index, but no one else.
	public void setIndex(int index) {
		this.index = index;
	}

	public void setType(LearnerAttributeType type) {
		this.type = type;
	}

	public void setTypeString(String typeS) {
		LearnerAttributeType[] types = LearnerAttributeType.values();
		for (int i = 0; i < types.length; i++) {
			if (types[i].equals(typeS)) {
				type = types[i];
				break;
			}
		}
	}

	public void setUse(int use) {
		this.use = use;
	}

	public int getUse() {
		return use;
	}

	public void setUseString(String use) {
		if (use.equals(INPUT_USTR))
			this.use = INPUT;
		else if (use.equals(OUTPUT_USTR))
			this.use = OUTPUT;
		else if (use.equals(IGNORE_USTR))
			this.use = IGNORE;
		else if (use.equals(ID_USTR))
			this.use = ID;
	}

	public void setTagString(String s) {
		tags = s;
	}

	public void setUsesCount(int n) {
		usesCount = n;
	}

	public String toString() {
		return name;
	}

	public int compareToDataValue(Object value, Object o) {
		return hierarchy.compareToDataValue(o);
	}

	public int countCorrectPredictions(ArrayList predictions) {
		int predictionCount = 0;
		for (Iterator x = predictions.iterator(); x.hasNext();) {
			Prediction p = (Prediction) x.next();
			if (!p.isCorrect())
				continue;
			if (p instanceof RulePrediction) {
				RuleList rules = ((RulePrediction) p).getUsedRules();
				for (Iterator y = rules.toArrayList().iterator(); y.hasNext();) {
					if (((Rule) y.next()).getLhs().matches(this)) {
						predictionCount++;
						break;
					}
				}
			} else
				predictionCount++;
		}
		return predictionCount;
	}

	public int countIncorrectPredictions(ArrayList predictions) {
		int predictionCount = 0;
		for (Iterator x = predictions.iterator(); x.hasNext();) {
			Prediction p = (Prediction) x.next();
			if (p.isCorrect())
				continue;
			if (p instanceof RulePrediction) {
				RuleList rules = ((RulePrediction) p).getUsedRules();
				for (Iterator y = rules.toArrayList().iterator(); y.hasNext();) {
					if (((Rule) y.next()).getLhs().matches(this)) {
						predictionCount++;
						break;
					}
				}
			} else
				predictionCount++;
		}
		return predictionCount;
	}

	public boolean equals(Object o) {
		return (o instanceof LearnerAttribute)
				&& name.equals(((LearnerAttribute) o).getName());
	}

	public void reIndex(int[] newIndeces) {
		for (int i = 0; i < newIndeces.length; i++) {
			if (newIndeces[i] == index) {
				index = i;
			}
		}
	}

	public Object clone() {
		LearnerAttribute a = new LearnerAttribute(name, index);
		a.hierarchy = hierarchy;
		a.type = type;
		a.use = use;
		a.tags = tags;
		a.usesCount = usesCount;
		a.infoGain = infoGain;
		a.costMatrix = costMatrix;
		a.avgCost = avgCost;
		a.matrixSize = matrixSize;
		return a;
	}
}
