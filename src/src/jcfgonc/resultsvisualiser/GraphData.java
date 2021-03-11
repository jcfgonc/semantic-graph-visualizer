package src.jcfgonc.resultsvisualiser;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import javax.swing.JComponent;

import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import graph.GraphReadWrite;
import graph.StringGraph;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import structures.DataType;

/**
 * Holds data/properties regarding a Semantic Graph (class StringGraph) and related GUI functions using GraphStream's API
 * 
 * @author jcfgonc@gmail.com
 *
 */
public class GraphData {

	private boolean selected;
	private StringGraph stringGraph;
	private String id;
	private Object2IntOpenHashMap<String> integerProperties;
	private Object2DoubleOpenHashMap<String> doubleProperties;
	private HashMap<String, String> stringProperties;
	private DualHashBidiMap<String, Integer> variable2columnNumber;
	private HashMap<String, DataType> variableTypes;
	private static final DecimalFormat df = new DecimalFormat("#.######");

	public GraphData(String id, StringGraph graph) {
		this.id = id;
		this.stringGraph = new StringGraph(graph);
		this.selected = false;
		this.integerProperties = new Object2IntOpenHashMap<String>();
		this.doubleProperties = new Object2DoubleOpenHashMap<String>();
		this.stringProperties = new HashMap<String, String>();
	}


	@Override
	public String toString() {
		return id;
	}

	public String getId() {
		return id;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean s) {
		// TODO move to GraphPanel
		selected = s;
//		if (isSelected()) {
//			multiGraph.setAttribute("ui.stylesheet", "graph { fill-color: rgba(192,224,255,128); }");
//		} else {
//			multiGraph.setAttribute("ui.stylesheet", "graph { fill-color: rgba(192,224,255,0); }");
//		}
	}

	public void toggleSelected() {
		// TODO move to GraphPanel
		setSelected(!isSelected());
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GraphData other = (GraphData) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public void createToolTipText(JComponent jc) {
		// n:time n:relationTypes n:relationTypesStd n:cycles n:patternEdges n:patternVertices n:matches s:query s:pattern s:conceptVarMap s:hash
		String text = "<html>";
		for (int id = 0; id < variable2columnNumber.size(); id++) { // use tsv/user order
			String var = variable2columnNumber.getKey(id);
			String value = "NULL";
			DataType dataType = variableTypes.get(var);
			switch (dataType) {
			case INTEGER:
				value = Integer.toString(integerProperties.getInt(var));
				break;
			case DOUBLE:
				value = df.format(doubleProperties.getDouble(var));
				break;
			case STRING:
				value = stringProperties.get(var);
				break;
			default:
				break;
			}
			text += String.format("%s:\t%s<br>", var, value); // var:\tvalue
		}
		jc.setToolTipText(text);
	}

	public void saveGraphCSV(String filename) throws IOException {
		GraphReadWrite.writeCSV(filename, stringGraph);
	}

	public StringGraph getStringGraph() {
		return stringGraph;
	}

	public int addIntegerProperty(String variable, int value) {
		return integerProperties.put(variable, value);
	}

	public double addDoubleProperty(String variable, double value) {
		return doubleProperties.put(variable, value);
	}

	public String addStringProperty(String variable, String value) {
		return stringProperties.put(variable, value);
	}

	public int getIntegerProperty(String variable) {
		return integerProperties.getInt(variable);
	}

	public double getDoubleProperty(String variable) {
		return doubleProperties.getDouble(variable);
	}

	public String getStringProperty(String variable) {
		return stringProperties.get(variable);
	}

	public Set<String> getIntegerVariables() {
		return Collections.unmodifiableSet(integerProperties.keySet());
	}

	public Set<String> getDoubleVariables() {
		return Collections.unmodifiableSet(doubleProperties.keySet());
	}

	public Set<String> getStringVariables() {
		return Collections.unmodifiableSet(stringProperties.keySet());
	}

	public void setVariable2ColumnNumber(DualHashBidiMap<String, Integer> variable2columnNumber) {
		this.variable2columnNumber = variable2columnNumber;
	}

	public void setVariableTypes(HashMap<String, DataType> variableTypes) {
		this.variableTypes = variableTypes;
	}

	public DataType getVariableType(String var) {
		return variableTypes.get(var);
	}

	public int getNumberOfVars() {
		return variable2columnNumber.size();
	}

	public int getColumnNumberFromVariable(String var) {
		return variable2columnNumber.get(var).intValue();
	}

	public String getVariableFromColumnNumber(int column) {
		return variable2columnNumber.inverseBidiMap().get(Integer.valueOf(column));
	}

	public boolean isVariableNumeric(String variable) {
		DataType type = variableTypes.get(variable);
		if (type == null) {
			throw new RuntimeException("variable " + variable + "does not exist");
		}
		if (type == DataType.DOUBLE || type == DataType.INTEGER)
			return true;
		return false;
	}

	public double getNumericVariable(String variable) {
		if (isVariableNumeric(variable)) {
			if (integerProperties.containsKey(variable))
				return integerProperties.getInt(variable);
			else if (doubleProperties.containsKey(variable))
				return doubleProperties.getDouble(variable);
			throw new RuntimeException("variable " + variable + "does not exist");
		}
		throw new RuntimeException("variable " + variable + "is not numeric");
	}

	public boolean isVariableString(String variable) {
		DataType type = variableTypes.get(variable);
		if (type == null) {
			throw new RuntimeException("variable " + variable + "does not exist");
		}
		return (type == DataType.STRING);
	}

}
