package ch.systemsx.sybit.crkwebui.server.db.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import model.JobDB;
import model.PDBScoreItemDB;
import model.UserSessionDB;
import ch.systemsx.sybit.crkwebui.server.db.EntityManagerHandler;
import ch.systemsx.sybit.crkwebui.shared.model.InputType;
import ch.systemsx.sybit.crkwebui.shared.model.StatusOfJob;

public class UploadToDb {

	/**
	 * @param args
	 */
	public static void main(String[] args) {


		if (args.length<1) {
			System.err.println("Usage: UploadToDb <dir name>");
			System.exit(1);
		}
		
		String jobDirectoriesRootName = args[0];
		
		Date submissionDate = new Date();
		


		File jobDirectoriesRoot = new File(jobDirectoriesRootName);
		
		File[] jobsDirectories = jobDirectoriesRoot.listFiles();
		
		for (File jobDirectory : jobsDirectories)
		{
			
			if (!jobDirectory.isDirectory()) continue;
			
			if (!jobDirectory.getName().matches("^\\d\\w\\w\\w$")) continue; 
			
			try 
			{
				System.out.print(jobDirectory.getName()+" ");
				
				ObjectInputStream in = new ObjectInputStream(new FileInputStream(
						new File(jobDirectory, jobDirectory.getName() + ".webui.dat")));
				
				PDBScoreItemDB pdbScoreItem = (PDBScoreItemDB)in.readObject();
		
				EntityManager entityManager = EntityManagerHandler.getEntityManager();
		
				long start = System.currentTimeMillis();
				
				//Remove Job from DB if already present
				entityManager.getTransaction().begin();
				
				String queryJobstr = "FROM " + JobDB.class.getName() + " WHERE jobId='" + pdbScoreItem.getPdbName() +"'";
				TypedQuery<JobDB> queryJob = entityManager.createQuery(queryJobstr, JobDB.class);
								
				List<JobDB> queryJobList = queryJob.getResultList();
				int querySize = queryJobList.size();
				
				if(querySize>0){
					System.out.print(": Already present (" + querySize + " time(s)) Removing and Updating.. ");
					for(JobDB itemJobDB:queryJobList){
						Long itemUid = itemJobDB.getUid();
						String queryPDBstr = "FROM " + PDBScoreItemDB.class.getName() + " WHERE jobItem_uid='" + itemUid +"'";
						TypedQuery<PDBScoreItemDB> queryPDB = entityManager.createQuery(queryPDBstr, PDBScoreItemDB.class);
						List<PDBScoreItemDB> queryPDBList = queryPDB.getResultList();
						if( queryPDBList != null)
						{
							for(PDBScoreItemDB itemPDB : queryPDBList)
							{
								entityManager.remove(itemPDB);
							}
						}
						entityManager.remove(itemJobDB);
					}
				}
				entityManager.getTransaction().commit();
				
				try
				{
					entityManager.getTransaction().begin();
					entityManager.persist(pdbScoreItem);
			
					String pdbCode = pdbScoreItem.getPdbName();
		
					JobDB job = new JobDB();
					job.setJobId(pdbCode);
					job.setEmail(null);
					job.setInput(pdbCode);
					job.setIp("localhost");
					job.setStatus(StatusOfJob.FINISHED.getName());
					job.setSubmissionDate(submissionDate);
					job.setInputType(InputType.PDBCODE.getIndex());
					job.setSubmissionId("-1");
		
					pdbScoreItem.setJobItem(job);
					job.setPdbScoreItem(pdbScoreItem);
					entityManager.persist(job);
		
					entityManager.getTransaction().commit();
					
					long end = System.currentTimeMillis();
					
					System.out.print(((end-start)/1000)+"s");
					
				}
				catch(Throwable e)
				{
					e.printStackTrace();
		
					try
					{
						entityManager.getTransaction().rollback();
					}
					catch(Throwable t)
					{
						t.printStackTrace();
					}
		
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
					System.out.println();
				}
			}
			catch(IOException t)
			{
				System.err.println("Failed to read file "+t.getMessage());
			}

			catch(Throwable t)
			{
				t.printStackTrace();
			}
		}



		


	}


}
