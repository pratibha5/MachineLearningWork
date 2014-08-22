package org.probe.report;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.logging.Level;

import org.probe.data.log.RLFileLogger;
import org.probe.data.log.RLLogger;
import org.probe.rule.RuleModel;

public class FileReportManager implements ReportManager {

	public FileReportManager(String dirLocation) {
		this.dirLocation = dirLocation;
		
		File file = new File(dirLocation);
		file.mkdir();
	}

	@Override
	public void saveSrc(File src) {
		saveToDirectory(src);
	}

	@Override
	public void saveTarget(File target) {
		saveToDirectory(target);
	}

	@Override
	public void saveTest(File test) {
		saveToDirectory(test);
	}

	@Override
	public void writeRuleModelToFile(RuleModel ruleModel, String fileName) {
		String message = RuleModelFormatter.format(ruleModel);
		createNewFileInDirectory(fileName, message);
	}

	private void createNewFileInDirectory(String name, String message) {
		try {
			StringBuilder sb = new StringBuilder();
			sb.append(dirLocation).append("//").append(name);
			File file = new File(sb.toString());

			PrintWriter pw = new PrintWriter(file);

			pw.write(message + "\n");
			pw.close();

		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage());
		}
	}

	private void saveToDirectory(File file) {
		String fileName = file.getName();
		StringBuilder sb = new StringBuilder();
		sb.append(dirLocation).append("//").append(fileName);

		File newFile = new File(sb.toString());

		copyFile(file, newFile);
	}

	private void copyFile(File from, File to) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(from));
			PrintWriter pr = new PrintWriter(to);

			String line = "";
			while ((line = br.readLine()) != null) {
				pr.write(line);
				pr.write("\n");
			}

			pr.close();
			br.close();
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage());
		}
	}

	private final String dirLocation;
	private final RLLogger LOGGER = RLFileLogger.getLogger();
}
