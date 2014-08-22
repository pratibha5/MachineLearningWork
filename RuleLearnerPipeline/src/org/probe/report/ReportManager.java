package org.probe.report;

import java.io.File;

import org.probe.rule.RuleModel;

public interface ReportManager {
	void saveSrc(File src);
	void saveTarget(File target);
	void saveTest(File test);
	
	void writeRuleModelToFile(RuleModel ruleModel, String fileName);
}
