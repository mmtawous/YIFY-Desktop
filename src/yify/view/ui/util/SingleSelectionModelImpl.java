package yify.view.ui.util;

import java.util.LinkedHashMap;
import java.util.Map;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * An implementation of a single selection model backed by a LinkedHashMap. This
 * implementation does not allow more than one item to be selected.
 * 
 * @author Mohamed Tawous
 * 
 * @param <T> the type of items this selection model will hold.
 *
 */
public class SingleSelectionModelImpl<T> {
	/**
	 * The data model of the SingleSelectionModel. Each item in the Key Set may be
	 * set to true or false indicating its selection status.
	 */
	Map<T, SimpleBooleanProperty> children;

	/**
	 * Constructs a new SingleSelectionModel.
	 */
	public SingleSelectionModelImpl() {
		children = new LinkedHashMap<T, SimpleBooleanProperty>();
	}

	/**
	 * Constructs a new SingleSelectionModel using an array of one or more items.
	 */
	public SingleSelectionModelImpl(T[] items) {
		this();
		addAll(items);
	}

	/**
	 * Returns true if the provided T exists in the data model and is selected.
	 * 
	 * @param item the item to be queried for selection.
	 * @return the selection status of the given T.
	 */
	public boolean isSelected(T item) {
		if (children.containsKey(item))
			return children.get(item).getValue();
		else
			return false;
	}

	/**
	 * Attempts to select the specified T. If the T exists and was selected
	 * successfully the method returns true, and false otherwise. Selecting any item
	 * will deselect all others.
	 * 
	 * @param item the T to be selected
	 */
	public boolean select(T item) {
		if (children.containsKey(item)) {
			// If any are selected then deselect them
			for (Map.Entry<T, SimpleBooleanProperty> entry : children.entrySet()) {
				entry.getValue().set(false);
			}
			SimpleBooleanProperty bool = children.get(item);
			if (bool != null) {
				bool.set(true);
				return true;
			} else
				return false;
		} else {
			return false;
		}
	}

	/**
	 * Returns the T that is currently selected. If no T is selected, null is
	 * returned.
	 * 
	 * @return the T currently selected
	 */
	public T getSelected() {
		for (Map.Entry<T, SimpleBooleanProperty> entry : children.entrySet()) {
			if (entry.getValue().get()) {
				return entry.getKey();
			}
		}

		return null;
	}

	/**
	 * Adds a new item to the SelectionModel data model. All newly added items are
	 * not selected by default.
	 * 
	 * @param item the item to be added.
	 */
	public void add(T item) {
		children.put(item, new SimpleBooleanProperty(false));
	}

	/**
	 * Adds an array of items of T to the SelectionModel data model. All newly added items
	 * are not selected by default.
	 * 
	 * @param items the item array containing items to be added.
	 */
	public void addAll(T[] items) {
		for (int i = 0; i < items.length; i++) {
			add(items[i]);
		}

	}

	/**
	 * Removes the given item from the SelectionModel data model and returns the
	 * selection status of the item removed. If the item does not exist in the data
	 * model null is returned instead.
	 * 
	 * @param item the T to be removed
	 */
	public Boolean remove(T item) {
		SimpleBooleanProperty bool = children.remove(item);
		if (bool == null) {
			return null;
		} else {
			return bool.getValue();
		}
	}
}
