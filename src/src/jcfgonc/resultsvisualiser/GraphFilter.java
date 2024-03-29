package src.jcfgonc.resultsvisualiser;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;

import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;

public class GraphFilter {
	/**
	 * list with the n (user specified) graphs shown in the GUI/graph panel
	 */
	private ArrayList<GraphData> visibleGraphList;
	/**
	 * original unmodified graph list
	 */
	private ArrayList<GraphData> originalGraphList;
	/**
	 * copy of the graph list which is progressively edited (compared to the
	 * original list it has graphs removed and it is user-sorted)
	 */
	private ArrayList<GraphData> graphList;
	/**
	 * number of visible graphs
	 */
	private int numberVisibleGraphs = 0;
	/**
	 * graphs which are currently selected (and highlighted)
	 */
	private HashSet<GraphData> selectedGraphs;
	/**
	 * graphs which the user marked as deleted
	 */
	private HashSet<GraphData> deletedGraphs;
	/**
	 * maps the minimum (for all the loaded graphs) of a variable to its name
	 */
	private Object2DoubleOpenHashMap<String> minimumOfVariable;
	/**
	 * maps the maximum (for all the loaded graphs) of a variable to its name
	 */
	private Object2DoubleOpenHashMap<String> maximumOfVariable;
	private Object2DoubleOpenHashMap<String> lowHighVariableDifference;
	private Object2DoubleOpenHashMap<String> variableFilterLow;
	private Object2DoubleOpenHashMap<String> variableFilterHigh;

	public GraphFilter(int numberShownGraphs, String graphFilepath) throws IOException {
		this.visibleGraphList = new ArrayList<>();
		this.selectedGraphs = new HashSet<>();
		this.deletedGraphs = new HashSet<GraphData>();
		this.minimumOfVariable = new Object2DoubleOpenHashMap<>();
		this.maximumOfVariable = new Object2DoubleOpenHashMap<>();
		this.lowHighVariableDifference = new Object2DoubleOpenHashMap<>();
		this.variableFilterLow = new Object2DoubleOpenHashMap<>();
		this.variableFilterHigh = new Object2DoubleOpenHashMap<>();

		this.originalGraphList = GraphDataRead.readTSV(graphFilepath);

		this.graphList = new ArrayList<>(originalGraphList);

		if (!graphList.isEmpty()) {
			setNumberVisibleGraphs(numberShownGraphs);
		}
	}

	/**
	 * returns the number of edited (added/removed) graphs
	 * 
	 * @return
	 */
	public int getNumberOfGraphs() {
		return graphList.size();
	}

	/**
	 * returns the number of graphs in the visible list
	 * 
	 * @return
	 */
	public int getNumberOfVisibleGraphs() {
		return visibleGraphList.size();
	}

	/**
	 * returns the visible graph list SAFE
	 * 
	 * @return
	 */
	public List<GraphData> getVisibleGraphList() {
		return Collections.unmodifiableList(visibleGraphList);
	}

	/**
	 * returns the list of all loaded graphs SAFE
	 * 
	 * @return
	 */
	public List<GraphData> getFullGraphList() {
		return Collections.unmodifiableList(originalGraphList);
	}

	/**
	 * true if there are graphs in the visible list, false otherwise
	 * 
	 * @return
	 */
	public boolean hasVisibleGraphs() {
		return !visibleGraphList.isEmpty();
	}

	/**
	 * sets the number of graphs in the visible list, deleting or copying from the
	 * loaded list as needed. TESTED, OK.
	 * 
	 * @param num
	 */
	public void setNumberVisibleGraphs(int num) {
		int numOriginal = getNumberOriginalGraphs();
		if (num > numOriginal) {
			num = numOriginal;
		}

		numberVisibleGraphs = num;
		clearAndRefillVisibleGraphList();
	}

	/**
	 * returns the number of loaded (original) graphs
	 * 
	 * @return
	 */
	public int getNumberOriginalGraphs() {
		return originalGraphList.size();
	}

	private void clearAndRefillVisibleGraphList() {
		visibleGraphList.clear();
		fillVisibleList();
	}

	/**
	 * inserts graphs into the visible list from the graphList until the visible
	 * list reaches 'numberVisibleGraphs' elements. TESTED, SEEMS OK.
	 */
	private void fillVisibleList() {
		Iterator<GraphData> graphListIterator = graphList.iterator();
		while (visibleGraphList.size() < numberVisibleGraphs && graphListIterator.hasNext()) {
			GraphData gd = graphListIterator.next();
			if (deletedGraphs.contains(gd))
				continue;
			visibleGraphList.add(gd);
		}
	}

	public void operatorFilterGraphs() {
		if (variableFilterLow.isEmpty() && variableFilterHigh.isEmpty())
			return;
		graphList.clear();
		// for each graphdata...
		outer: for (GraphData gd : originalGraphList) {
			if (deletedGraphs.contains(gd))
				continue;
			// ...check if each variable is within range
			for (String variable : variableFilterLow.keySet()) {
				double value = gd.getNumericVariable(variable);
				final double tol = 0.0001;
				double low = variableFilterLow.getDouble(variable);
				double high = variableFilterHigh.getDouble(variable);
				boolean check = value >= low - tol && value <= high + tol;
				if (!check)
					continue outer;
			}
			graphList.add(gd);
		}
		clearAndRefillVisibleGraphList();
		operatorClearSelection();
	}

	public void setGraphFilter(String variable, double lowValue, double highValue) {
		variableFilterLow.put(variable, lowValue);
		variableFilterHigh.put(variable, highValue);
	}

	public void operatorSortGraphs(String variableName, boolean sortAscending) {
		// sort the full list and then copy to the visible list
		graphList.sort(new Comparator<GraphData>() {

			@Override
			public int compare(GraphData o1, GraphData o2) {
				int comp = 0;
				if (o1.isVariableNumeric(variableName)) {
					double v1 = o1.getNumericVariable(variableName);
					double v2 = o2.getNumericVariable(variableName);
					comp = Double.compare(v1, v2);
				} else if (o1.isVariableString(variableName)) {
					comp = o1.getStringProperty(variableName).compareTo(o2.getStringProperty(variableName));
				} else {
					System.err.println("unknown type for variable " + variableName);
					System.exit(-2);
				}
				if (sortAscending) {
					return comp;
				} else {
					return -comp;
				}
			}
		});
		clearAndRefillVisibleGraphList();
		operatorClearSelection();
	}

	public void operatorClearSelection() {
		// TODO Auto-generated method stub
		// done in class GraphInteraction
	}

	public void operatorRestoreDeletedGraphs() {
		deletedGraphs.clear();
		graphList = new ArrayList<>(originalGraphList);
		visibleGraphList.clear();
		fillVisibleList(); // to force re-ordering from copy
		// mark previously selected graphs as not selected
	}

	private void deleteAndFill(Collection<GraphData> toDelete) {
		// only allow visible graphs to be removed (prevent removal of stuff not in the
		// visible window)
		ArrayList<GraphData> _toDelete = new ArrayList<>(16);
		HashSet<GraphData> visibleSet = new HashSet<>(visibleGraphList);
		for (GraphData gd : toDelete) {
			if (visibleSet.contains(gd)) {
				_toDelete.add(gd);
			}
		}

		visibleGraphList.removeAll(_toDelete);
		graphList.removeAll(_toDelete);
		deletedGraphs.addAll(_toDelete);
		// fill with new graphs properly ordered
		fillVisibleList();
	}

	public void operatorDeleteSelection() {
		if (selectedGraphs.isEmpty())
			return;
		// remove selection
		deleteAndFill(selectedGraphs);
	}

	public void operatorCropSelection() {
		if (selectedGraphs.isEmpty())
			return;
		// delete all visible graphs which are not selected
		ArrayList<GraphData> toDelete = new ArrayList<>();
		for (GraphData gd : visibleGraphList) {
			if (!selectedGraphs.contains(gd)) {
				toDelete.add(gd);
			}
		}
		deleteAndFill(toDelete);
	}

	public void operatorSelectAllVisible() {
		selectedGraphs.clear();
		selectedGraphs.addAll(visibleGraphList);
		visibleGraphList.parallelStream().forEach(graph -> {
			graph.setSelected(true);
		});
	}

	public void operatorInvertSelectionVisible() {
		visibleGraphList.parallelStream().forEach(graph -> {
			toggleSelected(graph);
		});
	}

	private void toggleSelected(GraphData gd) {
		gd.toggleSelected();
		if (gd.isSelected()) {
			selectedGraphs.add(gd);
		} else {
			selectedGraphs.remove(gd);
		}
	}

	public void saveSelectedGraphs(Component parentComponent) {
		saveGraphsSingleCSV(selectedGraphs, "selected", "Saving selected graphs", parentComponent);
	}

	public void saveFilteredGraphs(Component parentComponent) {
		saveGraphsSingleCSV(graphList, "filtered", "Saving filtered graphs", parentComponent);
	}

	@SuppressWarnings("unused")
	private void saveGraphsSingleCSV(Collection<GraphData> graphs, String suggestion, String title,
			Component parentComponent) {
		if (graphs.isEmpty()) {
			JOptionPane.showMessageDialog(parentComponent, "Nothing to save");
			return;
		}

		System.err.println("TODO: saveGraphsSingleCSV() in GraphFilter");
		String filename = (String) JOptionPane.showInputDialog(parentComponent, "Type the filename", title,
				JOptionPane.PLAIN_MESSAGE, null, null, suggestion);
		for (GraphData gd : graphs) {
		}
	}

	@SuppressWarnings("unused")
	private void saveIndividualGraphs(Collection<GraphData> graphs, String suggestion, String title,
			Component parentComponent) {
		if (graphs.isEmpty()) {
			JOptionPane.showMessageDialog(parentComponent, "Nothing to save");
			return;
		}

		String prefix = (String) JOptionPane.showInputDialog(parentComponent, "Type the files' prefix", title,
				JOptionPane.PLAIN_MESSAGE, null, null, suggestion);

		if (prefix == null || prefix.trim().isEmpty())
			return;

		File folderFile = new File("output");
		if (!folderFile.exists()) {
			folderFile.mkdir();
		}
		if (folderFile.isDirectory()) {
			for (GraphData gd : graphs) {
				String filename = "output" + File.separator + prefix + "_" + gd.getId() + ".csv";
				try {
					gd.saveGraphCSV(filename);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else {
			JOptionPane.showConfirmDialog(parentComponent, "Could not create output directory", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public double getMinimumOfVariable(String variable) {
		if (minimumOfVariable.containsKey(variable))
			return minimumOfVariable.getDouble(variable);

		double minimum = Double.MAX_VALUE;
		for (GraphData graph : graphList) {
			double val = graph.getNumericVariable(variable);
			if (val < minimum) {
				minimum = val;
			}
		}

		minimumOfVariable.put(variable, minimum);
		return minimum;
	}

	public double getMaximumOfVariable(final String variable) {
		if (maximumOfVariable.containsKey(variable))
			return maximumOfVariable.getDouble(variable);

		double maximum = -Double.MAX_VALUE;
		for (GraphData graph : graphList) {
			double val = graph.getNumericVariable(variable);
			if (val > maximum) {
				maximum = val;
			}
		}

		maximumOfVariable.put(variable, maximum);
		return maximum;
	}

	/**
	 * adapts the given value (must go from 0 to 100 (inclusive)) to be between the
	 * variable's low and high range
	 * 
	 * @param variable
	 * @param value
	 * @return
	 */
	public double getVariableAdaptedValue(String variable, int value) {
		double low = getMinimumOfVariable(variable);
		double range;
		lowHighVariableDifference = new Object2DoubleOpenHashMap<>();
		if (!lowHighVariableDifference.containsKey(variable)) {
			double high = getMaximumOfVariable(variable);
			range = high - low;
			lowHighVariableDifference.put(variable, range);
		} else {
			range = lowHighVariableDifference.getDouble(variable);
		}
		// double range=lowHighVariableDifference.g
		double res = low + (value / 100.0) * range;
		return res;
	}

	public void debugVisible() {
		System.out.println(visibleGraphList);
	}

	public void debugSelected() {
		System.out.println(sortToList(selectedGraphs));
	}

	public void debugDeleted() {
		System.out.println(sortToList(deletedGraphs));
	}

	private ArrayList<GraphData> sortToList(Collection<GraphData> c) {
		ArrayList<GraphData> list = new ArrayList<>(c);
		list.sort(new Comparator<GraphData>() {
			@Override
			public int compare(GraphData o1, GraphData o2) {
				int id1 = Integer.parseInt(o1.getId());
				int id2 = Integer.parseInt(o2.getId());
				return Integer.compare(id1, id2);
			}
		});
		return list;
	}

}
