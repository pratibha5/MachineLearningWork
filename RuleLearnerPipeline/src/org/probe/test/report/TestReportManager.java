package org.probe.test.report;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;

import org.junit.Before;
import org.junit.Test;
import org.probe.report.FileReportManager;
import org.probe.report.ReportManager;
import org.probe.rule.RuleOG;
import org.probe.rule.RuleModel;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class TestReportManager {
	
	@Before
	public void setup() {
		
	}

	@Test
	public void testSaveFile() {
		final String testStr = "THIS IS A TEST";
		final String testFileName = "testFile.txt";
		
		File file = new File(testFileName);
		writeTxtToFile(file, testStr);
		
		reportManager.saveSrc(file);
		
		File writtenFile = new File(DIR + "//" + testFileName);
		assertTrue(writtenFile.exists());

		String writtenTxt = readTxtFromFile(writtenFile);
		assertEquals(testStr,writtenTxt);
	}
	
	@Test
	public void testWriteRuleOGModel(){
		String reportName = "report.RuleOGs";
		
		String RuleOGStr1 = "((a>500)(b<300))->(class=1)";
		RuleOG RuleOG1 = RuleOG.parseString(RuleOGStr1);
		
		String RuleOGStr2 = "((a<500)(b<300))->(class=2)";
		RuleOG RuleOG2 = RuleOG.parseString(RuleOGStr2);
		
		RuleModel RuleOGModel = new RuleModel();
		RuleOGModel.addRule(RuleOG1);
		RuleOGModel.addRule(RuleOG2);
		
		reportManager.writeRuleModelToFile(RuleOGModel, reportName);
		
		File writtenFile = new File(DIR + "//" + reportName);
		assertTrue(writtenFile.exists());

		String writtenTxt = readTxtFromFile(writtenFile);
		assertEquals(RuleOGModel.toString(),writtenTxt);
	}
	
	private String readTxtFromFile(File file){
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			
			String line = "";
			StringBuilder sb = new StringBuilder();
			while((line = br.readLine()) != null){
				sb.append(line).append("\n");
			}
			br.close();
			
			return sb.toString().trim();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return null;
	}
	
	private void writeTxtToFile(File file, String txt){
		try {
			PrintWriter pw = new PrintWriter(file);
			pw.write(txt);
			pw.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private final String DIR = "Test//testoutput";
	private ReportManager reportManager = new FileReportManager(DIR);
}
