package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.button.ToggleButton;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;

public class InterfacesResiduesPanel extends FormPanel 
{
	private ResiduesPanel firstStructure;
	private ResiduesPanel secondStructure;
	
	private ToolBar toolbar;

	private SimpleComboBox<String> residuesFilterComboBox;
	
	private boolean showAll;

	public InterfacesResiduesPanel(MainController mainController)
	{
		// infoPanel.setFrame(true);
		this.getHeader().setVisible(false);
		this.setBodyBorder(false);
		this.setBorders(false);
		this.setLayout(new RowLayout(Orientation.HORIZONTAL));

		firstStructure = new ResiduesPanel(this,
										   MainController.CONSTANTS.interfaces_residues_panel_first_structure(), 
										   mainController);
		
		secondStructure = new ResiduesPanel(this,
											MainController.CONSTANTS.interfaces_residues_panel_second_structure(),
											mainController);

		this.add(firstStructure, new RowData(0.45, 1, new Margins(0)));

		FormPanel breakPanel = new FormPanel();
		breakPanel.setBodyBorder(false);
		breakPanel.setBorders(false);
		breakPanel.getHeader().setVisible(false);
		this.add(breakPanel, new RowData(0.1, 1, new Margins(0)));

		this.add(secondStructure, new RowData(0.45, 1, new Margins(0)));
		
		toolbar = new ToolBar();  
		
		residuesFilterComboBox = new SimpleComboBox<String>();
		residuesFilterComboBox.setId("residuesfilter");
		residuesFilterComboBox.setTriggerAction(TriggerAction.ALL);  
		residuesFilterComboBox.setEditable(false);  
		residuesFilterComboBox.setFireChangeEventOnSetValue(true);  
		residuesFilterComboBox.setWidth(100);  
		residuesFilterComboBox.add("All");  
		residuesFilterComboBox.add("Rim/Core");  
		residuesFilterComboBox.setSimpleValue("All");
		
		residuesFilterComboBox.setFieldLabel("Residues");
		residuesFilterComboBox.addListener(Events.Change, new Listener<FieldEvent>() 
		{  
			public void handleEvent(FieldEvent be) 
			{
				if(residuesFilterComboBox.getValue().getValue().equals("All"))
				{
					setShowAll(true);
				}
				else
				{
					setShowAll(false);
				}
				
				firstStructure.applyFilter();
				secondStructure.applyFilter();
			}  
		}); 
		
		toolbar.add(new FillToolItem());
		
		toolbar.add(residuesFilterComboBox);  
		
		this.setTopComponent(toolbar);
	}

	public ResiduesPanel getFirstStructurePanel() {
		return firstStructure;
	}

	public ResiduesPanel getSecondStructurePanel() {
		return secondStructure;
	}

	public void setShowAll(boolean showAll) {
		this.showAll = showAll;
	}

	public boolean isShowAll() {
		return showAll;
	}
}
