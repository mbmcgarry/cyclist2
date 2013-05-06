package edu.utah.sci.cyclist.view.panels;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ListViewBuilder;
import javafx.scene.control.SplitPane;
import javafx.scene.control.SplitPaneBuilder;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.util.Callback;
import edu.utah.sci.cyclist.model.Field;
import edu.utah.sci.cyclist.model.FieldProperties;
import edu.utah.sci.cyclist.model.Schema;

public class SchemaPanel extends TitledPane {
	public static final String ID 		= "schema-panel";
	public static final String TITLE	= "Schema";
	
	private Schema _schema;
	private ListView<Field> _dimensionsView;
	private ListView<Field> _measuresView;
	
	public SchemaPanel() {
		build();
	}
	
//	@Override
	public void setTitle(String title) {
		setTitle(title);
	}
	
	public void setSchema(Schema schema) {
		_schema = schema;

		ObservableList<Field> dimensions = FXCollections.observableArrayList();
		ObservableList<Field> mesures = FXCollections.observableArrayList();
		
		for (int f=0; f < schema.size(); f++) {
			Field field = schema.getField(f);
			switch (field.getString(FieldProperties.ROLE)) {
			case FieldProperties.VALUE_DIMENSION:
				dimensions.add(field);
				break;
			case FieldProperties.VALUE_MEASURE:
				mesures.add(field);
				break;
			case FieldProperties.VALUE_UNKNOWN:
				// ignore for now
				break;
			}
		}
		
		_dimensionsView.setItems(dimensions);
		_measuresView.setItems(mesures);
	}
	
	private void build() {
		setId(ID);
		setText(TITLE);
		
		SplitPane pane = SplitPaneBuilder.create()
				.maxHeight(150)
				.orientation(Orientation.VERTICAL)
				.items(
						_dimensionsView = ListViewBuilder.<Field>create()
								.prefWidth(100)
								.build(),
						_measuresView = ListViewBuilder.<Field>create()
								.prefWidth(100)
								.build()
					)
				.build();
		
		
		_dimensionsView.setCellFactory(new Callback<ListView<Field>, ListCell<Field>>() {
			
			@Override
			public ListCell<Field> call(ListView<Field> param) {
				return new FieldCell();
			}
		});
		
		_measuresView.setCellFactory(new Callback<ListView<Field>, ListCell<Field>>() {
			
			@Override
			public ListCell<Field> call(ListView<Field> param) {
				return new FieldCell();
			}
		});
		
		setContent(pane);
	}
	
	class FieldCell extends TextFieldListCell<Field> {		
			@Override
			public void updateItem(Field field, boolean empty) {
				super.updateItem(field, empty);	
				
				if (field != null) {
					setText(field.getName()+":"+field.getDataTypeName());
				} else {
					setText("");
				}
			}
	}
}