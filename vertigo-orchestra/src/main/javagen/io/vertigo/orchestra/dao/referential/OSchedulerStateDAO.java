package io.vertigo.orchestra.dao.referential;

import javax.inject.Inject;

import io.vertigo.dynamo.impl.store.util.DAO;
import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.dynamo.store.StoreServices;
import io.vertigo.dynamo.task.TaskManager;
import io.vertigo.orchestra.domain.referential.OSchedulerState;
import io.vertigo.lang.Generated;

/**
 * This class is automatically generated.
 * DO NOT EDIT THIS FILE DIRECTLY.
 */
@Generated
public final class OSchedulerStateDAO extends DAO<OSchedulerState, java.lang.String> implements StoreServices {

	/**
	 * Contructeur.
	 * @param storeManager Manager de persistance
	 * @param taskManager Manager de Task
	 */
	@Inject
	public OSchedulerStateDAO(final StoreManager storeManager, final TaskManager taskManager) {
		super(OSchedulerState.class, storeManager, taskManager);
	}

}
