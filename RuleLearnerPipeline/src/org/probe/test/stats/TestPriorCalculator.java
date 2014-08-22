package org.probe.test.stats;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.probe.data.DataModel;
import org.probe.data.FileDataManager;
import org.probe.data.FileType;
import org.probe.stats.PriorCalculator;
import org.probe.util.PrecisionFormatter;

public class TestPriorCalculator {

	@Test
	public void test() throws Exception{
		FileDataManager dataManager = new FileDataManager();
		dataManager.loadFromFile("Test//testDataFile.csv", FileType.CSV.getSeparator());
		
		DataModel dataModel = dataManager.getDataModel();
		
		double value = PriorCalculator.calculatePriorForClass(dataModel, "Pos");
		value = PrecisionFormatter.roundToNumDecimals(value, 3);
		
		assertTrue(Double.compare(0.667, value) == 0);
	}
}
