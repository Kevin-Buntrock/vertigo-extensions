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
package io.vertigo.dashboard.ui.commons.model;

import io.vertigo.commons.cache.CacheDefinition;
import io.vertigo.lang.Assertion;

public class CacheModel {
	private final CacheDefinition cacheDefinition;

	public CacheModel(final CacheDefinition cacheDefinition) {
		Assertion.checkNotNull(cacheDefinition);
		//---
		this.cacheDefinition = cacheDefinition;
	}

	public String getName() {
		return cacheDefinition.getName();
	}

	public int getTtlIdle() {
		return cacheDefinition.getTimeToIdleSeconds();
	}

	public int getTtl() {
		return cacheDefinition.getTimeToLiveSeconds();
	}

	public int getMaxElements() {
		return cacheDefinition.getMaxElementsInMemory();
	}

}