package structures.result;

import java.util.ArrayList;
import java.text.DecimalFormat;

import parameters.LearnerParameters;
import data.dataset.AttributeDoesNotExistException;
import structures.learner.attribute.VHierarchyNode;
import rule.Rule;
import rule.RuleList;
import rule.RuleModel;
import util.Arrays;
import util.MathUtil;
import util.Stats;

/**
 * @author Jonathan Lustgarten
 */
public class CrossValClassificationResult {
	private int numFolds;
	private double[][] predictions;
	private double[][] probabilities;
	private ArrayList<RulePrediction> rulePreds;
	private double[][] confusionMatrix;
	private double[] numCorrects;
	private double[] numIncorrects;
	private double[] numAbstentions;
	private double[] senss;
	private double[] specs;
	private double[] baccs;
	private double[] rcis;
	private ArrayList<RuleModel> models;
	private LearnerParameters lp;
	private VHierarchyNode classValue;

	/**
	 * @param nFolds
	 *            The Number of Folds
	 * @param nInstances
	 *            The Total Number of Samples
	 * @param nClasses
	 *            The Total Number of Classes
	 */
	public CrossValClassificationResult(int nFolds, int nInstances, int nClasses) {
		numFolds = nFolds;
		models = new ArrayList<RuleModel>(nFolds);
		predictions = new double[nInstances][2];
		probabilities = new double[nInstances][2];
		confusionMatrix = Arrays.init(new double[nClasses][nClasses + 1], 0);
		rcis = new double[numFolds];
		baccs = new double[numFolds];
		senss = new double[numFolds];
		specs = new double[numFolds];
		numCorrects = new double[numFolds];
		numIncorrects = new double[numFolds];
		numAbstentions = new double[numFolds];
		rulePreds = new ArrayList<RulePrediction>(nInstances);
	}

	public void setParameters(LearnerParameters param) {
		lp = param;
	}

	private void addInstances(int currI, int nInstances) {
		double[][] npreds = new double[predictions.length + nInstances - currI][2];
		System.arraycopy(predictions, 0, npreds, 0, predictions.length);
		double[][] nprobs = new double[probabilities.length + nInstances - currI][2];
		System.arraycopy(probabilities, 0, nprobs, 0, probabilities.length);
		predictions = npreds;
		probabilities = nprobs;
	}

	public void addFolds(ClassificationResult[] inferenceResults) throws AttributeDoesNotExistException {
		classValue = inferenceResults[0].getTrainingSet().getHierarchy(
				inferenceResults[0].getTrainingSet().classAttIndex());
		int currNumSamp = 0;
		for (int iFold = 0; iFold < inferenceResults.length; iFold++) {
			ClassificationResult res = inferenceResults[iFold];
			rcis[iFold] = res.getRCI();
			senss[iFold] = res.getSensitivity();
			specs[iFold] = res.getSpecificity();
			baccs[iFold] = res.balancedAccuracy();		
			numCorrects[iFold] = res.numCorrect();
			numIncorrects[iFold] = res.numIncorrect();
			numAbstentions[iFold] = res.numAbstentions();
			double[][] foldCM = res.confusionMatrix();
			for (int i = 0; i < foldCM.length; i++) {
				for (int j = 0; j < foldCM[i].length; j++)
					confusionMatrix[i][j] += foldCM[i][j];
			}
			double[][] preds = res.predictions();
			double[][] probs = res.probabilities();
			for (int i = 0; i < preds.length; i++) {
				int currInd = currNumSamp + i;
				if (currInd == predictions.length) {
					addInstances(i, preds.length);
				}
				predictions[currInd][0] = preds[i][0];
				predictions[currInd][0] = preds[i][1];
				probabilities[currInd][0] = probs[i][0];
				probabilities[currInd][0] = probs[i][0];
			}
			currNumSamp += res.getTestSet().numInstances();
			models.add(res.getModel());
			rulePreds.addAll(res.getRulePredictions());
		}
	}

	public double[] rciMSE() {
		//System.out.println(Arrays.toString(rcis, ", "));
		double[] semRCI = MathUtil.avgAndSE(rcis);
		semRCI[0] = Stats.getRCI(confusionMatrix);
		return semRCI;
	}

	public double[] baccMSE() {
		return MathUtil.avgAndSE(baccs);
	}

	public double numCorrect() {
		return MathUtil.sumArray(numCorrects);
	}

	public double numIncorrect() {
		return MathUtil.sumArray(numIncorrects);
	}

	public double numAbstentions() {
		return MathUtil.sumArray(numAbstentions);
	}
	
	public double pctCorrect() {
		return 100.0 * numCorrect() / (numCorrect() + numIncorrect() + numAbstentions());
	}

	/* 
	 * This method is only called in CrossValinferenceResultComparator.compare(),
	 * and only if the cross-val RCIs are different. 
	 */
	public double balancedAccuracy() {
		double bacc = 0;
		try {
			VHierarchyNode clHierarchy = lp.trainData.getHierarchy(
					lp.trainData.classAttIndex());
			//for (int iClass = 0; iClass < confusionMatrix.length; iClass++) {
			int iClass = 0; {
				double tp = 0, fp = 0, fn = 0, tn = 0;
				for (int iActual = 0; iActual < confusionMatrix.length; iActual++) {
					for (int iPred = 0; iPred < confusionMatrix.length; iPred++) {
						if (iActual == iClass) {
							if (iActual == iPred
									|| clHierarchy.getValue(iPred).inSubtree(clHierarchy.getValue(iActual))) {
								//tp += 1;
								tp += confusionMatrix[iActual][iPred];
							} else {
								//fn += 1;
								fn += confusionMatrix[iActual][iPred];
							}
						} else {
							if (iActual == iPred
									|| clHierarchy.getValue(iPred).inSubtree(clHierarchy.getValue(iActual))) {
								//tn += 1;
								tn += confusionMatrix[iActual][iPred];
							} else {
								//fp += 1;
								tn += confusionMatrix[iActual][iPred];
							}
						}
					}
				}
				if (tp + fn > 0 && tn + fp > 0)
					bacc += (tp / (tp + fn) + tn / (tn + fp)) / 2.0;
			}
		} catch (Exception e) {
			bacc = 0;
		}
		// Divide by the number of folds to get an average balanced accuracy??
		return bacc; /// confusionMatrix.length;
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		DecimalFormat df = new DecimalFormat("0.###");		
		final int N = (int) (numCorrect() + numIncorrect());
		
		if (lp != null) {
			buf.append("=== Learner parameters ===\n\n");
			buf.append(lp.toString() + "\n");
		}
		if (models != null && models.size() > 0 
				&& (models.get(0)).getParameters().verbosity > 0) {
			buf.append(predictionsString() + "\n");
		}
		buf.append("=== Cross-validation performance ===\n\n");
		buf.append(performanceString());
		return buf.toString();
	}

	public String performanceString() {
		StringBuffer buf = new StringBuffer();
		DecimalFormat df = new DecimalFormat("0.###");		
		String className = (classValue == null) ? "0" : classValue.getValue(0).getName();
		final int nPred = (int) (numCorrect() + numIncorrect());
		final int nAbst = (int) numAbstentions();
		final int nAll = nPred + nAbst;
		final int wStr = 35 + className.length();
		final String fmt = "%-" + Integer.toString(wStr) + "s"; 
		
		buf.append(String.format(fmt, "Accuracy ignoring abst.: ") 
				+ df.format(numCorrect() * 100.0 / nPred) + " %\n");
		buf.append(String.format(fmt, "Accuracy including abst.: ")
				+ df.format(pctCorrect()) + " %\n");
		double[] sn = MathUtil.avgAndSE(senss);
		buf.append(String.format(fmt, "Abstentions: ")
				+ (int) numAbstentions() 
				+ " (" + df.format(numAbstentions() * 100.0 / nAll) + " %)\n");	
		buf.append(String.format(fmt, "Sensitivity (class " + className + " vs rest): ") 
				+ df.format(sn[0]) + " +/- " + df.format(sn[1])+ " %\n");
		double[] sp = MathUtil.avgAndSE(specs);
		buf.append(String.format(fmt, "Specificity (class " + className + " vs rest): ") 
				+ df.format(sp[0]) + " +/- " + df.format(sp[1]) + " %\n");
		double[] bacc = baccMSE();
		buf.append(String.format(fmt, "Balanced accuracy: ")
				+ df.format(bacc[0]) + " +/- " + df.format(bacc[1]) + " %\n");
		double[] rci = rciMSE();
		buf.append(String.format(fmt, "Relative classifier information: ") 
				+ df.format(rci[0]) + " +/- " + df.format(rci[1]) + "\n");
	
		buf.append(ClassificationResult.confusionMatrixString(confusionMatrix, classValue) + "\n");
		return buf.toString();	
	}
	
	public RuleModel getRulesFromFold(int f) {
		return models.get(f);
	}

	public String getRules() {
		StringBuffer buf = new StringBuffer();
		for (int f = 0; f < numFolds; f++) {
			RuleList rules = getRulesFromFold(f).getRules();
			try {
				/*if (rules.size() > 0 && lp != null
						&& lp.getRuleGeneratorType() >= LearnerParameters.RULE_GENERATOR_BAYES_CLOBAL) {
					if (((Rule) rules.get(0)).getGeneratingModel() != null) {
						buf.append("=== Generating bayesian model ===\n");
						buf.append(((Rule) rules.get(0)).getGeneratingModel().toString() + "\n\n");
					}
				}*/
				buf.append("=== Rules from fold " + (f + 1) + " ===\n");
				buf.append(rules + "\n");
			} catch (OutOfMemoryError oome) {
				oome.printStackTrace();
				return buf.toString();
			}
		}
		return buf.toString();
	}

	public String predictionsString() {
		StringBuffer buf = new StringBuffer();
		buf.append("=== Predictions ===\n\n");
		buf.append("InstanceName\tPredictedClass\tActualClass\tRulesMatched\tRulesUsed\tCF-Value\n");
		for (RulePrediction p : rulePreds) {
			if (p.getPredictedValue() != null 
					&& ! p.getPredictedValue().equals("No prediction"))
				buf.append(p.toString() + "\n");
		}
		return buf.toString();
	}
}