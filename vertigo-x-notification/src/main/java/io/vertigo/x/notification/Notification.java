package io.vertigo.x.notification;

import java.util.Date;
import java.util.UUID;

import io.vertigo.lang.Assertion;

/**
 * @author pchretien
 */
public final class Notification {
	private final UUID uuid;
	private final String sender;
	private final String type;
	private final String title;
	private final String content;
	private final int ttlInSeconds;
	private final String targetUrl;
	private final Date creationDate;

	/**
	 * Constructor.
	 * @param uuid Uuid
	 * @param sender Sender name
	 * @param type Type
	 * @param title Title
	 * @param content Content
	 * @param ttlInSeconds TimeToLive in seconds
	 * @param creationDate Create date
	 * @param targetUrl Target URL of this notification
	 */
	Notification(final UUID uuid, final String sender, final String type, final String title, final String content, final int ttlInSeconds, final Date creationDate, final String targetUrl) {
		Assertion.checkNotNull(uuid);
		Assertion.checkArgNotEmpty(sender);
		Assertion.checkArgNotEmpty(type);
		Assertion.checkArgNotEmpty(title);
		Assertion.checkArgNotEmpty(content);
		Assertion.checkArgument(ttlInSeconds == -1 || ttlInSeconds > 0, "ttl must be positive or undefined (-1).");
		Assertion.checkArgNotEmpty(targetUrl);
		Assertion.checkNotNull(creationDate);
		//-----
		this.uuid = uuid;
		this.sender = sender;
		this.type = type;
		this.title = title;
		this.content = content;
		this.ttlInSeconds = ttlInSeconds;
		this.creationDate = creationDate;
		this.targetUrl = targetUrl;
	}

	/**
	 * @return Uuid
	 */
	public UUID getUuid() {
		return uuid;
	}

	/**
	 * @return Sender's name
	 */
	public String getSender() {
		return sender;
	}

	/**
	 * @return Notification's type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @return Notification's type
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @return Notification's content
	 */
	public String getContent() {
		return content;
	}

	/**
	 * @return Notification's TTL in seconds
	 */
	public int getTTLInSeconds() {
		return ttlInSeconds;
	}

	/**
	 * @return Creation date
	 */
	public Date getCreationDate() {
		return creationDate;
	}

	/**
	 * @return Notification's target url
	 */
	public String getTargetUrl() {
		return targetUrl;
	}
}
