package org.probe.test.data;

import org.junit.Before;
import org.junit.Test;
import org.probe.data.DataModel;
import org.probe.data.FileDataManager;
import org.probe.data.FileType;

public class TestDataManager {

	@Before
	public void init() {
		dataManager = new FileDataManager();
		try {
			dataManager.loadFromFile("Test//data.txt", FileType.TSV.getSeparator());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testLoadDataFromFiler() {
		FileDataManager dataManager = new FileDataManager();

		try {
			dataManager.loadFromFile("Test//sampleLearningData.csv", FileType.CSV.getSeparator());
		} catch (Exception e) {
			e.printStackTrace();
		}

		DataModel dataModel = dataManager.getDataModel();

	}
	
	private FileDataManager dataManager;
}