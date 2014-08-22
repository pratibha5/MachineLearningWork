package algo.rl;

import java.util.ArrayList;
import java.util.HashMap;

import parameters.DataParameters;
import parameters.LearnerParameters;
import structures.cr.ConflictResolver;
import structures.data.converters.output.OutputDataset;
import structures.data.converters.output.OutputTabOrCSV;
import data.dataset.Dataset;
import structures.learner.attribute.AttributeList;
import structures.learner.attribute.LearnerAttribute;
import structures.learner.attribute.VHierarchyNode;
import rule.Rule;
import rule.RuleList;
import rule.RuleModel;
import structures.result.ClassificationResult;
import structures.result.RulePrediction;
import util.Util;
import algo.rl.rule.RuleGenerator;
import algo.rl.rule.SAL;

/**
 * @author Jonathan
 */
public class RuleLearner {
	private RuleModel model; // Contains the parameters and the rules learned
	private LearnerParameters learnParams;
	private DataParameters dataParams;
	private LearnerAttribute classAtt;
	private HashMap<Integer, RuleList> matchedRules;

	public RuleLearner(Dataset train) {
		learnParams = new LearnerParameters();
		learnParams.trainData = train;
	}

	public RuleLearner(Dataset train, LearnerParameters lp, DataParameters dp) {
		learnParams = (LearnerParameters) lp.clone();
		dataParams = dp;
		learnParams.trainData = train;
	}

	public RuleLearner(Dataset train, Dataset source, LearnerParameters lp, DataParameters dp) {
		this(train, lp, dp);
		learnParams.sourceData = source;
	}

	public void learnModel() throws Exception {
		
		RuleGenerator rg;
		Dataset d = learnParams.trainData;

		if (!d.isDiscretized() && d.numContinuousAttributes() > 0) {
			Util.discDataset(d, learnParams.getDiscretizerIndex(),
					learnParams.getDiscretizerValue());
		}
		
		AttributeList al = d.rulegenAttributeList();
		classAtt = al.getTargetAttribute();
		int type = learnParams.getRuleGeneratorType();
		rg = new SAL(al, learnParams);
		RuleList rules = rg.generateRules();
		model = new RuleModel(learnParams, rules);
		matchedRules = null;
	}

	public void setParameters(LearnerParameters p) {
		model = null;
		this.learnParams = p;
	}

	public RuleModel getModel() {
		return model;
	}

	private HashMap<Integer, RuleList> getMatchingRules(Dataset tst) {
		HashMap<Integer, RuleList> matchRs = new HashMap<Integer, RuleList>(tst
				.numInstances());
		RuleList currRules = model.getRules();
		for (int s = 0; s < tst.numInstances(); s++) {
			RuleList rls = new RuleList();
			for (int i = 0; i < currRules.size(); i++) {
				Rule r = (Rule) currRules.get(i);
				r.setIndex(i);
				if (r.matchLhs(tst, s))
					rls.add(r);
			}
			matchRs.put(s, rls);
		}
		return matchRs;
	}

	private RulePrediction classify(RuleList rules, Dataset test,
			int sampNum) throws Exception {
		ConflictResolver cr = ConflictResolver.getCRArray()[learnParams
				.inferenceType()];
		cr.setTarget(classAtt);
		VHierarchyNode cvhn = learnParams.trainData.classAttribute()
				.hierarchy();
		if (rules.size() == 0) {
			return new RulePrediction(test.instanceName(sampNum), test
					.instanceClass(sampNum));
		}
		int pred = cr.predict(rules, test);
		double cert = cr.getCertaintyValue();
		RulePrediction rp = new RulePrediction(rules, 
				test.instanceName(sampNum), test.instanceClass(sampNum), 
				cvhn.getValue(pred), cr.getUsedRules(), cert);
		return rp;
	}

	public void setModel(RuleModel ruleModel) {
		model = ruleModel;
	}
	
	private ArrayList<RulePrediction> getPredAndProbs(Dataset test,
			HashMap<Integer, RuleList> matchdRules) {
		ArrayList<RulePrediction> preds = new ArrayList<RulePrediction>(test
				.numInstances());
		for (int s = 0; s < test.numInstances(); s++) {
			try {
				preds.add(classify(matchdRules.get(s), test, s));
			} catch (Exception e) {
				try {
					preds.add(new RulePrediction(test.instanceName(s), 
							test.instanceClass(s)));
				} catch (Exception e1) {
					e1.printStackTrace();
					System.exit(1);
				}
			}
		}
		return preds;
	}

	public ClassificationResult evaluateModel(Dataset test) throws Exception {
		RuleList rules = model.getRules();
		for (int i = 0; i < rules.size(); i++) {
			Rule r = rules.get(i);
			r.setTestFalsePos(0);
			r.setTestTruePos(0);
		}
		model.setRules(rules);
		ArrayList<RulePrediction> preds = new ArrayList<RulePrediction>();
		ClassificationResult res = new ClassificationResult(model, 
				learnParams.trainData, test);
		res.debug = learnParams.verbosity;
		matchedRules = getMatchingRules(test);
		preds = getPredAndProbs(test, matchedRules);
		res.setRulePredictions(preds);

		return res;
	}

	/**
	 * This method is only to be used when testing a new inference type; It assumes
	 * that all other parameters are the same and that the test set is the same.
	 * @param test
	 *            The test {@link Dataset}
	 * @param newInf
	 *            The new inference type
	 * 
	 * @return The inference result using the new inference conflict resolver
	 * @throws Exception 
	 */
	public ClassificationResult evaluateModel(Dataset test,
			LearnerParameters newInf) throws Exception {
		learnParams = newInf;
		RuleModel tm = new RuleModel(newInf, model.getRules());
		model = tm;
		ArrayList<RulePrediction> preds = new ArrayList<RulePrediction>();
		ClassificationResult res = new ClassificationResult(model, 
				learnParams.trainData, test);
		matchedRules = getMatchingRules(test);
		preds = getPredAndProbs(test, matchedRules);
		res.setRulePredictions(preds);

		return res;
	}
}