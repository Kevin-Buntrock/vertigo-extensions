package io.vertigo.x.plugins.notification.redis;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import io.vertigo.commons.daemon.Daemon;
import io.vertigo.commons.daemon.DaemonManager;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;
import io.vertigo.util.MapBuilder;
import io.vertigo.x.account.Account;
import io.vertigo.x.connectors.redis.RedisConnector;
import io.vertigo.x.impl.notification.NotificationEvent;
import io.vertigo.x.impl.notification.NotificationPlugin;
import io.vertigo.x.notification.Notification;
import io.vertigo.x.notification.NotificationBuilder;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

/**
 * @author pchretien
 */
public final class RedisNotificationPlugin implements NotificationPlugin {
	private static final String CODEC_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
	private final RedisConnector redisConnector;

	/**
	 * Constructor.
	 * @param redisConnector the connector to REDIS database
	 * @param daemonManager Daemon Manager
	 */
	@Inject
	public RedisNotificationPlugin(final RedisConnector redisConnector, final DaemonManager daemonManager) {
		Assertion.checkNotNull(redisConnector);
		Assertion.checkNotNull(daemonManager);
		//-----
		this.redisConnector = redisConnector;
		daemonManager.registerDaemon("cleanTooOldRedisNotification", RemoveTooOldElementsDaemon.class, 60 * 1000, this); //every minute

	}

	/** {@inheritDoc} */
	@Override
	public void send(final NotificationEvent notificationEvent) {
		//1 notif is store 5 times :
		// - data in map with key= notif:$uuid
		// - uuid in queue with key= notifs:all (for purge)
		// - uuid in queue with key= notifs:$accountId
		// - uuid in queue with key= type:$type;target:$target;uuid
		// - accountId in queue with key= accounts:$uuid

		try (final Jedis jedis = redisConnector.getResource()) {
			final Notification notification = notificationEvent.getNotification();
			final String uuid = notification.getUuid().toString();
			final String typedTarget = "type:" + notification.getType() + ";target:" + notification.getTargetUrl() + ";uuid";
			try (final Transaction tx = jedis.multi()) {
				tx.hmset("notif:" + uuid, toMap(notification));
				tx.lrem("notifs:all", 0, uuid);
				tx.lpush("notifs:all", uuid);

				for (final URI<Account> accountURI : notificationEvent.getToAccountURIs()) {
					final String notifiedAccount = "notifs:" + accountURI.getId();
					//On publie la notif (the last wins)
					tx.lrem(notifiedAccount, 0, uuid);
					tx.lpush(notifiedAccount, uuid);
					tx.lrem("accounts:" + uuid, 0, notifiedAccount);
					tx.lpush("accounts:" + uuid, notifiedAccount);
					tx.lrem(typedTarget, 0, uuid);
					tx.lpush(typedTarget, uuid);
				}
				tx.exec();
			} catch (final IOException ex) {
				throw WrappedException.wrapIfNeeded(ex, "Fail to send notif");
			}

		}
	}

	private static Map<String, String> toMap(final Notification notification) {
		final String creationDate = new SimpleDateFormat(CODEC_DATE_FORMAT).format(notification.getCreationDate());
		return new MapBuilder<String, String>()
				.put("uuid", notification.getUuid().toString())
				.put("sender", notification.getSender())
				.putNullable("type", notification.getType())
				.put("title", notification.getTitle())
				.put("content", notification.getContent())
				.put("creationDate", creationDate)
				.put("ttlInSeconds", String.valueOf(notification.getTTLInSeconds()))
				.put("targetUrl", notification.getTargetUrl())
				.build();
	}

	private static Notification fromMap(final Map<String, String> data) {
		try {
			final Date creationDate = new SimpleDateFormat(CODEC_DATE_FORMAT)
					.parse(data.get("creationDate"));
			return new NotificationBuilder(UUID.fromString(data.get("uuid")))
					.withSender(data.get("sender"))
					.withType(data.get("type"))
					.withTitle(data.get("title"))
					.withContent(data.get("content"))
					.withCreationDate(creationDate)
					.withTTLInSeconds(Integer.valueOf(data.get("ttlInSeconds")))
					.withTargetUrl(data.get("targetUrl"))
					.build();
		} catch (final ParseException e) {
			throw WrappedException.wrapIfNeeded(e, "Fail to parse notification");
		}
	}

	/** {@inheritDoc} */
	@Override
	public List<Notification> getCurrentNotifications(final URI<Account> accountURI) {
		final List<Response<Map<String, String>>> responses = new ArrayList<>();
		try (final Jedis jedis = redisConnector.getResource()) {
			final List<String> uuids = jedis.lrange("notifs:" + accountURI.getId(), 0, -1);
			final Transaction tx = jedis.multi();
			for (final String uuid : uuids) {
				responses.add(tx.hgetAll("notif:" + uuid));
			}
			tx.exec();
		}
		//----- we are using tx to avoid roundtrips
		final List<Notification> notifications = new ArrayList<>();
		for (final Response<Map<String, String>> response : responses) {
			final Map<String, String> data = response.get();
			if (!data.isEmpty()) {
				notifications.add(fromMap(data));
			}
		}
		cleanTooOldElements(notifications);
		return notifications;
	}

	/** {@inheritDoc} */
	@Override
	public void remove(final URI<Account> accountURI, final UUID notificationUUID) {
		try (final Jedis jedis = redisConnector.getResource()) {
			final String notifiedAccount = "notifs:" + accountURI.getId();
			final String uuid = notificationUUID.toString();
			//we remove notif from account stack and account from notif stack
			jedis.lrem(notifiedAccount, -1, uuid);
			jedis.lrem("accounts:" + uuid, -1, notifiedAccount);

			final List<String> notifiedAccounts = jedis.lrange("accounts:" + uuid, 0, -1);
			if (notifiedAccounts.isEmpty()) { //if no more account ref this notif we remove it
				//we remove list account for this notif
				jedis.del("accounts:" + uuid);

				//we remove uuid from queue by type and targetUrl
				final Notification notification = fromMap(jedis.hgetAll("notif:" + uuid));
				jedis.lrem("type:" + notification.getType() + ";target:" + notification.getTargetUrl() + ";uuid", -1, uuid);

				//we remove data of this notif
				jedis.del("notif:" + uuid);

				//we remove notif from global index (looking from tail)
				jedis.lrem("notifs:all", -1, uuid);
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public void removeAll(final String type, final String targetUrl) {
		try (final Jedis jedis = redisConnector.getResource()) {
			final List<String> uuids = jedis.lrange("type:" + type + ";target:" + targetUrl + ";uuid", 0, -1);
			for (final String uuid : uuids) {
				//we search accounts for this notif
				final List<String> notifiedAccounts = jedis.lrange("accounts:" + uuid, 0, -1);
				for (final String notifiedAccount : notifiedAccounts) {
					//we remove this notifs from accounts queue
					jedis.lrem(notifiedAccount, -1, uuid);
				}
				//we remove list account for this notif
				jedis.del("accounts:" + uuid);
				//we remove data of this notif
				jedis.del("notif:" + uuid);

				//we remove notif from global index (looking from tail)
				jedis.lrem("notifs:all", -1, uuid);
			}
			//we remove list notifId for this type and targetUrl
			jedis.del("type:" + type + ";target:" + targetUrl + ";uuid");
		}
	}

	/**
	 * @author npiedeloup
	 */
	public static final class RemoveTooOldElementsDaemon implements Daemon {
		private final RedisNotificationPlugin redisNotificationPlugin;

		/**
		 * @param redisNotificationPlugin This plugin
		 */
		public RemoveTooOldElementsDaemon(final RedisNotificationPlugin redisNotificationPlugin) {
			Assertion.checkNotNull(redisNotificationPlugin);
			//------
			this.redisNotificationPlugin = redisNotificationPlugin;
		}

		/** {@inheritDoc} */
		@Override
		public void run() {
			redisNotificationPlugin.cleanTooOldElements();
		}
	}

	void cleanTooOldElements() {

		boolean foundOneTooYoung = false;
		do {
			try (final Jedis jedis = redisConnector.getResource()) {
				final List<String> uuids = jedis.lrange("notifs:all", -1, 10); //return last (older) 10 uuid (but not sorted)
				for (final String uuid : uuids) {
					final Notification notification = fromMap(jedis.hgetAll("notif:" + uuid));
					if (isTooOld(notification)) {
						//we search accounts for this notif
						final List<String> notifiedAccounts = jedis.lrange("accounts:" + uuid, 0, -1);
						for (final String notifiedAccount : notifiedAccounts) {
							//we remove this notifs from accounts queue (looking from tail)
							jedis.lrem(notifiedAccount, -1, uuid);
						}
						//we remove list account for this notif
						jedis.del("accounts:" + uuid);
						//we remove data of this notif
						jedis.del("notif:" + uuid);
						//we remove notif from global index (looking from tail)
						jedis.lrem("notifs:all", -1, uuid);
						//we remove uuid from queue by type and targetUrl (looking from tail)
						jedis.lrem("type:" + notification.getType() + ";target:" + notification.getTargetUrl() + ";uuid", -1, uuid);
					} else {
						foundOneTooYoung = true; //we found one too recent, we stop after this loop
					}
				}
			}
		} while (!foundOneTooYoung);
	}

	private static void cleanTooOldElements(final List<Notification> notifications) {
		//on commence par la fin, dès qu'un élément est ok on stop les suppressions
		for (final ListIterator<Notification> it = notifications.listIterator(notifications.size()); it.hasPrevious();) {
			final Notification notification = it.previous();
			if (isTooOld(notification)) {
				it.remove();
			} else {
				break; //un élément est ok on stop les suppressions
			}
		}
	}

	private static boolean isTooOld(final Notification notification) {
		return notification.getTTLInSeconds() >= 0 && notification.getCreationDate().getTime() + notification.getTTLInSeconds() * 1000 < System.currentTimeMillis();
	}
}
