/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2018, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigo.rules.domain;

import io.vertigo.dynamo.domain.model.Entity;
import io.vertigo.dynamo.domain.model.UID;
import io.vertigo.dynamo.domain.model.VAccessor;
import io.vertigo.dynamo.domain.stereotype.Field;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.lang.Generated;

/**
 * This class is automatically generated.
 * DO NOT EDIT THIS FILE DIRECTLY.
 */
@Generated
public final class RuleFilterDefinition implements Entity {
	private static final long serialVersionUID = 1L;

	private Long id;
	private String field;
	private String operator;
	private String expression;

	@io.vertigo.dynamo.domain.stereotype.Association(
			name = "A_SEL_FID",
			fkFieldName = "SEL_ID",
			primaryDtDefinitionName = "DT_SELECTOR_DEFINITION",
			primaryIsNavigable = false,
			primaryRole = "SelectorDefinition",
			primaryLabel = "SelectorDefinition",
			primaryMultiplicity = "0..1",
			foreignDtDefinitionName = "DT_RULE_FILTER_DEFINITION",
			foreignIsNavigable = true,
			foreignRole = "RuleFilterDefinition",
			foreignLabel = "RuleFilterDefinition",
			foreignMultiplicity = "0..*")
	private final VAccessor<io.vertigo.rules.domain.SelectorDefinition> selIdAccessor = new VAccessor<>(io.vertigo.rules.domain.SelectorDefinition.class, "SelectorDefinition");

	/** {@inheritDoc} */
	@Override
	public UID<RuleFilterDefinition> getUID() {
		return UID.of(this);
	}

	/**
	 * Champ : ID.
	 * Récupère la valeur de la propriété 'id'.
	 * @return Long id <b>Obligatoire</b>
	 */
	@Field(domain = "DO_RULES_ID", type = "ID", required = true, label = "id")
	public Long getId() {
		return id;
	}

	/**
	 * Champ : ID.
	 * Définit la valeur de la propriété 'id'.
	 * @param id Long <b>Obligatoire</b>
	 */
	public void setId(final Long id) {
		this.id = id;
	}

	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'field'.
	 * @return String field
	 */
	@Field(domain = "DO_RULES_FIELD", label = "field")
	public String getField() {
		return field;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'field'.
	 * @param field String
	 */
	public void setField(final String field) {
		this.field = field;
	}

	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'operator'.
	 * @return String operator
	 */
	@Field(domain = "DO_RULES_OPERATOR", label = "operator")
	public String getOperator() {
		return operator;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'operator'.
	 * @param operator String
	 */
	public void setOperator(final String operator) {
		this.operator = operator;
	}

	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'expression'.
	 * @return String expression
	 */
	@Field(domain = "DO_RULES_EXPRESSION", label = "expression")
	public String getExpression() {
		return expression;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'expression'.
	 * @param expression String
	 */
	public void setExpression(final String expression) {
		this.expression = expression;
	}

	/**
	 * Champ : FOREIGN_KEY.
	 * Récupère la valeur de la propriété 'SelectorDefinition'.
	 * @return Long selId
	 */
	@Field(domain = "DO_RULES_ID", type = "FOREIGN_KEY", label = "SelectorDefinition")
	public Long getSelId() {
		return (Long) selIdAccessor.getId();
	}

	/**
	 * Champ : FOREIGN_KEY.
	 * Définit la valeur de la propriété 'SelectorDefinition'.
	 * @param selId Long
	 */
	public void setSelId(final Long selId) {
		selIdAccessor.setId(selId);
	}

	/**
	 * Association : SelectorDefinition.
	 * @return l'accesseur vers la propriété 'SelectorDefinition'
	 */
	public VAccessor<io.vertigo.rules.domain.SelectorDefinition> selectorDefinition() {
		return selIdAccessor;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return DtObjectUtil.toString(this);
	}
}
