package ch.systemsx.sybit.crkwebui.server.db.dao;

import java.util.List;

import ch.systemsx.sybit.crkwebui.shared.exceptions.DaoException;
import ch.systemsx.sybit.crkwebui.shared.model.ChainCluster;
import ch.systemsx.sybit.crkwebui.shared.model.PDBSearchResult;

/**
 * DAO for HomologsInfo item.
 * @author AS
 *
 */
public interface ChainClusterDAO 
{
	/**
	 * Retrieves list of chain clusters for pdb score item.
	 * @param pdbInfoUid uid of pdb info item
	 * @return list of chain cluster for pdb score item
	 * @throws DaoException when can not retrieve chain clusters
	 */
	public List<ChainCluster> getChainClusters(int pdbInfoUid) throws DaoException;
	
	/**
	 * Retrieves a list of pdb search items from ChainCluster table having a particular uniprot id
	 * @param uniProtId the unitprot id
	 * @return list of results
	 * @throws DaoException when can not retrieve items
	 */
	public List<PDBSearchResult> getPdbSearchItemsForUniProt(String uniProtId) throws DaoException;
}
