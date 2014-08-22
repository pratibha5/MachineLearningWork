/*
 * @(#)Relationship.java    1.1 02/01/21
 *
 */

package structures.constraints;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * This class represents the relationship part of a constraint (e.g. OR, <=)
 * 
 * The string representation of the objects left and right is compared against
 * the list of tags. As currently implemented, the left and right objects should
 * be strings.
 * 
 * @version 1.0 00/03/20
 * @author Jeremy Ludwig
 * 
 * Edited to make use of new code organization
 * 
 * @version 1.1 02/01/21
 * @author Will Bridewell
 */
public class Relationship implements Serializable {
	private String name;

	@SuppressWarnings("unchecked")
	protected Class rightClass;

	/**
	 * Default Constructor
	 */
	public Relationship() {
		name = new String();
		rightClass = String.class;
	}

	/**
	 * Creates a relationship with the name s. This name will be used to display
	 * this relationship in the constraint creation GUI.
	 */
	public Relationship(String s) {
		name = s;
		rightClass = String.class;
	}

	/**
	 * Create a list of the possible relationships.
	 * 
	 * NOTE: To add new relationships, simply create an object that extends
	 * Relationship and add it to the list below.
	 */
	public static ArrayList getRelationshipArray() {
		ArrayList relationships = new ArrayList();
		relationships.add(new AND());
		relationships.add(new OR());
		relationships.add(new XOR());
		relationships.add(new IFF());
		relationships.add(new IMPLIES());
		relationships.add(new LT());
		relationships.add(new EQ());
		relationships.add(new GT());
		relationships.add(new LE());
		relationships.add(new GE());

		return relationships;
	}

	/**
	 * Returns the name of this relationship
	 */
	public String toString() {
		return name;
	}

	/**
	 * Returns the type of object expected on the right hand side of the
	 * relationship. Currently, the type types are string and integer
	 */
	public Class getRightClass() {
		return rightClass;
	}

	/**
	 * Returns true if this relationship is satisfied
	 */
	public boolean satisfied(Object left, Object right, ArrayList tags) {
		return false;
	}

	/**
	 * Returns true if this relationship is unsatisfiable
	 */
	public boolean unsatisfiable(Object left, Object right, ArrayList tags) {
		return false;
	}

	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		out.writeObject(name);
		out.writeObject(rightClass);
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException {

		name = new String();
		name = (String) in.readObject();
		rightClass = (Class) in.readObject();
	}
}
