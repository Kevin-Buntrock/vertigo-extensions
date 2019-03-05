package io.vertigo.workflow.services;

import javax.inject.Inject;

import org.junit.Assert;

import io.vertigo.app.config.AppConfig;
import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.commons.transaction.VTransactionWritable;
import io.vertigo.workflow.MyAppConfig;

public class DbWorkflowManagerTest extends AbstractWorkflowManagerTest {

	@Inject
	private VTransactionManager transactionManager;

	private VTransactionWritable transaction;

	@Override
	protected AppConfig buildAppConfig() {
		return MyAppConfig.configWithDb();
	}

	@Override
	protected void doSetUp() throws Exception {
		Assert.assertFalse("the previous test hasn't correctly close its transaction.",
				transactionManager.hasCurrentTransaction());
		// manage transactions
		transaction = transactionManager.createCurrentTransaction();
	}

	@Override
	protected void doTearDown() throws Exception {
		Assert.assertTrue("All tests must rollback a transaction.", transactionManager.hasCurrentTransaction());
		// close transaction
		transaction.rollback();
	}
}