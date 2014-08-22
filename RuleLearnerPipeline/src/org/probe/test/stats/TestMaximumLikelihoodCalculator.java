package org.probe.test.stats;

import org.junit.Test;
import org.probe.data.DataAttribute;
import org.probe.data.DataModel;
import org.probe.data.FileDataManager;
import org.probe.data.FileType;
import org.probe.stats.MaximumLikelihoodCalculator;
import org.probe.util.PrecisionFormatter;

import static org.junit.Assert.assertTrue;

public class TestMaximumLikelihoodCalculator {
	
	@Test
	public void testWithColumnIndex() throws Exception{
		FileDataManager dataManager = new FileDataManager();
		dataManager.loadFromFile("Test//testDataFile.csv", FileType.CSV.getSeparator());
		
		DataModel dataModel = dataManager.getDataModel();
	
		double value = MaximumLikelihoodCalculator.caclulateMaximumLikelihood(dataModel, 2, "Up", "Pos");
		value = PrecisionFormatter.roundToNumDecimals(value, 3);
		
		assertTrue(Double.compare(0.667, value) == 0);
	}
	
	@Test
	public void testWithColumnName() throws Exception{
		FileDataManager dataManager = new FileDataManager();
		dataManager.loadFromFile("Test//testDataFile.csv", FileType.CSV.getSeparator());
		
		DataModel dataModel = dataManager.getDataModel();
		DataAttribute attribute = dataModel.getAttributes().get(3);
	
		double value = MaximumLikelihoodCalculator.caclulateMaximumLikelihood(dataModel, attribute, "Up", "Pos");
		value = PrecisionFormatter.roundToNumDecimals(value, 3);
		
		assertTrue(Double.compare(0.333, value) == 0);
	}
}
