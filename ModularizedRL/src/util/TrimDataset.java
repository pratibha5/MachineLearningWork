/*
 * TrimDataset.java
 *
 * Created on April 25, 2006, 4:10 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package util;

//import corefiles.structures.learner.rule.RuleList;
//import corefiles.structures.results.*;
//import corefiles.structures.results.CrossValInferenceResults.CrossValInferenceResult;
//import corefiles.structures.results.InferenceResults.InferenceResult;
import preprocess.Remove;
//import java.io.*;
//import java.lang.*;
//import java.util.*;
import data.dataset.*;

/**
 * This class filters the dataset according to the attribute
 * 
 * @author Jonathan
 */
public class TrimDataset {
	private String[] attsUsed;
	private Dataset currTrainData, currTestData;

	/**
	 * Creates a new instance of TrimDataset
	 */
	public TrimDataset(String[] aU, Dataset origDataTrain, Dataset origDataTest) {
		currTrainData = origDataTrain;
		currTestData = origDataTest;
		attsUsed = aU;
	}

	private int[] orderIndex(int[] nI, int[] atts) {
		int[] nA = new int[atts.length];
		for (int i = 0; i < nI.length; i++)
			nA[i] = atts[nI[i]];
		return nA;
	}

	public void getInterestingAttDataset() {
		int[] nAU = new int[2];
		for (int i = 0; i < attsUsed.length; i++) {
			int attPos = -1;
			try {
				attPos = currTrainData.attribute(attsUsed[i]).position();
			} catch (AttributeDoesNotExistException e) {
				System.err.println(e.getLocalizedMessage());
				e.printStackTrace();
			}
			if (attPos > -1)
				nAU = Arrays.append(nAU, attPos);
		}
		nAU[0] = currTrainData.getIdAttIndex();
		nAU[1] = currTrainData.classAttIndex();
		nAU = orderIndex(MathUtil.getSortIndex(Arrays.toDoubleArray(nAU)), nAU);
		Remove r = new Remove(currTrainData, currTestData);
		r.removeAttributes(nAU, true);
		currTrainData = r.getTrainSet();
		currTestData = r.getTestSet();
	}

	public Dataset getTrimTrain() {
		return currTrainData;
	}

	public Dataset getTrimTest() {
		return currTestData;
	}
}