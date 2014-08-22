package org.probe.data.discretize;

import java.util.List;

import org.probe.data.DataAttribute;
import org.probe.data.DataModel;

public interface BinGenerator {
	List<String> generate(DataModel dataModel, DataAttribute attribute, int numBins) throws Exception;
	List<String> generate(DataModel dataModel, DataAttribute attribute) throws Exception;
}
