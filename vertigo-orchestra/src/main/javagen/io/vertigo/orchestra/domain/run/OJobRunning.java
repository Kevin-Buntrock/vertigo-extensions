package io.vertigo.orchestra.domain.run;

import io.vertigo.dynamo.domain.model.Entity;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.domain.model.VAccessor;
import io.vertigo.dynamo.domain.stereotype.Field;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.lang.Generated;

/**
 * This class is automatically generated.
 * DO NOT EDIT THIS FILE DIRECTLY.
 */
 @Generated
@io.vertigo.dynamo.domain.stereotype.DataSpace("orchestra")
public final class OJobRunning implements Entity {
	private static final long serialVersionUID = 1L;

	private String jid;

	private String jobname;

	private Long nodeId;

	private java.time.ZonedDateTime execDate;

	private java.time.ZonedDateTime maxExecDate;

	@io.vertigo.dynamo.domain.stereotype.Association(
			name = "A_JOB_USR",
			fkFieldName = "USR_ID",
			primaryDtDefinitionName = "DT_O_USER",
			primaryIsNavigable = true,
			primaryRole = "User",
			primaryLabel = "User",
			primaryMultiplicity = "0..1",
			foreignDtDefinitionName = "DT_O_JOB_RUNNING",
			foreignIsNavigable = false,
			foreignRole = "JobRunning",
			foreignLabel = "JobRunning",
			foreignMultiplicity = "0..*")
	private final VAccessor<io.vertigo.orchestra.domain.referential.OUser> usrIdAccessor = new VAccessor<>(io.vertigo.orchestra.domain.referential.OUser.class, "user");


	/** {@inheritDoc} */
	@Override
	public URI<OJobRunning> getURI() {
		return DtObjectUtil.createURI(this);
	}

	
	/**
	 * Champ : ID.
	 * Récupère la valeur de la propriété 'Id de l'execution du job'.
	 * @return String jid <b>Obligatoire</b>
	 */
	@Field(domain = "DO_O_IDENTIFIANT_JOB", type = "ID", required = true, label = "Id de l'execution du job")
	public String getJid() {
		return jid;
	}

	/**
	 * Champ : ID.
	 * Définit la valeur de la propriété 'Id de l'execution du job'.
	 * @param jid String <b>Obligatoire</b>
	 */
	public void setJid(final String jid) {
		this.jid = jid;
	}
	
	
	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Nom du job'.
	 * @return String jobname
	 */
	@Field(domain = "DO_O_LIBELLE", label = "Nom du job")
	public String getJobname() {
		return jobname;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Nom du job'.
	 * @param jobname String
	 */
	public void setJobname(final String jobname) {
		this.jobname = jobname;
	}
	
	
	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Id du noeud'.
	 * @return Long nodeId <b>Obligatoire</b>
	 */
	@Field(domain = "DO_O_IDENTIFIANT", required = true, label = "Id du noeud")
	public Long getNodeId() {
		return nodeId;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Id du noeud'.
	 * @param nodeId Long <b>Obligatoire</b>
	 */
	public void setNodeId(final Long nodeId) {
		this.nodeId = nodeId;
	}
	
	
	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Date d'execution'.
	 * @return java.time.ZonedDateTime execDate
	 */
	@Field(domain = "DO_O_TIMESTAMP", label = "Date d'execution")
	public java.time.ZonedDateTime getExecDate() {
		return execDate;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Date d'execution'.
	 * @param execDate java.time.ZonedDateTime
	 */
	public void setExecDate(final java.time.ZonedDateTime execDate) {
		this.execDate = execDate;
	}
	
	
	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Date Max d'execution (Date d'exec + TO)'.
	 * @return java.time.ZonedDateTime maxExecDate
	 */
	@Field(domain = "DO_O_TIMESTAMP", label = "Date Max d'execution (Date d'exec + TO)")
	public java.time.ZonedDateTime getMaxExecDate() {
		return maxExecDate;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Date Max d'execution (Date d'exec + TO)'.
	 * @param maxExecDate java.time.ZonedDateTime
	 */
	public void setMaxExecDate(final java.time.ZonedDateTime maxExecDate) {
		this.maxExecDate = maxExecDate;
	}
	
	
	/**
	 * Champ : FOREIGN_KEY.
	 * Récupère la valeur de la propriété 'User'.
	 * @return Long usrId
	 */
	@Field(domain = "DO_O_IDENTIFIANT", type = "FOREIGN_KEY", label = "User")
	public Long getUsrId() {
		return (Long)  usrIdAccessor.getId();
	}

	/**
	 * Champ : FOREIGN_KEY.
	 * Définit la valeur de la propriété 'User'.
	 * @param usrId Long
	 */
	public void setUsrId(final Long usrId) {
		usrIdAccessor.setId(usrId);
	}
	
	/**
	 * Association : User.
	 * @return io.vertigo.orchestra.domain.referential.OUser
	 */
				
	public io.vertigo.orchestra.domain.referential.OUser getUser() {
		return usrIdAccessor.get();
	}

	/**
	 * Retourne l'URI: User.
	 * @return URI de l'association
	 */
	public io.vertigo.dynamo.domain.model.URI<io.vertigo.orchestra.domain.referential.OUser> getUserURI() {
		return usrIdAccessor.getURI();
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return DtObjectUtil.toString(this);
	}
}
