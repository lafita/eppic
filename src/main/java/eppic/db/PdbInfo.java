package eppic.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import eppic.model.ChainClusterDB;
import eppic.model.InterfaceClusterDB;
import eppic.model.InterfaceDB;
import eppic.model.PdbInfoDB;

/**
 * A class to wrap a PDB entry as extracted from the EPPIC database.
 * It essentially maps 1:1 to a PdbInfoDB, but we need to keep it separate of the model
 * because otherwise GWT and/or JPA would complain.
 * @author duarte_j
 *
 */
public class PdbInfo {

	public static final double MIN_AREA_LATTICE_COMPARISON = 100;
	
	private PdbInfoDB pdbInfo;
	
	private HashMap<String,ChainCluster> chainIdLookup;
	
	public PdbInfo(PdbInfoDB pdbInfo) {
		this.pdbInfo = pdbInfo;
		this.chainIdLookup = getChainIdLookup();
		
	}
	
	public PdbInfoDB getPdbInfo() {
		return pdbInfo;
	}
	
	public ChainCluster getChainCluster(String pdbChainCode) {
		return chainIdLookup.get(pdbChainCode);
	}
	
	private HashMap<String,ChainCluster> getChainIdLookup() {
		HashMap<String,ChainCluster> lookup = new HashMap<String,ChainCluster>();
		for (ChainCluster cc:getChainClusters()) {
			lookup.put(cc.getChainCluster().getRepChain(), cc);
			if (cc.getChainCluster().getMemberChains()!=null) {
				for (String chain:cc.getChainCluster().getMemberChains().split(",")) {
					lookup.put(chain,cc);
				}
			}
		}
		return lookup;
	}
	
	public List<ChainCluster> getChainClusters() {
		List<ChainCluster> list = new ArrayList<ChainCluster>();
		for (ChainClusterDB cc:pdbInfo.getChainClusters()) {
			list.add(new ChainCluster(cc));
		}
		return list;
	}
	
	/**
	 * Returns all Interface's for this PdbInfo by looping over all interface clusters
	 * @return
	 */
	public List<Interface> getInterfaces() {
		List<Interface> list = new ArrayList<Interface>();
		for (InterfaceClusterDB ic:pdbInfo.getInterfaceClusters()) {
			for (InterfaceDB interf:ic.getInterfaces()) {
				list.add(new Interface(interf, this));
			}
		}
		return list;
	}
	
	public int getNumInterfacesAboveArea(double area) {
		int count = 0;
		for (InterfaceClusterDB ic:pdbInfo.getInterfaceClusters()) {
			for (InterfaceDB interf:ic.getInterfaces()) {
				if (interf.getArea()>area) count++;	
			}			
		}
		return count;
	}
	
	public List<Interface> getInterfacesAboveArea(double area) {
		List<Interface> list = new ArrayList<Interface>();
		for (InterfaceClusterDB ic:pdbInfo.getInterfaceClusters()) {
			for (InterfaceDB interf:ic.getInterfaces()) {				
				if (interf.getArea()>area) list.add(new Interface(interf,this));
			}
		}
		return list;
	}
	
	public double[][] calcLatticeOverlapMatrix(PdbInfo other, SeqClusterLevel seqClusterLevel) {
		
		int interfCount1 = this.getNumInterfacesAboveArea(MIN_AREA_LATTICE_COMPARISON);
		int interfCount2 = other.getNumInterfacesAboveArea(MIN_AREA_LATTICE_COMPARISON);
	
		double[][] matrix = new double[interfCount1][interfCount2];

		for (Interface thisInterf : this.getInterfacesAboveArea(MIN_AREA_LATTICE_COMPARISON)) {
			for (Interface otherInterf : other.getInterfacesAboveArea(MIN_AREA_LATTICE_COMPARISON)) {
								
				double co = thisInterf.calcInterfaceOverlap(otherInterf, seqClusterLevel);
				matrix[thisInterf.getInterface().getInterfaceId()-1][otherInterf.getInterface().getInterfaceId()-1] = co;
			}
		}	
		
		return matrix;
	}
	
	public boolean haveSameContent(PdbInfo other, SeqClusterLevel seqClusterLevel) {
		
		int numChainClusters1 = this.getPdbInfo().getChainClusters().size();
		int numChainClusters2 = other.getPdbInfo().getChainClusters().size();
		
		if (numChainClusters1!=numChainClusters2) return false;
		
		
		for (ChainCluster chainCluster1:this.getChainClusters()) {
			
			boolean match = false; 
			
			int seqClusterId1 = chainCluster1.getSeqClusterId(seqClusterLevel);
			
			for (ChainCluster chainCluster2:other.getChainClusters()) {
				int seqClusterId2 = chainCluster2.getSeqClusterId(seqClusterLevel);
				if (seqClusterId1==seqClusterId2) {
					match = true;
					break;
				}
			}
			// if seqClusterId1 didn't match something in 2 then that's about it: there's different content
			if (!match) return false;
		}
		// if all chains have matches, then we have same content
		return true;
	}
}
