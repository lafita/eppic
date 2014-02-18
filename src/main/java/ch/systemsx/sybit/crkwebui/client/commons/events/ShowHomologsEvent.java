package ch.systemsx.sybit.crkwebui.client.commons.events;

import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowHomologsHandler;
import ch.systemsx.sybit.crkwebui.shared.model.ChainCluster;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event fired when homologs window is to be displayed.
 * @author nikhil
 */
public class ShowHomologsEvent extends GwtEvent<ShowHomologsHandler> {

	public static Type<ShowHomologsHandler> TYPE = new Type<ShowHomologsHandler>();
	
	/**
	 * homologs Info Item
	 */
	private final ChainCluster chainCluster;
	
	/**
	 * Name of the job id
	 */
	private final String jobId;
	
	/**
	 * X position of the window to display.
	 */
	private final int xPosition;
	
	/**
	 * Y position of the window to display.
	 */
	private final int yPosition;
	
	public ShowHomologsEvent(ChainCluster chainCluster,
			String jobId,
			int xPosition,
			int yPosition)
	{
		this.chainCluster = chainCluster;
		this.jobId = jobId;
		this.xPosition = xPosition;
		this.yPosition = yPosition;
	}
	
	@Override
	public Type<ShowHomologsHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(ShowHomologsHandler handler) {
		handler.onShowHomologs(this);
		
	}

	public ChainCluster getHomologsInfoItem() {
		return chainCluster;
	}

	public String getJobId() {
		return jobId;
	}

	public int getxPosition() {
		return xPosition;
	}

	public int getyPosition() {
		return yPosition;
	}

}
