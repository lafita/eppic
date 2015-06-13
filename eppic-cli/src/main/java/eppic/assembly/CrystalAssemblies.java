package eppic.assembly;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.contact.StructureInterfaceCluster;
import org.biojava.nbio.structure.contact.StructureInterfaceList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eppic.EppicParams;

/**
 * A representation of all valid assemblies in a crystal structure.
 * 
 * @author duarte_j
 *
 */
public class CrystalAssemblies implements Iterable<Assembly> {
	
	
	private static final Logger logger = LoggerFactory.getLogger(CrystalAssemblies.class);

	private LatticeGraph latticeGraph;
	private Structure structure;
	private List<StructureInterfaceCluster> interfaceClusters;

	/**
	 * The set of all valid assemblies in the crystal.
	 */
	private Set<Assembly> all;	
	
	/**
	 * Each of the clusters of equivalent assemblies.
	 * The representative is the first member and the maximal Assembly in terms of number of engaged interface clusters.
	 */
	private List<AssemblyGroup> clusters;
	
	private Map<Integer,AssemblyGroup> groups;
	
	
	public CrystalAssemblies(Structure structure, StructureInterfaceList interfaces) throws StructureException {
		
		this.structure = structure;
		this.latticeGraph = new LatticeGraph(structure, interfaces);
		this.interfaceClusters = interfaces.getClusters(EppicParams.CLUSTERING_CONTACT_OVERLAP_SCORE_CUTOFF);
		
		findValidAssemblies();
		
		initGroups();
		
		initClusters();
		
		
	}
	
	public int size() {
		return clusters.size();
	}
	
	/**
	 * Returns all topologically valid assemblies present in the crystal.
	 * The method traverses the tree of all possible combinations of n interface
	 * clusters (2^n in total), e.g. for a structure with 3 clusters {0,1,2} the tree looks like:
	 * <pre>
	 *         {}
	 *       /  |  \
	 *    {0}  {1}  {2}
	 *    |  X     X   |
	 *  {0,1} {0,2} {1,2}
	 *      \   |   /
	 *       {0,1,2}   
	 * </pre>
	 * As the tree is traversed, if a node is found to be an invalid assembly, then all of its children
	 * are pruned off and not tried. Thus the number of combinations reduces very quickly with a few
	 * pruned top nodes.
	 * @return
	 */
	private void findValidAssemblies() {
		
		latticeGraph.removeDuplicateEdges();
		
		Set<Assembly> validAssemblies = new HashSet<Assembly>();;
		
		// the list of nodes in the tree found to be invalid: all of their children will also be invalid
		List<Assembly> invalidNodes = new ArrayList<Assembly>();		
		
		Assembly emptyAssembly = new Assembly(this, new PowerSet(interfaceClusters.size()));
		
		validAssemblies.add(emptyAssembly); // the empty assembly (no engaged interfaces) is always a valid assembly
		
		Set<Assembly> prevLevel = new HashSet<Assembly>();
		prevLevel.add(emptyAssembly);
		Set<Assembly> nextLevel = null;
		
		for (int k = 1; k<=interfaceClusters.size();k++) {
			
			logger.debug("Traversing level {} of tree: {} parent nodes",k,prevLevel.size());
			
			nextLevel = new HashSet<Assembly>();
					
			for (Assembly p:prevLevel) {
				List<Assembly> children = p.getChildren(invalidNodes);
				
				for (Assembly c:children) {
					
					if (!c.isValid()) {
						logger.debug("Node {} is invalid, will prune off all of its children",c.toString());
						invalidNodes.add(c);
					} else {
						// we only add a child for next level if we know it's valid, if it wasn't valid 
						// then it's not added and thus the whole branch is pruned
						nextLevel.add(c);
						// add assembly as valid
						validAssemblies.add(c);
					}
				}
			}
			prevLevel = new HashSet<Assembly>(nextLevel); 
			
		}
		
		this.all = validAssemblies;
		

	}
	
	private void initGroups() {
		this.groups = new TreeMap<Integer, AssemblyGroup>();

		for (Assembly assembly:all) {

			StoichiometrySet stoSet = assembly.getStoichiometrySet();

			// we classify into groups those that are fully covering

			if (stoSet.isFullyCovering()) {
				// we assume they are valid, which implies even stoichiometry (thus the size of first will give the size for all)			
				int size = stoSet.getFirst().getCountForIndex(0);

				if (!groups.containsKey(size)) {
					AssemblyGroup group = new AssemblyGroup();
					groups.put(size, group);
					group.add(assembly);
				} else {
					groups.get(size).add(assembly);
				}
			} else {
				logger.info("Assembly {} will not be clustered since it doesn't cover all entities",assembly.toString());
				if (!groups.containsKey(-1)) {
					AssemblyGroup group = new AssemblyGroup();
					group.add(assembly);
					groups.put(-1, group);
				} else {
					groups.get(-1).add(assembly);
				}
			}
		}
	}
	
	private void initClusters() {
		
		clusters = new ArrayList<AssemblyGroup>();
		
		for (int size:groups.keySet()) {
			AssemblyGroup ag = groups.get(size);

			if (size>0) {
				List<AssemblyGroup> clustersForGroup = ag.sortIntoClusters();

				logger.debug("{} assemblies with size {} group into {} clusters",ag.size(),size,clustersForGroup.size());


				for (int i=0;i<clustersForGroup.size();i++) {

					if (clustersForGroup.get(i).size()>1) 
						logger.info("Using assembly {} as representative for assembly cluster {}",clustersForGroup.get(i).get(0),clustersForGroup.get(i));

					this.clusters.add(clustersForGroup.get(i)); 
				}
			} else {
				// for those in the "-1" group we just add each assembly as a single-member cluster
				for (Assembly assembly:ag) {
					AssemblyGroup g = new AssemblyGroup();
					g.add(assembly);
					clusters.add(g);
				}
			}
		}
	}

	@Override
	public Iterator<Assembly> iterator() {
		
		return getUniqueAssemblies().iterator();
	}
	
	/**
	 * Get all valid assemblies in the crystal (unclustered)
	 * @return
	 * @see #getUniqueAssemblies()
	 */
	public Set<Assembly> getAllAssemblies() {
		return all;
	}
	
	public Structure getStructure() {
		return structure;
	}
	
	public LatticeGraph getLatticeGraph() {
		return latticeGraph;
	}
	
	public List<StructureInterfaceCluster> getInterfaceClusters() {
		return interfaceClusters;
	}
	
	/**
	 * Returns the list of unique valid assemblies in the crystal, that is the representatives 
	 * of each of the assembly clusters. The representatives are chosen to be those assemblies that
	 * have maximal number of engaged interface clusters out of the group of equivalent assemblies 
	 * in the assembly cluster.
	 * @return
	 */
	public List<Assembly> getUniqueAssemblies() {
		List<Assembly> representatives = new ArrayList<Assembly>();
		for (AssemblyGroup cluster:clusters) {
			// we use the first member of each cluster (which is the maximal group, see 
			// AssemblyGroup.sortIntoClusters() ) as the representative
			representatives.add(cluster.get(0));
		}
		return representatives;
	}
	
	public List<AssemblyGroup> getClusters() {
		return clusters;
	}
	
	public Assembly generateAssembly(int interfaceClusterId) {
		int[] interfaceClusterIds = new int[] {interfaceClusterId};
		return generateAssembly(interfaceClusterIds);
	}
	
	public Assembly generateAssembly(int[] interfaceClusterIds) {
		
		PowerSet engagedSet = new PowerSet(interfaceClusters.size());
		
		for (int clusterId:interfaceClusterIds) {
			engagedSet.switchOn(clusterId-1);
		}
		
		Assembly a = new Assembly(this, engagedSet);
		
		return a;
	}
	
	/**
	 * Returns the multiplicity of the given interfaceClusterId, i.e. the number of edges of the 
	 * the given type present in the Assembly result of engaging just the given interface.
	 * For a valid assembly with cyclic symmetry, this will give the order n of the Cn symmetry
	 * @param interfaceClusterId
	 * @return
	 */
	public int getEdgeMultiplicity(int interfaceClusterId) {
		
		Assembly singleInterfaceClusterAssembly = generateAssembly(interfaceClusterId);
		
		return singleInterfaceClusterAssembly.getEdgeCountInFirstConnectedComponent(interfaceClusterId);

	}
	
}