package algo.rl.rule;

import parameters.LearnerParameters;
import data.dataset.Dataset;
import rule.RuleList;

public interface RuleGenerator {
	public RuleList generateRules();

	public void setDataset(Dataset trnData);

	public void setParameters(LearnerParameters p);

	public int ruleCount();
}
