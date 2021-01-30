package src.jcfgonc.resultsvisualiser;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import graph.GraphReadWrite;
import graph.StringGraph;
import structures.DataType;
import structures.Ticker;
import utils.NonblockingBufferedReader;
import utils.VariousUtils;

/**
 * Reads a TSV (tab separated values) file and creates a List of GraphData instances, one for each row of the file (excluding the first row which is the required header).
 * 
 * @author jcfgonc@gmail.com
 *
 */
public class GraphDataRead {

	public static ArrayList<GraphData> readTSV(String filename) throws IOException {
		System.out.print("loading " + filename + " ...");
		Ticker t = new Ticker();
		ArrayList<GraphData> graphs = new ArrayList<>(1 << 16);
		NonblockingBufferedReader br = new NonblockingBufferedReader(filename);
		HashMap<String, DataType> variableTypes = null;
		DualHashBidiMap<String, Integer> variable2columnNumber = null;
		// the first graph cell that shows up will be the main one (to be compatible with the pattern miner's output)
		String graphVariable = null;
		boolean headerRead = false;
		String line;
		int rowNumber = 0;
		while ((line = br.readLine()) != null) {
			String[] tokens = VariousUtils.fastSplit(line, '\t');
			if (!headerRead) {
				headerRead = true;
				variableTypes = createDataTypes(tokens);
				variable2columnNumber = createVariableIds(tokens);
				graphVariable = getGraphVariable(tokens);
			} else {
				GraphData gd = createGraphData(rowNumber, tokens, variableTypes, variable2columnNumber, graphVariable);
				graphs.add(gd);
				rowNumber++;
			}
		}
		// no more data to read
		br.close();
		System.out.println("done. Took " + t.getElapsedTime() + " seconds.");
		return graphs;
	}

	private static GraphData createGraphData(int rowNumber, String[] cells, HashMap<String, DataType> variableTypes,
			DualHashBidiMap<String, Integer> variable2columnNumber, String graphVariable) throws NoSuchFileException, IOException {
		// store the graph itself
		String graph_str = cells[variable2columnNumber.get(graphVariable)];
		StringGraph graph = GraphReadWrite.readCSVFromString(graph_str);
		// create class to hold data
		GraphData gd = new GraphData(Integer.toString(rowNumber), graph);
		gd.setVariable2ColumnNumber(variable2columnNumber);
		gd.setVariableTypes(variableTypes);
		// add remaining variables/properties
		Set<String> variables = variable2columnNumber.keySet();
		for (String variable : variables) {
			DataType vartype = variableTypes.get(variable);
			int varId = variable2columnNumber.get(variable).intValue();
			String value = cells[varId];
			switch (vartype) {
			case GRAPH: {
				// only one variable is stored as graph (the one above)
				break;
			}
			case INTEGER: {
				gd.addIntegerProperty(variable, (int) Double.parseDouble(value));
				break;
			}
			case DOUBLE: {
				gd.addDoubleProperty(variable, Double.parseDouble(value));
				break;
			}
			case STRING: {
				gd.addStringProperty(variable, value);
				break;
			}
			case UNDEFINED:
				break;
			default:
				break;
			}
		}
		return gd;
	}

	private static String getGraphVariable(String[] cells) {
		for (String cell : cells) {
			// each cell to be of the form x:y with x=datatype and y=dataname
			String[] tokens = VariousUtils.fastSplit(cell, ':');
			String type_s = tokens[0];
			String varname = tokens[1];
			if (type_s.equals("g"))
				return varname;
		}
		return null;
	}

	private static DualHashBidiMap<String, Integer> createVariableIds(String[] cells) {
		DualHashBidiMap<String, Integer> var2columnId = new DualHashBidiMap<>();
		for (int i = 0; i < cells.length; i++) {
			String cell = cells[i];
			String[] tokens = VariousUtils.fastSplit(cell, ':');
			// String type_s = tokens[0];
			String varname = tokens[1];
			var2columnId.put(varname, i);
		}
		return var2columnId;
	}

	private static HashMap<String, DataType> createDataTypes(String[] cells) {
		HashMap<String, DataType> types = new HashMap<>();
		for (String cell : cells) {
			// each cell to be of the form x:y with x=datatype and y=dataname
			String[] tokens = VariousUtils.fastSplit(cell, ':');
			String type_s = tokens[0];
			String varname = tokens[1];
			DataType type;
			switch (type_s) {
			case "d": {
				type = DataType.INTEGER;
				break;
			}
			case "f": {
				type = DataType.DOUBLE;
				break;
			}
			case "s": {
				type = DataType.STRING;
				break;
			}
			case "g": {
				type = DataType.GRAPH;
				break;
			}
			default:
				// if not defined (but there is something) store it as a string
				type = DataType.STRING;
			}
			types.put(varname, type);
		}
		return types;
	}

	public static void main(String[] args) throws IOException {
		String filename = "C:\\Desktop\\github\\BlenderMO\\moea_results_2021-01-26_04-16-50.tsv";
		while (true)
			readTSV(filename);
	}

}
