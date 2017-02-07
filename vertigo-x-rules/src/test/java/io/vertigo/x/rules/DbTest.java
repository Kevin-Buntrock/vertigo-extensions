package io.vertigo.x.rules;

import javax.inject.Inject;

import org.junit.Assert;

import io.vertigo.dynamo.transaction.VTransactionManager;
import io.vertigo.dynamo.transaction.VTransactionWritable;

/**
 * 
 * @author xdurand
 *
 */
public class DbTest {

	@Inject
	private VTransactionManager transactionManager;

	private VTransactionWritable transaction;

	protected void doSetUp() {
		Assert.assertFalse("the previous test hasn't correctly close its transaction.",
				transactionManager.hasCurrentTransaction());
		// manage transactions
		transaction = transactionManager.createCurrentTransaction();
	}

	protected void doTearDown() {
		Assert.assertTrue("All tests must rollback a transaction.", transactionManager.hasCurrentTransaction());
		// close transaction
		transaction.rollback();
	}

}
