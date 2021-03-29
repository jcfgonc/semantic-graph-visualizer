package src.jcfgonc.resultsvisualiser;

import java.awt.Color;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.apache.commons.lang3.mutable.MutableBoolean;

/**
 * TODO this class is to handle selection events. WIP
 * 
 * @author jcfgonc@gmail.com
 *
 */
public class GraphInteraction {
	/**
	 * graphs which are currently being selected with shift+mouse click
	 */
	private HashSet<GraphData> shiftSelectedGraphs;
	/**
	 * global status of the shift key
	 */
	private MutableBoolean shiftKeyPressed;
	/**
	 * a pointer to the currently clicked graph
	 */
	private GraphData currentlyClickedGD;
	/**
	 * a pointer to the last clicked graph
	 */
	private GraphData lastClickedGD;

	public GraphInteraction() {
		shiftKeyPressed = new MutableBoolean(false);
		this.shiftSelectedGraphs = new HashSet<>();

		for (GraphData gd : originalGraphList) {
			addMouseClickHandler(gd);
			graphMap.put(gd.getId(), gd);
		}

	}

	private void addMouseClickHandler(GraphData gd) {
		MouseAdapter mouseAdapter = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					String clickedComponent = e.getComponent().getName();
					if (shiftKeyPressed.isTrue()) {
						GraphData currentClickedTemp = getGraph(clickedComponent);
						int i0 = visibleGraphList.indexOf(currentClickedTemp);
						int i1;
						if (currentlyClickedGD == null) {
							i1 = 0;
						} else {
							i1 = visibleGraphList.indexOf(currentlyClickedGD);
						}
						unselectGraphs(shiftSelectedGraphs);
						shiftSelectedGraphs.clear();
						for (int i = Math.min(i0, i1); i <= Math.max(i0, i1); i++) {
							GraphData g = visibleGraphList.get(i);
							g.setSelected(true);
							selectedGraphs.add(g);
							shiftSelectedGraphs.add(g);
						}
					} else {
						shiftSelectedGraphs.clear();
						lastClickedGD = currentlyClickedGD;
						currentlyClickedGD = getGraph(clickedComponent);
						toggleSelected(currentlyClickedGD);
						setGraphBorderState(lastClickedGD, false);
						setGraphBorderState(currentlyClickedGD, true);
					}
				}
			}

			private void unselectGraphs(Collection<GraphData> graphs) {
				if (graphs == null || graphs.isEmpty())
					return;
				graphs.parallelStream().forEach(gd -> {
					gd.setSelected(false);
				});
				selectedGraphs.removeAll(graphs);
			}
		};
		gd.setMouseListener(mouseAdapter);
	}

	private void setGraphBorderState(GraphData gd, boolean enabled) {
		if (gd == null)
			return;
		if (enabled) {
			gd.getDefaultView().setBorder(new LineBorder(Color.BLACK));
		} else {
			gd.getDefaultView().setBorder(new EmptyBorder(0, 0, 0, 0));
		}
	}

	public void operatorClearSelection() {
		ArrayList<GraphData> graphs = new ArrayList<>();
		graphs.addAll(selectedGraphs);
		if (lastClickedGD != null) {
			graphs.add(currentlyClickedGD);
		}
		if (currentlyClickedGD != null) {
			graphs.add(currentlyClickedGD);
		}
		selectedGraphs.parallelStream().forEach(graph -> {
			graph.setSelected(false);
			setGraphBorderState(graph, false);
		});
		selectedGraphs.clear();

		lastClickedGD = null;
		currentlyClickedGD = null;
	}

	private void handleKeyEvents() {
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
}
