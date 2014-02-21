package eppic;

import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import owl.core.connections.SiftsConnection;
import owl.core.connections.UniProtConnection;
import owl.core.connections.UniprotLocalConnection;
import owl.core.runners.blast.BlastException;
import owl.core.sequence.HomologList;
import owl.core.sequence.Sequence;
import owl.core.sequence.UniprotVerMisMatchException;
import owl.core.structure.ChainCluster;
import owl.core.structure.PdbAsymUnit;
import owl.core.structure.PdbChain;

public class ChainEvolContextList implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final Log LOGGER = LogFactory.getLog(ChainEvolContextList.class);
	
	private PdbAsymUnit pdb;
	
	private TreeMap<String, ChainEvolContext> cecs; // one per representative chain
	
	private String uniprotVer;
	private int minNumSeqs;
	private int maxNumSeqs;
	private double queryCovCutoff;
	private double homSoftIdCutoff;
	private double homHardIdCutoff;
	
	private boolean useLocalUniprot;
	private transient UniProtConnection uniprotJapiConn;
	private transient UniprotLocalConnection uniprotLocalConn;
	
	private transient SiftsConnection siftsConn;

	
	public ChainEvolContextList(PdbAsymUnit pdb, EppicParams params) throws SQLException {
		this.pdb = pdb;
		
		this.cecs = new TreeMap<String, ChainEvolContext>();
		
		// if we fail to read a version, it will stay null. Should we rather throw exception?
		this.uniprotVer = HomologList.readUniprotVer(params.getBlastDbDir());
		LOGGER.info("Using UniProt version "+uniprotVer+" for blasting");
		
		if (params.getLocalUniprotDbName()!=null) {
			this.useLocalUniprot = true;
			this.uniprotLocalConn = new UniprotLocalConnection(params.getLocalUniprotDbName());
			LOGGER.info("Using local UniProt connection to retrieve UniProtKB data. Local database: "+params.getLocalUniprotDbName());
		} else {
			this.useLocalUniprot = false;
			this.uniprotJapiConn = new UniProtConnection();
			LOGGER.info("Using remote UniProt JAPI connection to retrieve UniProtKB data");
		}
		
		for (ChainCluster chainCluster:pdb.getProtChainClusters()) {
						
			PdbChain chain = chainCluster.getRepresentative();
			
			ChainEvolContext cec = new ChainEvolContext(this, chain.getSequenceMSEtoMET(), chain.getPdbChainCode());
			
			cecs.put(chain.getPdbChainCode(), cec);
		}
		
	}
	
	public ChainEvolContextList(List<Sequence> sequences, EppicParams params) throws SQLException {
		this.pdb = null;
		
		this.cecs = new TreeMap<String, ChainEvolContext>();

		// if we fail to read a version, it will stay null. Should we rather throw exception?
		this.uniprotVer = HomologList.readUniprotVer(params.getBlastDbDir());
		LOGGER.info("Using UniProt version "+uniprotVer+" for blasting");
		
		if (params.getLocalUniprotDbName()!=null) {
			this.useLocalUniprot = true;
			this.uniprotLocalConn = new UniprotLocalConnection(params.getLocalUniprotDbName());
			LOGGER.info("Using local UniProt connection to retrieve UniProtKB data. Local database: "+params.getLocalUniprotDbName());
		} else {
			this.useLocalUniprot = false;
			this.uniprotJapiConn = new UniProtConnection();
			LOGGER.info("Using remote UniProt JAPI connection to retrieve UniProtKB data");
		}
		
		for (Sequence sequence: sequences) {
						
			ChainEvolContext cec = new ChainEvolContext(this, sequence.getSeq(), sequence.getName());
			
			cecs.put(sequence.getName(), cec);
		}
		
	}
	
	public void addChainEvolContext(String representativeChain, ChainEvolContext cec) {
		this.cecs.put(representativeChain, cec);
	}
	
	/**
	 * Gets the ChainEvolContext corresponding to the given PDB chain code (can be 
	 * any PDB chain code, representative or not)
	 * @param pdbChainCode
	 * @return
	 */
	public ChainEvolContext getChainEvolContext(String pdbChainCode) {
		return cecs.get(pdb.getProtChainCluster(pdbChainCode).getRepresentative().getPdbChainCode());
	}
	
	/**
	 * Gets the Collection of all ChainEvolContext (sorted alphabetically by representative PDB chain code) 
	 * @return
	 */
	public Collection<ChainEvolContext> getAllChainEvolContext() {
		return cecs.values();
	}
	
	public PdbAsymUnit getPdb() {
		return this.pdb;
	}
	
	public String getUniprotVer() {
		return this.uniprotVer;
	}
	
	public int getMinNumSeqs() {
		return minNumSeqs;
	}
	
	public int getMaxNumSeqs() {
		return maxNumSeqs;
	}
	
	public double getQueryCovCutoff() {
		return queryCovCutoff;
	}
	
	public double getHomSoftIdCutoff() {
		return homSoftIdCutoff;
	}
	
	public double getHomHardIdCutoff() {
		return homHardIdCutoff;
	}
	
	public boolean isUseLocalUniprot() {
		return useLocalUniprot;
	}
	
	public UniprotLocalConnection getUniProtLocalConnection() {
		return uniprotLocalConn;
	}
	
	public UniProtConnection getUniProtJapiConnection() {
		return uniprotJapiConn;
	}
	
	public SiftsConnection getSiftsConn(String siftsLocation) throws IOException {
		// we store the sifts connection here in order not to reparse the file for every chain
		if (this.siftsConn==null) {
			this.siftsConn = new SiftsConnection(siftsLocation);
		}
		return this.siftsConn;
	}
	
	public void retrieveQueryData(EppicParams params) throws EppicException {
		params.getProgressLog().println("Finding query's UniProt mappings through SIFTS or blasting");
		params.getProgressLog().print("chains: ");
		for (ChainEvolContext chainEvCont:cecs.values()) {
			params.getProgressLog().print(chainEvCont.getRepresentativeChainCode()+" ");
			try {
				chainEvCont.retrieveQueryData(params);

			} catch (BlastException e) {
				throw new EppicException(e,"Couldn't run blast to retrieve query's UniProt mapping: "+e.getMessage(),true);
			} catch (IOException e) {
				throw new EppicException(e,"Problems while retrieving query data: "+e.getMessage(),true);
			} catch (InterruptedException e) {
				throw new EppicException(e,"Thread interrupted while running blast for retrieving query data: "+e.getMessage(),true);
			} catch (Exception e) { // for any kind of exceptions thrown while connecting through uniprot JAPI
				String msg = null;
				if (useLocalUniprot) {
					msg = "Problems while retrieving query data from UniProt local database. Error "+e.getMessage();;
				} else {
					msg = "Problems while retrieving query data through UniProt JAPI. Make sure you have the latest UniProtJAPI jar, or otherwise that the UniProt server is up\n"+e.getMessage();
				}
				throw new EppicException(e, msg, true);
			}
		}
		params.getProgressLog().println();
	}
	
	public void retrieveHomologs(EppicParams params) throws EppicException {
		
		// 1) we find homologs by blasting
		blastForHomologs(params);

		// 2) we remove the identicals to the query (removal base on blast id/coverage results, no further calculations needed)
		filterIdenticalsToQuery(params);
		
		// 3) we then retrieve the uniprot kb data: sequences, tax ids and taxons for all homologs above the given hard cutoffs thresholds
		// we won't need data for any other homolog
		retrieveHomologsData(params);
		
		// now that we have all data we don't need the connections anymore
		closeConnections();
		
		// 4) and now we apply the identity (soft/hard) cutoffs, which also does redundancy reduction in each iteration
		applyIdentityCutoff(params);

		// then we filter optionally for domain of life (needs the taxon data above)
		if (params.isFilterByDomain()) filterToSameDomainOfLife();

	}
	
	private void blastForHomologs(EppicParams params) throws EppicException {
		params.getProgressLog().println("Blasting for homologs");
		params.getProgressLog().print("chains: ");
		for (ChainEvolContext chainEvCont:cecs.values()) {
			if (!chainEvCont.hasQueryMatch()) {
				// no query uniprot match, we do nothing with this sequence
				continue;
			}
			params.getProgressLog().print(chainEvCont.getRepresentativeChainCode()+" ");
						
			try {
				chainEvCont.blastForHomologs(params);

			} catch (UniprotVerMisMatchException e) {
				throw new EppicException(e, "Mismatch of Uniprot versions! "+e.getMessage(), true);
			} catch (BlastException e) {
				throw new EppicException(e,"Couldn't run blast to retrieve homologs: "+e.getMessage() ,true);
			} catch (IOException e) {
				throw new EppicException(e,"Problem while blasting for sequence homologs: "+e.getMessage(),true);
			} catch (InterruptedException e) {
				throw new EppicException(e,"Thread interrupted while blasting for sequence homologs: "+e.getMessage(),true);
			}

		}
		params.getProgressLog().println();
	}
	
	private void retrieveHomologsData(EppicParams params) throws EppicException {
		params.getProgressLog().println("Retrieving UniProtKB data");
		params.getProgressLog().print("chains: ");
		for (ChainEvolContext chainEvCont:cecs.values()) {
			if (!chainEvCont.hasQueryMatch()) {
				// no query uniprot match, we do nothing with this sequence
				continue;
			}
			
			params.getProgressLog().print(chainEvCont.getRepresentativeChainCode()+" ");
			
			// we first apply the hard identity cutoff because that's the maximum list we can ever have after applying filters, i.e. that's all data we need
			chainEvCont.applyHardIdentityCutoff(params.getHomHardIdCutoff(), params.getQueryCoverageCutoff());
			
			try {
				chainEvCont.retrieveHomologsData();
			} catch (UniprotVerMisMatchException e) {
				throw new EppicException(e, "Mismatch of UniProt versions! "+e.getMessage(), true);
			} catch (IOException e) {
				throw new EppicException(e, "Problems while retrieving homologs data: "+e.getMessage(),true);
			} catch (SQLException e) {
				throw new EppicException(e, "Problem while retrieving homologs data from UniProt local database: "+e.getMessage(), true);
			} catch (Exception e) { // for any kind of exceptions thrown while connecting through uniprot JAPI
				String msg = null;
				if (useLocalUniprot) {
					msg = "Problems while retrieving homologs data from UniProt local database. Error "+e.getMessage();
				} else {
					msg = "Problems while retrieving homologs data through UniProt JAPI. Make sure you have the latest UniProtJAPI jar, or otherwise that the UniProt server is up\n"+e.getMessage();
				}
				throw new EppicException(e, msg, true);
			}

		}		
		params.getProgressLog().println();
	}
	
	private void applyIdentityCutoff(EppicParams params) throws EppicException {
		
		this.minNumSeqs = params.getMinNumSeqs();
		this.maxNumSeqs = params.getMaxNumSeqs();
		this.queryCovCutoff = params.getQueryCoverageCutoff();
		this.homSoftIdCutoff = params.getHomSoftIdCutoff();
		this.homHardIdCutoff = params.getHomHardIdCutoff();
		
		for (ChainEvolContext chainEvCont:cecs.values()) {
			if (!chainEvCont.hasQueryMatch()) {
				// no query uniprot match, we do nothing with this sequence
				continue;
			}

			try {
				
				// applies the identity cutoffs iteratively and performs the redundancy reduction procedure 
				chainEvCont.applyIdentityCutoff(params);


			} catch (IOException e) {
				throw new EppicException(e, "Problems while running blastclust for redundancy reduction of homologs: "+e.getMessage(), true);
			} catch (InterruptedException e) {
				throw new EppicException(e, "Problems while running blastclust for redundancy reduction of homologs: "+e.getMessage(), true);
			} catch (BlastException e) {
				throw new EppicException(e, "Problems while running blastclust for redundancy reduction of homologs: "+e.getMessage(), true);
			}

			
		}
	}
	
	private void filterToSameDomainOfLife() {
		for (ChainEvolContext chainEvCont:cecs.values()) {
			if (!chainEvCont.hasQueryMatch()) {
				// no query uniprot match, we do nothing with this sequence
				continue;
			}

			chainEvCont.filterToSameDomainOfLife();
		}
	}
	
	private void filterIdenticalsToQuery(EppicParams params) {
		for (ChainEvolContext chainEvCont:cecs.values()) {
			if (!chainEvCont.hasQueryMatch()) {
				// no query uniprot match, we do nothing with this sequence
				continue;
			}
			
			// remove hits totally identical to query (based on the blast id and query coverage numbers)
			chainEvCont.removeIdenticalToQuery(params.getMinQueryCovForIdenticalsRemoval());
		}
	}
	
	public void align(EppicParams params) throws EppicException {
		
		String alignProgram = params.getTcoffeeBin()==null?"clustalo":"t_coffee"; 
		params.getProgressLog().println("Aligning protein sequences with "+ alignProgram);
		params.getProgressLog().print("chains: ");
		for (ChainEvolContext chainEvCont:cecs.values()) {
			if (!chainEvCont.hasQueryMatch()) {
				// no query uniprot match, we do nothing with this sequence
				continue;
			}
			
			params.getProgressLog().print(chainEvCont.getRepresentativeChainCode()+" ");
			try {
				chainEvCont.align(params);
			} catch (IOException e) {
				throw new EppicException(e, "Problems while running "+alignProgram+" to align protein sequences: "+e.getMessage(),true);
			} catch (InterruptedException e) {
				throw new EppicException(e, "Thread interrupted while running "+alignProgram+" to align protein sequences: "+e.getMessage(),true);
			} catch (UniprotVerMisMatchException e) {
				throw new EppicException(e, "Uniprot versions mismatch while trying to read cached alignment: "+e.getMessage(),true);
			}

		}
		params.getProgressLog().println();
	}
	
	public void computeEntropies(EppicParams params) {
		for (ChainEvolContext chainEvCont:cecs.values()) {
			if (!chainEvCont.hasQueryMatch()) {
				// no query uniprot match, we do nothing with this sequence
				continue;
			}
			
			chainEvCont.computeEntropies(params.getReducedAlphabet());
		}
	}
	
	/**
	 * Returns the Domain of Life (was known as Kingdom) of the entry.
	 * If different chains have different Domain of Life assignments then a warning is logged and null returned.
	 * If no query match is present for any entry a null is returned.
	 * @return
	 */
	public String getDomainOfLife() {
		String dol = null;

		for (ChainEvolContext chainEvCont:cecs.values()) {
			if (!chainEvCont.hasQueryMatch()) {
				// no query uniprot match, we do nothing with this sequence
				continue;
			}

			if (chainEvCont.getQuery().hasTaxons()) {
				if (dol==null) {
					// we take as dol the first one available
					dol = chainEvCont.getQuery().getFirstTaxon();
				} else {					
					if (!chainEvCont.getQuery().getFirstTaxon().equals(dol)) {
						LOGGER.warn("Different domain of lifes for different chains of the same PDB. " +
							"First chain is "+dol+" while chain "+chainEvCont.getRepresentativeChainCode()+" is "+chainEvCont.getQuery().getFirstTaxon());
						dol = null;
						break;
					}					 
				}				
			}
		}
		return dol;
	}
	
	public void closeConnections() {
		if (useLocalUniprot) {			
			this.uniprotLocalConn.close();
			LOGGER.info("Connection to local UniProt database closed");
		}
		// there doesn't seem to be a way of closing the japi connection, we do nothing in that case
	}
}
