package org.probe.test.stats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.probe.data.DataAttribute;
import org.probe.data.DataModel;
import org.probe.data.DataSubModel;
import org.probe.data.FileDataManager;
import org.probe.data.util.DataSplitter;
import org.probe.stats.InformationGainCalculator;

import static org.junit.Assert.assertEquals;

public class TestInformationGainCalculator {

	@Before
	public void init(){
		expectedOrder.add("Urine Pushing");
		expectedOrder.add("Micturition pains");
		expectedOrder.add("Lumbar Pain");
		expectedOrder.add("Nausea");
		expectedOrder.add("Urethra Problems");
		expectedOrder.add("Temp");
	}
	
	@Test
	public void testInformationGainOnAllAtts() throws Exception {
		List<AttributeInfoGain> heap = new ArrayList<AttributeInfoGain>();

		FileDataManager dataManager = new FileDataManager();
		dataManager.loadFromFile("Test/id3dataset.csv", ",");
		DataModel dataModel = dataManager.getDataModel();

		List<DataAttribute> attributes = dataModel.getAttributes();
		for (DataAttribute attribute : attributes) {
			if (attribute.isClass() || attribute.isInstance())
				continue;

			DataSubModel[] subModels = DataSplitter.splitOnAttribute(dataModel,
					attribute);
			double informationGain = InformationGainCalculator
					.calculateInformationGainOnSubModels(dataModel, subModels);

			AttributeInfoGain infoGain = new AttributeInfoGain();
			infoGain.attrName = attribute.getAttributeName();
			infoGain.infoGain = informationGain;
			heap.add(infoGain);
		}

		Collections.sort(heap, new Comparator<AttributeInfoGain>() {
			@Override
			public int compare(AttributeInfoGain o1, AttributeInfoGain o2) {
				return Double.compare(o2.infoGain, o1.infoGain);
			}
		});
		
		for(int index = 0; index < heap.size(); index++){
			AttributeInfoGain infoGain = heap.get(index);
			System.out.println(infoGain.attrName + "--->" + infoGain.infoGain);
			
			String actualName = infoGain.attrName;
			String expectedName = expectedOrder.get(index);

			assertEquals("Unexpected Order of Attributes",expectedName, actualName);
		}
	}

	public class AttributeInfoGain implements Comparable<AttributeInfoGain> {
		public String attrName;
		public double infoGain;

		@Override
		public int compareTo(AttributeInfoGain obj) {
			if (this.infoGain > obj.infoGain)
				return 1;
			else if (this.infoGain < obj.infoGain)
				return -1;
			else
				return 0;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result
					+ ((attrName == null) ? 0 : attrName.hashCode());
			long temp;
			temp = Double.doubleToLongBits(infoGain);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			AttributeInfoGain other = (AttributeInfoGain) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (attrName == null) {
				if (other.attrName != null)
					return false;
			} else if (!attrName.equals(other.attrName))
				return false;
			if (Double.doubleToLongBits(infoGain) != Double
					.doubleToLongBits(other.infoGain))
				return false;
			return true;
		}

		private TestInformationGainCalculator getOuterType() {
			return TestInformationGainCalculator.this;
		}
	}
	
	private List<String> expectedOrder = new LinkedList<String>();
}
