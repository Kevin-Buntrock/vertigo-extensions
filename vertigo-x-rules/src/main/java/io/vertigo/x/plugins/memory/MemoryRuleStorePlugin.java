/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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

package io.vertigo.x.plugins.memory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import io.vertigo.lang.Assertion;
import io.vertigo.x.impl.rules.RuleConditionDefinition;
import io.vertigo.x.impl.rules.RuleDefinition;
import io.vertigo.x.impl.rules.RuleFilterDefinition;
import io.vertigo.x.impl.rules.RuleStorePlugin;
import io.vertigo.x.impl.rules.SelectorDefinition;

/**
 *
 * @author xdurand
 *
 */
public final class MemoryRuleStorePlugin implements RuleStorePlugin {

	private final Map<Long, RuleDefinition> inMemoryRuleStore = new ConcurrentHashMap<>();
	private final AtomicLong memoryRuleSequenceGenerator = new AtomicLong(0);

	private final Map<Long, RuleConditionDefinition> inMemoryConditionStore = new ConcurrentHashMap<>();
	private final AtomicLong memoryConditionSequenceGenerator = new AtomicLong(0);

	private final Map<Long, SelectorDefinition> inMemorySelectorStore = new ConcurrentHashMap<>();
	private final AtomicLong memorySelectorSequenceGenerator = new AtomicLong(0);

	private final Map<Long, RuleFilterDefinition> inMemoryFilterStore = new ConcurrentHashMap<>();
	private final AtomicLong memoryFilterSequenceGenerator = new AtomicLong(0);

	/**
	 *
	 * @param ruleDefinition
	 */
	@Override
	public void addRule(final RuleDefinition ruleDefinition) {
		Assertion.checkNotNull(ruleDefinition);
		Assertion.checkNotNull(ruleDefinition.getItemId());
		Assertion.checkState(ruleDefinition.getId() == null, "A new rule must not have an id");
		//---
		final Long generatedId = memoryRuleSequenceGenerator.addAndGet(1);
		ruleDefinition.setId(generatedId);
		inMemoryRuleStore.put(generatedId, ruleDefinition);
	}

	/**
	 *
	 * @param ruleDefinition
	 */
	@Override
	public void removeRule(final RuleDefinition ruleDefinition) {
		Assertion.checkNotNull(ruleDefinition);
		Assertion.checkNotNull(ruleDefinition.getId());
		//---
		inMemoryRuleStore.remove(ruleDefinition.getId());
	}

	/**
	 *
	 * @param ruleDefinition
	 */
	@Override
	public void updateRule(final RuleDefinition ruleDefinition) {
		Assertion.checkNotNull(ruleDefinition);
		Assertion.checkNotNull(ruleDefinition.getId());
		Assertion.checkState(inMemoryRuleStore.containsKey(ruleDefinition.getId()), "Cannot update this rule : Its id is unknown in the store");
		//---
		inMemoryRuleStore.put(ruleDefinition.getId(), ruleDefinition);
	}

	@Override
	public void addCondition(final RuleConditionDefinition ruleConditionDefinition) {
		Assertion.checkNotNull(ruleConditionDefinition);
		Assertion.checkState(ruleConditionDefinition.getId() == null, "A new condition must not have an id");
		//---
		final Long generatedId = memoryConditionSequenceGenerator.addAndGet(1);
		ruleConditionDefinition.setId(generatedId);
		inMemoryConditionStore.put(generatedId, ruleConditionDefinition);
	}

	@Override
	public void removeCondition(final RuleConditionDefinition ruleConditionDefinition) {
		Assertion.checkNotNull(ruleConditionDefinition);
		Assertion.checkNotNull(ruleConditionDefinition.getId());
		//---
		inMemoryConditionStore.remove(ruleConditionDefinition.getId());
	}

	@Override
	public void updateCondition(final RuleConditionDefinition ruleConditionDefinition) {
		Assertion.checkNotNull(ruleConditionDefinition);
		Assertion.checkNotNull(ruleConditionDefinition.getId());
		Assertion.checkState(inMemoryConditionStore.containsKey(ruleConditionDefinition.getId()), "Cannot update this condition : Its id is unknown in the store");
		//---
		inMemoryConditionStore.put(ruleConditionDefinition.getId(), ruleConditionDefinition);
	}

	@Override
	public List<RuleDefinition> findRulesByItemId(final Long itemId) {
		Assertion.checkNotNull(itemId);
		//---
		final List<RuleDefinition> ret = new ArrayList<>();

		for (final RuleDefinition ruleDefinition : inMemoryRuleStore.values()) {
			if (itemId.equals(ruleDefinition.getItemId())) {
				//Collect
				ret.add(ruleDefinition);
			}
		}

		return ret;
	}

	@Override
	public List<RuleConditionDefinition> findConditionByRuleId(final Long ruleId) {
		Assertion.checkNotNull(ruleId);
		//---
		final List<RuleConditionDefinition> ret = new ArrayList<>();

		for (final RuleConditionDefinition ruleDefinition : inMemoryConditionStore.values()) {
			if (ruleId.equals(ruleDefinition.getRudId())) {
				//Collect
				ret.add(ruleDefinition);
			}
		}

		return ret;
	}

	/**
	 *
	 * @param selectorDefinition
	 */
	@Override
	public void addSelector(final SelectorDefinition selectorDefinition) {
		Assertion.checkNotNull(selectorDefinition);
		Assertion.checkNotNull(selectorDefinition.getItemId());
		Assertion.checkState(selectorDefinition.getId() == null, "A new rule must not have an id");
		//---
		final Long generatedId = memorySelectorSequenceGenerator.addAndGet(1);
		selectorDefinition.setId(generatedId);
		inMemorySelectorStore.put(generatedId, selectorDefinition);
	}

	/**
	 *
	 * @param selectorDefinition
	 */
	@Override
	public void removeSelector(final SelectorDefinition selectorDefinition) {
		Assertion.checkNotNull(selectorDefinition);
		Assertion.checkNotNull(selectorDefinition.getId());
		//---
		inMemorySelectorStore.remove(selectorDefinition.getId());
	}

	/**
	 *
	 * @param selectorDefinition
	 */
	@Override
	public void updateSelector(final SelectorDefinition selectorDefinition) {
		Assertion.checkNotNull(selectorDefinition);
		Assertion.checkNotNull(selectorDefinition.getId());
		Assertion.checkState(inMemorySelectorStore.containsKey(selectorDefinition.getId()), "Cannot update this selector : Its id is unknown in the store");
		//---
		inMemorySelectorStore.put(selectorDefinition.getId(), selectorDefinition);
	}

	@Override
	public List<SelectorDefinition> findSelectorsByItemId(final Long itemId) {
		Assertion.checkNotNull(itemId);
		//---
		final List<SelectorDefinition> ret = new ArrayList<>();

		for (final SelectorDefinition selectorDefinition : inMemorySelectorStore.values()) {
			if (itemId.equals(selectorDefinition.getItemId())) {
				//Collect
				ret.add(selectorDefinition);
			}
		}

		return ret;
	}

	@Override
	public void addFilter(final RuleFilterDefinition ruleFilterDefinition) {
		Assertion.checkNotNull(ruleFilterDefinition);
		Assertion.checkNotNull(ruleFilterDefinition.getSelId());
		Assertion.checkState(ruleFilterDefinition.getId() == null, "A new filter must not have an id");
		//---
		final Long generatedId = memoryFilterSequenceGenerator.addAndGet(1);
		ruleFilterDefinition.setId(generatedId);
		inMemoryFilterStore.put(generatedId, ruleFilterDefinition);

	}

	@Override
	public void removeFilter(final RuleFilterDefinition ruleFilterDefinition) {
		Assertion.checkNotNull(ruleFilterDefinition);
		Assertion.checkNotNull(ruleFilterDefinition.getId());
		//---
		inMemoryFilterStore.remove(ruleFilterDefinition.getId());

	}

	@Override
	public List<RuleFilterDefinition> findFiltersBySelectorId(final Long selectorId) {
		Assertion.checkNotNull(selectorId);
		//---
		final List<RuleFilterDefinition> ret = new ArrayList<>();

		for (final RuleFilterDefinition ruleFilterDefinition : inMemoryFilterStore.values()) {
			if (selectorId.equals(ruleFilterDefinition.getSelId())) {
				//Collect
				ret.add(ruleFilterDefinition);
			}
		}

		return ret;
	}

	@Override
	public void updateFilter(final RuleFilterDefinition ruleFilterDefinition) {
		Assertion.checkNotNull(ruleFilterDefinition);
		Assertion.checkNotNull(ruleFilterDefinition.getId());
		Assertion.checkState(inMemoryFilterStore.containsKey(ruleFilterDefinition.getId()), "Cannot update this filter : Its id is unknown in the store");
		//---
		inMemoryFilterStore.put(ruleFilterDefinition.getId(), ruleFilterDefinition);
	}

}
