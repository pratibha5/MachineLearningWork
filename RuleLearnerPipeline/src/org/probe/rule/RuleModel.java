package org.probe.rule;

import java.io.*;
import java.util.Scanner;
import org.probe.algo.rule.LearnerParameters;
import org.probe.util.RuleList;

public class RuleModel{
    RuleList rules;
	LearnerParameters parameters;

	public RuleModel() {
		parameters = new LearnerParameters();
		rules = new RuleList();
	}

	public RuleModel(String[] options) throws Exception {
		parameters = new LearnerParameters(options);
	}

	public RuleModel(LearnerParameters p, RuleList rls) {
		parameters = p;
		rules = rls;
	}

	public void setParameters(String[] options) throws Exception {
		rules = new RuleList();
		parameters = new LearnerParameters(options);
	}

	public void setRules(RuleList rls) {
		rules = rls;
	}

	public void addRules(RuleList rls) {
		rules.addAll(rls);
	}

	public RuleList getRules() {
		return rules;
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("=== Learner parameters ===\n\n");
		buf.append(parameters.toString() + "\n");
		buf.append("=== Rules (" + rules.size() + ") ===\n\n");
		buf.append(rules.toString());
		return buf.toString();
	}

	public LearnerParameters getParameters() {
		return parameters;
	}

	public int numVariables() {
		return rules.getAttributes().size();
	}

	public static void main(String[] args) {
		if (args.length > 0) {
			String modelFileName = args[0];
			System.out.println("Reading rule model from file" + modelFileName);
			try {
				FileReader reader = new FileReader(modelFileName);
				Scanner scanner = new Scanner(reader);
			} catch (FileNotFoundException fnfe) {
				fnfe.printStackTrace();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}
}