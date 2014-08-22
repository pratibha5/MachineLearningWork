/*
 * ChiTrim.java
 *
 * Created on February 17, 2005, 11:35 AM
 */

package preprocess;

//import java.lang.*;
//import java.util.*;
//import java.io.*;
//import corefiles.structures.data.dataset.attribute.*;
import data.discretize.Discretizers;
import data.dataset.*;
import util.*;

/**
 * 
 * @author Jonathan
 */
public class ChiTrim {
	private double thresh;
	private Dataset data;

	/** Creates a new instance of ChiTrim */
	public ChiTrim(Dataset d, double threshold) {
		data = d;
		thresh = threshold;
	}

	public int[] trim() {
		System.out.println("Performing Chi-square trim with threshold of " + thresh);
		Discretizers disc = new Discretizers();
		disc.setData(data);
		disc.setMehtod(Discretizers.EBD, 1);
		int[] removeAttrarr = new int[0];
		System.out.print("Processing: ");
		double[][] attvs = data.attributeValuesWithRepValues();
		for (int i = 0; i < data.numAttributes(); i++) {
			if (i % data.numAttributes() / 10 == 0)
				System.out.print(".");
			if (i == data.classAttIndex() || i == data.getIdAttIndex())
				continue;
			else {
				Attribute att;
				try {
					att = data.attribute(i);
				} catch (AttributeDoesNotExistException e) {
					System.err.println(e.getLocalizedMessage());
					e.printStackTrace();
					continue;
				}
				/*BayesAttribute ba = new BayesAttribute(data.classAttribute());
				ba.addParent(att, data, attvs);
				if (MathUtil.chiSq(ba.getCPT()) <= thresh + 0.05)
					removeAttrarr = Arrays.append(removeAttrarr, att.position());
				ba = null;*/
			}
		}
		System.out.println("\nDone processing");
		disc = null;
		return removeAttrarr;
	}
}