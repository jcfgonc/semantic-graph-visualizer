package src.jcfgonc.resultsvisualiser;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import org.graphstream.ui.graphicGraph.GraphicGraph;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.util.ShortcutManager;

public class EmptyShortcutManager implements ShortcutManager, KeyListener {
	// Attributes

	/**
	 * The viewer to control.
	 */
	protected View view;

	// Construction

	public void init(GraphicGraph graph, View view) {
		this.view = view;
		view.addListener("Key", this);
	}

	public void release() {
		view.removeListener("Key", this);
	}

	// Events

	/**
	 * A key has been pressed.
	 * 
	 * @param event The event that generated the key.
	 */
	public void keyPressed(KeyEvent event) {
	}

	/**
	 * A key has been pressed.
	 * 
	 * @param event The event that generated the key.
	 */
	public void keyReleased(KeyEvent event) {
	}

	/**
	 * A key has been typed.
	 * 
	 * @param event The event that generated the key.
	 */
	public void keyTyped(KeyEvent event) {
	}
}