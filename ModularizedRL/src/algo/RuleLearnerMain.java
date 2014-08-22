package algo;

import java.io.*;
import java.util.ArrayList;

import algo.rl.RuleLearner;
import algo.rl.BiasSpaceSearch;
import parameters.DataParameters;
import parameters.LearnerParameters;
import structures.data.converters.output.OutputDataset;
import structures.data.converters.output.OutputTabOrCSV;
import data.dataset.AttributeDoesNotExistException;
import data.dataset.Dataset;
import data.dataset.IncompatibleDatatypeException;
import rule.Rule;
import rule.RuleList;
import rule.RuleModel;
import structures.result.CrossValClassificationResult;
import structures.result.ClassificationResult;

/**
 * @author Jonathan Lustgarten
 */
public class RuleLearnerMain {
	private LearnerParameters learnParams;
	private DataParameters dataParams;
	private Dataset trainData;
	private Dataset testData;
	private Dataset sourceData;
	private CrossValClassificationResult bssRes;
	private static String FILE_PATH_SEP = System.getProperty("file.separator");

	public RuleLearnerMain(LearnerParameters lp, DataParameters dp) throws IncompatibleDatatypeException {
		trainData = lp.trainData;
		testData = lp.testData;
		sourceData = lp.sourceData;
		learnParams = lp;
		dataParams = dp;
		if (trainData.classAttIndex() < 0) {
			throw new data.dataset.IncompatibleDatatypeException(
					"The data file " + trainData.getFileName()
					+ " does not specify a class attribute");
		}
		trainData.setRandSeed(learnParams.cvRandSeed);
	}

	public CrossValClassificationResult doBiasSpaceCrossVal() throws Exception {
		ClassificationResult[] results = new ClassificationResult[learnParams.getNumFolds()];
		for (int f = 0; f < learnParams.getNumFolds(); f++) {
			Dataset tst = trainData.testCV(learnParams.getNumFolds(), f);
			Dataset trn = trainData.trainCV(learnParams.getNumFolds(), f);
			BiasSpaceSearch bss = new BiasSpaceSearch(trn, learnParams, dataParams);
			bss.run();
			LearnerParameters bestParams = bss.getBestParameters();
			bssRes = bss.getBestResult();
			RuleLearner rl = new RuleLearner(trn);
			bestParams.trainData = trn;
			rl.setParameters(bestParams);
			rl.learnModel();
			results[f] = rl.evaluateModel(tst);
		}
		CrossValClassificationResult cvRes = null;
		cvRes = new CrossValClassificationResult(learnParams.getNumFolds(), 
				trainData.numInstances(), 
				trainData.classAttribute().hierarchy().numValues());
		cvRes.addFolds(results);
		return cvRes;
	}

	public CrossValClassificationResult doCrossVal() throws Exception {
		ClassificationResult[] ress = new ClassificationResult[learnParams.getNumFolds()];
		for (int f = 0; f < learnParams.getNumFolds(); f++) {
			System.out.print("Fold " + (f+1) + ". ");
			Dataset tst = trainData.testCV(learnParams.getNumFolds(), f);
			Dataset trn = trainData.trainCV(learnParams.getNumFolds(), f);
			RuleLearner rl = new RuleLearner(trn, learnParams, dataParams);
			rl.learnModel();
			System.out.println("Learned " + rl.getModel().getRules().size() + " rules");
			ress[f] = rl.evaluateModel(tst);
		}
		CrossValClassificationResult res = null;
		try {
			res = new CrossValClassificationResult(learnParams.getNumFolds(), 
					trainData.numInstances(), 
					trainData.classAttribute().hierarchy().numValues());
		} catch (AttributeDoesNotExistException e) {
			System.err.println(e.getLocalizedMessage());
			e.printStackTrace();
			System.exit(10);
		}
		res.addFolds(ress);
		return res;
	}

	public LearnerParameters doBiasSpaceSearch(Dataset data, LearnerParameters params) 
			throws Exception {
		BiasSpaceSearch bss = new BiasSpaceSearch(data, params, dataParams);
		bss.run();
		bssRes = bss.getBestResult();
		return bss.getBestParameters();
	}

	private void trainWholeTraining(LearnerParameters params) throws Exception {
		System.out.println("Learning on whole training data...");
		RuleLearner rl = new RuleLearner(trainData, params, dataParams);
		rl.learnModel();
		RuleModel rm = rl.getModel();
		writeDataUsedAtts(rm, trainData);
		if (rm.getParameters() != null) {
			RuleList pr = rm.getParameters().getPriorRules();
			if (pr != null) {
				System.out.println("\n=== Prior rules (" + pr.size() + ") ===\n");
				System.out.println(pr + "\n");
			}
		}

		System.out.println("Learned " + rm.getRules().size() + " rules\n");
		writeRules(trainData.getFileName(), rm.getRules());
		writeDiscDataCSV(trainData, "disc");
		ClassificationResult res;
		Dataset tstD;
		if (testData != null) {
			tstD = testData;
			//System.out.println("Result on test data:\n");
		} else {
			tstD = trainData;
			//System.out.println("Result from testing on train data:\n");
		}
		res = rl.evaluateModel(tstD);
		System.out.println(res.toString());
		writeClassificationResult(tstD.getFileName(), res);
	}

	public void run() throws Exception {
			
		if (learnParams.shouldDoBss() && learnParams.shouldDoCv()) {
			if (learnParams.getNumBssFolds() < 1)
				learnParams.setNumBssFolds(trainData.numInstances());
			System.out.println("Doing " + learnParams.getNumFolds() 
					+ "-fold cross-validation with internal bias space search...");
			CrossValClassificationResult res = doBiasSpaceCrossVal();
			System.out.println("\n" + res.toString());
			writeCVResult(trainData.getFileName(), res);
			//// Generate best rules
			//learnerParams.setNumBssFolds(10);	--PG2009
			learnParams.setNumBssFolds(learnParams.getNumFolds());	// --PG2009
			LearnerParameters newRLP = doBiasSpaceSearch(trainData, learnParams);
			trainWholeTraining(newRLP);
		} else if (learnParams.shouldDoCv()) {
			if (learnParams.getNumFolds() < 1)
				learnParams.setNumFolds(trainData.numInstances());
			System.out.println("Doing " + learnParams.getNumFolds() 
					+ "-fold cross-validation...");
			CrossValClassificationResult res = doCrossVal();
			System.out.println("\n" + res.toString());
			writeCVResult(trainData.getFileName(), res);
			trainWholeTraining(learnParams);	// PG2009
		} else if (learnParams.shouldDoBss()) {
			if (learnParams.getNumBssFolds() < 1)
				learnParams.setNumBssFolds(sourceData.numInstances());
			//System.out.println("Doing bias-space search for optimal parameters...");
			LearnerParameters newLP = doBiasSpaceSearch(trainData, learnParams);
			System.out.println("Bias space search result:\n");
			bssRes.setParameters(newLP);
			System.out.println(bssRes.toString());
			writeCVResult(trainData.getFileName(), bssRes);
			trainWholeTraining(newLP);
		} else {
			trainWholeTraining(learnParams);
		}
	}

	private void writeDataUsedAtts(RuleModel model, Dataset data) {
		ArrayList<String> vars = model.getRules().getAttributes();
		String[] svars = vars.toArray(new String[0]);
		data.keepAttributes(svars);
		String fileName = util.Util.appendFileNameSuffix(dataParams.getOutDirName()
				+ FILE_PATH_SEP + data.getFileName(), "sel");
		OutputTabOrCSV od = new OutputTabOrCSV(data, fileName, "\t");
		od.printDataset(false);
	}

	private void writeCVResult(String dataSetName,
			CrossValClassificationResult result) throws FileNotFoundException {
		PrintStream ps;		
		String fileName = util.Util.replaceFileNameSuffix(dataParams.getOutDirName()
				+ FILE_PATH_SEP + dataSetName, "cv.perf");
		ps = new PrintStream(new FileOutputStream(fileName));
		ps.println(result);
		ps.close();

		if (learnParams.shouldSaveCvRules()) {
			fileName = util.Util.replaceFileNameSuffix(dataParams.getOutDirName()
					+ FILE_PATH_SEP + dataSetName, "cv.rules");
			// It this fails, print the fold rules to standard output
			try {
				ps = new PrintStream(new FileOutputStream(fileName));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				ps = System.out;
			}
			ps.println("Rules from cross-validation: ");
			try {
				ps.println(result.getRules());
			} catch (NullPointerException e) {
				e.printStackTrace();
				// TODO: find out why this exception happens
			}
			ps.close();			
		}
	}

	private void writeClassificationResult(String dataSetName, ClassificationResult res) {
		PrintStream ps;
		String outFileStem = dataParams.getOutDirName() + FILE_PATH_SEP + dataSetName;
		String fName = util.Util.replaceFileNameSuffix(outFileStem, "perf");

		try {
			ps = new PrintStream(new FileOutputStream(fName));
			ps.println(res);
			ps.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		// Write the predictions to a new file
		String testDataName = dataParams.getTestFileName();
		fName = util.Util.replaceFileNameSuffix(outFileStem, "pred");
		try {
			ps = new PrintStream(new FileOutputStream(fName));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			ps = System.out;
		}
		ps.println(res.predictionsString());
		ps.close();
		
		writeRules(dataSetName, res.getModel().getRules());
		writeDataUsedAtts(res.getModel(), trainData);
	}

	public void writeRules(String dataSetName, RuleList rules) {
		try {
			String fName = util.Util.replaceFileNameSuffix(dataParams.getOutDirName()
					+ FILE_PATH_SEP + dataSetName, "rules");
			PrintStream ps = new PrintStream(new FileOutputStream(fName));
			ps.println("Rules from whole training data:\n");
			ps.println(rules.toString());
			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void writeDiscDataCSV(Dataset data, String suff) {
		String fileName = util.Util.appendFileNameSuffix(
				dataParams.getOutDirName() + FILE_PATH_SEP +  data.getFileName(), suff);
		OutputDataset od = new OutputTabOrCSV(data, fileName, "	");
		od.printDataset(true);
	}
}