/*
 * @(#)AttributeList.java    1.1 2002/01/21
 */

package org.probe.stats.structures.learner.attribute;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.probe.data.dataset.DefaultDataModel;

import data.dataset.*;

/**
 * A list of attributes.
 * 
 * @version 1.0 2000/03/20
 * @author Jeremy Ludwig
 * @see structures.learner.attribute.LearnerAttribute
 * 
 * Edited to make use of new code organization Merged with AttributeKeeper to
 * simplify organization createIntervals now creates 5 intervals by default
 * 1-VLow -INF -> -3 stdev. 2-Low -3 stdev -> -1 stdev. 3-Middle -1 stdev -> 1
 * stdev. 4-High 1 stdev -> 3 stdev. 5-VHigh 3 stdev -> INF
 * 
 * Renamed ignore() and output() to isIgnore() and isOutput() Added ID field and
 * isID() method. Reformatted output of intervalString().
 * @version 1.1 2002/01/21
 * @author Will Bridewell
 * 
 * Added columns "Uses in correct predictions" and "Uses in incorrect
 * predictions" to the table output by method toString(), and put the
 * predictions as its argument (toString(ArrayList)).
 * 
 * @author Jonathan Lustgarten
 * @version 1.2 2005/6/25
 * 
 * changed format to deal with
 * 
 */

public class AttributeList extends ArrayList {

	/*
	 * /** The index of the target attribute. This field should be accessed only
	 * after the data file has been read, or the target attribute has been
	 * manually defined. Its initial value is -1. @author Philip Ganchev
	 * 
	 * @since 2002/11/13
	 */
	// XXX May not work because the user can change the target between runs
	// during a single execution of the program. An attribute does not
	// have reference to all the attribute lists that own it. -PG20021114
	// protected int targetIndex = -1;
	/**
	 * The maximum number of values allowed in an attribute before it is
	 * considered continuous. Used for guessing the attribute type.
	 */
	public int maxDiscreteValueCount;

	public static final int DEFAULT_MAX_DISCRETE_VALUE_COUNT = 10;

	/**
	 * The number of attributes other than ID, target and ignored attributes.
	 * This value is calculated and set when attributes are read from data or
	 * imported, and used in WrapperPanel and Wrapper.
	 */
	public int nInputAttributes;

	/**
	 * Format for printing decimal numbers. The tenths digit is printed, and up
	 * to two additional digits if they are nonzero.
	 */
	final static String DECIMAL_FORMAT = "0.0##";

	/**
	 * Constructs a new attribute list with the specified program state.
	 * 
	 * @param ps
	 *            the ProgramState that owns this attribute list
	 */
	public AttributeList() {
		super();
		nInputAttributes = 0;
		maxDiscreteValueCount = DEFAULT_MAX_DISCRETE_VALUE_COUNT;
	}

	/**
	 * Returns attribute indexed by 'index'.
	 * 
	 * @return the attribute whose index in this list is 'index'
	 */
	public LearnerAttribute getAttribute(int index) {
		return (LearnerAttribute) get(index);
	}

	/**
	 * Returns attribute specified by 'name'.
	 * 
	 * @return the attribute whose name is 'name'
	 */
	public LearnerAttribute getAttribute(String name) {
		for (int x = 0; x < size(); x++)
			if (getAttribute(x).getName().equals(name))
				return getAttribute(x);
		return null;
	}

	/**
	 * Sets all entries in the attributes' cost matrices to 1.
	 * 
	 * @author Eric Williams
	 * @since 2002/07/22
	 */
	public void primeMatrix() {
		for (int i = 0; i < size(); i++) {
			getAttribute(i).primeMatrix();
		}
	}

	/**
	 * Reads the the cost misclassification costs file 'fCost'. The file
	 * specifies the cost of predicting any attribute values incorrectly. Not
	 * all classes of an attribute need to have misclassification costs and an
	 * attribute may have no misclassification costs defined at all.
	 * <p>
	 * The format of the file is:
	 * 
	 * <pre>
	 * {&lt;misclassification_cost&gt; &lt;delim&gt;}*
	 * &lt;miscassification_cost&gt; =&gt;
	 *    Target_name &lt;delim&gt; Predicted_class &lt;delim&gt; Actual_class &lt;delim&gt; Cost
	 * &lt;delim&gt; =&gt; &lt;Tab&gt; | &lt;Newline&gt; | &lt;CarrigeReturn&gt;
	 * </pre>
	 */
	public void importCosts(File fCost) throws IOException {
		FileReader costFileReader = new FileReader(fCost);

		StreamTokenizer streamToker = new StreamTokenizer(costFileReader);
		streamToker.resetSyntax();
		streamToker.wordChars(Character.MIN_VALUE, Character.MAX_VALUE);
		streamToker.whitespaceChars('\t', '\t');
		streamToker.whitespaceChars('\n', '\n');
		streamToker.whitespaceChars('\r', '\r');
		streamToker.parseNumbers();

		String classPred, classAct, targetName;
		double cost;
		LearnerAttribute targetAttrib;
		try {
			while (streamToker.nextToken() != StreamTokenizer.TT_EOF) {
				// Read target attribute
				targetName = streamToker.sval;

				// Set the target attribute name
				targetAttrib = getAttribute(targetName);

				// Read predicted class
				streamToker.nextToken();
				classPred = streamToker.sval;

				// Read actual class
				streamToker.nextToken();
				classAct = streamToker.sval;

				// Read misclassification cost
				streamToker.nextToken();
				cost = streamToker.nval;

				// Set the cost
				targetAttrib.setCost(classPred, classAct, cost);
			}
		} finally {
			costFileReader.close();
		}
	}

	/**
	 * Returns the first attribute whose name begins with "@" or equals "class".
	 * 
	 * @return the first attribute whose name begins with "@" or equals "class".
	 */
	public LearnerAttribute guessTargetAttribute() {
		for (Iterator i = iterator(); i.hasNext();) {
			LearnerAttribute attribute = (LearnerAttribute) i.next();
			String name = attribute.getName();
			if ((name.startsWith("@") || name.equalsIgnoreCase("class"))
					&& attribute.getUseString() == LearnerAttribute.INPUT_USTR) {
				return attribute;
			}
		}
		return null;
	}

	/**
	 * Returns the first attribute whose name is "ID" (ignoring case).
	 * 
	 * @return the first attribute whose name is "ID"
	 */
	public LearnerAttribute guessIdAttribute() {
		for (Iterator i = iterator(); i.hasNext();) {
			LearnerAttribute attribute = (LearnerAttribute) i.next();
			if (attribute.getName().startsWith("#")
					|| attribute.getName().equalsIgnoreCase("ID")
					&& attribute.getUseString() == LearnerAttribute.INPUT_USTR) {
				return attribute;
			}
		}
		return null;
	}

	/**
	 * If there is no target or ID attributes, it guesses which they are based
	 * on their names, and sets them.
	 */
	public void guessSetUses() {
		if (getTargetIndex() < 0) {
			LearnerAttribute target = guessTargetAttribute();
			if (target != null) {
				String name = target.getName();
				if (name.startsWith("=>")) {
					target.setName(name.substring(2).trim());
				}
				target.setUse(LearnerAttribute.OUTPUT);
			}
		}
		if (getIDIndex() < 0) {
			LearnerAttribute id = guessIdAttribute();
			if (id != null) {
				id.setUse(LearnerAttribute.ID);
			}
		}
	}

	/**
	 * Returns the index of the first attribute whose use is OUTPUT.
	 * 
	 * @return the index of the target attribue, or -1 if not found
	 */
	public int getTargetIndex() {
		for (int x = 0; x < size(); x++)
			if (getAttribute(x).isOutput()) {
				return x;
			}
		return -1;
	}

	/**
	 * Returns the target attribute.
	 * 
	 * @return the target attribute of this list, or null if not found
	 */
	public LearnerAttribute getTargetAttribute() {
		int x = getTargetIndex();
		return x > -1 ? (LearnerAttribute) get(x) : null;
	}

	/**
	 * Returns the index of the first identifier attribute.
	 * 
	 * @return the index of the first identifier attribute in this list, or -1
	 *         if not found
	 */
	public int getIDIndex() {
		for (int x = 0; x < size(); x++)
			if (getAttribute(x).isID())
				return x;
		return -1;
	}

	/**
	 * Returns the first identifier attribute.
	 * 
	 * @return the first identifier attribute in this list, or null if not found
	 */
	public LearnerAttribute getIDAttribute() {
		int x = getIDIndex();
		return (x > -1) ? ((LearnerAttribute) get(x)) : null;
	}

	public ArrayList getInputAttributes() {
		ArrayList inAtts = new ArrayList(size());
		for (Iterator i = iterator(); i.hasNext();) {
			LearnerAttribute a = (LearnerAttribute) i.next();
			if (!a.isID() && !a.isOutput() && !a.isIgnore()) {
				inAtts.add(a);
			}
		}
		return inAtts;
	}

	/**
	 * Returns the set of tags of any of the attributes.
	 * 
	 * @return the set of tags of any of the attributes in this list
	 */
	public ArrayList getTagList() {
		ArrayList list = new ArrayList();
		ArrayList subList;

		for (int x = 0; x < size(); x++) {
			subList = getAttribute(x).getTagList();

			for (int y = 0; y < subList.size(); y++) {
				String newStr = subList.get(y).toString();
				if (!list.contains(newStr))
					list.add(newStr);
			}
		}
		return list;
	}

	/**
	 * Resets the AttributeKeeper and initiates an examination of the training
	 * data to create default attributes.
	 */
	public void defineAttributes(DefaultDataModel data) throws Exception {

		// from examineData()
		LearnerAttribute a;
		for (int i = 0; i <data.numAttributes(); i++) {
			Attribute att = data.attribute(i);
			a = new LearnerAttribute(att.name(), i, 
					(att.hasContinuousValues() && att.wasContinuous() ? 
							LearnerAttributeType.Continuous
							: LearnerAttributeType.Discrete));
			if (att.isClass())
				a.setUse(a.OUTPUT);
			else if (att.isId())
				a.setUse(a.ID);
			else
				a.setUse(a.INPUT);
			a.setHierarchy(att.hierarchy());
			add(a);
		}
	}

	/**
	 * Creates initial attributes from examing the data file. The file should be
	 * tab-delimited, with the first row containing attribute names.
	 * 
	 * @param fData
	 *            the file specifying the attributes
	 * @param maxDiscreteValueCount
	 *            the number of integer or float values to consider discrete.
	 *            For example with <code>maxDiscreteValueCount</code> is
	 *            <code>3</code>, an attribute having values
	 *            <code>10, 27, 31</code> in the data would be initialized as
	 *            discrete, while one having values <code>10, 21, 27, 31</code>
	 *            would be initialized as continuous.
	 */
	public void importAttributeNames(File fData, int maxDiscreteValueCount,
			int[] cols) throws IOException {
		FileReader frData = new FileReader(fData);
		this.maxDiscreteValueCount = maxDiscreteValueCount;

		// Read only the first line so EOL is important
		StreamTokenizer toker = new StreamTokenizer(frData);
		toker.resetSyntax();
		toker.wordChars(Character.MIN_VALUE, Character.MAX_VALUE);
		toker.eolIsSignificant(true);
		toker.whitespaceChars('\t', '\t');
		toker.whitespaceChars('\n', '\n');
		toker.whitespaceChars('\r', '\r');

		// Read the attribute names
		try {
			int nCol = 0; // Index of current column in the file
			int nAtt = 0; // Index of current attribute in the cols array
			while ((toker.nextToken() != StreamTokenizer.TT_EOL)
					&& ((cols == null) || ((nAtt < cols.length))
							&& (nCol <= cols[nAtt]))) {
				// nCol++;
				if ((cols != null) && (nCol++ < cols[nAtt])) {
					continue; // Skip token, we don't want it as an attribute
				}
				nAtt++; // Use token as an attribute
				if (toker.ttype == StreamTokenizer.TT_WORD) {
					add(new LearnerAttribute(toker.sval, size()));
				} else if (toker.ttype == StreamTokenizer.TT_NUMBER) {
					add(new LearnerAttribute(String.valueOf(toker.nval), size()));
				} else {
					throw new IOException(
							"Unexpected EOF while reading attributes.");
				}
			}
		} finally {
			frData.close();
		}
	}

	/**
	 * Replaces attributes in the current list with their definition in c.
	 * Attributes that are not in both this list and c will be ignored.
	 */
	public void replaceAttributes(Collection c) {
		Iterator i = c.iterator();
		while (i.hasNext()) {
			LearnerAttribute att = (LearnerAttribute) i.next();
			for (int j = 0; j < size(); j++) {
				if (att.getName().equals(getAttribute(j).getName())) {
					att.setIndex(j);
					set(j, att);
					break;
				}
			}
		}
	}

	/**
	 * Returns interval information in an HTML format.
	 * 
	 * @return information about the attributes in this list, formatted in HTML
	 * @author Yasir Khalifa (table format) 2002/02
	 */
	// AttributeList no longer maintains any ordering information. As a result,
	// this code, which should really appear in the i/o section or Results.java
	// no longer prints the attributes in order of info gain. Will fix this
	// problem as soon as this code is moved.
	public String toString(ArrayList predictions, DataModel trainData) {
		StringBuffer buf = new StringBuffer();

		buf.append("Attributes in Training Data and in Rules\n");
		buf.append("Attribute\t");
		buf.append("Min\t");
		buf.append("Max\t");
		buf.append("Mean\t");
		buf.append("Std dev\t");
		buf.append("Info\ngain\t");
		buf.append("correct predictions\t");
		buf.append("incorrect predictions\n");

		DecimalFormat df = new DecimalFormat(DECIMAL_FORMAT);

		for (int i = 0; i < size(); i++) {
			LearnerAttribute att = getAttribute(i);
			int curAttIx = att.getIndex();
			Attribute ratt = null;
			try {
				ratt = trainData.attribute(curAttIx);
			} catch (AttributeDoesNotExistException e) {
				e.printStackTrace();
			}
			// Print continuous variables that aren't ignored or used for ID.
			if (!att.isIgnore() && !att.isID()) {
				if (att.isOutput()) {
					buf.append(att.getName() + " (target):\t");
				} else {
					buf.append(att.getName() + "\t");
				}
				if (att.getType() == LearnerAttributeType.Continuous) {
					buf.append(df.format(ratt.minimumValue()) + " ");
					buf.append(df.format(ratt.maximumValue()) + " ");
					buf.append(df.format(ratt.average()) + " ");
					buf.append(df.format(ratt.stdev()) + " ");
				} else if (att.getType() == LearnerAttributeType.Discrete) {
					buf.append("    ");
				}
				if (!att.isOutput()) {
					double igain = att.getInfoGain();
					if (igain > 0)
						buf.append("\t" + df.format(igain));
				}
				buf.append("\t ");
				int count = att.countCorrectPredictions(predictions);
				if (count > 0)
					buf.append(count);
				buf.append("\t ");
				count = att.countIncorrectPredictions(predictions);
				if (count > 0)
					buf.append(count);
				buf.append("\n");
			}
		}
		buf.append("\n\n");
		return buf.toString();
	}

	/**
	 * Calculates and sets the information gain of the attribute indexed by
	 * 'index'.
	 */
	public void computeInfoGains(DataModel train) {
		for (int i = 0; i < size(); i++) {
			computeInfoGain(i, train);
		}
	}

	/**
	 * Sets the information gains of all the attributes to 0.
	 */
	public void clearInfoGains() {
		for (int i = 0; i < size(); i++) {
			getAttribute(i).setInfoGain(0);
		}
	}

	/**
	 * Attempts to determine the data type of the 'index'-th attribute based on
	 * the values of the attribute in the data.
	 */
	protected LearnerAttributeType guessAttributeType(DataModel dl, int index) {
		// Object[] values = new Object[dl.size()];

		// for (int i = 0; i < dl.size(); i++) {
		// values[i] = dl.getObject(i, index);
		// }
		Attribute att = null;
		try {
			att = dl.attribute(index);
		} catch (Exception e) {
		}

		if (att.hasContinuousValues())
			return LearnerAttributeType.Continuous;
		else {
			return LearnerAttributeType.Discrete;
		}
	}

	/**
	 * Checks to see if the index-th attribute should be considered discrete. If
	 * it is, then checkForDiscret() fills in its values from k.
	 */
	protected void checkForDiscrete(KBest k, LearnerAttribute a,
			VHierarchyNode vh) {
		if (!k.isFull()) {
			a.setType(LearnerAttributeType.Discrete);

			for (int x = 0; x < k.index; x++)
				vh.addValue(new DiscreteNode(a.getName(), k.v[x]));
		}
	}

	/**
	 * If you're looking for the place where continuous values are discretized,
	 * my friend, you've come to the right tavern. Behold! If we decide to give
	 * a choice between several methods, then they might need to be organized in
	 * a fashion similar to the CF methods. Creates intervals for the index-th
	 * attribute. Defines intervals by the number of standard deviations from
	 * the mean. May become its own object if other methods of interval creation
	 * are implemented.
	 * 
	 * @param index
	 *            the index of the attribute to check
	 */
	private void useOldDiscretization(int index, VHierarchyNode vh,
			DataModel trainingData) {
		IntervalNode newInterval;
		Attribute att = null;
		double mean = 0;
		double sd = 1;
		try {
			att = trainingData.attribute(index);
			mean = att.average();
			sd = att.stdev();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// VLow=(-infinity, -3sd)
		HNode newNode = new HNode(att.name(), "1-VLow");
		vh.addValue(newNode);
		newInterval = new IntervalNode(att.name(), Double.NEGATIVE_INFINITY,
				mean - sd * 3);
		vh.addValue(newNode, newInterval);

		// LowLow=(-3sd, -2sd), HighLow=(-2sd, -1sd)
		newNode = new HNode(att.name(), "2-Low");
		vh.addValue(newNode);
		for (int x = 3; x > 1; x--) {
			newInterval = new IntervalNode(att.name(), mean - sd * x, mean - sd
					* (x - 1));
			vh.addValue(newNode, newInterval);
		}

		// LowMid=(-1sd, 0sd), HighMid=(0sd, 1sd)
		newNode = new HNode(att.name(), "3-Middle");
		vh.addValue(newNode);
		for (int x = -1; x < 1; x++) {
			newInterval = new IntervalNode(att.name(), mean + sd * x, mean + sd
					* (x + 1));
			vh.addValue(newNode, newInterval);
		}

		// LowHigh=(1sd, 2sd), HighHigh=(sd2, 3sd)
		newNode = new HNode(att.name(), "4-High");
		vh.addValue(newNode);
		for (int x = 1; x < 3; x++) {
			newInterval = new IntervalNode(att.name(), mean + sd * x, mean + sd
					* (x + 1));
			vh.addValue(newNode, newInterval);
		}

		// VHigh=(3sd, infinity)
		newNode = new HNode(att.name(), "5-VHigh");
		vh.addValue(newNode);
		newInterval = new IntervalNode(att.name(), mean + sd * 3,
				Double.POSITIVE_INFINITY);
		vh.addValue(newNode, newInterval);
	}

	// /**
	// * Creates intervals for the attributeList[nIndex].
	// * Intervals are created recursively with the N biggest gaps
	// */
	// private void CreateIntervals(int nIndex, int numberOfSplits)
	// {
	// //1 - get a list of all the data items of this variable (index)
	// //2 - sort this list in order of data item
	// //3 - determine item differences with next in list (me vs. me+1),
	// // save next item
	// //4 - sort according to difference factor
	// //5 - truncate to the number of items that are required
	// // (numberOfSplits)
	// //6 - sort according to data item order
	// //7 - Create intervals based on new Interval(LastIntervalEnd,
	// // me.next); Last interval end = me.next
	// }

	/**
	 * Calculates and sets the information gain of the attribute indexed by
	 * <code>attributeIndex</code>.
	 * 
	 * @param attributeIndex
	 *            the index of the attribute by <code>attributeIndex</code>
	 */
	public void computeInfoGain(int attributeIndex, DataModel train) {
		Object[] values = getAttribute(attributeIndex).getHierarchy()
				.getValueArray();
		LearnerAttribute target = getTargetAttribute();
		double[] p = new double[target.getHierarchy().numValues()];
		int n;

		// Initialize the p's
		for (int i = 0; i < p.length; i++)
			p[i] = 0;

		// Add up the counts
		for (int j = 0; j < train.numInstances(); j++) {
			try {
				n = target.getHierarchy().getValueIndex(
						train.attributeValueString(j, target.getIndex()));
			} catch (Exception e) {
				n = -1;
			}
			if (n != -1)
				p[n]++;
		}

		// Create the p ratios and the entropy sum
		double entropy = 0;
		for (int x = 0; x < p.length; x++) {
			p[x] /= (double) train.numInstances();
			entropy += (-1) * p[x] * org.probe.util.MathUtil.log2(p[x]);
		}

		// Set info-gain for each attribute
		double tempEntropy = 0;
		for (int k = 0; k < values.length; k++) {
			tempEntropy += this.entropy(values[k], attributeIndex, train);
		}

		getAttribute(attributeIndex).setInfoGain(entropy - tempEntropy);
	}

	/**
	 * Returns the entropy for one value of an attribute.
	 * 
	 * @param value
	 *            the value of attribute
	 * @param attributeIndex
	 *            the index of attribute in the this list.
	 * @return the entropy for the specified value of the attribute whose index
	 *         in this list is 'atributeIndex'.
	 */
	protected double entropy(Object value, int attributeIndex, DataModel train) {
		double[] p = new double[getTargetAttribute().getHierarchy().numValues()];
		int n;
		int nSv = 0;
		LearnerAttribute target = getTargetAttribute();

		// Initialize the p's
		for (int x = 0; x < p.length; x++) {
			p[x] = 0;
		}
		// Add up the counts
		for (int x = 0; x < train.numInstances(); x++) {
			// We only want Sv
			try {
				Object d = train.originalAttributeValue(x, attributeIndex);
				if (value.equals(d)
				// || attList.getA(nAttributeIndex).inSubtree(value, d)
				) {
					n = target.getHierarchy().getValueIndex(
							train.originalAttributeValue(x, target.getIndex()));
					if (n != -1) {
						p[n]++;
						nSv++;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// Create the p ratios and the entropy sum
		double dReturn = 0;
		for (int x = 0; x < p.length; x++) {
			p[x] = (nSv == 0) ? 0 : (p[x] / nSv);
			dReturn += (-1) * p[x] * org.probe.util.MathUtil.log2(p[x]);
		}

		// Multiply by the ratio Sv/S
		dReturn = dReturn * nSv / train.numInstances();
		return dReturn;
	}

	/**
	 * Returns a list of attributes in this attribute list, whose use is use.
	 * This is useful for finding non-unique ID attributes in the list.
	 */
	public ArrayList findAllByUse(int use) {
		ArrayList list = new ArrayList(size());
		LearnerAttribute a;
		for (java.util.Iterator i = iterator(); i.hasNext();) {
			a = (LearnerAttribute) i.next();
			if (a.getUse() == use) {
				list.add(a);
			}
		}
		return list;
	}

	/**
	 * Keeps the first k elements found. Useful in determining the number of
	 * integer or float values an attribute has, to help determine if it is
	 * continuous or discrete.
	 */
	protected class KBest {
		/** Index the of value in the list of identified values */
		public int index;

		/** The list of identified values */
		public double[] v;

		/**
		 * The largest number of identified values allowed. An attribute may
		 * default to type continuous if the data contains more than
		 * <code>count</code> values for that attribute.
		 */
		public int maxDiscreteValueCount;

		public KBest(int maxDiscreteValueCount) {
			this.maxDiscreteValueCount = maxDiscreteValueCount;
			index = 0;
			// Add one extra, e.g: want 2, must hold three
			v = new double[maxDiscreteValueCount + 1];
		}

		public void add(double d) {
			boolean bAdd = true;

			// Don't add values already in list
			for (int x = 0; x < index; x++)
				if (d == v[x])
					bAdd = false;

			if (bAdd)
				v[index++] = d;
		}

		public boolean isFull() {
			return (index > maxDiscreteValueCount);
		}
	}

	public int countInputAttributes() {
		nInputAttributes = size();
		for (int a = 0; a < size(); a++) {
			LearnerAttribute att = getAttribute(a);
			if (att.isID() || att.isOutput() || att.isIgnore()) {
				nInputAttributes--;
			}
		}
		return nInputAttributes;
	}

	public void reIndex(int[] newIndeces) {
		for (Iterator i = iterator(); i.hasNext();) {
			((LearnerAttribute) i.next()).reIndex(newIndeces);
		}
	}

	public static int[] getIndeces(ArrayList atts) {
		int[] indeces = new int[atts.size()];
		int j = 0;
		for (java.util.Iterator it = atts.iterator(); it.hasNext(); j++) {
			indeces[j] = ((LearnerAttribute) it.next()).getIndex();
		}
		return indeces;
	}

	public static boolean equals(ArrayList atts1, ArrayList atts2) {
		if (atts1.size() != atts2.size())
			return false;
		AttributeNameComparator comp = new AttributeNameComparator();
		java.util.Collections.sort(atts1, comp);
		java.util.Collections.sort(atts2, comp);
		for (int nAtt = 0; nAtt < atts1.size(); nAtt++) {
			LearnerAttribute a1 = (LearnerAttribute) atts1.get(nAtt);
			LearnerAttribute a2 = (LearnerAttribute) atts2.get(nAtt);
			// if (! a1.equals(a2))
			if (!a1.getName().equals(a2.getName()))
				return false;
		}
		return true;
	}

	/**
	 * Returns the number of unique conjuncts (attribute-value pairs)
	 * minus those associated with the target (class) attribute or ID attributes
	 * or attributes which the user specified as ignored.
	 * 
	 * @return the number of possible unique conjuncts (att-val pairs)
	 *         minus those associated with the target value
	 */
	public int countAttributeValues() {
			int count = 0;
			for (Iterator iter = iterator(); iter.hasNext();) {
				LearnerAttribute att = (LearnerAttribute) iter.next();
				if (!att.isOutput() && !att.isIgnore() && !att.isID()) {
					count += att.getHierarchy().numValues();
				}
				// System.out.println("Number of counts for unique conjuncts for
				// Attribute "+att.getName()+": "+att.getHierarchy().numValues());
				// System.out.println("Number of total conjuncts so far: "+count);
			}

			return count;
		}
	}
