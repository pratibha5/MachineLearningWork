/*
 * ProcessParams.java
 *
 * Created on May 1, 2006, 12:12 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package parameters;

import parameters.DataParameters;
import parameters.IncorrectParameterException;
import parameters.LearnerParameters;
//import parameters.algorithm.bayes.structure.K2Parameters;
import parameters.PreProcessParameters;
import util.*;

/**
 * Process parameters for data load, preprocessing, and then rule learning
 * 
 * @author Jonathan
 */
public class ProcessParams {
	private DataParameters dataParams;
	private LearnerParameters learnerParams;
	private PreProcessParameters preProcParams;

	/** Creates a new instance of ProcessParams 
	 * @throws Exception */
	public ProcessParams(String[] args) throws Exception {

		int indexLP = Arrays.indexOf(args, "-lp");
		int indexPPP = Arrays.indexOf(args, "-ppp");
		int indexDP = Arrays.indexOf(args, "-dp");

		if (indexDP <= -1) {
			System.err.println("Please specify the data parameters.");
			System.exit(1);
		}
		if (indexLP > -1) {
			if (indexPPP > -1)
				learnerParams = new LearnerParameters(Arrays.subArray(args,
						indexLP + 1, indexPPP));
			else
				learnerParams = new LearnerParameters(Arrays.subArray(args,
						indexLP + 1, indexDP));
		}
		if (indexPPP > -1) {
			preProcParams = new PreProcessParameters();
			preProcParams.processArgs(Arrays.subArray(args, indexPPP + 1, indexDP));
		}
		dataParams = new DataParameters();
		dataParams.processArgs(Arrays.subArray(args, indexDP + 1, args.length));
	}

	public DataParameters getDataParams() {
		return dataParams;
	}

	public PreProcessParameters getPreProcParams() {
		return preProcParams;
	}

	public LearnerParameters getLearnParams() {
			return learnerParams;
	}
}
