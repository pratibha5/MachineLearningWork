/*
 * SensitivityAnalaysis.java
 *
 * Created on June 10, 2006, 6:46 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package util;

//import corefiles.structures.results.InferenceResults.InferenceResult;
//import preprocess.Remove;
import parameters.LearnerParameters;
import data.dataset.*;
//import corefiles.structures.results.CrossValInferenceResults.CrossValInferenceResult;
//import java.io.*;
//import java.lang.*;
//import java.util.*;

/**
 * Performs analysis on attributes noting the effect on accuracy each attribute
 * has Uses the trimmed dataset
 * 
 * @author Jonathan Lustgarten
 */
public class SensitivityAnalaysis {
	private Dataset trimTrainData;
	private Dataset trimTestData;
	private LearnerParameters lp;
	private String[] attsUsed;

	/** Creates a new instance of SensitivityAnalaysis */
	public SensitivityAnalaysis(Dataset trainD, Dataset testD,
			LearnerParameters learnp, String[] attsU) {
		trimTrainData = trainD;
		trimTestData = testD;
		lp = learnp;
		attsUsed = attsU;
	}
}