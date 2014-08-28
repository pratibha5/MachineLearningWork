package org.probe.data;

import java.util.ArrayList;
import java.util.HashMap;

import org.probe.stats.structures.learner.attribute.AttributeList;
import org.probe.stats.structures.learner.attribute.VHierarchyNode;
import org.probe.data.dataset.*;
import weka.core.FastVector;
import weka.core.Instance;

public interface DataModel {
	
	public ArrayList<Attribute> getAttributes();
	public void setRandSeed(long rs);
	public void setInstances(double[][] insts);
	public org.probe.data.dataset.Instance getInstance(int inst) throws ValueNotFoundException;
	public Attribute attribute(int a) throws AttributeDoesNotExistException;
	public Attribute attribute(String name) throws AttributeDoesNotExistException;
	public void normAtt(String attName, double sub, double div) throws AttributeDoesNotExistException;
	public Attribute classAttribute();
	public int classAttIndex();
	public void setClassAttIndex(int pos);
	public int getIdAttIndex();
	public void setIdAttIndex(int pos);
	public int numAttributes();
	public void removeAttribute(String attrName) throws AttributeDoesNotExistException;
	public double[][] attributeValues();
	public double[][] getDoubleAttributeValues();
	public void recalculateAttributeInfo();
	public int numContinuousAttributes();
	public int[] getContinuousAttributeIndexes();
	public double attributeValue(int instIx, int attIx) throws AttributeDoesNotExistException, InstanceNotFoundException;
	public String attributeValueString(int instIx, int attIx) throws AttributeDoesNotExistException, InstanceNotFoundException,
	ValueNotFoundException ;
	public void setNewDoubleValues(double[][] nDVs);
	public double[] getClassValues() throws AttributeDoesNotExistException;
	public void setDiscretization(double[][] cutPoints);
	public void setDiscretization(double[][] pts, DefaultDataModel ref) throws AttributeDoesNotExistException;
	public boolean isDiscretized();
	public int numInstances();
	public void generateNewFolds(int numFolds);
	public DefaultDataModel getSubset(int[] insts) throws NullDatasetException;
	public DefaultDataModel testCV(int folds, int f) throws NullDatasetException;
	public DefaultDataModel trainCV(int folds, int f) throws NullDatasetException;
	public void setFileName(String newName);
	public void cover(int inst) throws InstanceNotFoundException;
	public void uncover(int inst) throws InstanceNotFoundException;
	public void cover(String inst) throws InstanceNotFoundException;
	public void uncover(String inst) throws InstanceNotFoundException;
	public void uncoverAll();
	public boolean isCovered(int inst) throws InstanceNotFoundException;
	public boolean isCovered(String inst) throws InstanceNotFoundException;
	public String getFileName();
	public String getInstanceName(int instanceIx) throws InstanceNotFoundException,
	ValueNotFoundException;
	public VHierarchyNode getHierarchy(int index) throws AttributeDoesNotExistException;
	public String instanceName(int i) throws InstanceNotFoundException,
	ValueNotFoundException;
	public String instanceClass(int i) throws InstanceNotFoundException,
	ValueNotFoundException;
	public int numClasses();
	public void setAttributes(ArrayList<Attribute> atts);
	public void setSeperator(String seperator);
	public String getSeperator();
	public void setHierarchy(int ind, VHierarchyNode vh)
			throws AttributeDoesNotExistException;
	public void setHierarchies(VHierarchyNode[] attHierarchs) throws Exception;
	public double[] attributeValues(int att) throws AttributeDoesNotExistException;
	public void setNewDoubleValues(double[][] attVals, DefaultDataModel trainData);
	public void keepAttributes(String[] names); //There is a private version of this
	public void removeAttributes(ArrayList<Integer> attIxs, boolean reverse);
	public void removeAttributes(int[] attrIndex, boolean reverse);
	public void removeTrivialAttributes();
	public StringBuffer print(boolean useDisc);
	public DefaultDataModel trainSplit(double percent);
	public DefaultDataModel testSplit(double percent);
	public DefaultDataModel rrvTrainSplit(int percent);
	public DefaultDataModel rrvTestSplit(int percent);
	public Attribute idAttribute() throws AttributeDoesNotExistException;
	public Object originalAttributeValue(int instIx, int attIx)
			throws AttributeDoesNotExistException, ValueNotFoundException,
			InstanceNotFoundException;
	public void setAttributeValues(double[][] nAttVals);
	public String instanceClass(String d) throws AttributeDoesNotExistException,
	ValueNotFoundException;
	public double attributeValue(String d1, int attribute)
			throws InstanceNotFoundException, AttributeDoesNotExistException;
	public Object attributeValueString(int instIx, String attName)
			throws AttributeDoesNotExistException, InstanceNotFoundException,
			ValueNotFoundException;
	public AttributeList rulegenAttributeList() throws Exception;
	public VHierarchyNode getVHierarchyNode(String attName, int instIx)
			throws AttributeDoesNotExistException, InstanceNotFoundException,
			ValueNotFoundException;
	public Object getMatchingValue(String attName, int instIx)
			throws AttributeDoesNotExistException, InstanceNotFoundException,
			ValueNotFoundException;
	public void equalizeFromTestDataModel(DefaultDataModel tstD);
	public void equalizeFromTrainDataModel(DefaultDataModel train);
	public String[] getInstanceNames();
	public DefaultDataModel getSubset(String[] instanceNames, boolean reverse);
	public FastVector getWekaAttributes(boolean uD);
	public FastVector getWekaAttributes(boolean uD, int[] vals);
	public Instance wekaInstance(int ts, FastVector wekaAtts, boolean uD);
	public Instance wekaInstanceTrainingOrder(DefaultDataModel train, int ts,
			FastVector wekaAtts, boolean uD);
	public double[][] attributeValuesWithRepValues();
	public void removeDiscretization();
	public int instanceClassAsInt(int inst);
	public void selectAttributesUsingDiscretization(double[][] thresh);
	public void selectAttributesUsingDiscretization(double[][] thresh, DefaultDataModel ref);
	public void selectAttributes(boolean[] vars);
	public void selectAttributes(boolean[] vars, DefaultDataModel ref);
	
}
