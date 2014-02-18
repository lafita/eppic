/**
 * 
 */
package ch.systemsx.sybit.crkwebui.server.db.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceUnit;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import eppic.model.JobDB_;
import eppic.model.PdbInfoDB_;
import ch.systemsx.sybit.crkwebui.shared.model.InputType;
import ch.systemsx.sybit.crkwebui.shared.model.StatusOfJob;
import eppic.model.JobDB;
import eppic.model.PdbInfoDB;

/**
 * Class to perform operations on CRK database such as adding by job,
 * removing a job or checking if a job is present in the database
 * @author biyani_n
 *
 */
public class DBHandler {
	
	public static final String DEFAULT_ONLINE_JPA = "eppicjpa";
	public static final String DEFAULT_OFFLINE_JPA = "eppic-offline-jpa";
	
	
	@PersistenceUnit
	private EntityManagerFactory emf;
	
	/**
	 * Default Constructor : Initializes factory with "crk" databse
	 */
	public DBHandler(){
		this.emf = Persistence.createEntityManagerFactory(DEFAULT_ONLINE_JPA);
	}
	
	/**
	 * Constructor
	 */
	public DBHandler(String persistenceUnitName){
		try{
			this.emf = Persistence.createEntityManagerFactory(persistenceUnitName);
		}
		catch(Throwable e){
			System.err.println(e.getMessage());
			System.err.println("Error in Initializing Entity Manager Factory with persistenceUnit: "+persistenceUnitName);
			System.err.println("Can you check if the database is really present, and the persistence.xml file contains the unit");
			System.exit(1);
		}
	}
	
	private EntityManager getEntityManager(){
		return this.emf.createEntityManager();
	}

	
	/**
	 * Adds entry to the DataBase when only PdbInfo specified.
	 * Inserts a pseudo job
	 * @param PdbInfoDB
	 * 
	 */
	public void addToDB(PdbInfoDB pdbScoreItem){
		EntityManager entityManager = this.getEntityManager();
		entityManager.getTransaction().begin();
		entityManager.persist(pdbScoreItem);

		String pdbCode = pdbScoreItem.getPdbCode();

		JobDB job = new JobDB();
		job.setJobId(pdbCode);
		job.setEmail(null);
		job.setInputName(pdbCode);
		job.setIp("localhost");
		job.setStatus(StatusOfJob.FINISHED.getName());
		job.setSubmissionDate(new Date());
		job.setInputType(InputType.PDBCODE.getIndex());
		job.setSubmissionId("-1");

		pdbScoreItem.setJob(job);
		job.setPdbInfo(pdbScoreItem);
		entityManager.persist(job);
		entityManager.getTransaction().commit();
		
		entityManager.close();
	
	}
	
	/**
	 * Adds entry to the DataBase when only pdbCode specified.
	 * Inserts a pseudo job and no pdbScoreItem
	 * @param String pdbCode
	 * 
	 */
	public void addToDB(String pdbCode){
		EntityManager entityManager = this.getEntityManager();
		entityManager.getTransaction().begin();
	
		JobDB job = new JobDB();
		job.setJobId(pdbCode);
		job.setEmail(null);
		job.setInputName(pdbCode);
		job.setIp("localhost");
		job.setStatus(StatusOfJob.ERROR.getName());
		job.setSubmissionDate(new Date());
		job.setInputType(InputType.PDBCODE.getIndex());
		job.setSubmissionId("-1");
		entityManager.persist(job);
		entityManager.getTransaction().commit();
		
		entityManager.close();
	}
	
	/**
	 * Removes entry from the DataBase of a specific JobID
	 * @param String jobID
	 * @return TRUE: if removed; FALSE: if not
	 */
	public boolean removefromDB(String jobID){
		EntityManager entityManager = this.getEntityManager();

		CriteriaBuilder cbJob = entityManager.getCriteriaBuilder();

		CriteriaQuery<JobDB> cqJob = cbJob.createQuery(JobDB.class);
		Root<JobDB> rootJob = cqJob.from(JobDB.class);
		cqJob.where(cbJob.equal(rootJob.get(JobDB_.jobId), jobID));
		cqJob.select(rootJob);
		List<JobDB> queryJobList = entityManager.createQuery(cqJob).getResultList();
		int querySize = queryJobList.size();
		
		if(querySize>0){
			entityManager.getTransaction().begin();
			for(JobDB job:queryJobList){
				try{
					// Remove the PdbScore entries related to the job
					CriteriaBuilder cbPDB = entityManager.getCriteriaBuilder();

					CriteriaQuery<PdbInfoDB> cqPDB = cbPDB.createQuery(PdbInfoDB.class);
					Root<PdbInfoDB> rootPDB = cqPDB.from(PdbInfoDB.class);
					cqPDB.where(cbPDB.equal(rootPDB.get(PdbInfoDB_.job), job));
					cqPDB.select(rootPDB);
					List<PdbInfoDB> queryPDBList = entityManager.createQuery(cqPDB).getResultList();
					
					if( queryPDBList != null) for(PdbInfoDB itemPDB : queryPDBList) entityManager.remove(itemPDB);
				
					// Remove Job
					entityManager.remove(job);
					
				}
				catch (Throwable e){
					System.err.println("Error in Removing Job from DB");
					e.printStackTrace();
				}
			}
			entityManager.getTransaction().commit();
			entityManager.close();
			return true;
		}
		else {
			entityManager.close();
			return false;
		}	
		
	}
	
	/**
	 * checks for a entry in the DataBase of a specific JobID
	 * @param String jobID
	 * @return TRUE: if present; FALSE: if not
	 */
	public boolean checkfromDB(String jobID){
		EntityManager entityManager = this.getEntityManager();
		
		CriteriaBuilder cbJob = entityManager.getCriteriaBuilder();

		CriteriaQuery<JobDB> cqJob = cbJob.createQuery(JobDB.class);
		Root<JobDB> rootJob = cqJob.from(JobDB.class);
		cqJob.where(cbJob.equal(rootJob.get(JobDB_.jobId), jobID));
		cqJob.select(rootJob);
		List<JobDB> queryJobList = entityManager.createQuery(cqJob).getResultList();
		int querySize = queryJobList.size();
		
		entityManager.close();
		
		if(querySize>0) return true;
		else return false;
		
	}
	
	/**
	 * copies a job from this database to another database
	 * @param DBHandler object to be copied in; JobID of the job to be copied
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 * 
	 */
	public void copytoDB(DBHandler dbhCopier, File jobDir) throws IOException, ClassNotFoundException{
		EntityManager orig = this.getEntityManager();
		EntityManager copier = dbhCopier.getEntityManager();
		
		//Get the job from the database
		CriteriaBuilder cbJob = orig.getCriteriaBuilder();

		CriteriaQuery<JobDB> cqJob = cbJob.createQuery(JobDB.class);
		Root<JobDB> rootJob = cqJob.from(JobDB.class);
		cqJob.where(cbJob.equal(rootJob.get(JobDB_.jobId), jobDir.getName()));
		cqJob.select(rootJob);
		JobDB queryJobOrig = orig.createQuery(cqJob).getSingleResult();
				
		//Create a new Job which is to be copied
		JobDB queryJob = new JobDB(queryJobOrig.getInputName(), queryJobOrig.getInputType());
		queryJob.setEmail(queryJobOrig.getEmail());
		queryJob.setIp(queryJobOrig.getIp());
		queryJob.setJobId(queryJobOrig.getJobId());
		queryJob.setStatus(queryJobOrig.getStatus());
		queryJob.setSubmissionDate(queryJobOrig.getSubmissionDate());
		queryJob.setSubmissionId(queryJobOrig.getSubmissionId());
		
		
		//Check if there is any PdbInfo entry 
		CriteriaBuilder cbPDB = orig.getCriteriaBuilder();
		CriteriaQuery<PdbInfoDB> cqPDB = cbPDB.createQuery(PdbInfoDB.class);
		Root<PdbInfoDB> rootPDB = cqPDB.from(PdbInfoDB.class);
		cqPDB.where(cbPDB.equal(rootPDB.get(PdbInfoDB_.job), queryJobOrig));
		cqPDB.select(rootPDB);
		int pdbScoreNum = orig.createQuery(cqPDB).getResultList().size();
		
		orig.close();		
		
		// Add Job to Copier database
		copier.getTransaction().begin();
		
		if( pdbScoreNum >= 1) {
			PdbInfoDB queryPDB = this.readFromWebui(jobDir);
			copier.persist(queryPDB);
						
			queryPDB.setJob(queryJob);
			queryJob.setPdbInfo(queryPDB);
			copier.persist(queryJob);
		}
		else copier.persist(queryJob);
		
		copier.getTransaction().commit();	
		copier.close();
		
	}
	
	/**
	 * Returns a list of user-jobs from the database
	 * @param 
	 * @return List<String>
	 */
	public List<String> getUserJobList(){
		EntityManager entityManager = this.getEntityManager();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		
		CriteriaQuery<String> criteriaQuery = criteriaBuilder.createQuery(String.class);
		Root<JobDB> jobRoot = criteriaQuery.from(JobDB.class);
		criteriaQuery.select(jobRoot.get(JobDB_.jobId));
		Predicate condition = criteriaBuilder.notEqual(jobRoot.get(JobDB_.submissionId), -1);
		criteriaQuery.where(condition);

		List<String> queryJobList = entityManager.createQuery(criteriaQuery).getResultList();
		
		entityManager.close();
		
		return queryJobList;
		
	}
	
	/**
	 * Returns a list of user-jobs from the database after a specified submission date
	 * @param Date subDate
	 * @return List<String>
	 */
	public List<String> getUserJobList(Date afterDate){
		EntityManager entityManager = this.getEntityManager();
		
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		
		CriteriaQuery<String> criteriaQuery = criteriaBuilder.createQuery(String.class);
		Root<JobDB> jobRoot = criteriaQuery.from(JobDB.class);
		criteriaQuery.select(jobRoot.get(JobDB_.jobId));
		Predicate idCondition = criteriaBuilder.notEqual(jobRoot.get(JobDB_.submissionId), -1);
		Predicate dateCondition = criteriaBuilder.greaterThanOrEqualTo(jobRoot.get(JobDB_.submissionDate), afterDate);
		Predicate condition = criteriaBuilder.and(idCondition, dateCondition);
		criteriaQuery.where(condition);

		List<String> queryJobList = entityManager.createQuery(criteriaQuery).getResultList();
		
		entityManager.close();
		
		return queryJobList;
		
	}
	
	/**
	 * Returns a PDBScoreItemDB object after reading it from the job's *.webui.dat file
	 * @param Directory Path
	 * @return PDBScoreItemDB Object
	 */
	public PdbInfoDB readFromWebui(File jobDir) throws IOException, ClassNotFoundException{		
		EntityManager entityManager = this.getEntityManager();
		
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		
		CriteriaQuery<String> criteriaQuery = criteriaBuilder.createQuery(String.class);
		Root<JobDB> jobRoot = criteriaQuery.from(JobDB.class);
		criteriaQuery.select(jobRoot.get(JobDB_.inputName));
		Predicate condition = criteriaBuilder.equal(jobRoot.get(JobDB_.jobId), jobDir.getName());
		criteriaQuery.where(condition);

		String pdbID = entityManager.createQuery(criteriaQuery).getSingleResult();
		int lastPeriodPos = pdbID.lastIndexOf('.');
		if (lastPeriodPos >= 0)
	    {
			pdbID = pdbID.substring(0, lastPeriodPos);
	    }
		
		File webuiFile = new File(jobDir, pdbID + ".webui.dat");
		
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(webuiFile));
		PdbInfoDB pdbScoreItem = (PdbInfoDB)in.readObject();
		in.close();
		return pdbScoreItem;
	}


}
