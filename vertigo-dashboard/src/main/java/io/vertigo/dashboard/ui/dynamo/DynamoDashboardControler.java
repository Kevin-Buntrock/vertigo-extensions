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
package io.vertigo.dashboard.ui.dynamo;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import io.vertigo.app.App;
import io.vertigo.app.Home;
import io.vertigo.commons.analytics.metric.Metric;
import io.vertigo.dashboard.services.data.DataFilter;
import io.vertigo.dashboard.services.data.TimeFilter;
import io.vertigo.dashboard.services.data.TimedDataSerie;
import io.vertigo.dashboard.services.data.TimedDatas;
import io.vertigo.dashboard.ui.AbstractDashboardModuleControler;
import io.vertigo.dashboard.ui.dynamo.model.DomainModel;
import io.vertigo.dashboard.ui.dynamo.model.EntityModel;
import io.vertigo.dashboard.ui.dynamo.model.TaskModel;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtStereotype;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;

public final class DynamoDashboardControler extends AbstractDashboardModuleControler {

	@Override
	public void doBuildModel(final App app, final Map<String, Object> model) {
		final List<Metric> metrics = getDataProvider().getMetrics();
		buildEntityModel(app, model, metrics);
		buildDomainModel(app, model, metrics);
		buildTaskModel(app, model);
	}

	private void buildTaskModel(final App app, final Map<String, Object> model) {
		final DataFilter dataFilter = DataFilter.builder("tasks").build();
		final TimeFilter timeFilter = TimeFilter.builder("now() - 1d", "now()").build();
		final TimedDatas tabularDatas = getDataProvider().getTabularData(Arrays.asList("duration:median", "duration:count"), dataFilter, timeFilter, false, "name");

		final List<TaskModel> tasks = Home.getApp().getDefinitionSpace().getAll(TaskDefinition.class)
				.stream()
				.map(taskDefinition -> new TaskModel(
						taskDefinition,
						getValue(tabularDatas, "/execute/" + taskDefinition.getName(), "duration:count"),
						getValue(tabularDatas, "/execute/" + taskDefinition.getName(), "duration:median")))
				.collect(Collectors.toList());

		model.put("tasks", tasks);

	}

	private static Double getValue(final TimedDatas tabularDatas, final String serieName, final String measureName) {
		final Optional<TimedDataSerie> timedDataSerieOpt = tabularDatas.getTimedDataSeries()
				.stream()
				.filter(timedDataSerie -> timedDataSerie.getValues().containsKey("name") && measureName.equals(timedDataSerie.getValues().get("name")))
				.findAny();

		if (timedDataSerieOpt.isPresent()) {
			final TimedDataSerie timedDataSerie = timedDataSerieOpt.get();
			if (timedDataSerie.getValues().containsKey(serieName)) {
				return (Double) timedDataSerie.getValues().get(serieName);
			}
		}
		return null;
	}

	private static void buildEntityModel(final App app, final Map<String, Object> model, final List<Metric> metrics) {
		final Map<String, Double> entityCounts = new HashMap<>();

		metrics
				.stream()
				.filter(metric -> "entityCount".equals(metric.getName()))
				.forEach(metric -> entityCounts.put(metric.getFeature(), metric.getValue()));

		final Map<String, Double> taskCounts = new HashMap<>();
		metrics
				.stream()
				.filter(metric -> "definitionUsageInDao".equals(metric.getName()))
				.forEach(metric -> taskCounts.put(metric.getFeature(), metric.getValue()));

		final Map<String, Double> fieldCount = new HashMap<>();
		metrics
				.stream()
				.filter(metric -> "definitionFieldCount".equals(metric.getName()))
				.forEach(metric -> fieldCount.put(metric.getFeature(), metric.getValue()));

		final Collection<DtDefinition> dtDefinitions = Home.getApp().getDefinitionSpace().getAll(DtDefinition.class);
		final List<EntityModel> entities = dtDefinitions
				.stream()
				.filter(DtDefinition::isPersistent)
				.map(dtDefinition -> new EntityModel(
						dtDefinition,
						entityCounts.get(dtDefinition.getName()),
						taskCounts.get(dtDefinition.getName()),
						fieldCount.get(dtDefinition.getName())))
				.collect(Collectors.toList());
		model.put("entities", entities);
		//---
		model.put("entityCount", entities.size());
		final long keyConceptCount = dtDefinitions.stream().filter(dtDefinition -> dtDefinition.getStereotype() == DtStereotype.KeyConcept).count();
		model.put("keyConceptCount", keyConceptCount);
	}

	private static void buildDomainModel(final App app, final Map<String, Object> model, final List<Metric> metrics) {
		final Map<String, Double> taskCount = new HashMap<>();

		metrics
				.stream()
				.filter(metric -> "domainUsageInTasks".equals(metric.getName()))
				.forEach(metric -> taskCount.put(metric.getFeature(), metric.getValue()));

		final Map<String, Double> dtDefinitionCount = new HashMap<>();
		metrics
				.stream()
				.filter(metric -> "domainUsageInDtDefinitions".equals(metric.getName()))
				.forEach(metric -> dtDefinitionCount.put(metric.getFeature(), metric.getValue()));

		final Collection<Domain> domains = Home.getApp().getDefinitionSpace().getAll(Domain.class);
		final List<DomainModel> domainModels = domains
				.stream()
				.filter(domain -> domain.getScope().isPrimitive()) // we display only primitives
				.map(domain -> new DomainModel(
						domain,
						taskCount.get(domain.getName()),
						dtDefinitionCount.get(domain.getName())))
				.collect(Collectors.toList());

		model.put("domains", domainModels);

	}

}
