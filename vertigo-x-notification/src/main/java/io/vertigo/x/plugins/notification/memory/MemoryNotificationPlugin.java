package io.vertigo.x.plugins.notification.memory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;

import io.vertigo.commons.daemon.Daemon;
import io.vertigo.commons.daemon.DaemonManager;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.lang.Assertion;
import io.vertigo.x.account.Account;
import io.vertigo.x.impl.notification.NotificationEvent;
import io.vertigo.x.impl.notification.NotificationPlugin;
import io.vertigo.x.notification.Notification;

/**
 * @author pchretien
 */
public final class MemoryNotificationPlugin implements NotificationPlugin {
	private final Map<URI<Account>, List<Notification>> notificationsByAccountURI = new ConcurrentHashMap<>();

	/**
	 * @param daemonManager Daemon Manager
	 */
	@Inject
	public MemoryNotificationPlugin(final DaemonManager daemonManager) {
		Assertion.checkNotNull(daemonManager);
		//-----
		daemonManager.registerDaemon("cleanTooOldMemoryNotification", RemoveTooOldNotificationsDaemon.class, 1000, this);
	}

	/** {@inheritDoc} */
	@Override
	public void send(final NotificationEvent notificationEvent) {
		Assertion.checkNotNull(notificationEvent);
		//-----
		//0 - Remplir la pile des événements

		//1 - Dépiler les événemnts en asynchrone FIFO
		for (final URI<Account> accountURI : notificationEvent.getToAccountURIs()) {
			obtainNotifications(accountURI).add(notificationEvent.getNotification());
		}

		//2 - gestion globale async des erreurs
	}

	/** {@inheritDoc} */
	@Override
	public List<Notification> getCurrentNotifications(final URI<Account> userProfileURI) {
		Assertion.checkNotNull(userProfileURI);
		//-----
		final List<Notification> notifications = notificationsByAccountURI.get(userProfileURI);
		if (notifications == null) {
			return Collections.emptyList();
		}
		cleanTooOldNotifications(notifications);
		return notifications;
	}

	private List<Notification> obtainNotifications(final URI<Account> accountURI) {
		Assertion.checkNotNull(accountURI);
		//-----
		List<Notification> notifications = notificationsByAccountURI.get(accountURI);
		if (notifications == null) {
			notifications = new ArrayList<>();
			notificationsByAccountURI.put(accountURI, notifications);
		}
		cleanTooOldNotifications(notifications);
		return notifications;
	}

	/** {@inheritDoc} */
	@Override
	public void remove(final URI<Account> accountURI, final UUID notificationUUID) {
		final List<Notification> notifications = notificationsByAccountURI.get(accountURI);
		if (notifications != null) {
			for (final Iterator<Notification> it = notifications.iterator(); it.hasNext();) {
				final Notification notification = it.next();
				if (notification.getUuid().equals(notificationUUID)) {
					it.remove();
				}
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public void removeAll(final String type, final String targetUrl) {
		for (final List<Notification> notifications : notificationsByAccountURI.values()) {
			for (final Iterator<Notification> it = notifications.iterator(); it.hasNext();) {
				final Notification notification = it.next();
				if (notification.getType().equals(type) && notification.getTargetUrl().equals(targetUrl)) {
					it.remove();
				}
			}
		}
	}

	/**
	 * @author npiedeloup
	 */
	public static final class RemoveTooOldNotificationsDaemon implements Daemon {
		private final MemoryNotificationPlugin memoryNotificationPlugin;

		/**
		 * @param memoryNotificationPlugin This plugin
		 */
		public RemoveTooOldNotificationsDaemon(final MemoryNotificationPlugin memoryNotificationPlugin) {
			Assertion.checkNotNull(memoryNotificationPlugin);
			//------
			this.memoryNotificationPlugin = memoryNotificationPlugin;
		}

		/** {@inheritDoc} */
		@Override
		public void run() {
			memoryNotificationPlugin.cleanTooOldNotifications();
		}
	}

	void cleanTooOldNotifications() {
		for (final List<Notification> notifications : notificationsByAccountURI.values()) {
			cleanTooOldNotifications(notifications);
		}
	}

	private static void cleanTooOldNotifications(final List<Notification> notifications) {
		//on commence par la fin, dès qu'un élément est ok on stop les suppressions
		for (final ListIterator<Notification> it = notifications.listIterator(notifications.size()); it.hasPrevious();) {
			final Notification notification = it.previous();
			if (notification.getTTLInSeconds() >= 0 && notification.getCreationDate().getTime() + notification.getTTLInSeconds() * 1000 < System.currentTimeMillis()) {
				it.remove();
			} else {
				break; //un élément est ok on stop les suppressions
			}
		}
	}
}
