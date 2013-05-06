package edu.utah.sci.cyclist.view.components;

import java.util.ArrayList;
import java.util.List;

import edu.utah.sci.cyclist.Resources;
import edu.utah.sci.cyclist.event.dnd.DnD;
import edu.utah.sci.cyclist.event.ui.CyclistDropEvent;
import edu.utah.sci.cyclist.model.Table;
import edu.utah.sci.cyclist.view.View;



import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ProgressIndicatorBuilder;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class ViewBase extends VBox implements View {
	
	public static final double EDGE_SIZE = 4;
	
	public enum Edge { TOP, BOTTOM, LEFT, RIGHT, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, NONE };
	
	private static final Cursor[] _cursor = {
		Cursor.N_RESIZE, Cursor.S_RESIZE, Cursor.E_RESIZE, Cursor.W_RESIZE, Cursor.NE_RESIZE, Cursor.NW_RESIZE, Cursor.SE_RESIZE, Cursor.SW_RESIZE, Cursor.DEFAULT
	};
	
	private Button _closeButton;
	private Button _minmaxButton;
	
	private Label _title;
	private ProgressIndicator _indicator;
	private ObjectProperty<EventHandler<ActionEvent>> selectPropery = new SimpleObjectProperty<>();
	private ObjectProperty<EventHandler<CyclistDropEvent>> datasourceActionProperty = new SimpleObjectProperty<>();

	private boolean _maximized = false;
	private HBox _actionsArea;
	private HBox _dataBar;
	private final Resize resize = new Resize();
	
	private int _maxNumTables = 1;
	
	private List<Table> _tables = new ArrayList<>();
	
	public ViewBase() {	
		super();
//		prefWidth(100);
//		prefHeight(100);
		getStyleClass().add("view");
		
		// Title
		HBox header = HBoxBuilder.create()
				.spacing(2)
				.padding(new Insets(0, 5, 0, 5))
				.styleClass("header")
				.alignment(Pos.CENTER_LEFT)
				.children(
					_title = LabelBuilder.create().build(),
					_indicator = ProgressIndicatorBuilder.create().progress(-1).maxWidth(8).maxHeight(8).visible(false).build(),
					_dataBar = HBoxBuilder.create()
						.id("databar")
						.minWidth(30)
						.build(),
					new Spring(),
					_actionsArea = new HBox(),
					_minmaxButton = ButtonBuilder.create().styleClass("flat-button").graphic(new ImageView(Resources.getIcon("maximize"))).build(),
					_closeButton = ButtonBuilder.create().styleClass("flat-button").graphic(new ImageView(Resources.getIcon("close_view"))).build()
				)
				.build();
		setHeaderListeners(header);
		setDatasourcesListeners();
		
		getChildren().add(header);
		
		setListeners();
	}
	
	public void setTitle(String title) {
		_title.setText(title);
	}
	
	public void setMaxNumTables(int n) {
		_maxNumTables = n;
	}
	
	public int getMaxNumTables() {
		return _maxNumTables;
	}
	
	public void setWaiting(boolean value) {
		_indicator.setVisible(value);
	}
	
	public boolean isMaximized() {
		return _maximized;
	}
	
	public void setMaximized(boolean value) {
		if (_maximized != value) {
			_maximized = value;
			_minmaxButton.setGraphic(new ImageView(Resources.getIcon(value ? "restore" : "maximize")));
		}
	}
	
	/*
	 * Max/min button
	 */
	public ObjectProperty<EventHandler<ActionEvent>> onMinmaxProperty() {
		return _minmaxButton.onActionProperty();
	}
	
	public EventHandler<ActionEvent> getOnMinmax() {
		return _minmaxButton.getOnAction();
	}
	
	public void setOnMinmax(EventHandler<ActionEvent> handler) {
		_minmaxButton.setOnAction(handler);
	}
	
	/*
	 * Close 
	 */
	public ObjectProperty<EventHandler<ActionEvent>> onCloseProperty() {
		return _closeButton.onActionProperty();
	}
	
	public EventHandler<ActionEvent> getOnClose() {
		return _closeButton.getOnAction();
	}
	
	public void setOnClose(EventHandler<ActionEvent> handler) {
		_closeButton.setOnAction(handler);
	}
	
	/*
	 * Select
	 */
	public ObjectProperty<EventHandler<ActionEvent>> onSelectProperty() {
		return selectPropery;
	}
	
	public EventHandler<ActionEvent> getOnSelect() {
		return selectPropery.get();
	}
	
	public void setOnSelect(EventHandler<ActionEvent> handler) {
		selectPropery.set(handler);
	}	
	
	/*
	 * Datasource
	 */
	
	public ObjectProperty<EventHandler<CyclistDropEvent>> onDatasourceActionProperty() {
		return datasourceActionProperty;
	}
	
	public EventHandler<CyclistDropEvent> getOnDatasourceAction() {
		return datasourceActionProperty.get();
	}
	
	public void setOnDatasourceAction(EventHandler<CyclistDropEvent> handler) {
		datasourceActionProperty.set(handler);
	}	
	
	
	public void addTable(Table table) {
		if (_tables.contains(table)) {
			System.out.println("view: already has table");
			return;
		}
		if (_tables.size() < _maxNumTables)
			_tables.add(table);
		else {
			_tables.set(_maxNumTables-1, table);
		}
		Button b = ButtonBuilder.create().styleClass("flat-button").graphic(new ImageView(Resources.getIcon("table"))).build();
		_dataBar.getChildren().add(b);
	}
	/*
	 * Content
	 */
	
	protected void setContent(Parent node) {
		node.setOnMouseMoved(_onMouseMove);
		
		getChildren().add(node);
		VBox.setVgrow(node, Priority.NEVER);
	}
	
	/*
	 * 
	 */
	protected void addActions(List<ButtonBase> actions) {
		_actionsArea.getChildren().addAll(actions);
	}
	
	protected void setActions(List<ButtonBase> actions) {
		_actionsArea.getChildren().clear();
		addActions(actions);
	}
	
	/*
	 * Listeners
	 */
	private void setHeaderListeners(HBox header) {
		final ViewBase view = this;
		final Delta delta = new Delta();
		
		header.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				delta.x = getTranslateX() - event.getSceneX();
				delta.y = getTranslateY() - event.getSceneY();
				if (selectPropery.get() != null)
					selectPropery.get().handle(new ActionEvent());
			}
		});
		
		header.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				Parent parent = view.getParent();
				double maxX = parent.getLayoutBounds().getMaxX() - getWidth();				
				double maxY = parent.getLayoutBounds().getMaxY() - getHeight();
//				System.out.println("parent maxY:"+parent.getLayoutBounds().getMaxY()+"  h:"+getHeight());
//				System.out.println("delta.y: "+delta.y+"  event.sy: "+event.getSceneY()+"  maxY:"+maxY);
//				System.out.println("x: "+Math.min(Math.max(0, delta.x + event.getSceneX()), maxX)+"  y:"+Math.min(Math.max(0, delta.y+event.getSceneY()), maxY));
//				setTranslateX(Math.min(Math.max(0, delta.x+event.getSceneX()), maxX)) ;
//				setTranslateY(Math.min(Math.max(0, delta.y+event.getSceneY()), maxY));
				
				setTranslateX(delta.x+event.getSceneX()) ;
				setTranslateY(delta.y+event.getSceneY());
			}
			
		});	
		
		EventHandler<MouseEvent> eh = new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if (selectPropery.get() != null)
					selectPropery.get().handle(new ActionEvent());
			}
		};
		
		header.setOnMouseClicked(eh);
		setOnMouseClicked(eh);
	}
	
	private void setDatasourcesListeners() {
		_dataBar.setOnDragEntered(new EventHandler<DragEvent>() {

			@Override
			public void handle(DragEvent event) {
				if (event.getDragboard().getContent(DnD.DATA_SOURCE_FORMAT) != null) {
					event.acceptTransferModes(TransferMode.COPY);
				}
				event.consume();
			}
		});
		
		_dataBar.setOnDragOver(new EventHandler<DragEvent>() {

			@Override
			public void handle(DragEvent event) {
				if (event.getDragboard().getContent(DnD.DATA_SOURCE_FORMAT) != null) {
					event.acceptTransferModes(TransferMode.COPY);
				}
				event.consume();
			}
		});
		
		_dataBar.setOnDragExited(new EventHandler<DragEvent>() {

			@Override
			public void handle(DragEvent event) {
				event.consume();
			}
		});
		
		_dataBar.setOnDragDropped(new EventHandler<DragEvent>() {

			@Override
			public void handle(DragEvent event) {
				if (event.getDragboard().getContent(DnD.DATA_SOURCE_FORMAT) != null) {
					String name = (String) event.getDragboard().getContent(DnD.DATA_SOURCE_FORMAT);
					if (getOnDatasourceAction() != null) {
						getOnDatasourceAction().handle(new CyclistDropEvent(CyclistDropEvent.DROP_DATASOURCE, name, event.getX(), event.getY()));
					}
				}
				event.setDropCompleted(true);
				event.consume();
			}
		});
	}
	
	private void setListeners() {
		
		
		setOnMouseMoved(_onMouseMove);
		setOnMousePressed(_onMousePressed);
		setOnMouseDragged(_onMouseDragged);
		setOnMouseExited(_onMouseExited);
	}
	
	private Edge getEdge(MouseEvent event) {
		double x = event.getX();
		double y = event.getY();
		double right = getWidth() - EDGE_SIZE;
		double bottom = getHeight() - EDGE_SIZE;
		
		Edge edge = Edge.NONE;
		
		if (x < EDGE_SIZE) {
			if (y < EDGE_SIZE) edge = Edge.TOP_LEFT;
			else if (bottom < y) edge = Edge.BOTTOM_LEFT;
			else edge = Edge.LEFT;
		} 
		else if (right < x) {
			if (y < EDGE_SIZE) edge = Edge.TOP_RIGHT;
			else if (bottom < y) edge = Edge.BOTTOM_RIGHT;
			else edge = Edge.RIGHT;			
		}
		else if (y < EDGE_SIZE) edge = Edge.TOP;
		else if (bottom < y) edge = Edge.BOTTOM;
		
		return edge;
	}
	
	private EventHandler<MouseEvent> _onMouseMove = new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent event) {
			Edge edge = getEdge(event);
			Cursor c = _cursor[edge.ordinal()];
			if (getCursor() != c)
				setCursor(c);
		}
	};
	
	private EventHandler<MouseEvent> _onMousePressed = new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent event) {
			resize.edge = getEdge(event);
			if ( resize.edge != Edge.NONE) {
				resize.x = getTranslateX();
				resize.y = getTranslateY();
				resize.width = getWidth();
				resize.height = getHeight();
				resize.sceneX = event.getSceneX();
				resize.sceneY = event.getSceneY() ;
			}
		}
	};
	
	private EventHandler<MouseEvent> _onMouseDragged = new EventHandler<MouseEvent>() {
        public void handle(MouseEvent event) {
//			System.out.println("OnMouseDrag: "+this);
        	if (resize.edge == Edge.NONE) {
        		return;
        	}
        	
        	setMaximized(false);
        	
        	double dx = resize.sceneX - event.getSceneX();
        	double dy = resize.sceneY - event.getSceneY();
        	
        	// top/bottom
        	if (resize.edge == Edge.TOP || resize.edge == Edge.TOP_LEFT || resize.edge == Edge.TOP_RIGHT) {
        		setTranslateY(resize.y-dy);
        		setPrefHeight(resize.height+dy);
        	} else if (resize.edge == Edge.BOTTOM || resize.edge == Edge.BOTTOM_LEFT || resize.edge == Edge.BOTTOM_RIGHT){
        		//setTranslateY(resize.y+dy);
        		setPrefHeight(resize.height-dy);           		
        	}
        	
        	// left/right
        	if (resize.edge == Edge.TOP_LEFT || resize.edge == Edge.LEFT || resize.edge == Edge.BOTTOM_LEFT) {
        		setTranslateX(resize.x-dx);
        		setPrefWidth(resize.width+dx);
        	} else if (resize.edge == Edge.TOP_RIGHT || resize.edge == Edge.RIGHT || resize.edge == Edge.BOTTOM_RIGHT){
        		//setTranslateY(resize.y+dy);
        		setPrefWidth(resize.width-dx);
        	}
        	
        	event.consume();
        }
	};
	
	private EventHandler<MouseEvent> _onMouseExited = new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent event) {
			setCursor(Cursor.DEFAULT);
		}
	};
	
	class Delta {
		public double x;
		public double y;
	}

	class Resize {
		public ViewBase.Edge edge;
		public double x;
		public double y;
		public double width;
		public double height;
		public double sceneX;
		public double sceneY;
	}
}



