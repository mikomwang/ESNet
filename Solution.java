import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * @author Michael Wang
 * ESNet Coding Challenge
 */
public class Solution {

	public static void main(String[] args) {
		HashMap<String, Long> inputFiles;
		HashMap<String, Long> tempFiles;
		ArrayList<Node> inputNodes;
		ArrayList<String> commands = new ArrayList<String>();
		String[] files; 
		
		for (String arg: args) {
			commands.add(arg);
		}
		files = parseFiles(commands); 
		inputFiles = extractFiles(files[0]);
		inputNodes = extractNodes(files[1]);
		Node nullNode = new Node(0, "NULL");
		tempFiles = sortFiles(inputFiles, inputNodes, nullNode);
		redistribute(tempFiles, inputNodes, nullNode);
		inputNodes.add(nullNode);
		printFiles(inputNodes, files[2]);
	}
	
	/** Prints a brief description of how to run the program on the command line
	 * Includes commands: -f [FILE INPUTS FILENAME]
	 * 			 		  -n [NODE INPUTS FILENAME]
	 * 			 	      -o [OUTPUT FILE]
	 * 			 		  -h "help" (prints usage)
	 */
	public static void usage() {
		System.out.println("The program should be run with the following options:\n"
				+ "-f [FILE INPUTS FILENAME]. This is a required file.\n"
				+ "-n [NODE INPUTS FILENAME]. This is a required file.\n"
				+ "-o [OUTPUT FILE FILENAME]. If an output file is not provided, the solution\n"
				+ "will be printed to the console.\n"
				+ "-h Prints usage information to standard error and stop."
				+ "\n\n"
				+ "Put the arguments in the following format:\n"
				+ "java Solution [OPTIONS]");
		System.exit(1);
	}
	
	/** Prints each file either to the -o file or the console's standard output. */
	public static void printFiles(ArrayList<Node> nodes, String output) {
		if (output != null) {
			PrintWriter writer = null;
			try {
				File file = new File(output);
				if (file.exists() && file.isFile()) {
					writer = new PrintWriter(output, "UTF-8");
				} else {
					System.out.println("ERROR: Output file not found!\n");
					usage();
				}
			} catch (FileNotFoundException e) {
				System.out.println("ERROR: Output file not found!\n");
				usage();
			} catch (UnsupportedEncodingException e) {
				System.out.println("ERROR: Unsupported Encoding Exception!\n");
				usage();
			}
			for (Node node: nodes) {
				for (String file: node.nodeFiles.keySet()) {
					writer.println(file + " " + node.nodeName);
				}
			}
			writer.close();
		} else {
			for (Node node: nodes) {
				for (String file: node.nodeFiles.keySet()) {
					System.out.println(file + " " + node.nodeName);
				}
			}
		}
	}
	
	/** Second pass through the files. Optimizes the file distribution onto
	 *  the nodes. */
	public static void redistribute(HashMap<String, Long> temp, ArrayList<Node> nodes, Node nullNode) {
		for (Node node: nodes) {
			findBestSubset(node, temp);
		}
		for (Map.Entry<String, Long> file: temp.entrySet()) {
			nullNode.update(file.getKey(), file.getValue());
		}
	}
	
	/** Helper method which moves all files on the node to the temporary
	 * array in order to help set up the call to optimize. */
	public static void findBestSubset(Node node, HashMap<String, Long> temp) {
		for (Map.Entry<String, Long> entry: node.nodeFiles.entrySet()) {
			String fileName = entry.getKey();
			Long fileSize = entry.getValue();
			temp.put(fileName, fileSize);
		}
		optimize(node, temp);
		// check files in node to see if they're in the optimize
		// if not remove, if they are leave it in there. add the new temp files
		// remove the added files from temp.
	}
	
	/** Optimizes the file placement from the nodes, seeing if there are better
	 * options in the temporary array which holds files that couldn't be placed 
	 * on nodes the first pass due to space limitations. */
	public static void optimize(Node node, HashMap<String, Long> files) {
		int generationSize = files.size();
		int counter = 0;
		long minRange = node.sizeUsed; 
		long maxRange = node.nodeSize;
		long optimalValue = -1;
		ArrayList<String> optimalFiles = null;
		HashMap<Long, ArrayList<String>> generations = new HashMap<Long, ArrayList<String>>();
		generations.put((long) 0, new ArrayList<String>());
		while (counter < generationSize) {
			generations = genWalker(generations, files);
			
			counter += 1;
		}
		for (Map.Entry<Long, ArrayList<String>> generation: generations.entrySet()) {
			if (generation.getKey() > minRange && generation.getKey() <= maxRange) {
				optimalValue = generation.getKey();
			}
		}
		
		if (optimalValue != -1) {
			optimalFiles = generations.get(optimalValue);
			rearrange(files, node, optimalFiles);
		}
	}
	
	/** Move files from our TEMP files based on most optimal for NODE size
	 *  used and NODE size left. */
	public static void rearrange(HashMap<String, Long> temp, Node node, ArrayList<String> optimal) {
		ArrayList<String> toRemove = new ArrayList<String>();
		ArrayList<String> toErase = new ArrayList<String>();
		for (String file: node.nodeFiles.keySet()) {
			if (!optimal.contains(file)) {
				long fileValue = temp.get(file);
				toErase.add(file);
			}
		}
		for (String fileName: toErase) {
			node.nodeFiles.remove(fileName);
		}
		for (Map.Entry<String, Long> tempFile: temp.entrySet()) {
			if (node.nodeFiles.containsKey(tempFile.getKey())) {
				toRemove.add(tempFile.getKey());
			} else if (optimal.contains(tempFile.getKey())) {
				node.update(tempFile.getKey(), tempFile.getValue());
				toRemove.add(tempFile.getKey());
			}
		}
		for (String fileName: toRemove) {
			temp.remove(fileName);
		}
	}
	
	/** Helper function to walk through generations and accumulate best fitting sum
	 * that does not exceed the target node size but can possibly be greater than the
	 * current nodes.
	 */
	public static HashMap<Long, ArrayList<String>> genWalker(HashMap<Long, ArrayList<String>> generationSubsets, 
			HashMap<String, Long> files) {
		long sum;
		ArrayList<String> newAdditions;
		HashMap<Long, ArrayList<String>> currentGen = new HashMap<Long, ArrayList<String>>(generationSubsets);
		for (Map.Entry<Long, ArrayList<String>> generation : currentGen.entrySet()){  
			for (Map.Entry<String, Long> file: files.entrySet()) {
				newAdditions = new ArrayList<String>();
				sum = file.getValue() + generation.getKey();
				if (generationSubsets.containsKey(sum)) {
					continue;
				} else if (generation.getValue().contains(file.getKey())) {
					continue;
				}
				else {
					for (String newfile: generation.getValue()) {
						newAdditions.add(newfile);
					}
					newAdditions.add(file.getKey());
					generationSubsets.put(sum, newAdditions);
				}
			}
			
		}
		
		return generationSubsets;
	}
			
	
	/** Places each item in FILES onto their corresponding NODES. Returns a temporary
	 * array containing all the files that have the potential to be placed on a node. */
	public static HashMap<String, Long> sortFiles(HashMap<String, Long> files, ArrayList<Node> nodes, Node nullNode) {
		int numFiles = files.size();
		HashMap<String, Long> tempFiles = new HashMap<String, Long>();
		long maxNodeSize = 0;
		for (Node node: nodes) {
			if (node.sizeLeft > maxNodeSize) {
				maxNodeSize = node.sizeLeft;
			}
		}
		for (int counter = 0; counter < numFiles; counter += 1) {
			String fileName = findLargestFile(files);
			long fileSize = files.get(fileName);
			Node node = findNode(nodes);
			if (node.canAdd(fileSize)) {
				node.update(fileName, fileSize);
			} else {
				if (fileSize > maxNodeSize) {
					nullNode.update(fileName, 0);
				} else {
					tempFiles.put(fileName, fileSize);
				}
			}
			
			files.remove(fileName);
			
		}
		return tempFiles;
	}
	
	/** Helper method to help determine the appropriate node. */
	public static Node findNode(ArrayList<Node> nodes) {
		Node targetNode = null;
		ArrayList<Node> temporaryNodeArray = new ArrayList<Node>();
		long currMinSizeUsed = Long.MAX_VALUE;
		long currMaxSizeLeft = Long.MIN_VALUE;
		for (Node node1: nodes) {
			if (node1.sizeUsed < currMinSizeUsed) {
				currMinSizeUsed = node1.sizeUsed;
			}
		} 
		for (Node node2: nodes) {
			if (node2.sizeUsed == currMinSizeUsed) {
				temporaryNodeArray.add(node2);
			}
		}
		for (Node node3: temporaryNodeArray) {
			if (node3.sizeLeft > currMaxSizeLeft) {
				targetNode = node3;
				currMaxSizeLeft = node3.sizeLeft;
			}
		}
		
		
		return targetNode;
	}
	
	/** Helper method to help determine the largest file. */
	public static String findLargestFile(HashMap<String, Long> files) {
		String largestFile = "";
		long largestSize = 0;
		for (String file: files.keySet()) {
			long temp = files.get(file);
			if (temp >= largestSize) {
				largestSize = temp;
				largestFile = file;
			}
		}
		return largestFile;
	}
	
	
	
	/** Extract the information from a given node file using its FILENAME, creates
	 *  a Node object for each node, and places the the nodes into an arraylist
	 *  for easy access and retrieval. */
	public static ArrayList<Node> extractNodes(String fileName) {
		ArrayList<Node> nodeList = new ArrayList<Node>();
		File file = new File(fileName);
		Scanner s;
		try {
			s = new Scanner(file);
			while (s.hasNextLine()) {
				String name = s.next();
				if (name.trim().startsWith("#")) {
					s.nextLine();
					continue;
				}
				long size = s.nextLong();
				Node node = new Node(size, name);
				nodeList.add(node);
				if (size < 0) {
					System.out.println("ERROR: Can't have negative values for file or node sizes!");
					usage();
				}
			}
		} catch (FileNotFoundException e) {
			System.out.println("ERROR: File not found!");
			usage();
		}
		return nodeList;
		
	}
	
	/** Extract the information from the given text files using its
	 * FILENAME into a HashMap for easier access and retrieval. */
	public static HashMap<String, Long> extractFiles(String fileName){
		HashMap<String, Long> fileMap = new HashMap<String, Long>();
		File file = new File(fileName);
		Scanner s;
		try {
			s = new Scanner(file);
			while (s.hasNextLine()) {
				String name = s.next();
				if (name.trim().startsWith("#")) {
					s.nextLine();
					continue;
				}
				long size = s.nextLong();
				fileMap.put(name, size);
				if (size < 0) {
					System.out.println("ERROR: Can't have negative values for file or node sizes!");
					usage();
				}
			}
		} catch (FileNotFoundException e) {
			System.out.println("ERROR: File not found!");
			usage();
		}

		return fileMap;
	}
	
	
	/** Sort through COMMANDS and place the files found into a string array.
	 *  The -f file will be located at index 0.
	 *  The -n file will be located at index 1.
	 *  The -o file will be located at index 2. (if found!). */
	public static String[] parseFiles(ArrayList<String> commands) {
		if (commands.get(0).equals("-h")) {
			usage();
		}
		if (commands.size() < 4 || !commands.contains("-f") || !commands.contains("-n")) {
			System.out.println("ERROR: Missing a required file!\n");
			usage();
		}
		String[] files = new String[3];
		int numargs = 0;
		int index = 0;
		int maxsize = commands.size() - 1;
		for (String arg: commands) {
			switch (arg) {
			case "-f":
				index += 1;
				if (index > maxsize) {
					System.out.println("ERRROR: Invalid command line format!\n");
					usage();
				}
				files[0] = commands.get(index);
				break;
			case "-n":
				index += 1;
				if (index > maxsize) {
					System.out.println("ERROR: Invalid command line format!\n");
					usage();
				}
				files[1] = commands.get(index);
				break;
			case "-o": 
				index += 1;
				if (index > maxsize) {
					System.out.println("ERROR: Invalid command line format!\n");
					usage();
				}
				files[2] = commands.get(index);
				break;
			default:
				index += 1;
				break;
			}
			numargs += 1;
		}
		if (numargs > 4 && !commands.contains("-o")) {
			System.out.println("ERROR: Too many arguments!\n");
			usage();
		} else if (numargs > 6 && commands.contains("-o")) {
			System.out.println("ERROR: Too many arguments!\n");
			usage();
		}
		return files;
	}

}
