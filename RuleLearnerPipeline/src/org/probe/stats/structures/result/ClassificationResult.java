package org.probe.stats.structures.result;

import java.text.DecimalFormat;
import java.util.ArrayList;

import data.dataset.Attribute;
import data.dataset.AttributeDoesNotExistException;
import data.dataset.DataModel;
import org.probe.stats.structures.learner.attribute.VHierarchyNode;
import org.probe.rule.RuleModel;
import org.probe.util.Arrays;
import org.probe.util.Stats;

public class ClassificationResult {
	private double[][] predictions;
	private double[][] probabilities;
	private ArrayList<RulePrediction> rulePreds;
	private double[][] confusionMatrix; // [observed][predicted]
	private int numCorrect;
	private int numIncorrect;
	private int numAbstentions;
	private double sensitivity;
	private double specificity;
	private double bAccuracy;
	private double rci;
	private RuleModel ruleModel;
	private DataModel train;
	private DataModel test;

	public int debug;
	
	public ClassificationResult(RuleModel mod, DataModel train, DataModel test) {
		this.train = train;
		this.test = test;
		//bAccuracy = 0;
		predictions = new double[test.numInstances()][2];
		probabilities = new double[test.numInstances()][2];
		try {
			VHierarchyNode vnh = train.getHierarchy(train.classAttIndex());
			for (int i = 0; i < test.numInstances(); i++) {
				predictions[i][0] = vnh.getValueIndex(test
						.originalAttributeValue(i, test.classAttIndex()));
				probabilities[i][0] = predictions[i][0];
			}
			confusionMatrix = Arrays.init(new double[vnh
					.numValues()][vnh.numValues() + 1], 0);
		} catch (Exception e) {
			System.err.println("Error in creating confusion matrix...");
			System.err.println("This should never happen!");
			e.printStackTrace();
			System.exit(1);
		}
		ruleModel = mod;
	}

	public double[][] confusionMatrix() {
		return confusionMatrix;
	}

	public double getRCI() {
		return rci;
	}

	public double[][] predictions() {
		return predictions;
	}

	public double[][] probabilities() {
		return probabilities;
	}

	private void setPredictions(double[] preds) throws Exception {
		if (preds.length != predictions.length)
			throw new Exception(
					"New predictions size differs from the old: " + preds.length 
					+ ". Old size: "+ predictions.length);
		Attribute clAtt = train.classAttribute();
		for (int i = 0; i < preds.length; i++) {
			predictions[i][1] = preds[i];
			int iActualVal = (int) predictions[i][0];
			if (preds[i] != -1 && iActualVal != -1) {
				boolean bRhs = false;
				int predValueInd = (int) predictions[i][1];
				VHierarchyNode actualValue = clAtt.hierarchy().getValue(
						iActualVal);
				VHierarchyNode[] pathRootToActualValue = clAtt.hierarchy()
						.getPathToRoot(actualValue);
				for (int counter = 0; counter < pathRootToActualValue.length; counter++) {
					int tmpIdx = clAtt.hierarchy().getValueIndex(
							pathRootToActualValue[counter]);
					// If we predicted any value in the path up the
					// hierarchy, then we correctly predicted this datum.
					if (predValueInd == tmpIdx)
						bRhs = true;
					// Inform the confusion matrix about our prediction
					// all the way up the hierarchy path.
					confusionMatrix[tmpIdx][predValueInd] += 1;
				}
				if (bRhs)
					numCorrect++;
				else
					numIncorrect++;
			} else {
				confusionMatrix[(int) predictions[i][0]][clAtt.hierarchy()
						.numValues()] += 1;
				numAbstentions += 1;
			}
		}
		rci = Stats.getRCI(confusionMatrix);
		calcSensSpecBacc();
	}

	private void setProbabilities(double[] probs) throws Exception {
		if (probs.length != probabilities.length)
			throw new Exception(
					"Prediction Size are not equivalent. Passed Size: "
							+ probs.length + " Real Size: "
							+ probabilities.length);
		for (int i = 0; i < probs.length; i++)
			probabilities[i][1] = probs[i];
	}

	public void setRulePredictions(ArrayList<RulePrediction> rpreds) throws Exception {
		rulePreds = rpreds;
		double[] preds = new double[predictions.length];
		double[] probs = new double[predictions.length];
		VHierarchyNode cvnh = train.classAttribute().hierarchy();
		for (int i = 0; i < rulePreds.size(); i++) {
			RulePrediction rp = rulePreds.get(i);
			if (rp.getPredictedValue() == null)
				preds[i] = -1;
			else
				preds[i] = cvnh.getValueIndex(rp.getPredictedValue());
			probs[i] = rp.getPredictedValueCertainty();
		}
		setPredictions(preds);
		setProbabilities(probs);
	}

	public DataModel getTrainingSet() {
		return train;
	}

	public DataModel getTestSet() {
		return test;
	}

	public int numCorrect() {
		return numCorrect;
	}

	public int numIncorrect() {
		return numIncorrect;
	}

	public int numAbstentions() {
		return numAbstentions;
	}

	public double pctCorrect() {
		return 100.0 * numCorrect / (numAbstentions + numCorrect + numIncorrect);
	}

	public double pctIncorrect() {
		return 100.0 * numIncorrect / (numAbstentions + numCorrect + numIncorrect);
	}

	public static double sens(double[][] cm) {
		double sens = 0;
		int iClass = 0; {
			double tp = 0, fp = 0, fn = 0, tn = 0;
			for (int iActual = 0; iActual < cm.length; iActual++) {
				for (int iPred = 0; iPred < cm.length; iPred++) {
					if (iActual == iClass) {
						if (iActual == iPred)
							tp += cm[iActual][iPred];
						else {
							fn += cm[iActual][iPred];
						}
					} else {
						if (iActual == iPred)
							tn += cm[iActual][iPred];
						else {
							fp += cm[iActual][iPred];
						}
					}
				}
			}
			
			if (tp + fn > 0)
				sens += tp / (tp + fn);
		}
		
		return sens * 100.0;
	}

	public static double spec(double[][] cm) {
		double spec = 0;
		int iClass = 0; {
			double tp = 0, fp = 0, fn = 0, tn = 0;
			for (int iActual = 0; iActual < cm.length; iActual++) {
				for (int iPred = 0; iPred < cm.length; iPred++) {
					if (iActual == iClass) {
						if (iActual == iPred)
							tp += cm[iActual][iPred];
						else {
							fn += cm[iActual][iPred];
						}
					} else {
						if (iActual == iPred)
							tn += cm[iActual][iPred];
						else {
							fp += cm[iActual][iPred];
						}
					}
				}
			}
			
			if (tn + fp > 0)
				spec += tn / (tn + fp);
		}
		
		return spec * 100.0;
	}

	public static double bacc(double[][] cm) {			
		return (sens(cm) + spec(cm)) / 2;
	}
	
	private void calcSensSpecBacc() {
		double sens = 0, spec = 0, bacc = 0;
		int iClass = 0; {
			double tp = 0, fp = 0, fn = 0, tn = 0;
			for (int iActual = 0; iActual < confusionMatrix.length; iActual++) {
				for (int iPred = 0; iPred < confusionMatrix.length; iPred++) {
					if (iActual == iClass) {
						if (iActual == iPred)
							tp += confusionMatrix[iActual][iPred];
						else {
							fn += confusionMatrix[iActual][iPred];
						}
					} else {
						if (iActual == iPred)
							tn += confusionMatrix[iActual][iPred];
						else {
							fp += confusionMatrix[iActual][iPred];
						}
					}
				}
			}
			
			if (tp + fn > 0)
				sens += tp / (tp + fn);
			else
				sens = Double.NaN;

			if (tn + fp > 0)
				spec += tn / (tn + fp);
			else
				spec = Double.NaN;
			
			if (tp + fn > 0 && tn + fp > 0)
				bacc = (tp / (tp + fn) + tn / (tn + fp)) / 2.0;
			else if (tp + fn > 0)
				bacc = tp / (tp + fn);
			else if (tn + fp > 0)
				bacc = tn / (tn + fp);
			else
				bacc = Double.NaN;
		}
		bAccuracy = bacc * 100.0;// / (confusionMatrix.length);
		sensitivity = sens * 100.0;// / (confusionMatrix.length);
		specificity = spec * 100.0;// / (confusionMatrix.length);
	}

	public double getSensitivity() {
		return sensitivity;
	}

	public double getSpecificity() {
		return specificity;
	}

	public double balancedAccuracy() {
		return bAccuracy;
	}

	public static String confusionMatrixString(double[][] cm, VHierarchyNode vhn) {
		int maxW = (int) Math.floor(Math.log10(org.probe.util.MathUtil.max(cm))) + 1;
		StringBuffer buf = new StringBuffer();
		buf.append("Confusion matrix:\n");
		for (int i = 0; i < cm.length; i++)
			buf.append(String.format("\t%" + maxW + "s", "c" + (i + 1)));
		buf.append(String.format("\t%" + maxW + "s", "Abstentions\n"));
		for (int i = 0; i < cm.length; i++) {
			for (int j = 0; j < cm[i].length; j++)
				buf.append(String.format("\t%" + maxW + "d", (int) cm[i][j]));
			buf.append("\t| c" + (i + 1) + "  =  " + vhn.getValue(i).getName());
			buf.append("\n");
		}
		return buf.toString(); 
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append(ruleModel.toString() + "\n");
		if (ruleModel.getParameters().verbosity > 2) {
			buf.append(predictionsString() + "\n");
		}
		//buf.append("===  Classification performance ===\n\n");
		//if (test != null)
		if (test.equals(train))
			buf.append("=== Classification perfomance on test data ===\n\n");
		else
			buf.append("=== Classification performance on training data ===\n\n");
		buf.append(performanceString());
		return buf.toString();
	}

	public String performanceString() {
		StringBuffer buf = new StringBuffer();
		DecimalFormat df = new DecimalFormat("0.###");
		// Get the name of the first class value
		VHierarchyNode classVal = null;
		String classVal0Name = "0";
		try {
			classVal = train.getHierarchy(train.classAttIndex());
			classVal0Name = classVal.getValue(0).getName();
		} catch (Exception e) {
			System.err.println("Error while printing confusion matrix!");
			e.printStackTrace();
		}
		final int nPred = numCorrect + numIncorrect;
		final int wStr = 35 + classVal0Name.length();
		final String fmt = "%-" + Integer.toString(wStr) + "s"; 		
		
		buf.append(String.format(fmt, "Accuracy ignoring abst.: ") 
				+ df.format(numCorrect * 100.0 / nPred) + " %\n");
		buf.append(String.format(fmt, "Accuracy inclding abst.: ") 
				+ df.format(pctCorrect()) + " %\n");
		buf.append(String.format(fmt, "Abstentions: ") + numAbstentions + " (" 
				+ df.format(numAbstentions * 100.0 / (nPred + numAbstentions)) + " %)\n");
		buf.append(String.format(fmt, "Sensitivity (class " + classVal0Name + " vs rest): ")
				+ df.format(sensitivity) + " %\n");
		buf.append(String.format(fmt, "Specificity (class " + classVal0Name + " vs rest): ")
				+ df.format(specificity) + " %\n");
		buf.append(String.format(fmt, "Balanced accuracy: ")
				+ df.format(bAccuracy) + " %\n");
		buf.append(String.format(fmt, "Relative classifier information: ") 
				+ df.format(rci) + "\n");
		
		buf.append(confusionMatrixString(confusionMatrix, classVal) + "\n");
		return buf.toString();
	}

	public RuleModel getModel() {
		return ruleModel;
	}

	public ArrayList<RulePrediction> getRulePredictions() {
		return rulePreds;
	}

	public String predictionsString() {
		StringBuffer buf = new StringBuffer();
		String idAttName = "InstanceID";
		try {
			idAttName = train.idAttribute().name();
		} catch (AttributeDoesNotExistException e) {
		}
		
		buf.append("=== Predictions ===\n\n");
		buf.append(idAttName + "\tPredictedClass\tActualClass\tRulesMatched\tRulesUsed\tCF-value\n");
		for (RulePrediction p : rulePreds) {
			if (p.getPredictedValue() != null 
					&& ! p.getPredictedValue().equals("No prediction")) {
				buf.append(p.toString() + "\n");
			}
		}
		return buf.toString();
	}
}