package io.vertigo.x.workflow.dao.instance;

import java.util.Optional;

import javax.inject.Inject;

import io.vertigo.app.Home;
import io.vertigo.dynamo.impl.store.util.DAO;
import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.dynamo.store.StoreServices;
import io.vertigo.dynamo.task.TaskManager;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.dynamo.task.model.Task;
import io.vertigo.dynamo.task.model.TaskBuilder;
import io.vertigo.x.workflow.domain.instance.WfActivity;

/**
 * DAO : Accès à un object (DTO, DTC). 
 * WfActivityDAO
 */
public final class WfActivityDAO extends DAO<WfActivity, java.lang.Long> implements StoreServices {

	/**
	 * Contructeur.
	 * @param storeManager Manager de persistance
	 * @param taskManager Manager de Task
	 */
	@Inject
	public WfActivityDAO(final StoreManager storeManager, final TaskManager taskManager) {
		super(WfActivity.class, storeManager, taskManager);
	}

	/**
	 * Creates a taskBuilder.
	 * @param name  the name of the task
	 * @return the builder 
	 */
	private static TaskBuilder createTaskBuilder(final String name) {
		final TaskDefinition taskDefinition = Home.getApp().getDefinitionSpace().resolve(name, TaskDefinition.class);
		return Task.builder(taskDefinition);
	}

	/**
	 * Execute la tache TK_FIND_ACTIVITY_BY_DEFINITION_WORKFLOW.
	 * @param wfwId Long 
	 * @param wfadId Long 
	 * @return Option de io.vertigo.x.workflow.domain.instance.WfActivity wfActivity
	*/
	public Optional<io.vertigo.x.workflow.domain.instance.WfActivity> findActivityByDefinitionWorkflow(final Long wfwId, final Long wfadId) {
		final Task task = createTaskBuilder("TK_FIND_ACTIVITY_BY_DEFINITION_WORKFLOW")
				.addValue("WFW_ID", wfwId)
				.addValue("WFAD_ID", wfadId)
				.build();
		return Optional.ofNullable((io.vertigo.x.workflow.domain.instance.WfActivity) getTaskManager()
				.execute(task)
				.getResult());
	}

}
