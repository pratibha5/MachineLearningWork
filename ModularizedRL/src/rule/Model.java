/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rule;

import parameters.LearnerParameters;

/**
 * 
 * @author Jonathan Lustgarten
 */
public interface Model {

	public LearnerParameters getParameters();

	public String toString();
}
