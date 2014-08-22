/*
 * Result.java
 *
 * Created on June 10, 2006, 7:14 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package structures.result;

import parameters.LearnerParameters;
import structures.learner.attribute.*;
//import corefiles.structures.learner.rule.*;
//import corefiles.structures.data.dataset.*;
import java.text.DecimalFormat;
//import corefiles.util.*;

/**
 * The Abstract Class for all results
 * 
 * @author Jonathan Lustgarten
 */
abstract class Result {
	protected double chiSqVal;
	protected int numCorrect; //
	protected int numIncorrect; //
	protected int numAbstentions; //
	protected int[][] conMatrix; //
	protected LearnerAttribute target;
	protected double accuracy;
	protected LearnerParameters lp;
	public final static DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.###");
	public final static DecimalFormat DECIMAL_FORMAT_PERCENT = new DecimalFormat("###.##");
	protected String[] attsUsed;

	public double getChiSq() {
		return chiSqVal;
	}

	public int getNumCorrect() {
		return numCorrect;
	}

	public int getNumIncorrect() {
		return numIncorrect;
	}

	public int[][] getConfusionMatrix() {
		return conMatrix;
	}

	public int getNumAbstentions() {
		return numAbstentions;
	}

	public LearnerParameters getLearnParams() {
		return lp;
	}
}