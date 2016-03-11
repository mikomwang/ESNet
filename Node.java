import java.util.ArrayList;
import java.util.HashMap;


/**
 * 
 * @author Michael Wang
 * ESNet Coding Challenge
 */

/** Node Class. */
public class Node {
	
	/** Node object constructor which initializes a new node given an 
	 * INITIALSIZE and a node NAME. */
	public Node(long initialSize, String name) {
		nodeSize = initialSize;
		sizeLeft = initialSize;
		sizeUsed = 0;
		nodeName = name;
		nodeFiles = new HashMap<String, Long>();
		
	}
	
	/** Updates our node by attaching a FILE to the node and increases the 
	 * size used parameter in the node by VALUE, while also decreasing the
	 * size left on the current node by VALUE. */
	public void update(String file, long value) {
		sizeUsed += value;
		sizeLeft -= value;
		nodeFiles.put(file, value);
	}
	
	public void remove(String file, long value) {
		sizeUsed -= value;
		sizeLeft += value;
		nodeFiles.remove(file);
	}
	
	public boolean canAdd(long value) {
		if (value > sizeLeft) {
			return false;
		}
		return true;
	}
	
	
	/** Class Attributes */
	public long nodeSize;
	public long sizeLeft;
	public long sizeUsed;
	public String nodeName;
	public HashMap<String, Long> nodeFiles;
}
