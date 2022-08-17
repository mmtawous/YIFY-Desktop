package yify.view.ui.util;

import java.util.LinkedHashMap;
import java.util.Map;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;

public class SingleSelectionModelImpl {
	Map<Node, SimpleBooleanProperty> children;

	public SingleSelectionModelImpl() {
		children = new LinkedHashMap<Node, SimpleBooleanProperty>();
	}

	public SingleSelectionModelImpl(Node... nodes) {
		this();
		addAll(nodes);
	}

	public boolean isSelected(Node node) {
		if (children.containsKey(node))
			return children.get(node).getValue();
		else
			return false;
	}

	/**
	 * Attempts to select the specified Node. If the node exists and was selected
	 * successfully the method returns true, and false otherwise. Selecting any node
	 * will deselect all others.
	 * 
	 * @param node the Node to be selected
	 */
	public boolean select(Node node) {
		if (children.containsKey(node)) {
			// If any are selected than deselect them
			for (Map.Entry<Node, SimpleBooleanProperty> entry : children.entrySet()) {
				entry.getValue().set(false);
			}

			children.get(node).set(true);;

			return true;
		} else {
			return false;
		}
	}

	public Node getSelected() {
		for (Map.Entry<Node, SimpleBooleanProperty> entry : children.entrySet()) {
			if (entry.getValue().get()) {
				return entry.getKey();
			}
		}

		return null;
	}

	public void add(Node node) {
		children.put(node, new SimpleBooleanProperty(false));
	}

	public void addAll(Node... nodes) {
		for (int i = 0; i < nodes.length; i++) {
			add(nodes[i]);
		}

	}

	public void remove(Node node) {
		children.remove(node);
	}
}
