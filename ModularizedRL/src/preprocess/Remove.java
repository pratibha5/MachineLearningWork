/*
 * Remove.java
 *
 * Created on March 29, 2005, 6:05 PM
 */

package preprocess;

import java.util.ArrayList;
import java.util.Vector;

import data.dataset.*;
//import corefiles.structures.data.dataset.attribute.*;
import util.*;

/**
 * @author Jonathan
 */

public class Remove {
	public static final int NOCUTPT = 1;

	private Dataset trainData;
	private Dataset testData;
	private Dataset validnData;

	public Remove(Dataset trainData) {
		this.trainData = trainData;
	}

	public Remove(Dataset trainData, Dataset testData) {
		this.trainData = trainData;
		this.testData = testData;
	}

	public Remove(Dataset trainData, Dataset testData, Dataset validnData) {
		this.trainData = trainData;
		this.testData = testData;
		this.validnData = validnData;
	}

	public void removeAttributes(Vector AttrNames)
			throws AttributeDoesNotExistException {
		int[] attrIs = new int[AttrNames.size()];
		for (int i = 0; i < attrIs.length; i++)
			attrIs[i] = trainData.attribute((String) AttrNames.elementAt(i)).position();
		removeAttributes(attrIs, false);
	}

	public void removeAttribute(String AttrName) {
		try {
			trainData.removeAttribute(AttrName);
		} catch (AttributeDoesNotExistException e) {
			System.err.println(e.getLocalizedMessage());
		}

		try {
			if (testData != null)
				testData.removeAttribute(AttrName);
		} catch (AttributeDoesNotExistException e) {
			System.err.println(e.getLocalizedMessage());
		}
	}

	private ArrayList<Integer> getAttsFromTrainToSet(Dataset data, int[] trainAttIds) {
		ArrayList<Integer> testAttIds = new ArrayList<Integer>(100);
		for (int i = 0; i < trainAttIds.length; i++) {
			try {
				testAttIds.add(data.attribute(trainData.attribute(trainAttIds[i]).name()).position());
			} catch (AttributeDoesNotExistException e) {
				System.err.println(e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
		return testAttIds;
	}

	public void removeAttributes(int[] attIxs, boolean keepIndex) {
		if (attIxs.length > 0) {
			System.out.println("Removing attributes that have no cut points:");
			System.out.println("Removing from training set");
			if (!keepIndex) {
				if (testData != null) {
					System.out.println("Removing from test set");
					testData.removeAttributes(getAttsFromTrainToSet(testData, attIxs), false);
				}
			} else {
				if (testData != null)
					testData.removeAttributes(getAttsFromTrainToSet(testData, attIxs), true);
			}
			trainData.removeAttributes(attIxs, true);
		} else
			System.out.println("No attributes to remove");
	}

	public void removeAttributes(String[] names) {
		if (testData != null) {
			System.out.print("Removing from test set...");
			testData.removeAttributes(names);
			System.out.println("  Done");
		}
		trainData.removeAttributes(names);
	}

	public void removeAttributes() {
		Vector<String> NullCutPts = new Vector(trainData.numAttributes());
		//int size = 0;
		//double[][] newCtPts;
		trainData.removeTrivialAttributes();
		if (testData != null)
			testData.removeTrivialAttributes();
	}

	public Dataset getTrainSet() {
		return trainData;
	}

	public Dataset getTestSet() {
		return testData;
	}

	public Dataset getValidnSet() {
		return validnData;
	}
}