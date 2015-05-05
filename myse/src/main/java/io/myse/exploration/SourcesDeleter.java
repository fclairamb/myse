package io.myse.exploration;

import static io.myse.common.LOG.LOG;
import io.myse.db.DBMgmt;
import io.myse.db.model.DBDescSource;
import io.myse.embeddedes.ElasticSearch;
import javax.persistence.EntityManager;

public class SourcesDeleter implements Runnable {

	@Override
	public void run() {
		EntityManager em = DBMgmt.getEntityManager();
		try {
			for (DBDescSource deleted : DBDescSource.allDeleted(em)) {
				em.getTransaction().begin();
				try {
					int nb = -1;
					while (nb != 0) {
						nb = deleted.deleteDocs(em);
						LOG.info("Deleting {}: Deleted {} DB files.", deleted.getShortName(), nb);
					}
					if (ElasticSearch.deleteIndex(deleted.getShortName())) {
						LOG.info("Deleting {}: Deleted index.", deleted.getShortName());
					}
					em.remove(deleted);
				} finally {
					em.getTransaction().commit();
				}
			}
			for (String shortName : ElasticSearch.listIndexes()) {
				DBDescSource dbSource = DBDescSource.get(shortName, em);
				if (dbSource == null) {
					LOG.warn("Deleting index \"{}\".", shortName);
					ElasticSearch.deleteIndex(shortName);
				}
			}
		} catch (Exception ex) {
			LOG.error(String.format("{}.run", this), ex);
		} finally {
			em.close();
		}
	}

	@Override
	public String toString() {
		return "SourcesDeleter";
	}

}
