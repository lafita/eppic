package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PDBScoreItem implements Serializable, ProcessingData
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String jobId;
	private String pdbName;
	private boolean isScoreWeighted;
	private double bioCutoff;
	private double xtalCutoff;
	private int homologsCutoff;
	private int minCoreSize;
	private int minMemberCoreSize;
	private double idCutoff;
	private double queryCovCutoff;
	private int maxNumSeqsCutoff;
	private double[] bsaToAsaCutoffs;
	private double bsaToAsaSoftCutoff;
	private double bsaToAsaRelaxStep;
	private boolean zoomUsed;
	private String title;
	
	private List<String> numHomologsStrings;

	private List<InterfaceItem> interfaceItems;
	
	private Map<InterfaceScoreItemKey, InterfaceScoreItem> interfaceScores;
	
	public PDBScoreItem() 
	{
		interfaceScores = new HashMap<InterfaceScoreItemKey, InterfaceScoreItem>();
		interfaceItems = new ArrayList<InterfaceItem>();
	}
	
	public String getPdbName() {
		return pdbName;
	}

	public void setPdbName(String pdbName) {
		this.pdbName = pdbName;
	}

	public boolean isScoreWeighted() {
		return isScoreWeighted;
	}

	public void setScoreWeighted(boolean isScoreWeighted) {
		this.isScoreWeighted = isScoreWeighted;
	}

	public double getBioCutoff() {
		return bioCutoff;
	}

	public void setBioCutoff(double bioCutoff) {
		this.bioCutoff = bioCutoff;
	}

	public double getXtalCutoff() {
		return xtalCutoff;
	}

	public void setXtalCutoff(double xtalCutoff) {
		this.xtalCutoff = xtalCutoff;
	}

	public int getHomologsCutoff() {
		return homologsCutoff;
	}

	public void setHomologsCutoff(int homologsCutoff) {
		this.homologsCutoff = homologsCutoff;
	}

	public int getMinCoreSize() {
		return minCoreSize;
	}

	public void setMinCoreSize(int minCoreSize) {
		this.minCoreSize = minCoreSize;
	}

	public int getMinMemberCoreSize() {
		return minMemberCoreSize;
	}

	public void setMinMemberCoreSize(int minMemberCoreSize) {
		this.minMemberCoreSize = minMemberCoreSize;
	}

	public double getIdCutoff() {
		return idCutoff;
	}

	public void setIdCutoff(double idCutoff) {
		this.idCutoff = idCutoff;
	}

	public double getQueryCovCutoff() {
		return queryCovCutoff;
	}

	public void setQueryCovCutoff(double queryCovCutoff) {
		this.queryCovCutoff = queryCovCutoff;
	}

	public int getMaxNumSeqsCutoff() {
		return maxNumSeqsCutoff;
	}

	public void setMaxNumSeqsCutoff(int maxNumSeqsCutoff) {
		this.maxNumSeqsCutoff = maxNumSeqsCutoff;
	}

	public double[] getBsaToAsaCutoffs() {
		return bsaToAsaCutoffs;
	}
	
	public void setBsaToAsaCutoffs(double[] bsaToAsaCutoffs) {
		this.bsaToAsaCutoffs = bsaToAsaCutoffs;
	}

	public double getBsaToAsaSoftCutoff() {
		return bsaToAsaSoftCutoff;
	}

	public void setBsaToAsaSoftCutoff(double bsaToAsaSoftCutoff) {
		this.bsaToAsaSoftCutoff = bsaToAsaSoftCutoff;
	}

	public double getBsaToAsaRelaxStep() {
		return bsaToAsaRelaxStep;
	}

	public void setBsaToAsaRelaxStep(double bsaToAsaRelaxStep) {
		this.bsaToAsaRelaxStep = bsaToAsaRelaxStep;
	}

	public boolean isZoomUsed() {
		return zoomUsed;
	}

	public void setZoomUsed(boolean zoomUsed) {
		this.zoomUsed = zoomUsed;
	}

	public String getJobId()
	{
		return jobId;
	}
	
	public void setJobId(String jobId)
	{
		this.jobId = jobId;
	}

	public void setInterfaceItems(List<InterfaceItem> interfaceItems) {
		this.interfaceItems = interfaceItems;
	}

	public List<InterfaceItem> getInterfaceItems() {
		return interfaceItems;
	}
	
	public void addInterfaceItem(InterfaceItem interfaceItem) {
		this.interfaceItems.add(interfaceItem);
	}

	public void setNumHomologsStrings(List<String> numHomologsStrings) {
		this.numHomologsStrings = numHomologsStrings;
	}
	
	public List<String> getNumHomologsStrings() {
		return this.numHomologsStrings;
	}
	
	public void setInterfaceScores(Map<InterfaceScoreItemKey, InterfaceScoreItem> interfaceScores) {
		this.interfaceScores = interfaceScores;
	}

	public Map<InterfaceScoreItemKey, InterfaceScoreItem> getInterfaceScores() {
		return interfaceScores;
	}
	
	public void addInterfaceScoreItem(InterfaceScoreItemKey interfaceScoreItemKey, InterfaceScoreItem interfaceScoreItem) {
		this.interfaceScores.put(interfaceScoreItemKey, interfaceScoreItem);
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}
}
