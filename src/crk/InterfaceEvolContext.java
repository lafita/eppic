package crk;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import owl.core.structure.ChainInterface;
import owl.core.structure.InterfaceRimCore;
import owl.core.structure.Pdb;
import owl.core.structure.Residue;

public class InterfaceEvolContext {

	private static final Log LOGGER = LogFactory.getLog(InterfaceEvolContext.class);

	private static final int FIRST  = 0;
	private static final int SECOND = 1;
	
	private static final double MAX_ALLOWED_UNREL_RES = 0.05; // 5% maximum allowed unreliable residues for core or rim
	
	private ChainInterface interf;
	private List<ChainEvolContext> chains;  // At the moment strictly 2 members (matching the 2 partners of interf). 
											// If either of the 2 molecules is not a protein then it's null.
	
	// the cache scoring values, these are filled upon call of scoreInterface
	private TreeMap<Integer,double[]> rimScores;	// cache of the last run scoreEntropy/scoreKaKs (strictly 2 members, 0 FIRST, 1 SECOND, use constants above)
	private TreeMap<Integer,double[]> coreScores;
	private double[] finalScores; // cache of the last run final scores (average of ratios of both sides), one per bsaToAsaCutoff (filled by getCalls)
	
	private TreeMap<Integer,ArrayList<Integer>> bioCalls; // cache of the votes for each bsaToAsaCutoff (bsaToAsaCutoff index to lists of interface member indices)
	private TreeMap<Integer,ArrayList<Integer>> xtalCalls;
	private TreeMap<Integer,ArrayList<Integer>> grayCalls;
	private TreeMap<Integer,ArrayList<Integer>> noPredictCalls;
		
	private ScoringType lastScoType; // the type of the last scoring run (either kaks or entropy)

	
	public InterfaceEvolContext(ChainInterface interf, List<ChainEvolContext> chains) {
		this.interf = interf;
		this.chains = chains;
		
	}

	public ChainInterface getInterface() {
		return interf;
	}
	
	/**
	 * Calculates the entropy scores for this interface.
	 * Subsequently use {@link #getCalls(double, double, int, int, int)} and {@link #getFinalScores()}
	 * to get the calls and final scores.
	 * @param weighted
	 */
	public void scoreEntropy(boolean weighted) {
		scoreInterface(weighted, ScoringType.ENTROPY);
		lastScoType = ScoringType.ENTROPY;
	}
	
	/**
	 * Calculates the ka/ks scores for this interface.
	 * Subsequently use {@link #getCalls(double, double, int, int, int)} and {@link #getFinalScores()}
	 * to get the calls and final scores. 
	 * @param weighted
	 */
	public void scoreKaKs(boolean weighted) {
		scoreInterface(weighted, ScoringType.KAKS);
		lastScoType = ScoringType.KAKS;
	}

	private void scoreInterface(boolean weighted, ScoringType scoType) {
		
		rimScores = new TreeMap<Integer, double[]>();
		coreScores = new TreeMap<Integer, double[]>();

		double[] rimScores1 = new double[this.interf.getNumBsaToAsaCutoffs()]; 
		double[] coreScores1 = new double[this.interf.getNumBsaToAsaCutoffs()];
		if (this.interf.isFirstProtein()) {
			InterfaceRimCore[] rimCores = this.interf.getFirstRimCores();
			for (int i=0;i<this.interf.getNumBsaToAsaCutoffs();i++){
				rimScores1[i]  = calcScore(rimCores[i].getRimResidues(), FIRST, scoType, weighted);
				coreScores1[i] = calcScore(rimCores[i].getCoreResidues(),FIRST, scoType, weighted);
			}
		} else {
			for (int i=0;i<this.interf.getNumBsaToAsaCutoffs();i++){
				rimScores1[i] = Double.NaN;
				coreScores1[i] = Double.NaN;
			}
		}
		rimScores.put(FIRST,rimScores1);
		coreScores.put(FIRST,coreScores1);

		double[] rimScores2 = new double[this.interf.getNumBsaToAsaCutoffs()]; 
		double[] coreScores2 = new double[this.interf.getNumBsaToAsaCutoffs()];

		if (this.interf.isSecondProtein()) {
			InterfaceRimCore[] rimCores = this.interf.getSecondRimCores();
			for (int i=0;i<this.interf.getNumBsaToAsaCutoffs();i++){
				rimScores2[i]  = calcScore(rimCores[i].getRimResidues(), SECOND, scoType, weighted);
				coreScores2[i] = calcScore(rimCores[i].getCoreResidues(),SECOND, scoType, weighted);
			}
		} else {
			for (int i=0;i<this.interf.getNumBsaToAsaCutoffs();i++){
				rimScores2[i] = Double.NaN;
				coreScores2[i] = Double.NaN;
			}
		}
		rimScores.put(SECOND,rimScores2);
		coreScores.put(SECOND,coreScores2);

	}
		
	private List<Residue> checkResiduesForPDBReliability(List<Residue> residues, ChainEvolContext chain, String pdbChainCode) {
		List<Residue> unreliableResidues = new ArrayList<Residue>();
		for (Residue res:residues){
			int resSer = chain.getResSerFromPdbResSer(pdbChainCode, res.getPdbSerial());
			if (resSer!=-1 && !chain.isPdbSeqPositionMatchingUniprot(resSer)) {
				unreliableResidues.add(res);
			}
		}
		if (!unreliableResidues.isEmpty()) {
			String msg = "Interface residue serials ";
			for (int i=0;i<unreliableResidues.size();i++) {
				msg+=unreliableResidues.get(i).getAaType().getThreeLetterCode()+unreliableResidues.get(i).getPdbSerial();
				if (i!=unreliableResidues.size()-1) {
					msg+=",";
				}
			}
			msg+=" can't be evaluated because of PDB SEQRES not matching the Uniprot sequence at those positions.";
			LOGGER.warn(msg);
		}
		return unreliableResidues;
	}
	
	private List<Residue> checkResiduesForCDSReliability(List<Residue> residues, ChainEvolContext chain, String pdbChainCode) {
		List<Residue> unreliableResidues = new ArrayList<Residue>();
		for (Residue res:residues){
			int resSer = chain.getResSerFromPdbResSer(pdbChainCode, res.getPdbSerial());
			if (resSer!=-1 && !chain.isPdbSeqPositionReliable(resSer)) {
				unreliableResidues.add(res);
			}				
		}
		if (!unreliableResidues.isEmpty()) {
			String msg = "Interface residue serials ";
			for (int i=0;i<unreliableResidues.size();i++) {
				msg+=unreliableResidues.get(i).getAaType().getThreeLetterCode()+unreliableResidues.get(i).getPdbSerial();
				if (i!=unreliableResidues.size()-1) {
					msg+=(",");
				}
			}
			msg+=" can't be evaluated because of unreliable CDS sequence information.";		
			LOGGER.warn(msg);
		}
		return unreliableResidues;
	}
	
	private double calcScore(List<Residue> residues, int molecId, ScoringType scoType, boolean weighted) {
		ChainEvolContext chain = null;
		String pdbChainCode = null;
		if (molecId==FIRST) {
			chain = chains.get(FIRST);
			pdbChainCode = interf.getFirstMolecule().getPdbChainCode();
		} else if (molecId == SECOND) {
			chain = chains.get(SECOND);
			pdbChainCode = interf.getSecondMolecule().getPdbChainCode();
		}
		double totalScore = 0.0;
		double totalWeight = 0.0;
		List<Double> conservScores = chain.getConservationScores(scoType);
		for (Residue res:residues){
			int resSer = chain.getResSerFromPdbResSer(pdbChainCode, res.getPdbSerial());

			if (resSer!=-1) {
				int queryPos = -2;
				if (scoType==ScoringType.ENTROPY) {
					queryPos = chain.getQueryUniprotPosForPDBPos(resSer); 
				} else if (scoType==ScoringType.KAKS) {
					queryPos = chain.getQueryCDSPosForPDBPos(resSer);
				}
				if (queryPos!=-1) {   
					double weight = 1.0;
					if (weighted) {
						weight = res.getBsa();
					}
					totalScore += weight*(conservScores.get(queryPos));
					totalWeight += weight;
				} else {
					
				}
			} else {
				LOGGER.warn("Can't map PISA pdb residue serial "+res.getPdbSerial()+" (res type: "+res.getAaType().getThreeLetterCode()+", PISA serial: "+res.getSerial()+")");
				LOGGER.warn("The residue will not be used for scoring");
			}
		}
		return totalScore/totalWeight;
	}
	
	public void writePdbFile(File file, ScoringType scoType) throws IOException {
		PrintStream ps = new PrintStream(file);
		String chain1 = null;
		String chain2 = null;
		if (interf.isFirstProtein()) {
			chain1 = interf.getFirstMolecule().getPdbChainCode();
			chains.get(FIRST).setConservationScoresAsBfactors(chain1,scoType);
			// we copy in order to leave the original Pdbs unaltered (essential to be able to apply transformations several times)
			Pdb pdb1 = chains.get(FIRST).getPdb(chain1).copy();
			pdb1.transform(interf.getFirstTransfOrth());
			pdb1.writeAtomLines(ps);
		}
		if (interf.isSecondProtein()) {
			
			chain2 = interf.getSecondMolecule().getPdbChainCode();
			String chain2forOutput = chain2; // the name we will put to the chain2 in the output pdb file
			if (chain1!=null && chain1.equals(chain2)) {
				// if both chains are named equally we want to still named them differently in the output pdb file
				// so that molecular viewers can handle properly the 2 chains as separate entities 
				char letter = chain1.charAt(0);
				if (letter!='Z' && letter!='z') {
					chain2forOutput = Character.toString((char)(letter+1)); // i.e. next letter in alphabet
				} else {
					chain2forOutput = Character.toString((char)(letter-25)); //i.e. 'A' or 'a'
				}
				LOGGER.warn("Chain "+chain2+" renamed to "+chain2forOutput+" to write the output PDB file "+file);
			}
			chains.get(SECOND).setConservationScoresAsBfactors(chain2,scoType);
			Pdb pdb2 = chains.get(SECOND).getPdb(chain2).copy();
			pdb2.transform(interf.getSecondTransfOrth());
			pdb2.setChainCode(chain2forOutput);
			pdb2.writeAtomLines(ps);
		}
		ps.close();
	}
	
	private double[] getScoreRatios(int molecId) {
		double[] ratios = new double[this.interf.getNumBsaToAsaCutoffs()];
		for (int i=0;i<this.interf.getNumBsaToAsaCutoffs();i++) {
			ratios[i] = coreScores.get(molecId)[i]/rimScores.get(molecId)[i];
		}
		return ratios;
	}
	
	private boolean hasEnoughHomologs(int molecId, int homologsCutoff){
		return this.chains.get(molecId).getNumHomologs()>=homologsCutoff;
	}

	private boolean isProtein(int molecId) {
		if (molecId==FIRST) {
			return this.interf.isFirstProtein();
		} else if (molecId==SECOND) {
			return this.interf.isSecondProtein();
		} else {
			throw new IllegalArgumentException("Fatal error! Wrong molecId "+molecId);
		}
	}
	
	private boolean[] hasEnoughCore(int molecId, int minMemberCoreSize) {

		InterfaceRimCore[] rimCores = null;
		if (molecId==FIRST) {
			rimCores = this.interf.getFirstRimCores();
		} else if (molecId==SECOND) {
			rimCores = this.interf.getSecondRimCores();
		}

		boolean[] bs = new boolean[this.interf.getNumBsaToAsaCutoffs()];
		for (int i=0;i<this.interf.getNumBsaToAsaCutoffs();i++) {
			bs[i] = rimCores[i].getCoreSize()>=minMemberCoreSize;
		}
		return bs;
	}

	private int[] countReliableCoreRes(int molecId) {
		InterfaceRimCore[] rimCores = null;
		String pdbChainCode = null;
		if (molecId==FIRST) {
			rimCores = this.interf.getFirstRimCores();
			pdbChainCode = interf.getFirstMolecule().getPdbChainCode();
		} else if (molecId==SECOND) {
			rimCores = this.interf.getSecondRimCores();
			pdbChainCode = interf.getSecondMolecule().getPdbChainCode();
		}

		int[] counts = new int[this.interf.getNumBsaToAsaCutoffs()];
		for (int i=0;i<this.interf.getNumBsaToAsaCutoffs();i++) {
			List<Residue> unreliableCoreResidues = new ArrayList<Residue>(); 
			unreliableCoreResidues.addAll(checkResiduesForPDBReliability(rimCores[i].getCoreResidues(), chains.get(molecId), pdbChainCode));
			if (lastScoType==ScoringType.KAKS) {
				unreliableCoreResidues.addAll(checkResiduesForCDSReliability(rimCores[i].getCoreResidues(), chains.get(molecId), pdbChainCode));
			}
			counts[i] = unreliableCoreResidues.size();
		}
		return counts;
	}
	
	private int[] countReliableRimRes(int molecId) {
		InterfaceRimCore[] rimCores = null;
		String pdbChainCode = null;
		if (molecId==FIRST) {
			rimCores = this.interf.getFirstRimCores();
			pdbChainCode = interf.getFirstMolecule().getPdbChainCode();
		} else if (molecId==SECOND) {
			rimCores = this.interf.getSecondRimCores();
			pdbChainCode = interf.getSecondMolecule().getPdbChainCode();
		}
		int[] counts = new int[this.interf.getNumBsaToAsaCutoffs()];
		for (int i=0;i<this.interf.getNumBsaToAsaCutoffs();i++) {
			List<Residue> unreliableRimResidues = new ArrayList<Residue>();
			
			unreliableRimResidues.addAll(checkResiduesForPDBReliability(rimCores[i].getRimResidues(), chains.get(molecId), pdbChainCode));
			if (lastScoType==ScoringType.KAKS) {
				unreliableRimResidues.addAll(checkResiduesForCDSReliability(rimCores[i].getRimResidues(), chains.get(molecId), pdbChainCode));
			}
			counts[i]=unreliableRimResidues.size();
		}
		return counts;
	}

	/**
	 * Gets the interface partners prediction calls.
	 * @param molecId
	 * @param bioCutoff
	 * @param xtalCutoff
	 * @param homologsCutoff
	 * @param minMemberCoreSize
	 * @return
	 */
	private CallType[] getMemberCalls(int molecId, double bioCutoff, double xtalCutoff, int homologsCutoff, int minMemberCoreSize) {
		CallType[] calls = new CallType[this.interf.getNumBsaToAsaCutoffs()];
		int memberSerial = molecId+1;
		InterfaceRimCore[] rimCores = null;
		if (molecId==FIRST) {
			rimCores = this.interf.getFirstRimCores();
		} else if (molecId==SECOND) {
			rimCores = this.interf.getSecondRimCores();
		}
		
		if (!isProtein(molecId)) {
			LOGGER.info("Interface member "+memberSerial+" calls NOPRED because it is not a protein");
		} else if (!hasEnoughHomologs(molecId, homologsCutoff)) {
			LOGGER.info("Interface member "+memberSerial+" calls NOPRED because there are not enough homologs to evaluate conservation scores");
		} else {
			boolean[] enoughCore = hasEnoughCore(molecId, minMemberCoreSize);
			int[] countsRelCoreRes = countReliableCoreRes(molecId);
			int[] countsRelRimRes = countReliableRimRes(molecId);


			double[] ratios = this.getScoreRatios(molecId);

			for (int i=0;i<this.interf.getNumBsaToAsaCutoffs();i++) {
				if (!isProtein(molecId)) {
					calls[i] = CallType.NO_PREDICTION;
				}
				else if (!hasEnoughHomologs(molecId,homologsCutoff)) {
					calls[i] = CallType.NO_PREDICTION;
				}
				else if (!enoughCore[i]) {
					LOGGER.info("Interface member "+memberSerial+" calls XTAL because core is too small ("+rimCores[i].getCoreSize()+" residues)");
					calls[i] = CallType.CRYSTAL;
				}
				else if (((double)countsRelCoreRes[i]/(double)rimCores[i].getCoreSize())>MAX_ALLOWED_UNREL_RES) {
					LOGGER.info("Interface member "+memberSerial+" calls NOPRED because there are not enough reliable core residues ("+
							countsRelCoreRes[i]+" unreliable residues out of "+rimCores[i].getCoreSize()+" residues in core)");
					calls[i] = CallType.NO_PREDICTION;
				}
				else if (((double)countsRelRimRes[i]/(double)rimCores[i].getRimSize())>MAX_ALLOWED_UNREL_RES) {
					LOGGER.info("Interface member "+memberSerial+" calls NOPRED because there are not enough reliable rim residues ("+
							countsRelRimRes[i]+" unreliable residues out of "+rimCores[i].getRimSize()+" residues in rim)");
					calls[i] = CallType.NO_PREDICTION;
				}
				else {
					if (ratios[i]<bioCutoff) {
						calls[i] = CallType.BIO;
					} else if (ratios[i]>xtalCutoff) {
						calls[i] = CallType.CRYSTAL;
					} else {
						calls[i] = CallType.GRAY;
					}
				}
			}
		}
		return calls;
	}

	/**
	 * Gets the prediction calls for this interface. See the {@link CallType} enum for 
	 * the possible prediction calls.
	 * @param bioCutoff
	 * @param xtalCutoff
	 * @param homologsCutoff
	 * @param minCoreSize
	 * @param minMemberCoreSize
	 * @return
	 */
	public CallType[] getCalls(double bioCutoff, double xtalCutoff, int homologsCutoff, int minCoreSize, int minMemberCoreSize) {
		CallType[] calls = new CallType[this.interf.getNumBsaToAsaCutoffs()];
		finalScores = new double[this.interf.getNumBsaToAsaCutoffs()];
		
		// the votes with voters (no anonymous vote here!)
		bioCalls = new TreeMap<Integer,ArrayList<Integer>>();
		xtalCalls = new TreeMap<Integer,ArrayList<Integer>>();
		grayCalls = new TreeMap<Integer,ArrayList<Integer>>();
		noPredictCalls = new TreeMap<Integer,ArrayList<Integer>>();

		for (int i=0;i<this.interf.getNumBsaToAsaCutoffs();i++) {
			ArrayList<Integer> bios = new ArrayList<Integer>();
			ArrayList<Integer> xtals = new ArrayList<Integer>();
			ArrayList<Integer> grays = new ArrayList<Integer>();
			ArrayList<Integer> nopreds = new ArrayList<Integer>();
			for (int k=0;k<chains.size();k++) {
				CallType[] memberCalls = getMemberCalls(k,bioCutoff, xtalCutoff, homologsCutoff, minMemberCoreSize);
				bioCalls.put(i,bios);
				xtalCalls.put(i,xtals);
				grayCalls.put(i,grays);
				noPredictCalls.put(i, nopreds);
				// cast your votes!
				if (memberCalls[i] == CallType.BIO) {
					bios.add(k);
				}
				else if (memberCalls[i] == CallType.CRYSTAL) {
					xtals.add(k);
				}
				else if (memberCalls[i] == CallType.GRAY) {
					grays.add(k);
				}
				else if (memberCalls[i] == CallType.NO_PREDICTION) {
					nopreds.add(k);
				}
			}
		}
		
		for (int i=0;i<this.interf.getNumBsaToAsaCutoffs();i++) {
			int countBio = bioCalls.get(i).size();
			int countXtal = xtalCalls.get(i).size();
			int countGray = grayCalls.get(i).size();
			int countNoPredict = noPredictCalls.get(i).size();

			// decision time!
			//int validVotes = countBio+countXtal+countGray;

			if (countNoPredict==chains.size()) {
				finalScores[i] = Double.NaN;
				calls[i]=CallType.NO_PREDICTION;
			}

			if (countBio>countXtal) {
				//TODO check the discrepancies among the different voters. The variance could be a measure of the confidence of the call
				//TODO need to do a study about the correlation of scores in members of the same interface
				//TODO it might be the case that there is good agreement and bad agreement would indicate things like a bio-mimicking crystal interface
				finalScores[i] = getAvrgRatio(bioCalls.get(i),i);
				calls[i] = CallType.BIO;
			} else if (countXtal>countBio) {
				finalScores[i] = getAvrgRatio(xtalCalls.get(i),i);
				calls[i] = CallType.CRYSTAL;
			} else if (countGray>countBio+countXtal) {
				// we use as final score the average of all gray member scores
				finalScores[i] = getAvrgRatio(grayCalls.get(i),i);
				calls[i] = CallType.GRAY;
			} else if (countBio==countXtal) {
				//TODO we are taking simply the average, is this the best solution?
				// weighting is not done here, scores are calculated either weighted/non-weighted before
				List<Integer> indices = new ArrayList<Integer>();
				indices.addAll(bioCalls.get(i));
				indices.addAll(xtalCalls.get(i));
				finalScores[i] = getAvrgRatio(indices,i);

				// first we check that the sum of core sizes is above the cutoff (if not we call xtal directly)
				if (!hasEnoughCore(indices,i,minCoreSize)) {
					calls[i] = CallType.CRYSTAL;
				} else {
					if (finalScores[i]<bioCutoff) {
						calls[i] = CallType.BIO;
					} else if (finalScores[i]>xtalCutoff) {
						calls[i] = CallType.CRYSTAL;
					} else {
						calls[i] = CallType.GRAY;
					}
				}
			}
		}
		return calls;
	}	

	/**
	 * Gets the average core to rim ratio for the list of given indices of members.
	 * @param a list of indices of the memberScores List
	 * @return
	 */
	private double getAvrgRatio(List<Integer> indices, int i) {
		double sum = 0.0;
		for (int ind:indices) {
			sum+=coreScores.get(ind)[i]/rimScores.get(ind)[i];				
		}
		return sum/(double)indices.size();
	}
	
	/**
	 * Tells whether the sum of core sizes of given indices of members is above the 
	 * minCoreSize cut-off.
	 * @param indices
	 * @return
	 */
	private boolean hasEnoughCore(List<Integer> indices, int i, int minCoreSize) {
		int size = getSumCoreSize(indices,i);
		if (size<minCoreSize) {
			return false;
		}
		return true;
	}
	
	/**
	 * Gets the sum of the core sizes of given indices of members 
	 * @param indices
	 * @return
	 */
	private int getSumCoreSize(List<Integer> indices, int i) {
		int size = 0;
		for (int ind:indices) {
			if (ind==FIRST) {
				size+=this.interf.getFirstRimCores()[i].getCoreSize();
			} else if (ind==SECOND) {
				size+=this.interf.getSecondRimCores()[i].getCoreSize();
			}
		}
		return size;
	}

	/**
	 * Gets the final scores (as many as bsaToAsaCutoffs used) computed upon last 
	 * call of {@link #getCalls(double, double, int, int, int)}
	 * Final scores are the scores computed from both members of the interface.
	 * @return
	 */
	public double[] getFinalScores() {
		return finalScores;
	}
	
	public double[] getRimScore(int molecId) {
		return rimScores.get(molecId);
	}
	
	public double[] getCoreScore(int molecId) {
		return coreScores.get(molecId);
	}
	
	private String getVotersString(int i) {
		String finalStr = "";
		for (CallType callType: CallType.values()) {
			List<Integer> calls = null;
			if (callType==CallType.BIO) {
				calls = bioCalls.get(i);
			} else if (callType==CallType.CRYSTAL) {
				calls = xtalCalls.get(i);
			} else if (callType==CallType.GRAY) {
				calls = grayCalls.get(i);
			} else if (callType==CallType.NO_PREDICTION) {
				calls = noPredictCalls.get(i);
			}
			String callsStr = "";
			for (int ind=0;ind<calls.size();ind++) {
				callsStr+=(calls.get(ind)+1);
				if (ind!=calls.size()-1) callsStr+=","; // skip the comma for the last member
			}
			finalStr+=String.format("\t%6s",callsStr);
		}
		return finalStr;
	}
	
	public static void printScoringHeaders(PrintStream ps) {
		ps.printf("%15s\t%6s\t","interface","area");
		ps.printf("%5s\t%5s\t%5s","size1", "size2","CA");
		ps.print("\t");
		ps.printf("%5s\t%5s\t%5s","core1","rim1","rat1");
		ps.print("\t");
		ps.printf("%5s\t%5s\t%5s","core2","rim2","rat2");
		ps.print("\t");
		ps.printf("%6s\t%5s\t%6s\t%6s\t%6s\t%6s",
				"call","score",CallType.BIO.getName(),CallType.CRYSTAL.getName(),CallType.GRAY.getName(),CallType.NO_PREDICTION.getName());
		ps.println();
	}
	
	private void printRimCoreInfo(PrintStream ps, int i, boolean printInterfDesc) {
		ChainInterface pi = this.getInterface();
		if (printInterfDesc) {
			ps.printf("%15s\t%6.1f",
				pi.getId()+"("+pi.getFirstMolecule().getPdbChainCode()+"+"+pi.getSecondMolecule().getPdbChainCode()+")",
				pi.getInterfaceArea());
		} else {
			ps.printf("%15s\t%6s","","");
		}
		boolean isProt1 = this.getInterface().isFirstProtein();
		boolean isProt2 = this.getInterface().isSecondProtein();
		ps.printf("%5d\t%5d\t%5.2f", (!isProt1)?0:pi.getFirstRimCores()[i].getCoreSize(),
									 (!isProt2)?0:pi.getSecondRimCores()[i].getCoreSize(),
									 this.getInterface().getBsaToAsaCutoffs()[i]);
		ps.print("\t");

	}
	
	private void printScores(PrintStream ps, int i, CallType[] calls) {
		ps.printf("%5.2f\t%5.2f\t%5.2f",
				this.getCoreScore(0)[i], this.getRimScore(0)[i], this.getCoreScore(0)[i]/this.getRimScore(0)[i]);
		ps.print("\t");
		ps.printf("%5.2f\t%5.2f\t%5.2f",
				this.getCoreScore(1)[i], this.getRimScore(1)[i], this.getCoreScore(1)[i]/this.getRimScore(1)[i]);
		ps.print("\t");
		// call type, score, voters
		ps.printf("%6s\t%5.2f", calls[i].getName(),	this.getFinalScores()[i]);
		ps.print(this.getVotersString(i));
		// more possible things to output: number of homologues (?)		
	}
	
	public void printScoresTable(PrintStream ps, double bioCutoff, double xtalCutoff, int homologsCutoff, int minCoreSize, int minMemberCoreSize) {
		CallType[] calls = getCalls(bioCutoff, xtalCutoff, homologsCutoff, minCoreSize, minMemberCoreSize);
		for (int i=0;i<interf.getBsaToAsaCutoffs().length;i++) {
			if (i==0) {
				printRimCoreInfo(ps, i, true);
			} else {
				printRimCoreInfo(ps, i, false);
			}
			printScores(ps, i, calls);
			ps.println();
		}
	}
	
	public void serialize(File serializedFile) throws IOException {
		FileOutputStream fileOut = new FileOutputStream(serializedFile);
		ObjectOutputStream out = new ObjectOutputStream(fileOut);
		out.writeObject(this);
		out.close();
		fileOut.close();
	}

	public static InterfaceEvolContext readFromFile(File serialized) throws IOException, ClassNotFoundException {
		FileInputStream fileIn = new FileInputStream(serialized);
		ObjectInputStream in = new ObjectInputStream(fileIn);
		InterfaceEvolContext interfSc = (InterfaceEvolContext) in.readObject();
		in.close();
		fileIn.close();		
		return interfSc;
	}
}
