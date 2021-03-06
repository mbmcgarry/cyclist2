package edu.utah.sci.cyclist.neup.ui.views.inventory;

import java.util.ArrayList;
import java.util.List;

import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import edu.utah.sci.cyclist.core.model.Configuration;
import edu.utah.sci.cyclist.neup.model.Inventory;
import edu.utah.sci.cyclist.neup.ui.views.inventory.InventoryView.AgentInfo;

public class InventoryEntry {
	private String _name;
	private boolean _selected = false;
	private ObservableList<Inventory> _inventory;
	private List<AgentInfo> _nodes = new ArrayList<>();
	private Color _color;
	
	public InventoryEntry(String name) {
		this._name = name;
		_color = Configuration.getInstance().getColor(name);
	}
	
	public String getName() {
		return _name;
	}
	
	public void add(AgentInfo node) {
		_nodes.add(node);
//		node.setSelected(_selected);
	}
	
	public void remove(AgentInfo node) {
		_nodes.remove(node);
	}
	
	public void select(boolean value) {
		_selected = value;
//		for (AgentInfo node : _nodes) {
//			node.setSelected(value);
//		}
	}
	
	public boolean isSelected() {
		return _selected;
	}
	
	public boolean isEmpty() {
		return _nodes.isEmpty();
	}
	
	public void setInventory(ObservableList<Inventory> inventory) {
		_inventory = inventory;
	}
	
	
	public ObservableList<Inventory> getInventory() {
		return _inventory;
	}
	
	public Color getColor() {
		return _color;
	}
}