package ch.systemsx.sybit.crkwebui.shared.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Settings stored on the server side and transfered to the client during initialization.
 * @author srebniak_a
 *
 */
public class ApplicationSettings implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Supported scoring methods.
	 */
	private List<SupportedMethod> scoresTypes;

	/**
	 * Grid settings - columns specifications.
	 */
	private Map<String, String> gridProperties;

	/**
	 * Input parameters.
	 */
	private InputParameters defaultParametersValues;

	/**
	 * Available values for reduced alphabet.
	 */
	private List<Integer> reducedAlphabetList;
	
	/**
	 * Available values for search mode.
	 */
	private List<String> searchModeList;
	
	// *********************************************
	// * Settings for captcha
	// *********************************************
	private int nrOfJobsForSession;
	private boolean useCaptcha;
	private String captchaPublicKey;
	private int nrOfAllowedSubmissionsWithoutCaptcha;
	// *********************************************
	
	/**
	 * Location where generated files are stored.
	 */
	private String resultsLocation;
	
	/**
	 * Human readable names of the run parameters.
	 */
	private Map<String, String> runParametersNames;
	
	/**
	 * Information which will be displayed in the bottom panel when accessing the page - e.g. when the server will not be accessible
	 * this information is loaded by each new access to the server so that it does not require the server to be restarted.
	 */
	private String notificationOnStart;
	
	/**
	 * News informing about new version of the application.
	 */
	private String newsMessage;
	
	/**
	 * Base url to pdb.org
	 */
	private String pdbLinkUrl;
	
	/**
	 * Base url to the uniprot.
	 */
	private String uniprotLinkUrl;
	
	/**
	 * Url to published paper.
	 */
	private String publicationLinkUrl;
	
	/**
	 * Content of the help panel.
	 */
	private String helpPageContent;
	
	/**
	 * Content of the downloads panel.
	 */
	private String downloadsPageContent;
	
	/**
	 * Flag pointing whether precompiled values stored in the db should be used.
	 */
	private boolean usePrecompiledResults;
	
	/**
	 * Name of the example pdb entry which is to be displayed in input view.
	 */
	private String examplePdb;

	/**
	 * Settings of main screen.
	 */
	private ScreenSettings screenSettings;
	
	/**
	 * Session identifier.
	 */
	private String sessionId;
	
	public ApplicationSettings()
	{
		this.scoresTypes = new ArrayList<SupportedMethod>();
	}

	/**
	 * Sets list of supported methods.
	 * @param scoresTypes list of supported methods
	 */
	public void setScoresTypes(List<SupportedMethod> scoresTypes) {
		this.scoresTypes = scoresTypes;
	}

	/**
	 * Retrieves list of supported methods.
	 * @return list of supported methods
	 */
	public List<SupportedMethod> getScoresTypes() {
		return scoresTypes;
	}

	/**
	 * Sets grid settings.
	 * @param gridProperties grid settings
	 */
	public void setGridProperties(Map<String, String> gridProperties) {
		this.gridProperties = gridProperties;
	}

	/**
	 * Retrieves grid settings.
	 * @return grid settings
	 */
	public Map<String, String> getGridProperties() {
		return gridProperties;
	}

	/**
	 * Sets default input parameters.
	 * @param defaultParametersValues default input parameters
	 */
	public void setDefaultParametersValues(
			InputParameters defaultParametersValues) {
		this.defaultParametersValues = defaultParametersValues;
	}

	/**
	 * Retrieves default input parameters.
	 * @return default input parameters
	 */
	public InputParameters getDefaultParametersValues() {
		return defaultParametersValues;
	}

	/**
	 * Sets possible values of reduced alphabet.
	 * @param reducedAlphabetList list of allowed values for reduced alphabet
	 */
	public void setReducedAlphabetList(List<Integer> reducedAlphabetList) {
		this.reducedAlphabetList = reducedAlphabetList;
	}

	/**
	 * Retrieves list of allowed values for reduced alphabet.
	 * @return list of allowed values for reduced alphabet
	 */
	public List<Integer> getReducedAlphabetList() {
		return reducedAlphabetList;
	}

	/**
	 * Sets nr of submitted jobs for current session.
	 * @param nrOfJobsForSession nr of submitted jobs for current session
	 */
	public void setNrOfJobsForSession(int nrOfJobsForSession) {
		this.nrOfJobsForSession = nrOfJobsForSession;
	}

	/**
	 * Retrieves nr of submitted jobs for current session.
	 * @return nr of submitted jobs for current session
	 */
	public int getNrOfJobsForSession() {
		return nrOfJobsForSession;
	}

	/**
	 * Retrieves location of generated results.
	 * @return location of generated results
	 */
	public String getResultsLocation() {
		return resultsLocation;
	}

	/**
	 * Sets location where results are accessible.
	 * @param resultsLocation location where results are accessible
	 */
	public void setResultsLocation(String resultsLocation) {
		this.resultsLocation = resultsLocation;
	}
	
	/**
	 * Retrieves information whether captcha validation should be used.
	 * @return information whether captcha validation should be used
	 */
	public boolean isUseCaptcha() {
		return useCaptcha;
	}

	/**
	 * Sets whether captcha validation should be used.
	 * @param useCaptcha information whether captcha validation should be used
	 */
	public void setUseCaptcha(boolean useCaptcha) {
		this.useCaptcha = useCaptcha;
	}

	/**
	 * Retrieves captcha public key.
	 * @return captcha public key
	 */
	public String getCaptchaPublicKey() {
		return captchaPublicKey;
	}

	/**
	 * Sets captcha public key.
	 * @param captchaPublicKey captcha public key
	 */
	public void setCaptchaPublicKey(String captchaPublicKey) {
		this.captchaPublicKey = captchaPublicKey;
	}
	
	/**
	 * Sets max nr of allowed submissions without captcha validation.
	 * @param nrOfAllowedSubmissionsWithoutCaptcha max nr of submissions without captcha validation 
	 */
	public void setNrOfAllowedSubmissionsWithoutCaptcha(
			int nrOfAllowedSubmissionsWithoutCaptcha) {
		this.nrOfAllowedSubmissionsWithoutCaptcha = nrOfAllowedSubmissionsWithoutCaptcha;
	}

	/**
	 * Retrieves max nr of submissions without captcha validation.
	 * @return max nr of submissions without captcha validation
	 */
	public int getNrOfAllowedSubmissionsWithoutCaptcha() {
		return nrOfAllowedSubmissionsWithoutCaptcha;
	}

	/**
	 * Sets human readable names of run parameters.
	 * @param runParametersNames human readable names of run parameters
	 */
	public void setRunParametersNames(Map<String, String> runParametersNames) {
		this.runParametersNames = runParametersNames;
	}

	/**
	 * Retrieves human readable names of run parameters.
	 * @return human readable names of run parameters
	 */
	public Map<String, String> getRunParametersNames() {
		return runParametersNames;
	}

	/**
	 * Sets text which is to be displayed as status.
	 * @param notificationOnStart text which is to be displayed as status
	 */
	public void setNotificationOnStart(String notificationOnStart) {
		this.notificationOnStart = notificationOnStart;
	}

	/**
	 * Retrieves text which is to be displayed as status.
	 * @return text which is to be displayed as status
	 */
	public String getNotificationOnStart() {
		return notificationOnStart;
	}
	
	/**
	 * Retrieves news about new features of the system.
	 * @return news about new features of the system
	 */
	public String getNewsMessage() {
		return newsMessage;
	}

	/**
	 * Sets news about new features of the system.
	 * @param newsMessage news about new features of the system
	 */
	public void setNewsMessage(String newsMessage) {
		this.newsMessage = newsMessage;
	}

	/**
	 * Sets base url to pdb.org
	 * @param pdbLinkUrl base url to pdb.org
	 */
	public void setPdbLinkUrl(String pdbLinkUrl) {
		this.pdbLinkUrl = pdbLinkUrl;
	}

	/**
	 * Retrieves base url to pdb.org.
	 * @return base url to pdb.org
	 */
	public String getPdbLinkUrl() {
		return pdbLinkUrl;
	}

	/**
	 * Sets list of allowed values for search mode.
	 * @param searchModeList list of allowed values for search mode
	 */
	public void setSearchModeList(List<String> searchModeList) {
		this.searchModeList = searchModeList;
	}

	/**
	 * Retrieves list of allowed values for search mode.
	 * @return list of allowed values for search mode
	 */
	public List<String> getSearchModeList() {
		return searchModeList;
	}

	/**
	 * Sets base url to uniprot.
	 * @param uniprotLinkUrl base url to uniprot
	 */
	public void setUniprotLinkUrl(String uniprotLinkUrl) {
		this.uniprotLinkUrl = uniprotLinkUrl;
	}

	/**
	 * Retrieves base url to uniprot.
	 * @return base url to uniprot
	 */
	public String getUniprotLinkUrl() {
		return uniprotLinkUrl;
	}
	
	/**
	 * Sets url to publication.
	 * @param publicationLinkUrl url to published paper
	 */
	public void setPublicationLinkUrl(String publicationLinkUrl) {
		this.publicationLinkUrl = publicationLinkUrl;
	}

	/**
	 * Retrieves url to publication.
	 * @return url to publication
	 */
	public String getPublicationLinkUrl() {
		return publicationLinkUrl;
	}

	/**
	 * Sets content which is to be used in help page.
	 * @param helpPageContent content of the help page
	 */
	public void setHelpPageContent(String helpPageContent) {
		this.helpPageContent = helpPageContent;
	}

	/**
	 * Retrieves content which is to be set as help.
	 * @return content of help panel
	 */
	public String getHelpPageContent() {
		return helpPageContent;
	}
	
	/**
	 * Sets content which is to be used in downloads page.
	 * @param downloadsPageContent content of the downloads page
	 */
	public void setDownloadsPageContent(String downloadsPageContent) {
		this.downloadsPageContent = downloadsPageContent;
	}

	/**
	 * Retrieves content which is to be set as downloads.
	 * @return content of downloads panel
	 */
	public String getDownloadsPageContent() {
		return downloadsPageContent;
	}

	/**
	 * Sets information whether precompiled results stored in the db should be used.
	 * @param usePrecompiledResults information whether precompiled values stored in the db should be used
	 */
	public void setUsePrecompiledResults(boolean usePrecompiledResults) {
		this.usePrecompiledResults = usePrecompiledResults;
	}

	/**
	 * Retrieves information whether precompiled results stored in the db should be used.
	 * @return whether precompiled results stored in the db should be used
	 */
	public boolean isUsePrecompiledResults() {
		return usePrecompiledResults;
	}

	/**
	 * Sets name of the example pdb entry to display in input panel.
	 * @param examplePdb name of the example pdb entry
	 */
	public void setExamplePdb(String examplePdb) {
		this.examplePdb = examplePdb;
	}

	/**
	 * Retrieves name of the example pdb entry to display in input panel.
	 * @return name of the example pdb entry
	 */
	public String getExamplePdb() {
		return examplePdb;
	}

	/**
	 * Sets settings of main application screen.
	 * @param screenSettings settings of main application screen
	 */
	public void setScreenSettings(ScreenSettings screenSettings) {
		this.screenSettings = screenSettings;
	}
	
	/**
	 * Retrieves settings of main application screen.
	 * @return settings of main application screen
	 */
	public ScreenSettings getScreenSettings() {
		return screenSettings;
	}
}
