package algo.rl;

import parameters.DataParameters;
import parameters.LearnerParameters;
import structures.cf.CertaintyFactor;
import structures.cr.ConflictResolver;
import data.dataset.*;
//import structures.learner.rule.RuleList;
import rule.RuleModel;
import structures.result.CrossValClassificationResult;
import structures.result.CrossValInferenceResultComparator;
import structures.result.ClassificationResult;
import util.Util;

public class BiasSpaceSearch {
	private Dataset train;
	private LearnerParameters inputParams;
	private LearnerParameters bestParams;
	private DataParameters dataParams;
	private CrossValClassificationResult bestResult;

	public BiasSpaceSearch(Dataset train, LearnerParameters lp, DataParameters dp) {
		this.train = train;
		inputParams = lp;
		dataParams = dp;
	}

	public void run() throws Exception {
		Dataset[][] folds = createFolds(inputParams.getNumBssFolds());
		bestResult = null;
		CrossValInferenceResultComparator cvComparator = new CrossValInferenceResultComparator();
		System.out.print("Doing " + inputParams.getNumBssFolds() + "-fold bias space search ");
		
		// If the maxFP parameter is set, as it is by default, use that value;
		// Otherwise search over various values.
		////if (!inputParams.isMaxFPSet())
		double mxFP = 0.1;
		double mnFP = 0;
		if (inputParams.getMaxFP() > 0)	{	// MaxFP not set
			mxFP = inputParams.getMaxFP();
			mnFP = inputParams.getMaxFP();
		}
		//	inputParams.setMaxFP(0.1);		// commented out: BSS should use the default FP param
		if (inputParams.getRuleGeneratorType() == LearnerParameters.RULE_GENERATOR_CLASSIC) {
			//{
			//for (double fp = 0.1; fp >= 0; fp -= .05) {
			for (double fp = mxFP; fp >= mnFP; fp -= .05) {
				LearnerParameters fpParams = (LearnerParameters) inputParams.clone();
				fpParams.setMaxFP(fp);
				//for (int c = 60; c <= 100; c += inputParams.getBssCfInc()) {
				for (double c = 0.6; c <= 1; c += inputParams.getBssCfInc()) {
					LearnerParameters cfClone = (LearnerParameters) fpParams.clone();
					//LearnerParameters cfClone = (LearnerParameters) inputParams.clone();
					//cfClone.setMinCf(c / 100.0);
					cfClone.setMinCf(c);
					//for (int iCfMeth = 0; iCfMeth < CertaintyFactor.getCfArray().length; iCfMeth++) {
					int[] cfTypes = new int[] {0, 1, 2, 3, 4};
					for (int iCfType : cfTypes) { 
						LearnerParameters cfTypClone = (LearnerParameters) cfClone.clone();
						cfTypClone.setCfMethod(iCfType);
						//for (int maxConj = 3; maxConj <= inputParameters.getMaxConjuncts(); maxConj++) {
						final int max_conj = inputParams.getMaxConjuncts();	// PG2009
						//for (int maxConj = max_conj; maxConj <= max_conj; maxConj++) {
						{
							LearnerParameters maxConjClone = (LearnerParameters) cfTypClone.clone();
							RuleModel[] models = new RuleModel[inputParams.getNumBssFolds()];
							//for (int infT = 0; infT < ConflictResolver.getCRArray().length; infT++) {
							int[] infTypes = new int [] {0, 1, 4};
							for (int infType : infTypes) {
								if (infType == 0)
									System.out.print(".");
								LearnerParameters infTypeClone = (LearnerParameters) maxConjClone.clone();
								ClassificationResult[] foldRes = new ClassificationResult[inputParams.getNumBssFolds()];
								for (int iFold = 0; iFold < inputParams.getNumBssFolds(); iFold++) {
									LearnerParameters foldClone = (LearnerParameters) infTypeClone.clone();
									foldClone.trainData = folds[iFold][0];
									RuleLearner rl = new RuleLearner(folds[iFold][0]);
									if (infType == 0) {
										rl.setParameters(foldClone);
										rl.learnModel();
										models[iFold] = rl.getModel();
										foldRes[iFold] = rl.evaluateModel(folds[iFold][1]);
									} else {
										rl.setModel(models[iFold]);
										foldRes[iFold] = rl.evaluateModel(folds[iFold][1], foldClone);
									}
								}
								CrossValClassificationResult cvRes = new CrossValClassificationResult(
										folds.length, train.numInstances(),
										train.classAttribute().hierarchy().numValues());
								cvRes.addFolds(foldRes);
								if (bestResult == null || cvComparator.compare(bestResult, cvRes) > 0) {
									bestResult = cvRes;
									bestParams = infTypeClone;
									bestParams.setDoBss(false);
								}
							}
						}
					}
				}
			}
		} else if (inputParams.getRuleGeneratorType() >= LearnerParameters.RULE_GENERATOR_BAYES_CLOBAL) {
			inputParams.setMaxFP(0.1);
			inputParams.setCfMethod(0);
			inputParams.setInferenceType(0); // 0 is weighted voting
			ClassificationResult[] foldRes = new ClassificationResult[inputParams.getNumBssFolds()];
			for (int fold = 0; fold < folds.length; fold++) {
				System.out.print(".");
				RuleLearner rl = new RuleLearner(folds[fold][0], inputParams, dataParams);
				rl.learnModel();
				foldRes[fold] = rl.evaluateModel(folds[fold][1]);
			}
			CrossValClassificationResult cvIR;
			cvIR = new CrossValClassificationResult(folds.length, 
					train.numInstances(), 
					train.classAttribute().hierarchy().numValues());
			cvIR.addFolds(foldRes);
			bestResult = cvIR;
			bestParams = inputParams;
		}
		System.out.println("  Done.");
	}

	protected Dataset[][] createFolds(int nFolds) throws Exception{
		Dataset[][] folds = new Dataset[nFolds][2];
		for (int i = 0; i < folds.length; i++) {
			try {
				folds[i][0] = train.trainCV(folds.length, i);
				folds[i][1] = train.testCV(folds.length, i);
				Dataset[] dats = new Dataset[] {folds[i][0], folds[i][1], inputParams.sourceData};
				//Util.discDatasets(folds[i], inputParams.getDiscretizerIndex(),
				Util.discDatasets(dats, inputParams.getDiscretizerIndex(),
						inputParams.getDiscretizerValue());
			} catch (NullDatasetException e) {
				throw new Exception("Can't create BSS fold " + (i + 1));
			}
		}
		return folds;
	}
	
	public CrossValClassificationResult getBestResult() {
		return bestResult;
	}

	public LearnerParameters getBestParameters() {
		return bestParams;
	}
}