/*
 * BRL.java
 *
 * Created on March 14, 2006, 8:44 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.probe.algo;

import data.ProcessParams;
import parameters.DataParameters;
import parameters.LearnerParameters;
import parameters.PreProcessParameters;
import preprocess.Preprocess;
import data.dataset.DataModel;

/**
 * The Main controller for the whole program
 * 
 * @author Jonathan Lustgarten
 * @since 1.0
 * @version 4.0
 */
public class InitializeParameters {
	private final ProcessParams pp;
	private Preprocess preProc;
	public DataModel trainData;
	private DataModel testData;
	private DataModel sourceData;
	public final DataParameters dataParams;
	private final PreProcessParameters preProcParams;
	public final LearnerParameters learnerParams;
	
	public Preprocess getPreProc() {
		return preProc;
	}
	public void setTrainSet() {
		preProc = new Preprocess(dataParams, preProcParams, learnerParams);
	}
	public Preprocess getPp() {
		return preProc;
	}
	public void setPp(String[] args) {
		try {
		//	pp = new ProcessParams(args);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * Creates a new instance of BRL
	 * @throws Exception
	 */
	public InitializeParameters(String[] args) throws Exception {
		pp = new ProcessParams(args);
		dataParams = pp.getDataParams();
		preProcParams = pp.getPreProcParams();
		learnerParams = pp.getLearnParams();
		preProc = new Preprocess(dataParams, preProcParams, learnerParams);
	}

	@SuppressWarnings("static-access")
	public Boolean run() throws Exception {
		
		Boolean retVal = false;
		preProc.run();

		if (!(dataParams.shouldCombineAtts() && !dataParams.shouldCombineSamples()
				&& !dataParams.shouldTransposeOnly() && !dataParams.shouldConvertTabCSV() ) ) {
			if (!dataParams.getLoadCVFiles()) {
				trainData = preProc.getTrainSet();
				testData = preProc.getTestSet();
				sourceData = preProc.getSourceData();
				preProc = null;
			}
			System.gc();

			if (learnerParams != null) {
				retVal = true;
			}
		}
		return retVal;
	}

	public LearnerParameters getLearnerParameters() {
		return learnerParams;
	}

	public void processLearnerParams() throws Exception {
		learnerParams.trainData = trainData;
		learnerParams.testData = testData;
		learnerParams.sourceData = sourceData;
		if (learnerParams.shouldDoCv() && ! learnerParams.shouldDoBss()) {
			if (learnerParams.getNumFolds() < 1)
				learnerParams.setNumFolds(trainData.numInstances());
			if (learnerParams.getNumFolds() > trainData.numInstances())
				throw new Exception("Number folds is greater than number of instances!");
		} else if (learnerParams.shouldDoBss() && ! learnerParams.shouldDoCv()) {
			if (learnerParams.getNumBssFolds() < 1)
				learnerParams.setNumBssFolds(trainData.numInstances());
			if (learnerParams.getNumBssFolds() > trainData.numInstances())
				throw new Exception("Number BSS folds is greater than number of instances!");
		} else if (learnerParams.shouldDoCv() && learnerParams.shouldDoBss()) {
			if (learnerParams.getNumFolds() < 1 || learnerParams.getNumBssFolds() < 1)
				throw new Exception("Invalid nuimber of folds");
			else if (learnerParams.getNumFolds() * learnerParams.getNumBssFolds() > trainData.numInstances())
				throw new Exception("Total number of folds is greater than number of instances!");
		}
	}
}
