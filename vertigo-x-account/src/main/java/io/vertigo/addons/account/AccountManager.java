package io.vertigo.addons.account;

import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.file.model.VFile;
import io.vertigo.lang.Component;

import java.util.Collection;
import java.util.Set;

/**
 * @author pchretien
 */
public interface AccountManager extends Component {

	/**
	 * @param accountURI Account to logged
	 */
	void login(URI<Account> accountURI);

	/**
	 * @return Logged account
	 */
	URI<Account> getLoggedAccount();

	//----

	/**
	 * @return Accounts count
	 */
	long getAccountsCount();

	/**
	 * @param accountURI Account uri
	 * @return Account
	 */
	Account getAccount(URI<Account> accountURI);

	//Can't get all accounts without Filter (user ListState filter)
	//Collection<Account> getAllAccount();

	/**
	 * @param accountURI Account uri
	 * @return Set of groups of this account
	 */
	Set<URI<AccountGroup>> getGroupURIs(URI<Account> accountURI);

	//l'id doit être renseigné !!
	void saveAccount(Account account);

	//-----Gestion des groupes
	/**
	 * @return
	 */
	long getGroupsCount();

	//il est possible de proposer tous les groupes mais pas tous les accounts ?
	Collection<AccountGroup> getAllGroups();

	AccountGroup getGroup(URI<AccountGroup> groupURI);

	Set<URI<Account>> getAccountURIs(URI<AccountGroup> groupURI);

	void saveGroup(AccountGroup group);

	//-----
	void attach(URI<Account> accountURI, URI<AccountGroup> groupURI);

	void detach(URI<Account> accountURI, URI<AccountGroup> groupURI);

	//-----
	void setPhoto(URI<Account> accountURI, VFile photo);

	VFile getPhoto(URI<Account> accountURI);

}
