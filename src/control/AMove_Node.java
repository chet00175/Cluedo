package control;
import java.awt.Point;

/**
 * Node class for the Algorithm AStar used to hold all the information from one node to the end node.
 */
public class AMove_Node implements Comparable<AMove_Node> {
	private AMove_Node next;
	private Point position;
	private double length;

	// Distance in Points form the start Point
	private int depth = 0;

	private double estimate;

	/**
	 * Given a next, child total length and estimate
	 * @param next - Point this came from
	 * @param Point - This Point
	 * @param totalLength
	 * @param estimate
	 */
	public AMove_Node(Point Point, double length, double estimate, AMove_Node next) {
		this.next = next;
		this.position = Point;
		this.length = length;
		this.estimate = estimate;

		this.setDepth(this.next != null ? this.next.getDepth()+1 : 0);
	}

	@Override
	public int compareTo(AMove_Node other) {

		int value = (int) Math.signum(this.getTotalLength() - other.getTotalLength());
		if( value == 0 ){
			// If the totals are the same
			// Return the lowest estimate
			return (int)(this.getLength() - other.getLength());
		}

		// Return who has the lowest estimate
		return value;
	}

	public AMove_Node getNext() {
		return next;
	}

	public void setNext(AMove_Node start) {
		this.next = start;
	}

	public Point getPoint() {
		return position;
	}

	public double getTotalLength() {
		return (length + estimate);
	}

	public double getLength() {
		return length;
	}

	public void setLength(double length) {
		this.length = length;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public double getEstimate() {
		return estimate;
	}

	public void setEstimate(double estimate) {
		this.estimate = estimate;
	}

	/**
	 * @return the size of the entire linked list
	 */
	public int getSize() {
		return next == null ? 1 : next.getSize() + 1;
	}

	public String toString(){
		return position.toString();
	}

	/**
	 * Returns the last elements in the linked list by iterating through the elements
	 * @return Last AMove_Node connected to the end of the list
	 */
	public AMove_Node getLast(){
		AMove_Node next = this;
		while( next.getNext() != null ){
			next = next.getNext();
		}

		return next;
	}
}
