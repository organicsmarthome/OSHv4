package osh.comdriver.logger;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.RollbackException;

import org.eclipse.persistence.exceptions.DatabaseException;

import osh.core.exceptions.OSHException;
import osh.core.logging.IGlobalLogger;
import osh.datatypes.registry.StateExchange;


/**
 * This class helps with logging of all measurements 
 * / actions taken by the controller box 
 * 
 * @author Kaibin Bao, Ingo Mauser
 */
public abstract class ValueDatabaseLogger extends ValueLogger {
	
	protected EntityManagerFactory emf;
	
	
	/**
	 * CONSTRUCTOR
	 * @param persistenceUnit
	 * @param logger
	 */
	public ValueDatabaseLogger(
			String persistenceUnit,
			IGlobalLogger logger) {
		this.dbwriter = new WriterThread(persistenceUnit, logger);
		this.dbwriter.start();
		this.dbwriter.setName("DB Writer Thread for PU " + persistenceUnit);
		this.logger = logger;
	}

	
	/**
	 * ref. to singleton writer 
	 */
	protected WriterThread dbwriter = null;

	public static class WriterThread extends Thread {
		private EntityManagerFactory emf = null;
		private BlockingQueue<Object> entities;
		private IGlobalLogger logger;
		
		private Set<Class<? extends Object>> unloggableTypes = new HashSet<>();

		public WriterThread(String persistenceUnit, IGlobalLogger logger) {
			this.emf = Persistence.createEntityManagerFactory(persistenceUnit);
			this.entities = new LinkedBlockingDeque<Object>();
			this.logger = logger;
		}
		
		public void put(Object e) {
			try {
				entities.put(e);
			} 
			catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}

		@Override
		public void run() {
			
			try {

				EntityManager em = emf.createEntityManager();
				em.getTransaction().begin();

				while (true) {
					// get top entity
					Object entity = entities.peek();
					// if there is currently no entity -> flush
					if ( entity == null ) {
						if ( em.getTransaction().isActive() ) {
							try {
								em.getTransaction().commit();
							}
							catch (DatabaseException e) {
								// should not happen...
								this.logger.logError(e.getMessage());
							}
							catch (RollbackException rbe) {
								// everything has been rolled back ???
//								this.logger.logDebug(rbe.getMessage(), rbe);
								this.logger.logDebug(rbe.getMessage());
							}
							finally {
								em.clear();
								em.getTransaction().begin();
							}
							
						} 
						else {
							em.getTransaction().begin();
						}
					}
					
					try {
						// wait for entity, get top entity and remove it from queue
						entity = entities.take();
					} 
					catch (InterruptedException e) {
						e.printStackTrace();
						return;
					}

					// only log loggable types
					if (!this.unloggableTypes.contains(entity.getClass())) {
						try {
//							em.persist(entity);
							em.merge(entity);
						} 
						catch (DatabaseException e) {
//							this.logger.logDebug(e.getMessage(), e);
							this.logger.logDebug(e.getMessage());
							@SuppressWarnings("unused")
							int debug = 0;
							// should not happen...
						}
						catch ( RollbackException e ) {
//							this.logger.logDebug(e.getMessage(), e);
							this.logger.logDebug(e.getMessage());
							// rollback is bad...
							@SuppressWarnings("unused")
							int debug = 0;
							//...it should not happen
						} 
						catch (IllegalArgumentException e) {
//							this.logger.logWarning("Could not log entity, ignore it from now on", e));
							this.logger.logWarning(e.getMessage());
							this.unloggableTypes.add(entity.getClass());
						} 
						catch ( Exception e ) {
//							this.logger.logDebug(e.getMessage(), e);
							this.logger.logDebug(e.getMessage());
						}
						
						// do NOT commit all the time...
//						finally {
//							try {
//								em.getTransaction().commit();
//								em.clear();
//							} catch (Exception e) {
//								this.logger.logDebug(e.getMessage());
//								//e.printStackTrace();
//							}
//						}
					} /* if (!unloggableTypes.contains(entity.getClass())) */
					
					try {
						Thread.sleep(1); // NEW by IMA
					}
					catch (Exception e) {
						logger.logDebug(e);
					}
				} /* while(true) */

			} catch (Exception e) {
				logger.logDebug("ExchangeDatabaseWriter died: ", e);
				logger.logError("ARGHH!!! Writer thread died! because of ..." + e.getStackTrace(), e);
			}
			//em.close();
		}
	}
	
	protected void put( Object entity ) {
		dbwriter.put(entity);
	}
	
	@Override
	public void log(long timestamp, Object entity) throws OSHException {

		if (entity == null) {
			throw new OSHException("Persisting NULL does not make sense...");
		}

		this.put(entity);
	}
	
	/**
	 * Retrieves the device state from the database
	 * 
	 * @param ox
	 */
	public StateExchange retrieve( UUID device, Long timestamp, Class<? extends StateExchange> exchangeClass ) {
		StateExchange stateExchange;

		EntityManager em = emf.createEntityManager();
		
		em.getTransaction().begin();
		
		Query q = em.createQuery("SELECT p FROM " + exchangeClass.getSimpleName() + " p WHERE p.mid.deviceId = :uuid AND p.mid.timestamp <= :timestamp ORDER BY p.mid.timestamp DESC").setMaxResults(1);
		q.setParameter("uuid", device.toString());
		q.setParameter("timestamp", timestamp);
		if ( q.getResultList().size() == 1 ) {
			stateExchange = (StateExchange) q.getSingleResult();
		} 
		else {
			stateExchange = null;
		}
		
		em.getTransaction().commit();
		
		em.close();
		
		return stateExchange;
	}
	
	@Override
	protected void finalize() throws Throwable {
        emf.close();
		super.finalize();
	}
}
