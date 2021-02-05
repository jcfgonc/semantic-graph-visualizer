package src.jcfgonc.resultsvisualiser;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.swingViewer.DefaultView;
import org.graphstream.ui.view.Viewer;

import slider.RangeSlider;
import utils.OSTools;

public class GraphResultsGUI extends JFrame {
	private static final long serialVersionUID = 5828909992252367118L;
	private static final int FONT_SIZE_MINIMUM = 8;
	private static final int FONT_SIZE_DEFAULT = 18;
	private static final int FONT_SIZE_MAXIMUM = 48;
	private static final String graphDatafile = "C:\\Desktop\\github\\BlenderMO\\moea_results_2021-02-05_02-31-46_sym.tsv";
	private static final int NODE_SIZE_MINIMUM = 0;
	private static final int NODE_SIZE_DEFAULT = 24;
	private static final int NODE_SIZE_MAXIMUM = 100;
	private static final int NUMBER_VISIBLE_GRAPHS_MINIMUM = 1;
	private static final int NUMBER_VISIBLE_GRAPHS_DEFAULT = 32;
	private static final int NUMBER_VISIBLE_GRAPHS_MAXIMUM = 1024;
	private static final int GRAPHS_PER_COLUMN_MINIMUM = 1;
	private static final int GRAPHS_PER_COLUMN_DEFAULT = 4;
	private static final int GRAPHS_PER_COLUMN_MAXIMUM = 10;

	/**
	 * Launch the application.
	 * 
	 * @throws IOException
	 * @throws NoSuchFileException
	 */
	public static void main(String[] args) throws NoSuchFileException, IOException {
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GraphResultsGUI frame = new GraphResultsGUI();
					frame.initializeTheRest();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private JPanel contentPane;
	private JPanel graphPanel;
	private JScrollPane scrollPane;
	private JPanel settingsPanel;
	private JSlider numVariablesSlider;
	private JSplitPane horizontalPanel;
	private JPanel numVariablesPanel;
	private JLabel numVariablesLabel;
	private JPanel fontScalePanel;
	private JSlider fontScaleSlider;
	private JLabel fontSizeLabel;
	private JPanel nodeSizePanel;
	private JSlider nodeSizeSlider;
	private JLabel nodeSizeLabel;
	private JPanel renderingControlPanel;
	private JPanel filteringPanel;
	private JComboBox<String> sortingVariableBox;
	private JPanel numGraphsPanel;
	private JPanel sortingPanel;
	private JSlider numGraphsSlider;
	private JLabel numGraphsLabel;
	private JPanel emptyPanel;
	private JMenuBar menuBar;
	private JMenu fileMenu;
	private JMenu editMenu;
	private JMenuItem selectAllMenuItem;
	private JMenuItem selectNoneMenuItem;
	private JMenuItem invertSelectionMenuItem;
	private JSeparator separator;
	private JMenuItem deleteSelectionMenuItem;
	private JMenuItem cropSelectionMenuItem;
	private JMenuItem exitMenuItem;
	private JMenuItem saveSelectionMenuItem;
	private JMenuItem openFileMenuItem;
	private JLabel sortLabel;
	private JPanel variablesFilterPanel;
	private JMenuItem saveFilteredMenuItem;
	private GraphFilter graphFilter;
	private MutableBoolean shiftKeyPressed;
	private HashMap<String, JLabel> minimumVariableLabelMap;
	private HashMap<String, JLabel> maximumVariableLabelMap;
	private int graphFontSize = FONT_SIZE_DEFAULT;
	private int graphNodeSize = NODE_SIZE_DEFAULT;
	private int graphsPerVariable = GRAPHS_PER_COLUMN_DEFAULT;
	private int graphSize;
	private JComboBox<String> sortingDirectionBox;
	private JMenu mnTools;
	private JMenuItem debugVisibleMenuItem;
	private JMenuItem debugDeletedMenuItem;
	private JMenuItem debugSelectedMenuItem;
	private JMenuItem restoreDeletedMenuItem;
	private JMenu mnView;
	private JMenuItem stopLayoutMenuItem;
	private JMenuItem restartLayoutMenuItem;
	private HashSet<GraphData> visibleGraphs;
	private JMenuItem stopAllLayoutMenuItem;

	/**
	 * Create the frame.
	 * 
	 * @throws IOException
	 * @throws NoSuchFileException
	 * @throws UnsupportedLookAndFeelException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ClassNotFoundException
	 */
	public GraphResultsGUI() throws NoSuchFileException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException,
			UnsupportedLookAndFeelException {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		initialize();
	}

	@SuppressWarnings("deprecation")
	private void initialize() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		horizontalPanel = new JSplitPane();
		contentPane.add(horizontalPanel, BorderLayout.CENTER);
		horizontalPanel.setEnabled(true);
		horizontalPanel.setContinuousLayout(true);
		horizontalPanel.setResizeWeight(1.0);
		horizontalPanel.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				windowResized();
			}
		});

		graphPanel = new JPanel();
		scrollPane = new JScrollPane(graphPanel);
		scrollPane.setViewportBorder(null);
		scrollPane.setDoubleBuffered(true);
		scrollPane.setBorder(null);
		scrollPane.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		scrollPane.setAlignmentY(Component.TOP_ALIGNMENT);
		scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		scrollPane.getVerticalScrollBar().setUnitIncrement(32);
		horizontalPanel.setLeftComponent(scrollPane);
		graphPanel.setBorder(null);
		graphPanel.setLayout(new GridLayout(1, 0, 0, 0));
		scrollPane.getViewport().addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				updateGraphVisibility(e);
			}
		});

		settingsPanel = new JPanel();
		settingsPanel.setMinimumSize(new Dimension(224, 10));
		horizontalPanel.setRightComponent(settingsPanel);
		settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));

		renderingControlPanel = new JPanel();
		renderingControlPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)),
				"Rendering", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		settingsPanel.add(renderingControlPanel);
		renderingControlPanel.setLayout(new BoxLayout(renderingControlPanel, BoxLayout.Y_AXIS));

		numVariablesPanel = new JPanel();
		renderingControlPanel.add(numVariablesPanel);
		numVariablesPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)),
				"Graphs per Row", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));

		numVariablesSlider = new JSlider(GRAPHS_PER_COLUMN_MINIMUM, GRAPHS_PER_COLUMN_MAXIMUM, GRAPHS_PER_COLUMN_DEFAULT);
		numVariablesSlider.setPaintLabels(true);
		numVariablesPanel.add(numVariablesSlider);
		numVariablesSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (GraphResultsGUI.this.isVisible()) {
					updateGraphsVariableControl(source);
				}
			}
		});

		numVariablesLabel = new JLabel(Integer.toString(4));
		numVariablesPanel.add(numVariablesLabel);

		fontScalePanel = new JPanel();
		renderingControlPanel.add(fontScalePanel);
		fontScalePanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)),
				"Font Size", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));

		fontScaleSlider = new JSlider(FONT_SIZE_MINIMUM, FONT_SIZE_MAXIMUM, FONT_SIZE_DEFAULT);
		fontScaleSlider.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					fontScaleSlider.setValue(FONT_SIZE_DEFAULT);
				}
			}
		});
		fontScaleSlider.setPaintLabels(true);
		fontScalePanel.add(fontScaleSlider);
		fontScaleSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (GraphResultsGUI.this.isVisible()) {
					updateGraphFontsSizeControl(source);
				}
			}
		});

		fontSizeLabel = new JLabel(Integer.toString(graphFontSize));
		fontScalePanel.add(fontSizeLabel);

		nodeSizePanel = new JPanel();
		renderingControlPanel.add(nodeSizePanel);
		nodeSizePanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)),
				"Node Size", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));

		nodeSizeSlider = new JSlider(NODE_SIZE_MINIMUM, NODE_SIZE_MAXIMUM, NODE_SIZE_DEFAULT);
		nodeSizeSlider.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					nodeSizeSlider.setValue(NODE_SIZE_DEFAULT);
				}
			}
		});
		nodeSizeSlider.setPaintLabels(true);
		nodeSizePanel.add(nodeSizeSlider);
		nodeSizeSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (GraphResultsGUI.this.isVisible()) {
					updateNodeSizeControl(source);
				}
			}
		});

		nodeSizeLabel = new JLabel(Integer.toString(graphNodeSize));
		nodeSizePanel.add(nodeSizeLabel);

		numGraphsPanel = new JPanel();
		renderingControlPanel.add(numGraphsPanel);
		numGraphsPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)),
				"Graphs per Screen", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		numGraphsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		numGraphsSlider = new JSlider(NUMBER_VISIBLE_GRAPHS_MINIMUM, NUMBER_VISIBLE_GRAPHS_MAXIMUM, NUMBER_VISIBLE_GRAPHS_DEFAULT);
		numGraphsSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (GraphResultsGUI.this.isVisible()) {
					updateNumberVisibleGraphsControl(source);
				}
			}
		});
		numGraphsPanel.add(numGraphsSlider);

		numGraphsLabel = new JLabel(Integer.toString(NUMBER_VISIBLE_GRAPHS_DEFAULT));
		numGraphsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		numGraphsPanel.add(numGraphsLabel);

		filteringPanel = new JPanel();
		filteringPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(128, 128, 128)),
				"Filtering Options", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		settingsPanel.add(filteringPanel);
		filteringPanel.setLayout(new BoxLayout(filteringPanel, BoxLayout.Y_AXIS));

		sortingPanel = new JPanel();
		filteringPanel.add(sortingPanel);

		sortLabel = new JLabel("Sort Graphs in");
		sortingPanel.add(sortLabel);

		sortingDirectionBox = new JComboBox<String>();
		sortingDirectionBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sortGraphs();
			}
		});
		sortingDirectionBox.setMaximumRowCount(2);
		sortingDirectionBox.setModel(new DefaultComboBoxModel<String>(new String[] { "ascending", "descending" }));
		sortingPanel.add(sortingDirectionBox);

		sortingVariableBox = new JComboBox<>();
		sortingPanel.add(sortingVariableBox);
		sortingVariableBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sortGraphs();
			}
		});

		variablesFilterPanel = new JPanel();
		filteringPanel.add(variablesFilterPanel);
		variablesFilterPanel.setLayout(new BoxLayout(variablesFilterPanel, BoxLayout.Y_AXIS));

		emptyPanel = new JPanel();
		emptyPanel.setBorder(null);
		filteringPanel.add(emptyPanel);
		emptyPanel.setLayout(new BorderLayout(0, 0));

		addComponentListener(new ComponentAdapter() { // window resize event
			@Override
			public void componentResized(ComponentEvent e) {
				windowResized();
			}
		});

		menuBar = new JMenuBar();
		contentPane.add(menuBar, BorderLayout.NORTH);

		fileMenu = new JMenu("File");
		menuBar.add(fileMenu);

		openFileMenuItem = new JMenuItem("Open File");
		fileMenu.add(openFileMenuItem);

		saveSelectionMenuItem = new JMenuItem("Save Selected Graphs");
		saveSelectionMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				graphFilter.saveSelectedGraphs(GraphResultsGUI.this);
			}
		});
		fileMenu.add(saveSelectionMenuItem);

		saveFilteredMenuItem = new JMenuItem("Save Filtered Graphs");
		saveFilteredMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
		saveFilteredMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				graphFilter.saveFilteredGraphs(GraphResultsGUI.this);
			}
		});
		fileMenu.add(saveFilteredMenuItem);

		exitMenuItem = new JMenuItem("Exit");
		exitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
		exitMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				quit();
			}
		});
		fileMenu.add(exitMenuItem);

		editMenu = new JMenu("Edit");
		menuBar.add(editMenu);

		selectAllMenuItem = new JMenuItem("Select All");
		selectAllMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK));
		selectAllMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectAll();
			}
		});

		deleteSelectionMenuItem = new JMenuItem("Delete Selection  ");
		deleteSelectionMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		deleteSelectionMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deleteSelectedGraphs();
			}
		});
		editMenu.add(deleteSelectionMenuItem);

		cropSelectionMenuItem = new JMenuItem("Crop Selection");
		cropSelectionMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cropSelection();
			}
		});
		editMenu.add(cropSelectionMenuItem);

		restoreDeletedMenuItem = new JMenuItem("Restore Deleted");
		restoreDeletedMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				restoreDeletedGraphs();

			}
		});
		editMenu.add(restoreDeletedMenuItem);

		separator = new JSeparator();
		editMenu.add(separator);
		editMenu.add(selectAllMenuItem);

		selectNoneMenuItem = new JMenuItem("Select None");
		selectNoneMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK));
		selectNoneMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectNone();
			}
		});
		editMenu.add(selectNoneMenuItem);

		invertSelectionMenuItem = new JMenuItem("Invert Selection");
		invertSelectionMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_MASK));
		invertSelectionMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				invertSelection();
			}
		});
		editMenu.add(invertSelectionMenuItem);

		mnView = new JMenu("View");
		menuBar.add(mnView);

		restartLayoutMenuItem = new JMenuItem("Restart graph(s) layout");
		restartLayoutMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				restartGraphsLayoutVisible();
			}
		});
		mnView.add(restartLayoutMenuItem);

		stopLayoutMenuItem = new JMenuItem("Stop graph(s) layout");
		stopLayoutMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stopGraphsLayoutVisible();
			}
		});
		mnView.add(stopLayoutMenuItem);

		stopAllLayoutMenuItem = new JMenuItem("Stop graph(s) layout (including hidden graphs)");
		stopAllLayoutMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stopGraphsLayoutAll();
			}
		});
		mnView.add(stopAllLayoutMenuItem);

		mnTools = new JMenu("Debug");
		menuBar.add(mnTools);

		debugVisibleMenuItem = new JMenuItem("Visible");
		debugVisibleMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, 0));
		debugVisibleMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				graphFilter.debugVisible();
			}
		});
		mnTools.add(debugVisibleMenuItem);

		debugSelectedMenuItem = new JMenuItem("Selected");
		debugSelectedMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0));
		debugSelectedMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				graphFilter.debugSelected();
			}
		});
		mnTools.add(debugSelectedMenuItem);

		debugDeletedMenuItem = new JMenuItem("Deleted");
		debugDeletedMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0));
		debugDeletedMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				graphFilter.debugDeleted();
			}
		});
		mnTools.add(debugDeletedMenuItem);
	}

	private void restartGraphsLayoutVisible() {
		List<GraphData> visibleGraphList = graphFilter.getVisibleGraphList();
		visibleGraphList.parallelStream().forEach(gd -> {
			Viewer viewer = gd.getViewer();
			viewer.disableAutoLayout();
			viewer.enableAutoLayout();
		});
	}

	private void stopGraphsLayoutVisible() {
		List<GraphData> visibleGraphList = graphFilter.getVisibleGraphList();
		if (visibleGraphList.isEmpty())
			return;
		visibleGraphList.parallelStream().forEach(gd -> {
			Viewer viewer = gd.getViewer();
			viewer.disableAutoLayout();
		});
	}

	private void stopGraphsLayoutAll() {
		List<GraphData> graphs = graphFilter.getFullGraphList();
		graphs.parallelStream().forEach(gd -> {
			Viewer viewer = gd.getViewer();
			viewer.disableAutoLayout();
		});
	}

	private void quit() {
		System.exit(0);
	}

	/**
	 * invoked when the graph's scrollbar is dragged
	 * 
	 * @param e
	 */
	private void updateGraphVisibility(ChangeEvent e) {
		if (!isVisible()) {
			return;
		}
//		for (GraphData gd : graphFilter.getVisibleGraphList()) {
//			DefaultView defaultView = gd.getDefaultView();
//			boolean isVisible = !defaultView.getVisibleRect().isEmpty();
//			defaultView.setEnabled(isVisible);
//			defaultView.setVisible(isVisible);
//		}
	}

	private void windowResized() {
		layoutGraphPanel();
	}

	protected void initializeTheRest() throws NoSuchFileException, IOException {
		double w = 640 * OSTools.getScreenScale();
		double h = 480 * OSTools.getScreenScale();
		ToolTipManager ttm = ToolTipManager.sharedInstance();
		ttm.setInitialDelay(250);
		ttm.setDismissDelay(3600 * 1000);
		handleKeyEvents();
		graphFilter = new GraphFilter(graphDatafile, NUMBER_VISIBLE_GRAPHS_DEFAULT, shiftKeyPressed);
		setSize(new Dimension((int) w, (int) h));
		visibleGraphs = new HashSet<GraphData>();
		addVisibleGraphsToPanel();
		layoutGraphPanel();
		updateFontsSize();
		updateNodesSize();
		if (!graphFilter.hasVisibleGraphs()) {
			System.err.println("no graph data loaded...");
			System.exit(-1);
		}
		createSortingOptions();
		// create the filtering panels
		createFilteringPanels();
		// center window
		setLocationRelativeTo(null);
	}

	private void createSortingOptions() {
		// create sorting box options
		GraphData gd0 = graphFilter.getVisibleGraphList().get(0);
		int numberOfVars = gd0.getNumberOfVars();
		ArrayList<String> sortingVariables = new ArrayList<>(numberOfVars);
		for (int i = 0; i < numberOfVars; i++) {
			String variable = gd0.getVariableFromColumnNumber(i);
			if (gd0.isVariableNumeric(variable) || gd0.isVariableString(variable)) {
				sortingVariables.add(variable);
			}
		}
		sortingVariableBox.setModel(new DefaultComboBoxModel<String>(sortingVariables.toArray(new String[0])));
	}

	private void createFilteringPanels() {
		GraphData gd = graphFilter.getVisibleGraphList().get(0);
		minimumVariableLabelMap = new HashMap<>();
		maximumVariableLabelMap = new HashMap<>();
		for (int i = 0; i < gd.getNumberOfVars(); i++) {
			String variable = gd.getVariableFromColumnNumber(i);
			if (!gd.isVariableNumeric(variable))
				continue;

			JPanel variablePanel = new JPanel();
			variablePanel.setBorder(new TitledBorder(null, variable, TitledBorder.LEADING, TitledBorder.TOP, null, null));
			variablesFilterPanel.add(variablePanel);

			double min = graphFilter.getMinimumOfVariable(variable);
			double max = graphFilter.getMaximumOfVariable(variable);

			JLabel lowLimitLabel = new JLabel(Double.toString(min));
			minimumVariableLabelMap.put(variable, lowLimitLabel);
			variablePanel.add(lowLimitLabel);

			RangeSlider rangeSlider = new RangeSlider();
			rangeSlider.setUpperValue(100);
			variablePanel.add(rangeSlider);

			JLabel highLimitLabel = new JLabel(Double.toString(max));
			maximumVariableLabelMap.put(variable, highLimitLabel);
			variablePanel.add(highLimitLabel);

			rangeSlider.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					RangeSlider slider = (RangeSlider) e.getSource();
					double lowValue = graphFilter.getVariableAdaptedValue(variable, slider.getValue());
					double highValue = graphFilter.getVariableAdaptedValue(variable, slider.getUpperValue());
					lowLimitLabel.setText(String.format(Locale.ROOT, "%.2f", lowValue));
					highLimitLabel.setText(String.format(Locale.ROOT, "%.2f", highValue));
					if (slider.getValueIsAdjusting())
						return;
					if (GraphResultsGUI.this.isVisible()) {
						updateGraphFiltering(variable, lowValue, highValue);
					}
				}
			});
			rangeSlider.setValue(0);
		}
	}

	private void handleKeyEvents() {
		shiftKeyPressed = new MutableBoolean(false);
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {

			@Override
			public boolean dispatchKeyEvent(KeyEvent e) {
				switch (e.getID()) {
				case KeyEvent.KEY_PRESSED:
					if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
						shiftKeyPressed.setTrue();
					}
					break;
				case KeyEvent.KEY_RELEASED:
					if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
						shiftKeyPressed.setFalse();
					}
					break;
				}
				return false;
			}
		});
	}

	private void invertSelection() {
		graphFilter.operatorInvertSelectionVisible();
	}

	private void selectAll() {
		graphFilter.operatorSelectAllVisible();
	}

	private void selectNone() {
		graphFilter.operatorClearSelection();
	}

	private void addVisibleGraphsToPanel() {
		HashSet<GraphData> newVisibleGraphs = new HashSet<>();
		List<GraphData> visibleGraphList = graphFilter.getVisibleGraphList();

		for (GraphData gd : visibleGraphList) {
			DefaultView dv = gd.getDefaultView();
			// graph is added to panel here
			graphPanel.add(dv);
			newVisibleGraphs.add(gd);
		}

		// this code is used to enable/disable auto layout for added/removed graphs
		HashSet<GraphData> addedGraphs = whatGotAdded(visibleGraphs, newVisibleGraphs);
		HashSet<GraphData> removedGraphs = whatGotRemoved(visibleGraphs, newVisibleGraphs);
		addedGraphs.parallelStream().forEach(gd -> {
			gd.getViewer().enableAutoLayout();
		});
		removedGraphs.parallelStream().forEach(gd -> {
			gd.getViewer().disableAutoLayout();
		});
//		System.out.format("addedGraphs: %s\n", addedGraphs);
//		System.out.format("removedGraphs: %s\n", removedGraphs);
		visibleGraphs = newVisibleGraphs; // let GC clear old visibleGraphs

		updateGraphsSize();
	}

	private <T> HashSet<T> whatGotRemoved(HashSet<T> oldSet, HashSet<T> newSet) {
		// what's in old which aint in new
		HashSet<T> removed = new HashSet<>();
		for (T element : oldSet) {
			if (!newSet.contains(element)) {
				removed.add(element);
			}
		}
		return removed;
	}

	private <T> HashSet<T> whatGotAdded(HashSet<T> oldSet, HashSet<T> newSet) {
		// what's in old which aint in new
		HashSet<T> added = new HashSet<>();
		for (T element : newSet) {
			if (!oldSet.contains(element)) {
				added.add(element);
			}
		}
		return added;
	}

	private void updateGraphsVariableControl(JSlider source) {
		graphsPerVariable = source.getValue();
		layoutGraphPanel();
	}

	private void layoutGraphPanel() {
		int nVisibleG = graphFilter.getVisibleGraphList().size();
		if (nVisibleG < graphsPerVariable) {
			graphsPerVariable = nVisibleG;
		}
		if (graphsPerVariable > 0) {
			int panelWidth = scrollPane.getViewport().getWidth();
			this.graphSize = panelWidth / graphsPerVariable - 2;
			updateGraphsSize();
			GridLayout layout = (GridLayout) graphPanel.getLayout();
			layout.setColumns(graphsPerVariable);
			layout.setRows(0);
			numVariablesLabel.setText(Integer.toString(graphsPerVariable));
		}
		graphPanel.revalidate();
		graphPanel.repaint();
		// numGraphsSlider.setValue(graphsPerVariable);
	}

	private void updateGraphsSize() {
		for (GraphData gd : graphFilter.getVisibleGraphList()) {
			DefaultView view = gd.getDefaultView();
			Dimension size = new Dimension(graphSize, graphSize);
			view.setPreferredSize(size);
		}
	}

	private void updateGraphFontsSizeControl(JSlider source) {
		graphFontSize = source.getValue();
		fontSizeLabel.setText(Integer.toString(graphFontSize));
		updateFontsSize();
	}

	private void updateFontsSize() {
		List<GraphData> visibleGraphList = graphFilter.getVisibleGraphList();
		visibleGraphList.parallelStream().forEach(gd -> {
			MultiGraph graph = gd.getMultiGraph();
			String style = String.format("edge { text-size: %d; } node { text-size: %d; }", graphFontSize, graphFontSize);
			graph.addAttribute("ui.stylesheet", style);
		});
	}

	private void updateNodeSizeControl(JSlider source) {
		graphNodeSize = source.getValue();
		nodeSizeLabel.setText(Integer.toString(graphNodeSize));
		updateNodesSize();
	}

	private void updateNodesSize() {
		List<GraphData> visibleGraphList = graphFilter.getVisibleGraphList();
		visibleGraphList.parallelStream().forEach(gd -> {
			MultiGraph graph = gd.getMultiGraph();
			String style;
			if (graphNodeSize == 0) {
				style = String.format("node { stroke-mode: none; }");
			} else {
				style = String.format("node { stroke-mode: plain; size: %dpx; }", graphNodeSize, graphNodeSize);
			}
			graph.addAttribute("ui.stylesheet", style);
		});
	}

	private void updateNumberVisibleGraphsControl(JSlider source) {
		numGraphsLabel.setText(Integer.toString(source.getValue()));
		if (source.getValueIsAdjusting())
			return;
		graphFilter.setNumberVisibleGraphs(source.getValue());
		// --
		graphPanel.removeAll();
		stopGraphsLayoutVisible();
		addVisibleGraphsToPanel();
		layoutGraphPanel();
		updateFontsSize();
		updateNodesSize();
		graphPanel.revalidate();
		graphPanel.repaint();
	}

	private void restoreDeletedGraphs() {
		graphFilter.operatorRestoreDeletedGraphs();
		graphPanel.removeAll();
		stopGraphsLayoutVisible();
		addVisibleGraphsToPanel();
		layoutGraphPanel();
		updateFontsSize();
		updateNodesSize();
		graphPanel.revalidate();
		graphPanel.repaint();
	}

	private void cropSelection() {
		graphFilter.operatorCropSelection();
		graphPanel.removeAll();
		stopGraphsLayoutVisible();
		addVisibleGraphsToPanel();
		layoutGraphPanel();
		updateFontsSize();
		updateNodesSize();
		graphPanel.revalidate();
		graphPanel.repaint();
	}

	private void sortGraphs() {
		String variable = (String) sortingVariableBox.getSelectedItem();
		String directionText = (String) sortingDirectionBox.getSelectedItem();
		boolean sortAscending = directionText.equals("ascending");
		graphFilter.operatorSortGraphs(variable, sortAscending);
		graphPanel.removeAll();
		stopGraphsLayoutVisible();
		addVisibleGraphsToPanel();
		layoutGraphPanel();
		updateFontsSize();
		updateNodesSize();
		graphPanel.revalidate();
		graphPanel.repaint();
	}

	private void deleteSelectedGraphs() {
		graphFilter.operatorDeleteSelection();
		graphPanel.removeAll();
		stopGraphsLayoutVisible();
		addVisibleGraphsToPanel();
		layoutGraphPanel();
		updateFontsSize();
		updateNodesSize();
		graphPanel.revalidate();
		graphPanel.repaint();
	}

	private void updateGraphFiltering(String variable, double lowValue, double highValue) {
		graphFilter.setGraphFilter(variable, lowValue, highValue);
		graphFilter.operatorFilterGraphs();
		graphPanel.removeAll();
		stopGraphsLayoutVisible();
		addVisibleGraphsToPanel();
		layoutGraphPanel();
		updateFontsSize();
		updateNodesSize();
		graphPanel.revalidate();
		graphPanel.repaint();
	}

}
