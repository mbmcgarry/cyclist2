/*******************************************************************************
 * Copyright (c) 2013 SCI Institute, University of Utah.
 * All rights reserved.
 *
 * License for the specific language governing rights and limitations under Permission
 * is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction, 
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions: The above copyright notice 
 * and this permission notice shall be included in all copies  or substantial portions of the Software. 
 *  
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
 *  INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR 
 *  A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR 
 *  COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER 
 *  IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION 
 *  WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Contributors:
 *     Yarden Livnat  
 *******************************************************************************/
package edu.utah.sci.cyclist.view;

import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.SplitPaneBuilder;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import edu.utah.sci.cyclist.Resources;
import edu.utah.sci.cyclist.view.components.Workspace;
import edu.utah.sci.cyclist.view.panels.SchemaPanel;
import edu.utah.sci.cyclist.view.panels.TablesPanel;
import edu.utah.sci.cyclist.view.panels.ToolsPanel;
import edu.utah.sci.cyclist.view.wizard.WorkspaceWizard;

public class MainScreen extends VBox {
	public static final String ID = "main-screen";
	
	private MenuBar _menubar;
	private SplitPane _toolsArea;
	private HBox _content;
	private TablesPanel _datasourcesPanel;
	private SchemaPanel _dimensionsPanel;
	private SchemaPanel _measuresPanel;
	private ToolsPanel _toolsPanel;
		
	/**
	 * Constructor
	 */
	public  MainScreen(Stage stage) {
		super();
		setId(ID);
		
		init(stage);
	}
	
	public Window getWindow() {
		return getScene().getWindow();
	}
	
	public ObjectProperty<String> selectWorkspace(ObservableList<String> list) {
		
		WorkspaceWizard wizard = new WorkspaceWizard();
		wizard.setItems(list);
		return wizard.show(getScene().getWindow());
	}
	
	
	public void setWorkspace(Workspace workspace) {
		HBox.setHgrow(workspace, Priority.ALWAYS);
		_content.getChildren().add(workspace);
	}
	
	public TablesPanel getDatasourcesPanel() {
		return _datasourcesPanel;
	}
	
	public SchemaPanel getDimensionPanel() {
		return _dimensionsPanel;
	}
	
	public SchemaPanel getMeauresPanel() {
		return _measuresPanel;
	}
	
	public ToolsPanel getToolsPanel() {
		return _toolsPanel;
	}
	
	private void init(Stage stage){
		// create the screen
		
		// -- menubar
		_menubar = createMenuBar(stage);
		
				
		// -- tables and schema
		_toolsArea = SplitPaneBuilder.create()
				.prefWidth(130)
				.orientation(Orientation.VERTICAL)
				.items(
						_datasourcesPanel = new TablesPanel(),	
						_dimensionsPanel = new SchemaPanel("Dimensions"),
						_measuresPanel = new SchemaPanel("Measures"),
						_toolsPanel = new ToolsPanel()
					)
				.build();
		VBox.setVgrow(_toolsArea, Priority.SOMETIMES);
		
		// -- workspace
		_content = HBoxBuilder.create()
						.children(
								_toolsArea
								// workspace
								)
						.build();
		
		// -- setup
		VBox.setVgrow(_content, Priority.ALWAYS);
		getChildren().addAll(_menubar, _content);
	}
	
	/*
	 * Menus & Actions
	 */
	
	private MenuItem _datasourceMenuItem;
	private MenuItem _workspaceMenuItem;
	private MenuItem _quitMenuItem;
	private MenuItem _saveMenuItem;
	
	
	public ObjectProperty<EventHandler<ActionEvent>> onAddDatasource() {
		return _datasourceMenuItem.onActionProperty();
	}
	
	public ObjectProperty<EventHandler<ActionEvent>> onSelectWorkspace() {
		return _workspaceMenuItem.onActionProperty();
	}
	
	public ObjectProperty<EventHandler<ActionEvent>> onSave() {
		return _saveMenuItem.onActionProperty();
	}
	
	public ObjectProperty<EventHandler<ActionEvent>> onQuit() {
		return _quitMenuItem.onActionProperty();
	}
	
	private MenuBar createMenuBar(Stage stage) {
		MenuBar menubar = new MenuBar();
		
		// -- File menu
		Menu fileMenu = createFileMenu();

		menubar.getMenus().add(fileMenu);
		return menubar;
	}
	
	private Menu createFileMenu() {
		_datasourceMenuItem = new MenuItem("Add Datatable", new ImageView(Resources.getIcon("open.png")));
		
		_workspaceMenuItem = new MenuItem("Workspace"); //new ImageView(Resources.getIcon("workspace.png")));
		
		_saveMenuItem = new MenuItem("Save");
		_saveMenuItem.setAccelerator(KeyCombination.keyCombination("Meta+S"));
		
		// -- Quit
		_quitMenuItem = new MenuItem("Quit");
		_quitMenuItem.setAccelerator(KeyCombination.keyCombination("Meta+Q"));
		
		// -- setup the menu 
		Menu fileMenu = new Menu("File");
		fileMenu.getItems().addAll(
					_datasourceMenuItem, 
					new SeparatorMenuItem(), 
					_workspaceMenuItem, 
					_saveMenuItem,
					new SeparatorMenuItem(), 
					_quitMenuItem);
		return fileMenu;
	}

}
