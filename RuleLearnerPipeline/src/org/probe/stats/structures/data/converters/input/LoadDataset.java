package org.probe.stats.structures.data.converters.input;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.probe.data.dataset.DefaultDataModel;

import data.dataset.*;

public interface LoadDataset {
	public DefaultDataModel loadData() throws IncompatibleDatatypeException,
			FileNotFoundException, IOException, Exception;

	public DefaultDataModel loadData(String sep) throws IncompatibleDatatypeException,
			FileNotFoundException, IOException, Exception;
}
