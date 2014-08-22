package structures.learner.SAL;

//import structures.learner.attribute.HNode;
import rule.Conjunct;

/**
 * Encapsulates information about a target attribue and its values.
 * 
 * version 1.0 Jonathan Lustgarten Changed it so instead of comparing what type
 * of node, actually using the number of children the node has to determine
 * whether or not it is a leaf node.
 */
public class TargetValueCounter {
	/**
	 * Represents the target name-value pair of this target value extension.
	 */
	private Conjunct target;

	public int targetCount; // FIXME document!

	/**
	 * Is the target value of this extension a leaf in the target attribute's
	 * value hierarchy?
	 */
	private boolean isLeaf;

	/**
	 * Constructs a target value extension for conjunct <code>c</code>
	 * 
	 * @param c
	 *            the underlying conjunct of this target value extension.
	 */
	public TargetValueCounter(Conjunct c) {
		target = c;
		isLeaf = (c.getValue().getChildCount() == 0);
		targetCount = 0;
	}

	/**
	 * Returns the target attribute name-value pair as a conjunct.
	 */
	public Conjunct getConjunct() {
		return target;
	}

	/**
	 * Returns <code>true</code> if this target value is a leaf in the value
	 * hierarchy, <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if this target value is a leaf in the value
	 *         hierarchy
	 */
	public boolean isLeaf() {
		return isLeaf;
	}
}
