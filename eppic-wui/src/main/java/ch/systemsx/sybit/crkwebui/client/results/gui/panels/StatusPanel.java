package ch.systemsx.sybit.crkwebui.client.results.gui.panels;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.gui.panels.DisplayPanel;
import ch.systemsx.sybit.crkwebui.client.commons.services.eppic.CrkWebServiceProvider;
import ch.systemsx.sybit.crkwebui.client.commons.util.EscapedStringGenerator;
import ch.systemsx.sybit.crkwebui.shared.model.ProcessingInProgressData;
import ch.systemsx.sybit.shared.model.StatusOfJob;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.core.client.util.Padding;
import com.sencha.gxt.widget.core.client.FramedPanel;
import com.sencha.gxt.widget.core.client.ProgressBar;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BoxLayoutContainer.BoxLayoutPack;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer.HorizontalLayoutData;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.form.FormPanel;
import com.sencha.gxt.widget.core.client.form.TextArea;

/**
 * Panel used to display status of submitted job.
 * @author srebniak_a, nikhil
 *
 */
public class StatusPanel extends DisplayPanel
{
	private DockLayoutPanel dock;			//main container
	
	private FormPanel formPanel;

	//private IdentifierHeaderPanel identifierHeaderPanel;
	
	//Displays when the job is running
	private HorizontalLayoutContainer statusContainer;
	
	//Displays when error occurs while loading
	
	//Elements from status container
	private HTML jobId;
	private HTML status;
	private Image runningImage;
	private TextArea log;
	private TextButton killJob;
	private TextButton newJob;
	private ProgressBar statusBar;
	private HTML userMessage;
	
	public StatusPanel()
	{
		init();
	}

	/**
	 * Initializes content of the panel.
	 */
	private void init()
	{
		
		dock = new DockLayoutPanel(Unit.PX);
		dock.addStyleName("eppic-default-font");
		
		//identifierHeaderPanel = new IdentifierHeaderPanel(ApplicationContext.getWindowData().getWindowWidth() - 150, null, ResultsPanel.ASSEMBLIES_VIEW); 
		//dock.addNorth(identifierHeaderPanel,50);
		
		statusContainer = createStatusContainer();
		
		dock.add(statusContainer);
		
		this.setData(dock);

	}
	
	/**
	 * Creates the container with log, kill job button, status etc.
	 * @return the container
	 */
	private HorizontalLayoutContainer createStatusContainer(){
		HorizontalLayoutContainer statusContainer = new HorizontalLayoutContainer();
		statusContainer.getElement().setPadding(new Padding(20,0,10,0));
		statusContainer.addStyleName("eppic-default-font");

		FramedPanel framedPanel = new FramedPanel();
		framedPanel.getHeader().setVisible(false);
		framedPanel.setButtonAlign(BoxLayoutPack.CENTER);
		
		formPanel = new FormPanel();
		framedPanel.setWidget(formPanel);
		
		VerticalLayoutContainer formContainer = new VerticalLayoutContainer();
		
		HorizontalLayoutContainer statusTitleContainer = new HorizontalLayoutContainer();
		
		status = new HTML();
		status.addStyleName("eppic-status-header");
		statusTitleContainer.add(status, new HorizontalLayoutData(-1, 30, new Margins(0, 20, 0, 0)));
		
		runningImage = createRunnungImage();
		//HorizontalLayoutContainer imageCon = new HorizontalLayoutContainer();
		//imageCon.add(runningImage, new HorizontalLayoutData(-1,-1, new Margins(10, 0, 0, 0)));
		//statusTitleContainer.add(imageCon, new HorizontalLayoutData(-1, 30));
		
		formContainer.add(statusTitleContainer, new VerticalLayoutData(-1, 30));
		
		formContainer.add(createStatusDataPanel(), new VerticalLayoutData(-1, 120));		
		
		userMessage = new HTML();
		formContainer.add(userMessage, new VerticalLayoutData(-1, -1));
		
		log = createLogTextarea();
		formContainer.add(log, new VerticalLayoutData(1,1, new Margins(20,0,0,0)));

		formPanel.setWidget(formContainer);
		
		statusContainer.add(new SimpleContainer(), new HorizontalLayoutData(0.05,1));
		statusContainer.add(formPanel, new HorizontalLayoutData(0.90,1));
		statusContainer.add(new SimpleContainer(), new HorizontalLayoutData(0.05,1));
		
		//TRY THIS BUT DOUBT IT WILL WORK!!!
		//ResultsPanel.headerPanel.pdbIdentifierPanel.informationLabel.setHTML("Assembly Analysis of: ");
		//ResultsPanel.headerPanel.pdbIdentifierPanel.pdbNameLabel.setHTML("<a target='_blank' href='http://www.pdb.org/pdb/explore/explore.do?structureId="+ApplicationContext.getPdbInfo().getPdbCode()+"'>"+ApplicationContext.getPdbInfo().getPdbCode()+"</a>");

		return statusContainer;
	}
	
	private HTML createErrorMessage() {
	    HTML errorLogLabel = new HTML(AppPropertiesManager.CONSTANTS.status_panel_error_message());
	    errorLogLabel.addStyleName("eppic-status-panel-error-message");
	    return errorLogLabel;
	}
	
	/**
	 * Creates the panel with progress bar, stop icon and jobId
	 */
	private VerticalLayoutContainer createStatusDataPanel(){
		VerticalLayoutContainer mainContainer = new VerticalLayoutContainer();
		
		statusBar = new ProgressBar();
		statusBar.setPixelSize(350, 20);
		
		HTML jobIdLabel = new HTML(AppPropertiesManager.CONSTANTS.status_panel_jobId()+":&nbsp;");
		jobIdLabel.addStyleName("eppic-status-jobId");
		jobId = new HTML();
		jobId.addStyleName("eppic-status-jobId");
		
		HorizontalLayoutContainer jobIdPanel = new HorizontalLayoutContainer();
		jobIdPanel.add(jobIdLabel);
		jobIdPanel.add(jobId);

		VerticalLayoutContainer barAndIdCon = new VerticalLayoutContainer();
		barAndIdCon.add(statusBar, new VerticalLayoutData(-1, -1, new Margins(0,10,5,0)));
		barAndIdCon.add(jobIdPanel);
		
		mainContainer.add(barAndIdCon, new VerticalLayoutData(370, 40));
		
		killJob = createStopJobButton();
		newJob = createNewJobButton();
		
		HorizontalLayoutContainer buttonContainer = new HorizontalLayoutContainer();
		buttonContainer.add(newJob, new HorizontalLayoutData(-1, -1, new Margins(30, 30, 10, 0)));
		buttonContainer.add(killJob, new HorizontalLayoutData(-1, -1, new Margins(30, 30, 10, 0)));
		
		mainContainer.add(buttonContainer);
		
		return mainContainer;
		
	}
	
	/**
	 * Creates running image 
	 */
	private Image createRunnungImage(){
		Image img = new Image("resources/icons/running.gif");
		img.getElement().<XElement>cast().applyStyles("verticalAlign:bottom;");
		
		return img;
	}
	
	/**
	 * Creates text area used to store log of processing.
	 * @return log textarea
	 */
	private TextArea createLogTextarea()
	{
		TextArea log = new TextArea();
		log.setReadOnly(true);
		log.addStyleName("eppic-status-log");
		return log;
	}
	
	/**
	 * Creates button used to stop execution of the job
	 * @return stop job button
	 */
	private TextButton createStopJobButton()
	{
		TextButton killJob = new TextButton(AppPropertiesManager.CONSTANTS.status_panel_stop());
		killJob.addSelectHandler(new SelectHandler() {
			
			@Override
			public void onSelect(SelectEvent event) {
				CrkWebServiceProvider.getServiceController().stopJob(jobId.getHTML());
				
			}
		});

		killJob.setWidth(100);
		
		return killJob;
	}
	
	/**
	 * Creates button used to start new job
	 * @return stop job button
	 */
	private TextButton createNewJobButton()
	{
		TextButton newJob = new TextButton(AppPropertiesManager.CONSTANTS.status_panel_new_job());
		newJob.addSelectHandler(new SelectHandler() {
			
			@Override
			public void onSelect(SelectEvent event) {
				History.newItem("");
				
			}
		});

		newJob.setWidth(100);
		
		return newJob;
	}

	/**
	 * Sets content of the status panel.
	 * @param statusData status data of selected job
	 */
	public void fillData(ProcessingInProgressData statusData)
	{
		dock.remove(statusContainer);
		dock.add(statusContainer);
		enableControls();
		
		int scrollBefore = log.getElement().getFirstChildElement().getScrollTop();
		log.setValue(statusData.getLog());
		log.getElement().getFirstChildElement().setScrollTop(scrollBefore);

		status.setHTML(String.valueOf(statusData.getStatus()));
		jobId.setHTML(statusData.getJobId());
		//identifierHeaderPanel.setPDBText(statusData.getInputName(), null, null, 0, 0, statusData.getInputType());
		//identifierHeaderPanel.setEmptyDownloadResultsLink();

		if(status.getHTML() != null){
			if ((status.getHTML().equals(StatusOfJob.RUNNING.getName())) ||
					 (status.getHTML().equals(StatusOfJob.WAITING.getName())) ||
					 (status.getHTML().equals(StatusOfJob.QUEUING.getName())) ||
					  status.getHTML().equals(StatusOfJob.ERROR.getName()))
			{
				runningImage.setVisible(true);
				
				if(!status.getHTML().equals(StatusOfJob.ERROR.getName())) {
        				String message = AppPropertiesManager.CONSTANTS.status_panel_subtitle();
        				String link = GWT.getHostPageBaseURL() + "#id/" + 
        						EscapedStringGenerator.generateEscapedString(statusData.getJobId());
        				message = message.replaceFirst("%s", "<a href='"+link+"'>");
        				message = message.replaceFirst("%s", "</a>");
        				userMessage.setHTML(message);
				}
				killJob.setVisible(true);

				double processCompleted =  (statusData.getStep().getCurrentStepNumber()*1.0)/
						(statusData.getStep().getTotalNumberOfSteps()*1.0);
				String progressText = statusData.getStep().getCurrentStep();

				if(statusData.getStep().getCurrentStepNumber() != 0)
					progressText +=   " (" + statusData.getStep().getCurrentStepNumber() +
					"/" + statusData.getStep().getTotalNumberOfSteps() + ")";
				
				statusBar.updateProgress(processCompleted, progressText);
				if(status.getHTML().equals(StatusOfJob.ERROR.getName())){
				    	disableControls();
				    //here was the message
				    status.setHTML(StatusOfJob.ERROR.getName() + ": " + statusData.getInputName());
					userMessage.setHTML(createErrorMessage().toString());
				}

			}
			else if (status.getHTML().equals(StatusOfJob.STOPPED.getName())){
				userMessage.setHTML(AppPropertiesManager.CONSTANTS.status_panel_stopped_text());
				killJob.setVisible(false);
				runningImage.setVisible(false);
				statusBar.updateProgress(0.0, StatusOfJob.STOPPED.getName() + ": " + statusData.getInputName());
			}
		}
		else
		{
			userMessage.setHTML("");
			killJob.setVisible(false);
			runningImage.setVisible(false);
			statusBar.updateProgress(0.0, "");
		}
	}

	private void enableControls() {
	    statusBar.setVisible(true);
	    killJob.setVisible(true);
	    newJob.setVisible(true);
	}
	
	private void disableControls() {
	    statusBar.setVisible(false);
	    killJob.setVisible(false);
	    //newJob.setVisible(false);
	}

	/**
	 * Cleans content of status panel.
	 */
	public void cleanData()
	{
		log.setValue("");
		status.setHTML("");
		jobId.setHTML("");
		//identifierHeaderPanel.setPDBText("", null, null, 0, 0, InputType.NONE.getIndex());
		userMessage.setHTML("");
		statusBar.updateProgress(0, "");
		//identifierHeaderPanel.setEmptyDownloadResultsLink();
	}
}
