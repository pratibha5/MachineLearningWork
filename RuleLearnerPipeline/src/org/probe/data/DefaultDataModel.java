package org.probe.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.probe.data.DataModel;
import org.probe.stats.structures.data.converters.input.TabCsvDataLoader;
import org.probe.stats.structures.learner.attribute.AttributeList;
import org.probe.stats.structures.learner.attribute.VHierarchyNode;
import org.probe.util.Arrays;
import org.probe.util.MathUtil;
import org.probe.data.dataset.*;
import weka.core.FastVector;
import weka.core.Instance;

public class DefaultDataModel implements DataModel{
	// Data set information
	private String fileName;
	private String sep;

	// Attribute information
	private HashMap<Integer, Attribute> posToAttribute;
	private HashMap<String, Attribute> nameToAttribute;
	private int[] doubleAttrPos;
	private int[] objectAttrPos;
	private int classPos;
	//private int samplePos; // Position of the the attribute that identifies technical replicates
	private int idPos;
	private boolean isDiscretized;
	private long cvRandSeed = 1;

	// Data information
	private int[][][] foldSplits;
	private double[][] attributeValues; // Attribute row, instance column
	private boolean[] instCovered;
	private VHierarchyNode[] vhierarchies;
	public int iRepAtt = -1;

	public DefaultDataModel() {
		posToAttribute = new HashMap<Integer, Attribute>();
		nameToAttribute = new HashMap<String, Attribute>();
		classPos = -1;
		idPos = -1;
	}

	public DefaultDataModel(ArrayList<Attribute> atts, String fileName, String seperator) {
		this.fileName = fileName;
		sep = seperator;
		// Attribute setting
		posToAttribute = new HashMap<Integer, Attribute>(atts.size());
		nameToAttribute = new HashMap<String, Attribute>(atts.size());
		doubleAttrPos = new int[atts.size()];
		objectAttrPos = new int[atts.size()];
		vhierarchies = new VHierarchyNode[atts.size()];
		int numDA = 0;
		int numOA = 0;
		for (int i = 0; i < atts.size(); i++) {
			Attribute a = atts.get(i);
			a.setPosition(i);
			a.setReferenceDataModel(this);
			if (a.isClass())
				classPos = i;
			else if (a.isId())
				idPos = i;
			posToAttribute.put(new Integer(i), a);
			nameToAttribute.put(a.name(), a);
			if (a.hasContinuousValues()) {
				doubleAttrPos[numDA] = i;
				a.setDoublePosition(numDA);
				numDA++;
			} else {
				objectAttrPos[numOA] = i;
				numOA++;
				if (vhierarchies[i] == null)
					vhierarchies[i] = a.genHierarchy();
			}
		}
		int[] tDA = new int[numDA];
		System.arraycopy(doubleAttrPos, 0, tDA, 0, numDA);
		doubleAttrPos = tDA;
		//tDA = null;
		int[] tOA = new int[numOA];
		System.arraycopy(objectAttrPos, 0, tOA, 0, numOA);
		objectAttrPos = tOA;
	}

	public void setRandSeed(long rs) {
		cvRandSeed = rs;
	}

	/**
	 * Adds the instance values to the data set; assumes an NxN matrix
	 * 
	 * @param insts
	 *            The attribute values with attributes as columns
	 */
	public void setInstances(double[][] insts) {
		instCovered = new boolean[insts.length];
		attributeValues = new double[insts[0].length][insts.length];
		for (int i = 0; i < insts.length; i++) {
			instCovered[i] = false;
			for (int j = 0; j < insts[i].length; j++)
				attributeValues[j][i] = insts[i][j];
		}
		recalculateAttributeInfo();
		//System.out.println("DataModel.setInstances() " + instCovered.length);
	}

	/**
	 * @param inst
	 * @return
	 * @throws ValueNotFoundException
	 */
	public org.probe.data.dataset.Instance getInstance(int inst)
			throws ValueNotFoundException {
		//corefiles.structures.data.dataset.Instance ind;
		if (inst > attributeValues[0].length - 1)
			return null;
		double[] vals = new double[attributeValues.length];
		for (int i = 0; i < attributeValues.length; i++)
			vals[i] = attributeValues[i][inst];
		return new org.probe.data.dataset.Instance(
				posToAttribute.get(idPos).getValue(attributeValues[idPos][inst]),
				vals, instCovered[inst]);
	}

	public Attribute attribute(int a) throws AttributeDoesNotExistException {
		if (a >= posToAttribute.size())
			throw new AttributeDoesNotExistException(a, posToAttribute.size());
		Attribute x = posToAttribute.get(a);
		VHierarchyNode h = x.hierarchy();
		return posToAttribute.get(a);
	}

	public Attribute attribute(String name)
			throws AttributeDoesNotExistException {
		if (nameToAttribute.containsKey(name))
			return nameToAttribute.get(name);
		else
			throw new AttributeDoesNotExistException(name);
	}

	public void normAtt(String attName, double sub, double div) 
			throws AttributeDoesNotExistException {
		Attribute att = attribute(attName);
		if (att.hasContinuousValues()) {
			for (int i = 0; i < attributeValues[att.position()].length; i++) {
				attributeValues[att.position()][i] -= sub;
				attributeValues[att.position()][i] /= div;
			}
		}
	}

	public Attribute classAttribute() {
		return posToAttribute.get(classPos);
	}

	public int classAttIndex() {
		return classPos;
	}

	public void setClassAttIndex(int pos) {
		if (classPos > -1) {
			posToAttribute.get(classPos).setIsClass(false);
		}
		classPos = pos;
		posToAttribute.get(pos).setIsClass(true);
	}

	/**
	 * Retrieves the index of the ID attribute
	 * 
	 * @return The position of the ID attribute if there is one, else -1
	 */
	public int getIdAttIndex() {
		return idPos;
	}

	public void setIdAttIndex(int pos) {
		idPos = pos;
	}

	/**
	 * Retrieves the total number of attributes stored in the dataset.
	 * 
	 * @return The number of attributes in the dataset
	 */
	public int numAttributes() {
		return posToAttribute.size();
		//return attributeValues == null ? 0 : attributeValues.length;
	}
	
	/**
	 * Optimized for removing a single attribute. To remove multiple attributes, use
	 * one of the methods <code>removeAttributes()</code>.
	 * 
	 * @param attrName
	 *            The Attribute to remove
	 * @throws AttributeDoesNotExistException
	 */
	public void removeAttribute(String attrName)
			throws AttributeDoesNotExistException {
		Attribute att = attribute(attrName);
		if (att.position() < classPos)
			classPos--;
		if (att.position() < idPos)
			idPos--;
		if (att.isClass()) {
			classPos = -1;
			System.out.println("Removing the class attribute!");
		}
		if (att.position() == idPos)
			idPos = -1;
		double[][] navs = new double[attributeValues.length - 1][0];
		System.arraycopy(attributeValues, 0, navs, 0, att.position());
		for (int i = att.position() + 1; i < posToAttribute.size(); i++) {
			navs[i - 1] = attributeValues[i];
			Attribute nAtt = posToAttribute.get(i);
			nAtt.setPosition(nAtt.position() - 1);
			posToAttribute.put(i - 1, nAtt);
		}
		posToAttribute.remove(posToAttribute.size() - 1);
		nameToAttribute.remove(att.name());
		attributeValues = navs;
		att = null;
	}

	/**
	 * Returns a copy of the dataset attribute values such that any changes to
	 * the returned copy are not reflected in the database. Might cause memory
	 * issues, possibly.
	 * 
	 * @return A copy of the dataset attribute values
	 */
	public double[][] attributeValues() {
		double[][] nAtts = new double[attributeValues.length][0];
		for (int i = 0; i < nAtts.length; i++) {
			double[] nds = new double[attributeValues[i].length];
			System.arraycopy(attributeValues[i], 0, nds, 0, nds.length);
			nAtts[i] = nds;
		}
		return nAtts;
	}
	
	private ArrayList<Attribute> cloneAttributes() {
		ArrayList<Attribute> clonedAtts = new ArrayList<Attribute>(
				posToAttribute.size());
		for (int i = 0; i < posToAttribute.size(); i++)
			clonedAtts.add((Attribute) posToAttribute.get(new Integer(i)).clone());
		return clonedAtts;
	}

	public double[][] getDoubleAttributeValues() {
		double[][] ndvs = new double[doubleAttrPos.length][0];
		for (int i = 0; i < ndvs.length; i++) {
			double[] ndvsI = new double[attributeValues[doubleAttrPos[i]].length];
			System.arraycopy(attributeValues[doubleAttrPos[i]], 0, ndvsI, 0,
					ndvsI.length);
			ndvs[i] = ndvsI;
		}
		return ndvs;
	}

	public void recalculateAttributeInfo() {
		for (int i = 0; i < attributeValues.length; i++) {
			posToAttribute.get(new Integer(i)).recalculateInfo(attributeValues[i]);
		}
	}

	/**
	 * Returns the number of attributes that were continuous before the data set
	 * was discretized
	 * 
	 * @return the number of continuous attributes;
	 */
	public int numContinuousAttributes() {
		return doubleAttrPos.length;
	}

	/**
	 * Returns an array of indices indicating where the continuous variables are 
	 * in relation to all variables within the data set.
	 * 
	 * @return The variable indexes
	 */
	public int[] getContinuousAttributeIndexes() {
		int[] ndas = new int[doubleAttrPos.length];
		System.arraycopy(doubleAttrPos, 0, ndas, 0, ndas.length);
		return ndas;
	}

	public double attributeValue(int instIx, int attIx)
			throws AttributeDoesNotExistException, InstanceNotFoundException {
		if (attIx < 0 || attIx > attributeValues.length - 1)
			throw new AttributeDoesNotExistException(attIx,
					attributeValues.length);
		if (instIx < 0 || instIx > attributeValues[attIx].length - 1)
			throw new InstanceNotFoundException(instIx,
					attributeValues[attIx].length);
		return attributeValues[attIx][instIx];
	}

	/**
	 * Returns either the double value if there is no discretization, or the
	 * discretized range as a string if there is the String value.
	 * 
	 * @param instIx
	 *            The instance index in the data set
	 * @param attIx
	 *            The attribute index in the data set
	 * @return The String representation of the attribute value
	 * @throws AttributeDoesNotExistException
	 * @throws InstanceNotFoundException
	 * @throws ValueNotFoundException
	 */
	public String attributeValueString(int instIx, int attIx)
			throws AttributeDoesNotExistException, InstanceNotFoundException,
			ValueNotFoundException {
		if (attIx < 0 || attIx > attributeValues.length - 1)
			throw new AttributeDoesNotExistException(attIx,
					attributeValues.length);
		if (instIx < 0 || instIx > attributeValues[attIx].length - 1)
			throw new InstanceNotFoundException(instIx,
					attributeValues[attIx].length);

		return posToAttribute.get(attIx).getValue(attributeValue(instIx, attIx));
	}

	public void setNewDoubleValues(double[][] nDVs) {
		for (int i = 0; i < doubleAttrPos.length; i++) {
			attributeValues[doubleAttrPos[i]] = nDVs[i];
			Attribute vda = posToAttribute.get(doubleAttrPos[i]);
			vda.recalculateInfo(nDVs[i]);
		}
	}
		
	public double[] getClassValues() throws AttributeDoesNotExistException {
		if (classPos == -1)
			throw new AttributeDoesNotExistException("Class Attribute");
		double[] cp = new double[attributeValues[classPos].length];
		System.arraycopy(attributeValues[classPos], 0, cp, 0, cp.length);
		return cp;
	}
	
	public void setDiscretization(double[][] cutPoints) {
		for (int i = 0; i < cutPoints.length; i++) {
			Attribute att = posToAttribute.get(doubleAttrPos[i]);
			try {
				att.setDiscretization(cutPoints[i]);
				att.recalculateInfo(attributeValues[doubleAttrPos[i]]);
			} catch (Exception e) {
				System.err.println("Error in setting discretzation at att: "
						+ att.name());
				System.err.println("Pos: " + att.position());
				System.err.println("Discretization policy length: " + cutPoints[i].length);
				att.setDiscretization(new double[0]);
				att.recalculateInfo(attributeValues[doubleAttrPos[i]]);
			}
			vhierarchies[doubleAttrPos[i]] = att.genHierarchy();
			/*
			 * if(!(atts[i]==null||atts[i].length==0))
			 * System.out.println(doubleAttrPos[i]);
			 */
		}
		isDiscretized = true;
	}

	/**
	 * Adds discretization policies to the data set using the policies and the
	 * training dataset as a reference. This allows corresponding attributes to
	 * be in different positions in the two data sets.
	 * 
	 * @param pts
	 *            The discretization bins
	 * @param ref
	 *            The reference dataset
	 * @throws AttributeDoesNotExistException 
	 */
	public void setDiscretization(double[][] pts, DefaultDataModel ref) throws AttributeDoesNotExistException {
		int[] datts = ref.getContinuousAttributeIndexes();
		for (int i = 0; i < datts.length; i++) {
			if (nameToAttribute.containsKey(ref.attribute(datts[i]).name())) {
				Attribute att = nameToAttribute.get((ref.attribute(datts[i])).name());
				att.setDiscretization(pts[i]);
				att.recalculateInfo(attributeValues[att.position()]);
				vhierarchies[att.position()] = ref.getHierarchy(datts[i]);
			}
		}
		isDiscretized = true;		// PG2009
	}

	public boolean isDiscretized() {
		return isDiscretized;
	}

	public int numInstances() {
		return 
			attributeValues == null ? 0
			: attributeValues[0] == null ? 0
			: attributeValues[0].length;
	}

	private int[] genRandomOrder(int[] oa) {
		int[] ra = new int[oa.length];
		ArrayList<Integer> tmps = new ArrayList<Integer>(oa.length);
		for (int i = 0; i < oa.length; i++)
			tmps.add(new Integer(oa[i]));
		int ind = 0;
		
		Random r = new Random(cvRandSeed);
		while (tmps.size() > 0) {
			ra[ind] = tmps.remove(r.nextInt(tmps.size())).intValue();
			ind++;
		}
		return ra;
	}

	/**
	 * Creates stratified folds for cross-validation.
	 * 
	 * @param numFolds
	 *            The number of folds to be generated
	 */
	public void generateNewFolds(int numFolds) {
		Attribute classAtt = classAttribute();

		// Set up distribution:
		// Store instance IDs in the respective classes
		double[] dists = new double[classAtt.numValues()];
		int numInstances = (int) classAtt.counts();
		double[] tmpDist = new double[dists.length];
		// Copy the distribution of class values to the variables "dists" and "tmpDist"
		System.arraycopy(classAtt.getDistribution(), 0, dists, 0, dists.length);  
		System.arraycopy(dists, 0, tmpDist, 0, dists.length);
		foldSplits = new int[numFolds][2][0];
		int[][] instances = new int[dists.length][0];
		for (int i = 0; i < instances.length; i++)
			instances[i] = new int[(int) dists[i]];
		for (int s = 0; s < attributeValues[classPos].length; s++) {
			int sCV = (int) attributeValues[classPos][s];
			instances[sCV][(int) (dists[sCV] - tmpDist[sCV])] = s;
			tmpDist[sCV] -= 1;
		}
		tmpDist = null;

		// Randomize and stratify instances
		for (int i = 0; i < instances.length; i++)
			instances[i] = genRandomOrder(instances[i]);
		int[] newInstOrder = new int[numInstances];
		int i = 0;
		do {
			for (int cc = 0; cc < instances.length; cc++) {
				if (instances[cc].length > 0) {
					newInstOrder[i] = instances[cc][0];
					instances[cc] = Arrays.remove(instances[cc], 0);
					i += 1;
				}
			}
		} while (i < newInstOrder.length);

		// Select the instances for each fold
		if (numFolds > 1) {
			int strtVal = 0;
			for (int f = 0; f < numFolds; f++) {
				int foldTestSize = (int) Math.round((numInstances - strtVal)
						/ (1.0 * (numFolds - f)));
				foldSplits[f][1] = new int[foldTestSize];
				foldSplits[f][0] = new int[numInstances - foldTestSize];
				if (strtVal == 0) {
					System.arraycopy(newInstOrder, 0, foldSplits[f][1], 0,
							foldTestSize);
					System.arraycopy(newInstOrder, foldTestSize,
							foldSplits[f][0], 0, foldSplits[f][0].length);
				} else if (strtVal + foldTestSize == numInstances) {
					System.arraycopy(newInstOrder, strtVal, foldSplits[f][1],
							0, foldTestSize);
					System.arraycopy(newInstOrder, 0, foldSplits[f][0], 0, strtVal);
				} else {
					System.arraycopy(newInstOrder, 0, foldSplits[f][0], 0, strtVal);
					System.arraycopy(newInstOrder, strtVal, foldSplits[f][1],
							0, foldTestSize);
					System.arraycopy(newInstOrder, strtVal + foldTestSize,
							foldSplits[f][0], strtVal, foldSplits[f][0].length - strtVal);
				}
				strtVal += foldTestSize;
			}
		} else {
			foldSplits[0][1] = new int[newInstOrder.length];
			foldSplits[0][0] = new int[newInstOrder.length];
			System.arraycopy(newInstOrder, 0, foldSplits[0][1], 0,
					newInstOrder.length);
			System.arraycopy(newInstOrder, 0, foldSplits[0][0], 0,
					newInstOrder.length);
		}
	}

	/**
	 * Performs Stratified Cross-Fold Generation
	 * 
	 * @param numofFold
	 *            The Number of Folds to be generated
	 */
	/*
	public void generateNewFolds_old(int numofFold) {
		Attribute classAtt = this.classAttribute();
		// Set up distribution
		double[] dists = new double[classAtt.numValues()];
		double[] tmpDist = new double[dists.length];
		System.arraycopy(classAtt.getDistribution(), 0, dists, 0, dists.length);
		System.arraycopy(dists, 0, tmpDist, 0, dists.length);
		foldSplits = new int[numofFold][2][0];
		int[][] insts = new int[dists.length][0];
		for (int s = 0; s < insts.length; s++)
			insts[s] = new int[(int) dists[s]];
		for (int s = 0; s < attributeValues[classPos].length; s++) {
			int sCV = (int) attributeValues[classPos][s];
			insts[sCV][(int) (dists[sCV] - tmpDist[sCV])] = s;
			tmpDist[sCV] -= 1;
		}
		tmpDist = null;
		// Finished storing instance IDs in the respective classes
		// Randomize instances
		for (int s = 0; s < insts.length; s++)
			insts[s] = genRandomOrder(insts[s]);
		// Select instances for folds
		for (int f = 0; f < numofFold; f++) {// For Every Fold
			for (int fc = 0; fc < dists.length; fc++) {// for every class in
				// the fold
				for (int i = 0; i < insts[fc].length; i++) {
					if (numofFold == 1 || i % numofFold == f)
						foldSplits[f][1] = ArrayUtil.append(foldSplits[f][1],
								insts[fc][i]);
					else
						foldSplits[f][0] = ArrayUtil.append(foldSplits[f][0],
								insts[fc][i]);
				}
			}
		}
	}
	*/

	/** 
	 * Random resampling validation 
	 */
	private int[] generateRandomRRV(int percent) {
		int totalNum = (int) (numInstances() * (percent / 100.0));
		Attribute classAtt = this.classAttribute();
		int[] dists = new int[classAtt.getDistribution().length - 1];
		int[] tmpDist = new int[dists.length];
		System.arraycopy(classAtt.getDistribution(), 0, dists, 0, dists.length);
		System.arraycopy(dists, 0, tmpDist, 0, dists.length);
		int[][] insts = new int[dists.length][0];
		for (int s = 0; s < insts.length; s++)
			insts[s] = new int[dists[s]];
		for (int s = 0; s < attributeValues[classPos].length; s++) {
			int sCV = (int) attributeValues[classPos][s];
			insts[sCV][dists[sCV] - tmpDist[sCV]] = s;
			tmpDist[sCV] -= 1;
		}
		int[][] totals = new int[dists.length][0];
		for (int i = 0; i < totals.length; i++) {
			try {
				totals[i] = new int[(int) (totalNum
						* classAtt.count(i) / (numInstances()))];
			} catch (ValueNotFoundException e) {
				totals[i] = new int[0];
			}
			Random r = new Random();
			for (int j = 0; j < totals[i].length; j++)
				totals[i][j] = insts[i][r.nextInt(insts[i].length)];
		}
		int[] instRRV = new int[0];
		for (int i = 0; i < totals.length; i++)
			instRRV = Arrays.concatenate(instRRV, totals[i]);
		return instRRV;
	}

	public DefaultDataModel getSubset(int[] insts) throws NullDatasetException {
		if (insts.length == 0)
			throw new NullDatasetException();
		DefaultDataModel tmpD = new DefaultDataModel(cloneAttributes(), fileName, sep);
		double[][] vals = new double[insts.length][attributeValues.length];
		for (int i = 0; i < insts.length; i++) {
			for (int j = 0; j < attributeValues.length; j++)
				vals[i][j] = attributeValues[j][insts[i]];
		}
		tmpD.setInstances(vals);
		// tmpD.recalculateAttributeInfo();
		tmpD.classPos = classPos;
		tmpD.idPos = idPos;
		tmpD.isDiscretized = isDiscretized;
		if (this.vhierarchies != null) {
			tmpD.vhierarchies = new VHierarchyNode[vhierarchies.length];
			for (int i = 0; i < vhierarchies.length; i++)
				tmpD.vhierarchies[i] = vhierarchies[i];
		} else {
			tmpD.vhierarchies = new VHierarchyNode[tmpD.numAttributes()];
			for (int i = 0; i < tmpD.vhierarchies.length; i++) {
				if (i == idPos) {
					tmpD.vhierarchies[i] = null;
					continue;
				} else if (!tmpD.posToAttribute.get(i).hasContinuousValues())
					tmpD.vhierarchies[i] = tmpD.posToAttribute.get(i)
							.genHierarchy();
			}
		}

		return tmpD;
	}

	public DefaultDataModel testCV(int folds, int f)
			throws NullDatasetException {
		if (foldSplits == null)
			generateNewFolds(folds);
		if (foldSplits.length != folds)
			generateNewFolds(folds);
		int[] insts = foldSplits[f][1];
		DefaultDataModel data = getSubset(insts);
		data.setFileName(
				fileName.substring(0, fileName.lastIndexOf(".")) + "_Fold"
				+ (f + 1) + "_Test." + (sep.equalsIgnoreCase(",") ? "csv" : "tsv"));
		return data;
	}

	public DefaultDataModel trainCV(int folds, int f)
			throws NullDatasetException {
		if (foldSplits == null)
			generateNewFolds(folds);
		if (foldSplits.length != folds)
			generateNewFolds(folds);
		int[] insts = foldSplits[f][0];
		DefaultDataModel data = getSubset(insts);
		data.vhierarchies = vhierarchies;
		data.setFileName(
				fileName.substring(0, fileName.lastIndexOf(".")) + "_Fold"
				+ (f + 1) + "_Train." + (sep.equalsIgnoreCase(",") ? "csv" : "tsv"));
		return data;
	}

	public void setFileName(String newName) {
		fileName = newName;
	}

	public void cover(int inst) throws InstanceNotFoundException {
		if (inst > instCovered.length)
			throw new InstanceNotFoundException(inst);
		if (inst < 0)
			throw new InstanceNotFoundException(inst);
		instCovered[inst] = true;
	}

	public void uncover(int inst) throws InstanceNotFoundException {
		if (inst > instCovered.length)
			throw new InstanceNotFoundException(inst);
		if (inst < 0)
			throw new InstanceNotFoundException(inst);
		instCovered[inst] = false;
	}

	public void cover(String inst) throws InstanceNotFoundException {
		Attribute smAtt = posToAttribute.get(idPos);
		double val = smAtt.getRepresentation(inst);
		if (val == -1)
			throw new InstanceNotFoundException(inst);
		else
			cover((int) val);
	}
	
	public void uncover(String inst) throws InstanceNotFoundException {
		Attribute smAtt = posToAttribute.get(idPos);
		double val = smAtt.getRepresentation(inst);
		if (val == -1)
			throw new InstanceNotFoundException(inst);
		else
			uncover((int) val);
	}

	/**
	 * Uncovers all instances
	 */
	public void uncoverAll() {
		for (int i = 0; i < instCovered.length; i++)
			instCovered[i] = false;
	}

	public boolean isCovered(int inst) throws InstanceNotFoundException {
		if (inst > instCovered.length)
			throw new InstanceNotFoundException(inst);
		if (inst < 0)
			throw new InstanceNotFoundException(inst);
		return instCovered[inst];
	}

	/**
	 * Checks whether the instance is covered, by first retrieving the index
	 * of the instance in the data set, then checking whether the instance with 
	 * that index is covered.
	 * 
	 * @param inst
	 *            The instance name
	 * @return If the instance has been covered
	 * @throws InstanceNotFoundException
	 */
	public boolean isCovered(String inst) throws InstanceNotFoundException {
		Attribute smAtt = posToAttribute.get(idPos);
		double val = smAtt.getRepresentation(inst);
		if (val == -1)
			throw new InstanceNotFoundException(inst);
		else
			return isCovered((int) val);
	}

	/**
	 * The name of the file from which this data set was loaded from.
	 * 
	 * @return The name of the file as a string
	 */
	public String getFileName() {
		return fileName;
	}

	public String getInstanceName(int instanceIx) throws InstanceNotFoundException,
			ValueNotFoundException {
		if (instanceIx < 0 || instanceIx > numInstances())
			throw new InstanceNotFoundException(instanceIx);
		return posToAttribute.get(idPos).getValue(
				attributeValues[idPos][instanceIx]);
	}

	public VHierarchyNode getHierarchy(int index)
			throws AttributeDoesNotExistException {
		if (index > vhierarchies.length - 1)
			throw new AttributeDoesNotExistException(index, posToAttribute.size());
		return vhierarchies[index];
	}

	public String instanceName(int i) throws InstanceNotFoundException,
			ValueNotFoundException {
		if (i < 0 || i > attributeValues[classPos].length - 1)
			throw new InstanceNotFoundException(i);
		if (idPos == -1)
			return "Instance " + (i + 1);
		return posToAttribute.get(idPos).getValue(
				attributeValues[idPos][i]);
	}

	public String instanceClass(int i) throws InstanceNotFoundException,
			ValueNotFoundException {
		if (i < 0 || i > attributeValues[classPos].length - 1)
			throw new InstanceNotFoundException(i);
		return posToAttribute.get(classPos).getValue(
				attributeValues[classPos][i]);
	}

	public int numClasses() {
		return posToAttribute.get(classPos).getDistribution().length - 1;
	}

	public void setAttributes(ArrayList<Attribute> atts) {
		posToAttribute = new HashMap<Integer, Attribute>(atts.size());
		nameToAttribute = new HashMap<String, Attribute>(atts.size());
		doubleAttrPos = new int[atts.size()];
		objectAttrPos = new int[atts.size()];
		vhierarchies = new VHierarchyNode[atts.size()];
		int numDA = 0;
		int numOA = 0;
		for (int i = 0; i < atts.size(); i++) {
			Attribute a = atts.get(i);
			a.setPosition(i);
			a.setReferenceDataModel(this);
			if (a.isClass()) {
				this.classPos = i;
				vhierarchies[i] = a.genHierarchy();
			} else if (a.isId()) {
				this.idPos = i;
				this.instCovered = new boolean[a.numValues()];
				for (int k = 0; k < this.instCovered.length; k++)
					instCovered[k] = false;
			}
			posToAttribute.put(new Integer(i), a);
			nameToAttribute.put(a.name(), a);
			if (a.hasContinuousValues()) {
				doubleAttrPos[numDA] = i;
				numDA++;
			} else {
				vhierarchies[i] = a.genHierarchy();
				objectAttrPos[numOA] = i;
				numOA++;
			}
		}
		int[] tDA = new int[numDA];
		System.arraycopy(this.doubleAttrPos, 0, tDA, 0, numDA);
		this.doubleAttrPos = tDA;
		tDA = null;
		int[] tOA = new int[numOA];
		System.arraycopy(this.objectAttrPos, 0, tOA, 0, numOA);
		this.objectAttrPos = tOA;
		tOA = null;
		this.isDiscretized = false;
	}

	public void setSeperator(String seperator) {
		sep = seperator;
	}

	public String getSeperator() {
		return sep;
	}

	public void setHierarchy(int ind, VHierarchyNode vh)
			throws AttributeDoesNotExistException {
		if (ind < 0 || ind > attributeValues.length - 1)
			throw new AttributeDoesNotExistException(ind,
					attributeValues.length - 1);
		vhierarchies[ind] = vh;
	}

	public void setHierarchies(VHierarchyNode[] attHierarchs) throws Exception {
		if (attHierarchs.length != attributeValues.length)
			throw new Exception(
					"Number of hierarchies differs from number of attributes.");
		vhierarchies = attHierarchs;
	}

	public double[] attributeValues(int att)
			throws AttributeDoesNotExistException {
		if (att < 0 || att > attributeValues.length - 1)
			throw new AttributeDoesNotExistException(att + 1,
					attributeValues.length);
		double[] attvs = new double[attributeValues[att].length];
		System.arraycopy(attributeValues[att], 0, attvs, 0, attvs.length);
		return attvs;
	}

	/**
	 * Uses the training dataset to change the test dataset values by using a
	 * lookup function (keeps the sets in their respective order).
	 * 
	 * @param attVals
	 *            The new double attribute values
	 * @param trainData
	 *            The training dataset
	 */
	public void setNewDoubleValues(double[][] attVals, DefaultDataModel trainData) {
		int[] trainDoubleAtts = trainData.doubleAttrPos;
		for (int i = 0; i < trainDoubleAtts.length; i++) {
			if (nameToAttribute.containsKey(trainData.posToAttribute.get(
					trainDoubleAtts[i]).name())) {
				Attribute tatt = nameToAttribute.get(trainData.posToAttribute.get(
						trainDoubleAtts[i]).name());
				attributeValues[tatt.position()] = attVals[i];
				tatt.recalculateInfo(attributeValues[tatt.position()]);
			}
		}
	}

	private void keepAttributes(ArrayList<Integer> attIxs) {
		double[][] newAttVs = new double[attIxs.size()][0];
		HashMap<Integer, Attribute> pTA = new HashMap<Integer, Attribute>(
				attIxs.size());
		HashMap<String, Attribute> sTA = new HashMap<String, Attribute>(
				attIxs.size());
		int[] datts = new int[0];
		int[] satts = new int[0];
		VHierarchyNode[] nnodes = new VHierarchyNode[attIxs.size()];
		for (int i = 0; i < attIxs.size(); i++) {
			newAttVs[i] = attributeValues[attIxs.get(i).intValue()];
			Attribute cA = posToAttribute.get(attIxs.get(i));
			cA.setPosition(i);
			pTA.put(i, cA);
			sTA.put(cA.name(), cA);
			if (cA.hasContinuousValues()) {
				cA.setDoublePosition(datts.length);
				datts = Arrays.append(datts, i);
			} else
				satts = Arrays.append(satts, i);
			if (cA.isClass())
				classPos = i;
			if (cA.isId())
				idPos = i;
			nnodes[i] = vhierarchies[attIxs.get(i)];
		}
		attributeValues = newAttVs;
		posToAttribute = pTA;
		nameToAttribute = sTA;
		doubleAttrPos = datts;
		objectAttrPos = satts;
		vhierarchies = nnodes;
	}

	public void keepAttributes(String[] names) {
		ArrayList<Integer> ixs = new ArrayList<Integer>(names.length);
		for (int i = 0; i < names.length; i++) {
			if (nameToAttribute.containsKey(names[i]))
				ixs.add(nameToAttribute.get(names[i]).position());
		}
		removeAttributes(ixs, true);
	}

	public void removeAttributes(ArrayList<Integer> attIxs,
			boolean reverse) {
			//Integer[] atts = attIxs.toArray(new Integer[0]);
			//int[] intIxs = new int[atts.length];
			int[] intIxs = new int[attIxs.size()];
			for (int i = 0; i < intIxs.length; i++)
				intIxs[i] = attIxs.get(i).intValue();
			removeAttributes(intIxs, reverse);
	}

	
	public void removeAttributes(int[] attrIndex, boolean reverse) {
		if (attrIndex.length > 0) {
			int[] indToRemove = MathUtil.quickSort(attrIndex);
			ArrayList<Integer> attsToKeep = new ArrayList<Integer>(
					numAttributes() - indToRemove.length);
			boolean isClassAttPres = false;
			boolean isIdAttPres = false;
			if (!reverse) {
				if (indToRemove[0] > 0) {
					// Keeps all attributes till the first index to remove
					for (int i = 0; i < indToRemove[0]; i++) {
						if (i == classPos)
							isClassAttPres = true;
						if (i == idPos)
							isIdAttPres = true;
						attsToKeep.add(i);
					}
				}
				for (int i = 0; i < indToRemove.length - 1; i++) {
					// From the first index to the second-to-last
					for (int j = indToRemove[i] + 1; j < indToRemove[i + 1]; j++) {
						if (j == classPos)
							isClassAttPres = true;
						if (j == idPos)
							isIdAttPres = true;
						attsToKeep.add(j);
					}
				}
				if (indToRemove[indToRemove.length - 1] < numAttributes() - 1) {
					// From the last index to the end of the attribute values
					for (int i = indToRemove[indToRemove.length - 1] + 1; i < numAttributes(); i++) {
						if (i == classPos)
							isClassAttPres = true;
						if (i == idPos)
							isIdAttPres = true;
						attsToKeep.add(i);
					}
				}
			} else { // "reverse" is true
				for (int i = 0; i < indToRemove.length; i++) {
					attsToKeep.add(indToRemove[i]);
					if (indToRemove[i] == classPos)
						isClassAttPres = true;
					if (indToRemove[i] == idPos)
						isIdAttPres = true;
				}
			}
			if (!isClassAttPres && classPos >= 0) {
				attsToKeep.add(1, classPos);
			}
			if (!isIdAttPres && idPos >= 0) {
				attsToKeep.add(0, idPos);
			}
			keepAttributes(attsToKeep);
		}
	}

	public void removeAttributes(String[] names) {
		ArrayList<Integer> nInds = new ArrayList<Integer>(names.length);
		for (int i = 0; i < names.length; i++) {
			if (nameToAttribute.containsKey(names[i]))
				nInds.add(nameToAttribute.get(names[i]).position());
		}
		removeAttributes(nInds, false);
	}

	/**
	 * Removes attributes which have no cut points (that is, they have only one 
	 * bin). This operation is irreversible!
	 */
	public void removeTrivialAttributes() {
		ArrayList<Integer> attsKept = new ArrayList<Integer>();
		for (int i = 0; i < attributeValues.length; i++) {
			Attribute att = posToAttribute.get(i);
			if (att.wasContinuous() && att.hasContinuousValues()) {
				if (att.cutPoints() != null
						&& att.cutPoints().length > 0)
					attsKept.add(i);
			} else
				attsKept.add(i);
		}
		if (attsKept.size() > 0)
			keepAttributes(attsKept);
		else {
			System.out.println("Removed all attributes. Exiting.");
			System.exit(1);
		}
	}

	private StringBuffer getHeader() {
		StringBuffer outHead = new StringBuffer();
		outHead.append(posToAttribute.get(0).name());
		for (int i = 1; i < posToAttribute.size(); i++) {
			outHead.append(sep);
			outHead.append(posToAttribute.get(i).name());
		}
		outHead.append('\n');
		return outHead;
	}

	public StringBuffer print(boolean useDisc) {
		StringBuffer buf = new StringBuffer(100000);
		buf.append(getHeader());
		for (int i = 0; i < numInstances(); i++) {
			Attribute att = posToAttribute.get(0);
			if (!useDisc && att.hasContinuousValues())
				buf.append(attributeValues[0][i]);
			else {
				try {
					buf.append(att.getValue(attributeValues[0][i]));
				} catch (ValueNotFoundException e) {
					System.err.println(e.getLocalizedMessage());
					buf.append("?");
				}
			}
			for (int a = 1; a < attributeValues.length; a++) {
				buf.append(sep);
				att = posToAttribute.get(a);
				if (!useDisc && att.hasContinuousValues())
					buf.append(attributeValues[a][i]);
				else {
					try {
						buf.append(att.getValue(attributeValues[a][i]));
					} catch (ValueNotFoundException e) {
						System.err.println(e.getLocalizedMessage());
						buf.append("?");
					}
				}
			}
			if (i < numInstances() - 1)
				buf.append('\n');
		}
		buf.trimToSize();
		return buf;
	}

	public DefaultDataModel trainSplit(double percent) {
		if (foldSplits == null)
			generateNewFolds((int) (100 / percent));
		if (foldSplits.length != (int) (100 / percent))
			generateNewFolds((int) (100 / percent));
		int[] insts = foldSplits[0][0];
		DefaultDataModel tmpD;
		try {
			tmpD = getSubset(insts);
		} catch (NullDatasetException e) {
			System.err.println(e.getLocalizedMessage());
			e.printStackTrace();
			return null;
		}
		tmpD.setFileName(
				fileName.substring(0, fileName.lastIndexOf("."))
				+ "_percent" + percent + "_train." 
				+ (sep.equalsIgnoreCase(",") ? "csv" : "tsv"));
		return tmpD;
	}

	public DefaultDataModel testSplit(double percent) {
		if (foldSplits == null)
			generateNewFolds((int) (100 / percent));
		if (foldSplits.length != (int) (100 / percent))
			generateNewFolds((int) (100 / percent));
		int[] insts = foldSplits[0][1];
		DefaultDataModel tmpD;
		try {
			tmpD = getSubset(insts);
		} catch (NullDatasetException e) {
			System.err.println(e.getLocalizedMessage());
			e.printStackTrace();
			return null;
		}
		tmpD.setFileName(
				fileName.substring(0, fileName.lastIndexOf("."))
				+ "_Percent" + percent + "_test."
				+ (sep.equalsIgnoreCase(",") ? "csv" : "tsv"));
		return tmpD;
	}

	public DefaultDataModel rrvTrainSplit(int percent) {
		int[] insts = generateRandomRRV(percent);
		DefaultDataModel tmpD;
		try {
			tmpD = getSubset(insts);
		} catch (NullDatasetException e) {
			System.err.println(e.getLocalizedMessage());
			e.printStackTrace();
			return null;
		}
		tmpD.setFileName(
				fileName.substring(0, fileName.lastIndexOf(".")) + "_RRV_"
				+ percent + "_train." + (sep.equalsIgnoreCase(",") ? "csv" : "tsv"));
		return tmpD;
	}

	public DefaultDataModel rrvTestSplit(int percent) {
		int[] insts = generateRandomRRV(percent);
		DefaultDataModel tmpD;
		try {
			tmpD = getSubset(insts);
		} catch (NullDatasetException e) {
			System.err.println(e.getLocalizedMessage());
			e.printStackTrace();
			return null;
		}
		tmpD.setFileName(
				fileName.substring(0, fileName.lastIndexOf(".")) + "_RRV"
				+ percent + "_test." + (sep.equalsIgnoreCase(",") ? "csv" : "tsv"));
		return tmpD;
	}

	public Attribute idAttribute() throws AttributeDoesNotExistException {
		if (idPos == -1)
			throw new AttributeDoesNotExistException("ID attribute");
		return posToAttribute.get(idPos);
	}

	/**
	 * @param instIx
	 *            The insrtance index
	 * @param attIx
	 *            The attribute index
	 * @return The String representation of the original attribute value
	 *         inputted into the dataset (post pre-processing)
	 * @throws AttributeDoesNotExistException
	 * @throws ValueNotFoundException
	 * @throws InstanceNotFoundException
	 */
	public Object originalAttributeValue(int instIx, int attIx)
			throws AttributeDoesNotExistException, ValueNotFoundException,
			InstanceNotFoundException {
		if (attIx < 0 || attIx > this.posToAttribute.size() - 1)
			throw new AttributeDoesNotExistException(attIx, posToAttribute
					.size() - 1);
		if (instIx < 0 || instIx > this.numInstances() - 1)
			throw new InstanceNotFoundException(instIx, numInstances() - 1);
		Attribute att = posToAttribute.get(attIx);
		if (att.hasContinuousValues())
			return Double.toString(attributeValues[attIx][instIx]);
		else
			return att.getValue(attributeValues[attIx][instIx]);
	}

	public void setAttributeValues(double[][] nAttVals) {
		instCovered = new boolean[nAttVals[0].length];
		for (int i = 0; i < instCovered.length; i++)
			instCovered[i] = false;
		attributeValues = nAttVals;
		recalculateAttributeInfo();
	}

	public String instanceClass(String d) throws AttributeDoesNotExistException,
			ValueNotFoundException {
		if (!idAttribute().hasValue(d))
			return null;
		return classAttribute().getStringValue(
				attributeValues[classPos][(int) idAttribute()
						.getRepresentation(d)]);
	}

	public double attributeValue(String d1, int attribute)
			throws InstanceNotFoundException, AttributeDoesNotExistException {
		Attribute smpAtt = posToAttribute.get(idPos);
		if (attribute > posToAttribute.size() - 1 || attribute < 0)
			throw new AttributeDoesNotExistException(attribute, posToAttribute
					.size() - 1);
		if (smpAtt.hasValue(d1))
			return attributeValues[attribute][(int) smpAtt
					.getRepresentation(d1)];
		else
			throw new InstanceNotFoundException(d1);
	}

	public Object attributeValueString(int instIx, String attName)
			throws AttributeDoesNotExistException, InstanceNotFoundException,
			ValueNotFoundException {
		if (!nameToAttribute.containsKey(attName))
			throw new AttributeDoesNotExistException(attName);
		return attributeValueString(instIx, nameToAttribute.get(
				attName).position());
	}

	public AttributeList rulegenAttributeList() throws Exception {
		AttributeList atl = new AttributeList();
		atl.defineAttributes(this);
		return atl;
	}

	public VHierarchyNode getVHierarchyNode(String attName, int instIx)
			throws AttributeDoesNotExistException, InstanceNotFoundException,
			ValueNotFoundException {
		if (!nameToAttribute.containsKey(attName))
			throw new AttributeDoesNotExistException(attName);
		Attribute a = nameToAttribute.get(attName);
		double av = attributeValue(instIx, a.position());
		if (a.hasContinuousValues())
			return vhierarchies[a.position()].getValue(new Double(av));
		else
			return vhierarchies[a.position()].getValue(a.getStringValue(av));
	}

	public Object getMatchingValue(String attName, int instIx)
			throws AttributeDoesNotExistException, InstanceNotFoundException,
			ValueNotFoundException {
		if (!nameToAttribute.containsKey(attName))
			throw new AttributeDoesNotExistException(attName);
		Attribute a = nameToAttribute.get(attName);
		double av = attributeValue(instIx, a.position());
		if (a.hasContinuousValues())
			return new Double(av);
		else
			return a.getStringValue(av);
	}

	public void equalizeFromTestDataModel(DefaultDataModel tstD) {
		HashMap<Integer, Attribute> tstAtt = tstD.posToAttribute;
		for (int i = 0; i < tstAtt.size(); i++) {
			Attribute a = tstAtt.get(i);
			if (nameToAttribute.containsKey(a.name())) {
				Attribute trnA = nameToAttribute.get(a.name());
				trnA.addValues(a);
			}
		}
	}

	public void equalizeFromTrainDataModel(DefaultDataModel train) {
		HashMap<Integer, Attribute> tstAtt = train.posToAttribute;
		for (int i = 0; i < tstAtt.size(); i++) {
			Attribute a = tstAtt.get(i);
			if (nameToAttribute.containsKey(a.name())) {
				Attribute tstA = nameToAttribute.get(a.name());
				tstA.addValues(a);
			}
		}
		for (int i = 0; i < numAttributes(); i++) {
			Attribute currA = posToAttribute.get(i);
			if (train.nameToAttribute.containsKey(currA.name()))
				vhierarchies[i] = train.vhierarchies[train.nameToAttribute.get(
						currA.name()).position()];
		}

	}

	public String[] getInstanceNames() {
		if (idPos < 0) {
			String[] nInsts = new String[attributeValues[0].length];
			for (int i = 0; i < nInsts.length; i++)
				nInsts[i] = "S" + (i + 1);
			return nInsts;
		} else {
			String[] nInsts = new String[attributeValues[idPos].length];
			Attribute a = posToAttribute.get(idPos);
			for (int i = 0; i < nInsts.length; i++) {
				try {
					nInsts[i] = a.getStringValue(attributeValues[idPos][i]);
				} catch (ValueNotFoundException e) {
					e.printStackTrace();
					System.exit(10);
				}
			}
			return nInsts;
		}
	}

	/**
	 * Assumes that all the instances have been read into the dataset.
	 * This method is not meant to be called on a fold!
	 * 
	 * @param instanceNames
	 *            All the instances to return
	 * @return a new dataset with all the instances specified
	 */
	public DefaultDataModel getSubset(String[] instanceNames, boolean reverse) {
		Attribute idAttIx = posToAttribute.get(idPos);
		ArrayList<Integer> instsArrayList = new ArrayList<Integer>();
		for (int i = 0; i < instanceNames.length; i++) {
			if (idAttIx.hasValue(instanceNames[i]))
				instsArrayList.add((int) idAttIx.getRepresentation(instanceNames[i]));
		}
		Integer[] inds = instsArrayList.toArray(new Integer[0]);
		int[] instsArr = Arrays.toIntArray(inds);
		if (reverse) {
			instsArr = MathUtil.quickSort(instsArr);
			int[] na = new int[idAttIx.numValues() - instsArr.length];
			int nai = 0;
			int sai = 0;
			for (int i = 0; i < idAttIx.numValues() && nai < na.length; i++) {
				if (instsArr[sai] == i)
					sai++;
				else {
					na[nai] = i;
					nai++;
				}
			}
			instsArr = na;
		}

		try {
			return getSubset(instsArr);
		} catch (NullDatasetException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return null;
	}

	public FastVector getWekaAttributes(boolean uD) {
		FastVector wekaAtts = new FastVector();
		for (int i = 0; i < posToAttribute.size(); i++) {
			Attribute att = posToAttribute.get(i);
			// System.out.println(att.name());
			if (att.isClass() || att.isId())
				continue;
			if (att.hasContinuousValues() && !uD)
				wekaAtts.addElement(new weka.core.Attribute(att.name()));
			else
				wekaAtts.addElement(att.getWekaAttribute());
		}
		wekaAtts.addElement(classAttribute().getWekaAttribute());
		return wekaAtts;
	}

	public FastVector getWekaAttributes(boolean uD, int[] vals) {
		FastVector wekaAtts = new FastVector();
		for (int i = 0; i < vals.length; i++) {
			Attribute att = posToAttribute.get(vals[i]);
			if (att.hasContinuousValues() && !uD)
				wekaAtts.addElement(new weka.core.Attribute(att.name()));
			else
				wekaAtts.addElement(att.getWekaAttribute());
		}
		return wekaAtts;
	}

	public Instance wekaInstance(int ts, FastVector wekaAtts, boolean uD) {
		Instance newInst = new Instance(wekaAtts.size());
		int currI = 0;
		for (int i = 0; i < posToAttribute.size() && currI < wekaAtts.size(); i++) {
			Attribute attr = posToAttribute.get(i);
			weka.core.Attribute att = (weka.core.Attribute) wekaAtts
					.elementAt(currI);
			if (attr.isClass() || attr.isId())
				continue;
			if (attr.hasContinuousValues()) {
				if (uD) {
					try {
						newInst.setValue(att, att.indexOfValue(attr
								.getValue(attributeValues[i][ts])));
					} catch (ValueNotFoundException e) {
						e.printStackTrace();
						System.exit(10);
					}
				} else
					newInst.setValue(att, attributeValues[i][ts]);
			} else {
				try {
					newInst.setValue(att, att.indexOfValue(attr
							.getStringValue(attributeValues[i][ts])));
				} catch (ValueNotFoundException e) {
					e.printStackTrace();
					System.exit(16);
				}
			}
			currI++;
		}
		weka.core.Attribute natt = (weka.core.Attribute) wekaAtts.lastElement();
		org.probe.data.dataset.Attribute classAtt = classAttribute();
		try {
			newInst.setValue(natt, classAtt
					.getStringValue(attributeValues[classPos][ts]));
		} catch (ValueNotFoundException e) {
			e.printStackTrace();
			System.exit(17);
		}
		return newInst;
	}

	public Instance wekaInstanceTrainingOrder(DefaultDataModel train, int ts,
			FastVector wekaAtts, boolean uD) {
		Instance newInst = new Instance(wekaAtts.size());
		for (int i = 0; i < wekaAtts.size(); i++) {
			weka.core.Attribute attr = (weka.core.Attribute) wekaAtts.elementAt(i);
			if (nameToAttribute.containsKey(attr.name())) {
				org.probe.data.dataset.Attribute att = nameToAttribute.get(attr.name());
				if (att.hasContinuousValues()) {
					if (uD) {
						try {
							newInst.setValue(attr, 
									att.getRepresentation(
											att.getValue(attributeValues[att.position()][ts])));
						} catch (ValueNotFoundException e) {
							e.printStackTrace();
						}
					} else {
						try {
							newInst.setValue(attr, 
									att.getStringValue(attributeValues[att.position()][ts]));
						} catch (ValueNotFoundException e) {
							e.printStackTrace();
							System.exit(18);
						}
					}
				} else {
					try {
						newInst.setValue(attr,
								att.getStringValue(attributeValues[att.position()][ts]));
					} catch (ValueNotFoundException e) {
						e.printStackTrace();
						System.exit(18);
					}
				}
			}
		}
		return newInst;
	}

	/**
	 * Returns the set of double values that represent the transformed
	 * (discretized) values if they exist. For string values it returns the
	 * integer representation
	 * 
	 * @return a matrix of attribute as rows and instances as columns
	 */
	public double[][] attributeValuesWithRepValues() {
		double[][] nAtts = new double[attributeValues.length][0];
		for (int i = 0; i < attributeValues.length; i++) {
			double[] nds = new double[attributeValues[i].length];
			System.arraycopy(attributeValues[i], 0, nds, 0, nds.length);
			nAtts[i] = nds;
			Attribute att = posToAttribute.get(i);
			if (att.hasContinuousValues()) {
				for (int j = 0; j < attributeValues[i].length; j++) {
					try {
						nAtts[i][j] = att.getRepresentation(att
								.getValue(nAtts[i][j]));
					} catch (ValueNotFoundException e) {
						e.printStackTrace();
						System.exit(1);
					}
				}
			}
		}
		return nAtts;
	}

	public void removeDiscretization() {
		for (int i = 0; i < doubleAttrPos.length; i++) {
			Attribute att = posToAttribute.get(doubleAttrPos[i]);
			att.removeDiscretization();
		}
		isDiscretized = false;	// PG2009
	}

	public int instanceClassAsInt(int inst) {
		return (int) attributeValues[classPos][inst];
	}

	public void selectAttributesUsingDiscretization(double[][] thresh) {
		boolean[] vrs = new boolean[numAttributes()];
		int[] dblAtts = getContinuousAttributeIndexes();
		int dblInd = 0;
		for (int i = 0; i < vrs.length; i++) {
			if (dblInd < thresh.length && i == dblAtts[dblInd]) {
				vrs[i] = (thresh[dblInd] != null && thresh[dblInd].length > 0);
				dblInd += 1;
			} else
				vrs[i] = true;
		}
		selectAttributes(vrs);
	}

	public void selectAttributesUsingDiscretization(double[][] thresh, DefaultDataModel ref) {
		boolean[] vrs = new boolean[numAttributes()];
		for (int i = 0; i < vrs.length; i++) {
			try {
				Attribute currAtt = attribute(i);
				if (currAtt.hasContinuousValues()) {
					try {
						Attribute refAtt = ref.attribute(currAtt.name());
						if (thresh[refAtt.doublePosition()] != null
								&& thresh[refAtt.doublePosition()].length > 0)
							vrs[i] = true;
						else
							vrs[i] = false;
					} catch (Exception e) {
						vrs[i] = false;
					}
				} else
					vrs[i] = true;
			} catch (Exception e) {
				vrs[i] = false;
			}
		}
		selectAttributes(vrs);
	}

	public void selectAttributes(boolean[] vars) {
		ArrayList<Integer> attsKeep = new ArrayList<Integer>(vars.length);
		for (int i = 0; i < vars.length; i++) {
			if (vars[i])
				attsKeep.add(new Integer(i));
		}
		keepAttributes(attsKeep);
	}

	public void selectAttributes(boolean[] vars, DefaultDataModel ref) {
		ArrayList<Integer> atts = new ArrayList<Integer>(this.numAttributes());
		for (int i = 0; i < vars.length; i++) {
			try {
				if (nameToAttribute.containsKey(ref.attribute(i).name())) {
					Attribute att = nameToAttribute.get((ref.attribute(i)
							.name()));
					if (vars[i]) {
						atts.add(att.position());
					}
				}
				atts = orderAttributes(atts);
				keepAttributes(atts);
			} catch (AttributeDoesNotExistException e) {
				System.err.println(e.getLocalizedMessage());
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	private ArrayList<Integer> orderAttributes(ArrayList<Integer> ol) {
		ArrayList<Integer> nl = new ArrayList<Integer>(ol.size());
		int[] lst = new int[ol.size()];
		for (int i = 0; i < ol.size(); i++)
			lst[i] = ol.get(i).intValue();
		lst = MathUtil.quickSort(lst);
		for (int j = 0; j < lst.length; j++)
			nl.add(new Integer(lst[j]));
		return nl;
	}

	public ArrayList<Attribute> getAttributes() {
		return cloneAttributes();
	}

	public boolean equals(Object o) {
		if (! (o instanceof DefaultDataModel))
			return false;
		DefaultDataModel d = (DefaultDataModel) o;
		return //fileName.equals(d.fileName) 
			classAttribute().name().equals(d.classAttribute())
			//&& getAttributes().equals(d.getAttributes());
			&& numAttributes() == d.numAttributes()
			&& numContinuousAttributes() == d.numContinuousAttributes()
			&& numClasses() == d.numClasses()
			&& numInstances() == d.numInstances()
			//&& attributeValues [][]
			//&& vhierarchies
			//&& getAttributes()
			//&& getClassValues()
			//&& idAttribute().toString().equals(d.idAttribute())
			;
			
	}
	
	public static void main(String[] args) throws Exception {
		TabCsvDataLoader ltcsv = new TabCsvDataLoader(args[0]);
		DefaultDataModel d = ltcsv.loadData();		// Throws Exception
		for (int i = 0; i < 10; i++) {
			System.out.println("Fold: " + i);
			try {
				d.trainCV(10, i);
				d.testCV(10, i);
			} catch (NullDatasetException e) {
				e.printStackTrace();
				try {
					d.trainCV(10, i);
					d.testCV(10, i);
				} catch (Exception e2) {
					// TODO: handle exception
				}
			}
		}
	}
}