package io.vertigo.workflow.domain.model;

import io.vertigo.dynamo.domain.model.DtStaticMasterData;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.domain.stereotype.Field;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.lang.Generated;

/**
 * This class is automatically generated.
 * DO NOT EDIT THIS FILE DIRECTLY.
 */
@Generated
public final class WfMultiplicityDefinition implements DtStaticMasterData {
	private static final long serialVersionUID = 1L;

	private String wfmdCode;
	private String label;

	/** {@inheritDoc} */
	@Override
	public URI<WfMultiplicityDefinition> getURI() {
		return DtObjectUtil.createURI(this);
	}
	
	/**
	 * Champ : ID.
	 * Récupère la valeur de la propriété 'Multiplicity code'.
	 * @return String wfmdCode <b>Obligatoire</b>
	 */
	@Field(domain = "DO_WF_CODE", type = "ID", required = true, label = "Multiplicity code")
	public String getWfmdCode() {
		return wfmdCode;
	}

	/**
	 * Champ : ID.
	 * Définit la valeur de la propriété 'Multiplicity code'.
	 * @param wfmdCode String <b>Obligatoire</b>
	 */
	public void setWfmdCode(final String wfmdCode) {
		this.wfmdCode = wfmdCode;
	}
	
	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Label'.
	 * @return String label
	 */
	@Field(domain = "DO_WF_LABEL", label = "Label")
	public String getLabel() {
		return label;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Label'.
	 * @param label String
	 */
	public void setLabel(final String label) {
		this.label = label;
	}
	
	/** {@inheritDoc} */
	@Override
	public String toString() {
		return DtObjectUtil.toString(this);
	}
}
