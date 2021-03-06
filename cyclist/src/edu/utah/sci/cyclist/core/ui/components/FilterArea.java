package edu.utah.sci.cyclist.core.ui.components;

import java.util.ArrayList;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ToolBar;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import edu.utah.sci.cyclist.core.event.dnd.DnD;
import edu.utah.sci.cyclist.core.event.ui.FilterEvent;
import edu.utah.sci.cyclist.core.model.Filter;
import edu.utah.sci.cyclist.core.model.Table;

public class FilterArea extends ToolBar {

	private ObservableList<Filter> _filters = FXCollections.observableArrayList();
	private ObservableList<Filter> _remoteFilters = FXCollections.observableArrayList();
	private ObjectProperty<EventHandler<FilterEvent>> _action = new SimpleObjectProperty<>();
	private int _lastRemoteFilter = 0;
	private BooleanProperty _showRemote = new SimpleBooleanProperty();
	private ObjectProperty<Table> _tableProperty = new SimpleObjectProperty<>();

	public FilterArea() {
		build();
	}

	public ObservableList<Filter> getFilters() {
		return _filters;
	}

	public ObservableList<Filter> getRemoteFilters() {
		return _remoteFilters;
	}

	public ObjectProperty<EventHandler<FilterEvent>> onAction() {
		return _action;
	}

	public void setOnAction( EventHandler<FilterEvent> handler) {
		_action.set(handler);
	}

	public EventHandler<FilterEvent> getOnAction() {
		return _action.get();
	}

	public ObjectProperty<Table> tableProperty() {
		return _tableProperty;
	}

	public Table getTable() {
		return _tableProperty.get();
	}

	public void copy(FilterArea other) {
		_filters.addAll(other._filters);
		_remoteFilters.addAll(other._remoteFilters);
	}
	
	public BooleanProperty showRemoteProperty() {
		return _showRemote;
	}
	
	public boolean getShowRemote() {
		return _showRemote.get();
	}
	
	public void setShowRemote(boolean value) {
		_showRemote.set(value);
	}

	private void build() {
		getStyleClass().add("filter-area");
		setOrientation(Orientation.HORIZONTAL);

		_filters.addListener(new ListChangeListener<Filter>() {
			@Override
			public void onChanged(ListChangeListener.Change<? extends Filter> c) {
				while (c.next()) {
					if (c.wasAdded()) {
						for (Filter filter : c.getAddedSubList()) {
							FilterGlyph glyph = createFilterGlyph(filter, false);
							getItems().add(glyph);
						}
					} else if (c.wasRemoved()) {
						for (Filter filter : c.getRemoved()) {
							for (Node node : getItems()) {
								FilterGlyph glyph = (FilterGlyph) node;
								if (glyph.getFilter() == filter) {
									getItems().remove(glyph);
									break;
								}
							}
						}
					}
				}
			}

		});

		_remoteFilters.addListener(new ListChangeListener<Filter>() {

			@Override
			public void onChanged(ListChangeListener.Change<? extends Filter> c) {
				java.util.List<Filter> addedFilters = new ArrayList<Filter>(); 
				while (c.next()) {
					if (c.wasAdded()) {
						for (Filter filter : c.getAddedSubList()) {
							FilterGlyph glyph = createFilterGlyph(filter, true);
							glyph.managedProperty().bind(showRemoteProperty());
							glyph.visibleProperty().bind(showRemoteProperty());
							getItems().add(_lastRemoteFilter++, glyph);
							addedFilters.add(filter);
						}
					} else if (c.wasRemoved()) {
						for (Filter filter : c.getRemoved()) {
							for (Node node : getItems()) {
								FilterGlyph glyph = (FilterGlyph) node;
								if (glyph.getFilter() == filter) {
									getItems().remove(glyph);
									_lastRemoteFilter--;
									break;
								}
							}
						}
					}
				}
			}

		});

		//Change the glyph display when the selected table changes.
		tableProperty().addListener(new InvalidationListener() {

			@Override
			public void invalidated(Observable observable) {
				Table table = tableProperty().get();
				if (table == null) {

				} else {
					for (Node node : getItems()) {
						FilterGlyph glyph = (FilterGlyph) node;
						glyph.validProperty().set(table.hasField(glyph.getFilter().getField()));
					}
				}

			}
		});
	}

	private FilterGlyph createFilterGlyph(Filter filter, boolean remote) {

		boolean filterIsValid = true;
		if(tableProperty().get() != null){
			filterIsValid = tableProperty().get().hasField(filter.getField());
		}

		final FilterGlyph glyph = new FilterGlyph(filter, remote, filterIsValid);
		glyph.setDisable(remote);

		glyph.setOnDragDetected(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				//                                Field field = glyph.getFilter().getField();
				Filter filter  = glyph.getFilter();
				Dragboard db = glyph.startDragAndDrop(TransferMode.COPY);

				DnD.LocalClipboard clipboard = DnD.getInstance().createLocalClipboard();
				clipboard.put(DnD.FILTER_FORMAT, Filter.class, filter);

				ClipboardContent content = new ClipboardContent();
				content.putString(filter.getName());

				clipboard.put(DnD.DnD_SOURCE_FORMAT, FilterArea.class, FilterArea.this);

				SnapshotParameters snapParams = new SnapshotParameters();
				snapParams.setFill(Color.TRANSPARENT);

				content.putImage(glyph.snapshot(snapParams, null));        

				db.setContent(content);

				//                                getChildren().remove(glyph);
				//                                getFilters().remove(glyph.getFilter());
			}
		});

		glyph.setOnDragDone(new EventHandler<DragEvent>() {

			@Override
			public void handle(DragEvent event) {
			}
		});


		glyph.setOnAction(new EventHandler<FilterEvent>() {
			@Override
			public void handle(FilterEvent event) {
				if (event.getEventType() == FilterEvent.SHOW ) {
					if (getOnAction() != null) {
						getOnAction().handle(event);
					}
				} else if (event.getEventType() == FilterEvent.DELETE) {
                        if (getOnAction() != null) {
                        	getOnAction().handle(event);
                        }
						getFilters().remove(glyph.getFilter());
					} else if(event.getEventType() == FilterEvent.REMOVE_FILTER_FIELD){
						//If filter is connected to a numeric field - clean the field connection as well.
						if (getOnAction() != null) {
							getOnAction().handle(event);
						}
					}
			}
		});
		return glyph;
	}
}