package test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.junit.Test;

import data.dataset.Dataset;
import parameters.DataParameters;
import rule.RuleList;
import rule.RuleModel;
import structures.learner.attribute.AttributeList;
import structures.learner.attribute.LearnerAttribute;
import structures.result.ClassificationResult;
import util.Util;
import algo.InitializeParameters;
import algo.RuleLearnerMain;
import algo.rl.rule.RuleGenerator;
import algo.rl.rule.SAL;
import algo.rl.RuleLearner;

public class TestRuleLearningAlgorithm {

	static Properties prop;
	@Test
	public void test() throws Exception{
		System.out.println("Rule Learner version 2010-05-29");
		String args[] = null;
		args = TestProperties.loadProperties("/RLParameters.properties");
		InitializeParameters rl;
		
		if (args.length < 2) {
			System.out.println("Usage:");
			System.out.println(" -lp learning_params -ppp pre-processing_params -dp data_params");
			System.out.println(" -h  (Prints help text)");
			System.exit(0);
		}
		System.out.println("Args: " + util.Arrays.toString(args));
		if (args[0].equalsIgnoreCase("-h")) {
			if (args[1].equalsIgnoreCase("-dp")) {
				System.out.println("Help on data parameters:\n");
				System.out.println(DataParameters.getHelpString());
			} else {
				System.err.println("Cannot recognize parameter: " + args[1]);
				System.err.println("Help exists for '-dp' (data parameters)");
				System.exit(1);
			}
		} else {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			System.out.println("Starting: " + df.format(new Date()));
			System.out.println("On machine " + java.net.InetAddress.getLocalHost().getHostName());
			long mxMem = (long) (Runtime.getRuntime().maxMemory() / 0.889d); // division is due to a bug in Java
			String mxMemStr = mxMem <= 1024L ? mxMem + "" : mxMem <= 1024L ? (mxMem / 1024L) + "k" 	: (mxMem / 1024L / 1024L) + "m";
			System.out.println("With max memory " + mxMemStr + "(" + mxMem + ")");
			long millis = System.currentTimeMillis();
			
			LearnerAttribute classAtt = null;
			RuleGenerator rg;
			rl = new InitializeParameters(args);
			RuleModel rm;
			Dataset testData;
			RuleLearner rlearner = new RuleLearner(rl.trainData, rl.learnerParams, rl.dataParams);
			if(rl.run()){
				rl.processLearnerParams();
				RuleLearnerMain rlm = new RuleLearnerMain(rl.learnerParams, rl.dataParams);
				
				Dataset d = rl.learnerParams.trainData;

				if (!d.isDiscretized() && d.numContinuousAttributes() > 0) {
					Util.discDataset(d, rl.learnerParams.getDiscretizerIndex(),
							rl.learnerParams.getDiscretizerValue());
				}
				
				AttributeList al = d.rulegenAttributeList();
				rg = new SAL(al, rl.learnerParams);
				RuleList rules = rg.generateRules();
				rm = new RuleModel(rl.learnerParams, rules);
				if (rm.getParameters() != null) {
					RuleList pr = rm.getParameters().getPriorRules();
				}

				System.out.println("Learned " + rm.getRules().size() + " rules\n");
				rlm.writeRules(rl.trainData.getFileName(), rm.getRules());
				rlm.writeDiscDataCSV(rl.trainData, "disc");
				ClassificationResult res;
				Dataset tstD;
				testData = rl.learnerParams.testData;
				if (testData != null) {
					tstD = testData;
					System.out.println("Result on test data:\n");
				} else {
					tstD = rl.trainData;
				}
				StringBuffer buf = new StringBuffer();
				buf.append(rm.toString() + "\n");
				System.out.println(buf);
				rlearner.setParameters(rl.learnerParams);
				rlearner.setModel(rm);
			}
			//rl.run();
			System.out.println("Total running time: " + util.Util.timeSince(millis));
		}
		System.exit(0);
	}
	
	

}
