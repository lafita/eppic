package ch.systemsx.sybit.crkwebui.server.db.dao.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import eppic.model.ChainClusterDB_;
import eppic.model.PdbInfoDB_;
import ch.systemsx.sybit.crkwebui.server.db.EntityManagerHandler;
import ch.systemsx.sybit.crkwebui.server.db.dao.ChainClusterDAO;
import ch.systemsx.sybit.crkwebui.shared.exceptions.DaoException;
import ch.systemsx.sybit.crkwebui.shared.model.ChainCluster;
import ch.systemsx.sybit.crkwebui.shared.model.PDBSearchResult;
import eppic.model.ChainClusterDB;
import eppic.model.PdbInfoDB;

/**
 * Implementation of ChainClusterDAO.
 * @author AS
 *
 */
public class ChainClusterDAOJpa implements ChainClusterDAO
{
	@Override
	public List<ChainCluster> getChainClusters(int pdbInfoUid) throws DaoException
	{
		EntityManager entityManager = null;
		
		try
		{
			List<ChainCluster> result = new ArrayList<ChainCluster>();
			
			entityManager = EntityManagerHandler.getEntityManager();			
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			
			CriteriaQuery<ChainClusterDB> criteriaQuery = criteriaBuilder.createQuery(ChainClusterDB.class);
			Root<ChainClusterDB> chainClusterRoot = criteriaQuery.from(ChainClusterDB.class);
			Path<PdbInfoDB> pdbInfoPath = chainClusterRoot.get(ChainClusterDB_.pdbInfo);
			Predicate condition = criteriaBuilder.equal(pdbInfoPath.get(PdbInfoDB_.uid), pdbInfoUid);
			criteriaQuery.where(condition);
			
			Query query = entityManager.createQuery(criteriaQuery);
			@SuppressWarnings("unchecked")
			List<ChainClusterDB> numHomologsStringItemDBs = query.getResultList();
			
			for(ChainClusterDB homologsInfoItemDB : numHomologsStringItemDBs)
			{
				result.add(ChainCluster.create(homologsInfoItemDB));
			}
			
			return result;
		}
		catch(Throwable e)
		{
			e.printStackTrace();
			throw new DaoException(e);
		}
		finally
		{
			try
			{
				entityManager.close();
			}
			catch(Throwable t)
			{
				t.printStackTrace();
			}
		}
	}


	@Override
	public List<PDBSearchResult> getPdbSearchItemsForUniProt(String uniProtId)
			throws DaoException {
		EntityManager entityManager = null;
	
		try
		{
			List<PDBSearchResult> resultList = new ArrayList<PDBSearchResult>();
			
			entityManager = EntityManagerHandler.getEntityManager();
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<PdbInfoDB> criteriaQuery = criteriaBuilder.createQuery(PdbInfoDB.class);
			
			Root<ChainClusterDB> root = criteriaQuery.from(ChainClusterDB.class);
			criteriaQuery.select(root.get(ChainClusterDB_.pdbInfo));
			criteriaQuery.where(criteriaBuilder.equal(root.get(ChainClusterDB_.refUniProtId), uniProtId));
			Query query = entityManager.createQuery(criteriaQuery);
			
			@SuppressWarnings("unchecked")
			List<PdbInfoDB> pdbItemDBs = query.getResultList();
			
			for(PdbInfoDB pdbItemDB: pdbItemDBs){
				PDBSearchResult result = new PDBSearchResult(pdbItemDB.getUid(),
															pdbItemDB.getPdbCode(), 
															pdbItemDB.getTitle(), 
															pdbItemDB.getReleaseDate(), 
															pdbItemDB.getSpaceGroup(), 
															pdbItemDB.getResolution(), 
															pdbItemDB.getRfreeValue(), 
															pdbItemDB.getExpMethod());
				
				resultList.add(result);
			}
			
			return resultList;
			
		}
		catch(Throwable e)
		{
			e.printStackTrace();
			throw new DaoException(e);
		}
		finally
		{
			try
			{
				entityManager.close();
			}
			catch(Throwable t)
			{
				t.printStackTrace();
			}
		}
		
	}
}