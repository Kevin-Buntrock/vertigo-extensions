package io.vertigo.x.notification;

import java.util.Date;
import java.util.UUID;

import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;
import io.vertigo.util.DateUtil;

/**
 * @author pchretien
 */
public final class NotificationBuilder implements Builder<Notification> {
	private String myType;
	private String myTitle;
	private String myContent;
	private String mySender;
	private Date myCreationDate;
	private int myTtlInSeconds = -1;
	private String myTargetUrl;
	private final UUID uuid;

	/**
	 * Constructor.
	 */
	public NotificationBuilder() {
		uuid = UUID.randomUUID();
	}

	/**
	 * Constructor.
	 * @param uuid Notification uuid
	 */
	public NotificationBuilder(final UUID uuid) {
		Assertion.checkNotNull(uuid);
		//-----
		this.uuid = uuid;
	}

	/**
	 * @param sender Sender's name
	 * @return this builder
	 */
	public NotificationBuilder withSender(final String sender) {
		Assertion.checkArgument(mySender == null, "sender already set");
		Assertion.checkArgNotEmpty(sender);
		//-----
		mySender = sender;
		return this;
	}

	/**
	 * @param type Notification's type
	 * @return this builder
	 */
	public NotificationBuilder withType(final String type) {
		Assertion.checkArgument(myType == null, "type already set");
		//type is nullable
		//-----
		myType = type;
		return this;
	}

	/**
	 * @param creationDate Creation date
	 * @return this builder
	 */
	public NotificationBuilder withCreationDate(final Date creationDate) {
		Assertion.checkArgument(myCreationDate == null, "creationDate already set");
		Assertion.checkNotNull(creationDate);
		//-----
		myCreationDate = creationDate;
		return this;
	}

	/**
	 * @param title Notification's title
	 * @return this builder
	 */
	public NotificationBuilder withTitle(final String title) {
		Assertion.checkArgument(myTitle == null, "title already set");
		Assertion.checkArgNotEmpty(title);
		//-----
		myTitle = title;
		return this;
	}

	/**
	 * @param content Notification's content
	 * @return this builder
	 */
	public NotificationBuilder withContent(final String content) {
		Assertion.checkArgument(myContent == null, "content already set");
		Assertion.checkArgNotEmpty(content);
		//-----
		myContent = content;
		return this;
	}

	/**
	 * @param ttlInSeconds Notification's TimeToLive
	 * @return this builder
	 */
	public NotificationBuilder withTTLInSeconds(final int ttlInSeconds) {
		Assertion.checkArgument(ttlInSeconds > 0 || ttlInSeconds == -1, "ttl must be strictly positive or undefined.");
		//-----
		myTtlInSeconds = ttlInSeconds;
		return this;
	}

	/**
	 * @param targetUrl Notification's target url
	 * @return this builder
	 */
	public NotificationBuilder withTargetUrl(final String targetUrl) {
		Assertion.checkArgument(myTargetUrl == null, "targetUrl already set");
		Assertion.checkArgNotEmpty(targetUrl);
		//-----
		myTargetUrl = targetUrl;
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public Notification build() {
		if (myCreationDate == null) {
			myCreationDate = DateUtil.newDateTime();
		}
		return new Notification(uuid, mySender, myType, myTitle, myContent, myTtlInSeconds, myCreationDate, myTargetUrl);
	}
}
