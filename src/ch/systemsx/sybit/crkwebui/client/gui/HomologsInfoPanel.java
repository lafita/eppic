package ch.systemsx.sybit.crkwebui.client.gui;

import java.util.List;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;
import ch.systemsx.sybit.crkwebui.shared.model.HomologsInfoItem;
import ch.systemsx.sybit.crkwebui.shared.model.QueryWarningItem;

import com.extjs.gxt.ui.client.core.Template;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.tips.ToolTip;
import com.google.gwt.core.client.GWT;

/**
 * Panel used to store information about homologs.
 * @author AS
 *
 */
public class HomologsInfoPanel extends LayoutContainer
{
	public HomologsInfoPanel(final MainController mainController,
							 final HomologsInfoItem homologsInfoItem,
							 final InfoPanel infoPanel)
	{
		
		if(homologsInfoItem.isHasQueryMatch())
		{
			final EmptyLinkWithTooltip chainsLink = new EmptyLinkWithTooltip("Chain "+homologsInfoItem.getChains(), 
																	   		 MainController.CONSTANTS.homologs_panel_chains_hint(),
																	   		 mainController, 
																	   		 0);
			
			chainsLink.addStyleName("eppic-action");
			
			chainsLink.addListener(Events.OnClick, new Listener<BaseEvent>() {
	
				@Override
				public void handleEvent(BaseEvent be) {
					mainController.showAlignments(homologsInfoItem, 
												  chainsLink.getAbsoluteLeft() + chainsLink.getWidth(),
												  chainsLink.getAbsoluteTop() + chainsLink.getHeight() + 10);
				}
				
			});
			
			this.add(chainsLink);
			
			Label startUniprotLabel = new Label(" (");
			this.add(startUniprotLabel);
			
			LinkWithTooltip uniprotIdLabel = new LinkWithTooltip(homologsInfoItem.getUniprotId(), 
																 MainController.CONSTANTS.homologs_panel_uniprot_hint(),
																 mainController, 
																 0, 
																 mainController.getSettings().getUniprotLinkUrl() + homologsInfoItem.getUniprotId());
			uniprotIdLabel.addStyleName("eppic-external-link");
			this.add(uniprotIdLabel);
			
			Label endUniprotLabel = new Label(") ");
			this.add(endUniprotLabel);
			
			int nrOfHomologs = homologsInfoItem.getNumHomologs();
			String nrOfHomologsText = String.valueOf(nrOfHomologs) + " homolog";
			
			if(nrOfHomologs > 1)
			{
				nrOfHomologsText += "s";
			}
			
			String alignmentId = homologsInfoItem.getChains().substring(0, 1);
			String downloadLink = GWT.getModuleBaseURL() + "fileDownload?type=fasta&id=" + mainController.getSelectedJobId() + "&alignment=" + alignmentId; 
			
			LinkWithTooltip nrHomologsLabel = new LinkWithTooltip(nrOfHomologsText, 
																  MainController.CONSTANTS.homologs_panel_nrhomologs_hint(),
																  mainController, 
																  0, 
																  downloadLink);
			
			nrHomologsLabel.addStyleName("eppic-internal-link");
			this.add(nrHomologsLabel);
		}
		else
		{
			final EmptyLink chainsLink = new EmptyLink(homologsInfoItem.getChains());
			chainsLink.addStyleName("eppic-action");
			
			final ToolTip chainsLinkTooltip = infoPanel.getQueryWarningsTooltip();
			
			chainsLink.addListener(Events.OnClick, new Listener<BaseEvent>() {

				@Override
				public void handleEvent(BaseEvent be) {
					chainsLinkTooltip.getToolTipConfig().setTemplate(new Template(generateHomologsNoQueryMatchTemplate(homologsInfoItem.getQueryWarnings())));
					chainsLinkTooltip.showAt(chainsLink.getAbsoluteLeft() + chainsLink.getWidth(),
						  					 chainsLink.getAbsoluteTop() + chainsLink.getHeight() + 10);
				}
				
			});
			
			this.add(chainsLink);
		}
	}
	
	private String generateHomologsNoQueryMatchTemplate(List<QueryWarningItem> warnings)
	{
		String warningsList = "<div><ul style=\"list-style: disc; margin: 0px 0px 0px 15px;\">";
		
		for(QueryWarningItem warning : warnings)
		{
			if((warning.getText() != null) &&
				(!warning.getText().equals("")))
			{
				warningsList += "<li>" + warning.getText() + "</li>";
			}
		}
		
		warningsList += "</ul></div>";
			
		return warningsList;
	}
	
}
