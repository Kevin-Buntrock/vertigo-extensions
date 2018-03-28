package io.vertigo.orchestra.domain.node;

import io.vertigo.dynamo.domain.model.Entity;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.domain.stereotype.Field;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.lang.Generated;

/**
 * This class is automatically generated.
 * DO NOT EDIT THIS FILE DIRECTLY.
 */
@Generated
@io.vertigo.dynamo.domain.stereotype.DataSpace("orchestra")
public final class ONode implements Entity {
	private static final long serialVersionUID = 1L;

	private String nodId;
	private Integer capacity;
	private Integer used;
	private java.time.Instant lastHeartbeat;

	/** {@inheritDoc} */
	@Override
	public URI<ONode> getURI() {
		return DtObjectUtil.createURI(this);
	}
	
	/**
	 * Champ : ID.
	 * Récupère la valeur de la propriété 'Id'.
	 * @return String nodId <b>Obligatoire</b>
	 */
	@Field(domain = "DO_O_UUID", type = "ID", required = true, label = "Id")
	public String getNodId() {
		return nodId;
	}

	/**
	 * Champ : ID.
	 * Définit la valeur de la propriété 'Id'.
	 * @param nodId String <b>Obligatoire</b>
	 */
	public void setNodId(final String nodId) {
		this.nodId = nodId;
	}
	
	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'capacity'.
	 * @return Integer capacity <b>Obligatoire</b>
	 */
	@Field(domain = "DO_O_INTEGER", required = true, label = "capacity")
	public Integer getCapacity() {
		return capacity;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'capacity'.
	 * @param capacity Integer <b>Obligatoire</b>
	 */
	public void setCapacity(final Integer capacity) {
		this.capacity = capacity;
	}
	
	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'used'.
	 * @return Integer used <b>Obligatoire</b>
	 */
	@Field(domain = "DO_O_INTEGER", required = true, label = "used")
	public Integer getUsed() {
		return used;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'used'.
	 * @param used Integer <b>Obligatoire</b>
	 */
	public void setUsed(final Integer used) {
		this.used = used;
	}
	
	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Last activity'.
	 * @return java.time.Instant lastHeartbeat <b>Obligatoire</b>
	 */
	@Field(domain = "DO_O_TIMESTAMP", required = true, label = "Last activity")
	public java.time.Instant getLastHeartbeat() {
		return lastHeartbeat;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Last activity'.
	 * @param lastHeartbeat java.time.Instant <b>Obligatoire</b>
	 */
	public void setLastHeartbeat(final java.time.Instant lastHeartbeat) {
		this.lastHeartbeat = lastHeartbeat;
	}
	
	/** {@inheritDoc} */
	@Override
	public String toString() {
		return DtObjectUtil.toString(this);
	}
}