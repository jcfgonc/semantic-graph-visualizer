package src.jcfgonc.resultsvisualiser;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridLayout;
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

import slider.RangeSlider;
import utils.OSTools;

public class GraphResultsGUI extends JFrame {
	private static final long serialVersionUID = 5828909992252367118L;
	private static final String graphDatafile = "C:\\Desktop\\github\\PatternMiner\\results\\pattern_results_with_ss_V22.tsv";

	private static final int FONT_SIZE_MINIMUM = 8;
	private static final int FONT_SIZE_DEFAULT = 18;
	private static final int FONT_SIZE_MAXIMUM = 48;

	private static final int NODE_SIZE_MINIMUM = 1;
	private static final int NODE_SIZE_DEFAULT = 18;
	private static final int NODE_SIZE_MAXIMUM = 100;

	private static final int NUMBER_VISIBLE_GRAPHS_MINIMUM = 16;
	private static final int NUMBER_VISIBLE_GRAPHS_DEFAULT = 32;
	private static final int NUMBER_VISIBLE_GRAPHS_MAXIMUM = 2048;

	private static final int GRAPHS_PER_COLUMN_MINIMUM = 1;
	private static final int GRAPHS_PER_COLUMN_DEFAULT = 4;
	private static final int GRAPHS_PER_COLUMN_MAXIMUM = 8;

	private static final int GRAPH_ZOOM_MINIMUM = 1;
	private static final int GRAPH_ZOOM_DEFAULT = 100;
	private static final int GRAPH_ZOOM_MAXIMUM = 250;

	private static final int GRAPH_ROTATION_MINIMUM = -360;
	private static final int GRAPH_ROTATION_DEFAULT = 0;
	private static final int GRAPH_ROTATION_MAXIMUM = 360;

	/**
	 * Launch the application.
	 * 
	 * @throws IOException
	 * @throws NoSuchFileException
	 */
	public static void main(String[] args) throws NoSuchFileException, IOException {
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		System.setProperty("org.graphstream.ui", "org.graphstream.ui.swingViewer.util.SwingDisplay");
		System.setProperty("org.graphstream.ui", "swing");

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GraphResultsGUI frame = new GraphResultsGUI();
					frame.initializeTheRest();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private JPanel contentPane;
	private JPanel graphPanel;
	private JScrollPane graphsScrollPane;
	private JPanel settingsPanel;
	private JSlider numColumnsGraphPanelSlider;
	private JSplitPane horizontalPanel;
	private JPanel numColumnsGraphPanel;
	private JLabel numColumnsGraphPanelLabel;
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
	private HashMap<String, JLabel> minimumVariableLabelMap;
	private HashMap<String, JLabel> maximumVariableLabelMap;
	private JComboBox<String> sortingDirectionBox;
	private JMenu mnTools;
	private JMenuItem debugVisibleMenuItem;
	private JMenuItem debugDeletedMenuItem;
	private JMenuItem debugSelectedMenuItem;
	private JMenuItem restoreDeletedMenuItem;
	private JMenu mnView;
	private JMenuItem stopLayoutMenuItem;
	private JMenuItem restartLayoutMenuItem;
	private GraphPanelHandler graphPanelHandler;
	private JMenuItem shakeLayoutMenuItem;
	private JPanel graphMagnificationPanel;
	private JSlider graphMagnificationSlider;
	private JLabel graphMagnificationLabel;
	private JMenuItem resetViewMenuItem;
//	private GraphInteraction graphInteraction;
	private JPanel graphRotationPanel;
	private JSlider graphRotationSlider;
	private JLabel graphRotationLabel;
	private JScrollPane settingsScrollPane;

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
		graphPanel.setBorder(null);
		graphPanel.setLayout(new GridLayout(1, 0, 0, 0));

		graphsScrollPane = new JScrollPane(graphPanel);
		graphsScrollPane.setViewportBorder(null);
		graphsScrollPane.setBorder(null);
		graphsScrollPane.getVerticalScrollBar().setUnitIncrement(32);
		horizontalPanel.setLeftComponent(graphsScrollPane);

		settingsPanel = new JPanel();
		settingsScrollPane = new JScrollPane(settingsPanel);
		settingsScrollPane.setViewportBorder(null);
		settingsScrollPane.setBorder(null);
		settingsScrollPane.getVerticalScrollBar().setUnitIncrement(32);

		horizontalPanel.setRightComponent(settingsScrollPane);
		settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));

		renderingControlPanel = new JPanel();
		renderingControlPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)),
				"Rendering", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		settingsPanel.add(renderingControlPanel);
		renderingControlPanel.setLayout(new BoxLayout(renderingControlPanel, BoxLayout.Y_AXIS));

		// ----------

		numColumnsGraphPanel = new JPanel();
		renderingControlPanel.add(numColumnsGraphPanel);
		numColumnsGraphPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)),
				"Graphs per Row", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		numColumnsGraphPanelSlider = new JSlider(GRAPHS_PER_COLUMN_MINIMUM, GRAPHS_PER_COLUMN_MAXIMUM, GRAPHS_PER_COLUMN_DEFAULT);
		numColumnsGraphPanelSlider.setPaintLabels(true);
		numColumnsGraphPanel.add(numColumnsGraphPanelSlider);
		numColumnsGraphPanelSlider.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					numColumnsGraphPanelSlider.setValue(GRAPHS_PER_COLUMN_DEFAULT);
				}
			}
		});
		numColumnsGraphPanelSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				int num = source.getValue();
				numColumnsGraphPanelLabel.setText(Integer.toString(num));
				windowResized();
			}
		});
		numColumnsGraphPanelLabel = new JLabel(Integer.toString(GRAPHS_PER_COLUMN_DEFAULT));
		numColumnsGraphPanel.add(numColumnsGraphPanelLabel);

		// ----------

		fontScalePanel = new JPanel();
		renderingControlPanel.add(fontScalePanel);
		fontScalePanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)),
				"Font Size", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		fontScaleSlider = new JSlider(FONT_SIZE_MINIMUM, FONT_SIZE_MAXIMUM, FONT_SIZE_DEFAULT);
		fontScaleSlider.setPaintLabels(true);
		fontScalePanel.add(fontScaleSlider);
		fontScaleSlider.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					fontScaleSlider.setValue(FONT_SIZE_DEFAULT);
				}
			}
		});
		fontScaleSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				int size = source.getValue();
				fontSizeLabel.setText(Integer.toString(size));
				changeGraphFontsSize(size);
			}
		});
		fontSizeLabel = new JLabel(Integer.toString(FONT_SIZE_DEFAULT));
		fontScalePanel.add(fontSizeLabel);

		// ----------

		nodeSizePanel = new JPanel();
		renderingControlPanel.add(nodeSizePanel);
		nodeSizePanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)),
				"Node Size", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		nodeSizeSlider = new JSlider(NODE_SIZE_MINIMUM, NODE_SIZE_MAXIMUM, NODE_SIZE_DEFAULT);
		nodeSizeSlider.setPaintLabels(true);
		nodeSizePanel.add(nodeSizeSlider);
		nodeSizeSlider.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					nodeSizeSlider.setValue(NODE_SIZE_DEFAULT);
				}
			}
		});
		nodeSizeSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				int size = source.getValue();
				nodeSizeLabel.setText(Integer.toString(size));
				changeGraphNodeSize(size);
			}
		});
		nodeSizeLabel = new JLabel(Integer.toString(NODE_SIZE_DEFAULT));
		nodeSizePanel.add(nodeSizeLabel);

		// ----------

		numGraphsPanel = new JPanel();
		renderingControlPanel.add(numGraphsPanel);
		numGraphsPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)),
				"Number of Graphs", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		numGraphsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		numGraphsSlider = new JSlider(NUMBER_VISIBLE_GRAPHS_MINIMUM, NUMBER_VISIBLE_GRAPHS_MAXIMUM, NUMBER_VISIBLE_GRAPHS_DEFAULT);
		numGraphsSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				int num = source.getValue();
				numGraphsLabel.setText(Integer.toString(num));

				if (source.getValueIsAdjusting())
					return;

				changeNumberGraphs(num);
			}
		});
		numGraphsPanel.add(numGraphsSlider);
		numGraphsLabel = new JLabel(Integer.toString(NUMBER_VISIBLE_GRAPHS_DEFAULT));
		numGraphsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		numGraphsPanel.add(numGraphsLabel);

		// ----------

		graphMagnificationPanel = new JPanel();
		renderingControlPanel.add(graphMagnificationPanel);
		graphMagnificationPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)),
				"Graph magnification", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		graphMagnificationPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		graphMagnificationSlider = new JSlider(GRAPH_ZOOM_MINIMUM, GRAPH_ZOOM_MAXIMUM, GRAPH_ZOOM_DEFAULT);
		graphMagnificationSlider.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					graphMagnificationSlider.setValue(GRAPH_ZOOM_DEFAULT);
				}
			}
		});
		graphMagnificationSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				int factor = source.getValue();
				graphMagnificationLabel.setText(Integer.toString(factor));

				changeGraphsMagnification(factor);
			}
		});
		graphMagnificationPanel.add(graphMagnificationSlider);
		graphMagnificationLabel = new JLabel(Integer.toString(GRAPH_ZOOM_DEFAULT));
		graphMagnificationLabel.setAlignmentX(0.5f);
		graphMagnificationPanel.add(graphMagnificationLabel);

		// ----------

		graphRotationPanel = new JPanel();
		renderingControlPanel.add(graphRotationPanel);
		graphRotationPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)),
				"Graph rotation (degrees)", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		graphRotationPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		graphRotationSlider = new JSlider(GRAPH_ROTATION_MINIMUM, GRAPH_ROTATION_MAXIMUM, GRAPH_ROTATION_DEFAULT);
		graphRotationSlider.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					graphRotationSlider.setValue(GRAPH_ROTATION_DEFAULT);
				}
			}
		});
		graphRotationSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				int angle = source.getValue();
				graphRotationLabel.setText(Integer.toString(angle));

				changeGraphsRotation(angle);
			}
		});
		graphRotationPanel.add(graphRotationSlider);
		graphRotationLabel = new JLabel(Integer.toString(GRAPH_ROTATION_DEFAULT));
		graphRotationLabel.setAlignmentX(0.5f);
		graphRotationPanel.add(graphRotationLabel);

		// ----------
		// ----------
		// ----------

		sortingPanel = new JPanel();
		renderingControlPanel.add(sortingPanel);

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

		// ----------
		// ----------
		// ----------

		filteringPanel = new JPanel();
		filteringPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(128, 128, 128)),
				"Filtering Options", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		settingsPanel.add(filteringPanel);
		filteringPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

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

		// ----------
		// ----------
		// ----------

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
				restartGraphsLayout();
			}
		});
		mnView.add(restartLayoutMenuItem);

		shakeLayoutMenuItem = new JMenuItem("Shake graph(s) layout");
		shakeLayoutMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				shakeGraphsLayout();
			}
		});
		mnView.add(shakeLayoutMenuItem);

		stopLayoutMenuItem = new JMenuItem("Stop graph(s) layout");
		stopLayoutMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stopGraphsLayout();
			}
		});
		mnView.add(stopLayoutMenuItem);

		resetViewMenuItem = new JMenuItem("Reset graph(s) view");
		resetViewMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				resetViewGraphs();
			}
		});
		mnView.add(resetViewMenuItem);

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

	private void initializeTheRest() throws NoSuchFileException, IOException {
		// tooltip show and dismiss delays
		ToolTipManager ttm = ToolTipManager.sharedInstance();
		ttm.setInitialDelay(250);
		ttm.setDismissDelay(3600 * 1000);

		graphFilter = new GraphFilter(graphDatafile, NUMBER_VISIBLE_GRAPHS_DEFAULT);
		if (!graphFilter.hasVisibleGraphs()) {
			System.err.println("no graph data loaded...");
			System.exit(-1);
		}

		// graphInteraction = new GraphInteraction();
		createSortingOptions();
		// create the filtering panels
		createFilteringPanels();
		graphPanelHandler = new GraphPanelHandler(graphPanel, NUMBER_VISIBLE_GRAPHS_DEFAULT);
		refreshGraphs();

		// center and maximize window
		setLocationRelativeTo(null);
		setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);
		// setSize(new Dimension((int) w, (int) h));
		this.setVisible(true);
		Dimension eqr = OSTools.getEquivalentResolutionDPI(new Dimension(640, 480));
		this.setMinimumSize(eqr);
	}

	/**
	 * restarts (stops and then starts) the graph layout for the visible graphs
	 */
	private void restartGraphsLayout() {
		graphPanelHandler.restartGraphsLayout();
	}

	/**
	 * stops the graph layout for the visible graphs (graphFilter.getVisibleGraphList())
	 */
	private void stopGraphsLayout() {
		graphPanelHandler.stopGraphsLayout();
	}

	private void shakeGraphsLayout() {
		graphPanelHandler.shakeGraphs();
	}

	private void resetViewGraphs() {
		graphMagnificationSlider.setValue(GRAPH_ZOOM_DEFAULT);
		graphRotationSlider.setValue(GRAPH_ROTATION_DEFAULT);
		graphPanelHandler.resetViewGraphs();
	}

	private void quit() {
		System.exit(0);
	}

	private void windowResized() {
		int panelWidth = graphsScrollPane.getViewport().getWidth();
		int graphsPerColumn = numColumnsGraphPanelSlider.getValue();
		graphPanelHandler.setupPanelSize(panelWidth, graphsPerColumn);
		graphsScrollPane.revalidate();
		graphsScrollPane.repaint();
	}

	/**
	 * adds the sorting options (the objectives) to the sorting variable box
	 */
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

	/**
	 * creates the ranging sliders and labels, one for each numeric objective
	 */
	private void createFilteringPanels() {
		GraphData gd = graphFilter.getVisibleGraphList().get(0);
		minimumVariableLabelMap = new HashMap<>();
		maximumVariableLabelMap = new HashMap<>();
		int numberOfVars = gd.getNumberOfVars();
		for (int i = 0; i < numberOfVars; i++) {
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
						filterGraphs(variable, lowValue, highValue);
					}
				}
			});
//			rangeSlider.setValue(0);
		}
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

	/**
	 * calls the panel's handling code to update with the filter's visible graphs
	 */
	private void refreshGraphs() {
		List<GraphData> visibleGraphList = graphFilter.getVisibleGraphList();
		graphPanelHandler.refreshGraphs(visibleGraphList);
	}

	private void changeGraphFontsSize(int graphFontSize) {
		graphPanelHandler.updateFontsSize(graphFontSize);
	}

	private void changeGraphNodeSize(int graphNodeSize) {
		graphPanelHandler.updateNodesSize(graphNodeSize);
	}

	private void changeGraphsMagnification(int factor) {
		graphPanelHandler.changeGraphsMagnification(factor);
	}

	private void changeGraphsRotation(int angle) {
		graphPanelHandler.changeGraphsRotation(angle);
	}

	private void changeNumberGraphs(int numOfGraphs) {
		// can't reduce number of rendered graphs (graphstream seems to have a resource leak)
		if (numOfGraphs < graphFilter.getNumberOfVisibleGraphs())
			return;

		graphFilter.setNumberVisibleGraphs(numOfGraphs);
		graphPanelHandler.setNumberOfGraphs(numOfGraphs);

		// additional graphs may have been given and we must layout them again
		refreshGraphs();
		graphsScrollPane.revalidate();
		graphsScrollPane.repaint();
	}

	private void restoreDeletedGraphs() {
		graphFilter.operatorRestoreDeletedGraphs();
		refreshGraphs();
	}

	private void cropSelection() {
		graphFilter.operatorCropSelection();
		refreshGraphs();
	}

	private void sortGraphs() {
		String variable = (String) sortingVariableBox.getSelectedItem();
		String directionText = (String) sortingDirectionBox.getSelectedItem();
		boolean sortAscending = directionText.equals("ascending");
		graphFilter.operatorSortGraphs(variable, sortAscending);
		refreshGraphs();
	}

	private void deleteSelectedGraphs() {
		graphFilter.operatorDeleteSelection();
		refreshGraphs();
	}

	/**
	 * filters the visible graphs to have the given variable between the given low and high values/range
	 * 
	 * @param variable
	 * @param lowValue
	 * @param highValue
	 */
	private void filterGraphs(String variable, double lowValue, double highValue) {
		graphFilter.setGraphFilter(variable, lowValue, highValue);
		graphFilter.operatorFilterGraphs();
		refreshGraphs();
	}

}
