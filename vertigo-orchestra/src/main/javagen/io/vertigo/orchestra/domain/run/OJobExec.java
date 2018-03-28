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
public final class OJobExec implements Entity {
	private static final long serialVersionUID = 1L;

	private String jexId;
	private java.time.Instant startExecInstant;
	private java.time.Instant maxExecInstant;

	@io.vertigo.dynamo.domain.stereotype.Association(
			name = "A_JEX_JRN",
			fkFieldName = "JOB_ID",
			primaryDtDefinitionName = "DT_O_JOB_RUN",
			primaryIsNavigable = true,
			primaryRole = "JobRun",
			primaryLabel = "JobRun",
			primaryMultiplicity = "1..1",
			foreignDtDefinitionName = "DT_O_JOB_EXEC",
			foreignIsNavigable = false,
			foreignRole = "JobExec",
			foreignLabel = "JobEXec",
			foreignMultiplicity = "0..*")
	private final VAccessor<io.vertigo.orchestra.domain.run.OJobRun> jobIdAccessor = new VAccessor<>(io.vertigo.orchestra.domain.run.OJobRun.class, "JobRun");

	@io.vertigo.dynamo.domain.stereotype.Association(
			name = "A_JEX_JMO",
			fkFieldName = "JMO_ID",
			primaryDtDefinitionName = "DT_O_JOB_MODEL",
			primaryIsNavigable = true,
			primaryRole = "JobModel",
			primaryLabel = "JobModel",
			primaryMultiplicity = "1..1",
			foreignDtDefinitionName = "DT_O_JOB_EXEC",
			foreignIsNavigable = false,
			foreignRole = "JobExec",
			foreignLabel = "JobExec",
			foreignMultiplicity = "0..*")
	private final VAccessor<io.vertigo.orchestra.domain.model.OJobModel> jmoIdAccessor = new VAccessor<>(io.vertigo.orchestra.domain.model.OJobModel.class, "JobModel");

	/** {@inheritDoc} */
	@Override
	public URI<OJobExec> getURI() {
		return DtObjectUtil.createURI(this);
	}
	
	/**
	 * Champ : ID.
	 * Récupère la valeur de la propriété 'Id'.
	 * @return String jexId <b>Obligatoire</b>
	 */
	@Field(domain = "DO_O_UUID", type = "ID", required = true, label = "Id")
	public String getJexId() {
		return jexId;
	}

	/**
	 * Champ : ID.
	 * Définit la valeur de la propriété 'Id'.
	 * @param jexId String <b>Obligatoire</b>
	 */
	public void setJexId(final String jexId) {
		this.jexId = jexId;
	}
	
	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Start exec'.
	 * @return java.time.Instant startExecInstant <b>Obligatoire</b>
	 */
	@Field(domain = "DO_O_TIMESTAMP", required = true, label = "Start exec")
	public java.time.Instant getStartExecInstant() {
		return startExecInstant;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Start exec'.
	 * @param startExecInstant java.time.Instant <b>Obligatoire</b>
	 */
	public void setStartExecInstant(final java.time.Instant startExecInstant) {
		this.startExecInstant = startExecInstant;
	}
	
	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Max execution (start + timeout)'.
	 * @return java.time.Instant maxExecInstant <b>Obligatoire</b>
	 */
	@Field(domain = "DO_O_TIMESTAMP", required = true, label = "Max execution (start + timeout)")
	public java.time.Instant getMaxExecInstant() {
		return maxExecInstant;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Max execution (start + timeout)'.
	 * @param maxExecInstant java.time.Instant <b>Obligatoire</b>
	 */
	public void setMaxExecInstant(final java.time.Instant maxExecInstant) {
		this.maxExecInstant = maxExecInstant;
	}
	
	/**
	 * Champ : FOREIGN_KEY.
	 * Récupère la valeur de la propriété 'JobRun'.
	 * @return String jobId <b>Obligatoire</b>
	 */
	@Field(domain = "DO_O_JOB_IDENTIFIANT", type = "FOREIGN_KEY", required = true, label = "JobRun")
	public String getJobId() {
		return (String)  jobIdAccessor.getId();
	}

	/**
	 * Champ : FOREIGN_KEY.
	 * Définit la valeur de la propriété 'JobRun'.
	 * @param jobId String <b>Obligatoire</b>
	 */
	public void setJobId(final String jobId) {
		jobIdAccessor.setId(jobId);
	}
	
	/**
	 * Champ : FOREIGN_KEY.
	 * Récupère la valeur de la propriété 'JobModel'.
	 * @return Long jmoId <b>Obligatoire</b>
	 */
	@Field(domain = "DO_O_IDENTIFIANT", type = "FOREIGN_KEY", required = true, label = "JobModel")
	public Long getJmoId() {
		return (Long)  jmoIdAccessor.getId();
	}

	/**
	 * Champ : FOREIGN_KEY.
	 * Définit la valeur de la propriété 'JobModel'.
	 * @param jmoId Long <b>Obligatoire</b>
	 */
	public void setJmoId(final Long jmoId) {
		jmoIdAccessor.setId(jmoId);
	}

 	/**
	 * Association : JobModel.
	 * @return l'accesseur vers la propriété 'JobModel'
	 */
	public VAccessor<io.vertigo.orchestra.domain.model.OJobModel> jobModel() {
		return jmoIdAccessor;
	}
	
	@Deprecated
	public io.vertigo.orchestra.domain.model.OJobModel getJobModel() {
		// we keep the lazyness
		if (!jmoIdAccessor.isLoaded()) {
			jmoIdAccessor.load();
		}
		return jmoIdAccessor.get();
	}

	/**
	 * Retourne l'URI: JobModel.
	 * @return URI de l'association
	 */
	@Deprecated
	public io.vertigo.dynamo.domain.model.URI<io.vertigo.orchestra.domain.model.OJobModel> getJobModelURI() {
		return jmoIdAccessor.getURI();
	}

 	/**
	 * Association : JobRun.
	 * @return l'accesseur vers la propriété 'JobRun'
	 */
	public VAccessor<io.vertigo.orchestra.domain.run.OJobRun> jobRun() {
		return jobIdAccessor;
	}
	
	@Deprecated
	public io.vertigo.orchestra.domain.run.OJobRun getJobRun() {
		// we keep the lazyness
		if (!jobIdAccessor.isLoaded()) {
			jobIdAccessor.load();
		}
		return jobIdAccessor.get();
	}

	/**
	 * Retourne l'URI: JobRun.
	 * @return URI de l'association
	 */
	@Deprecated
	public io.vertigo.dynamo.domain.model.URI<io.vertigo.orchestra.domain.run.OJobRun> getJobRunURI() {
		return jobIdAccessor.getURI();
	}
	
	/** {@inheritDoc} */
	@Override
	public String toString() {
		return DtObjectUtil.toString(this);
	}
}