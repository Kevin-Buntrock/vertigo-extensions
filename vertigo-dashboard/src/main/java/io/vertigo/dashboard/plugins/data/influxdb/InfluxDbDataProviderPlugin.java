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
package io.vertigo.dashboard.plugins.data.influxdb;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Named;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.dto.QueryResult.Series;

import io.vertigo.dashboard.impl.services.data.DataProviderPlugin;
import io.vertigo.dashboard.services.data.ClusteredMeasure;
import io.vertigo.dashboard.services.data.DataFilter;
import io.vertigo.dashboard.services.data.TimeFilter;
import io.vertigo.dashboard.services.data.TimedDataSerie;
import io.vertigo.dashboard.services.data.TimedDatas;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Tuples;
import io.vertigo.lang.Tuples.Tuple2;

public final class InfluxDbDataProviderPlugin implements DataProviderPlugin {

	private final InfluxDB influxDB;

	@Inject
	public InfluxDbDataProviderPlugin(
			@Named("host") final String host,
			@Named("user") final String user,
			@Named("password") final String password) {
		Assertion.checkArgNotEmpty(host);
		Assertion.checkArgNotEmpty(user);
		Assertion.checkArgNotEmpty(password);
		//---
		influxDB = InfluxDBFactory.connect(host, user, password);

	}

	@Override
	public TimedDatas getTimeSeries(final String appName, final List<String> measures, final DataFilter dataFilter, final TimeFilter timeFilter) {
		Assertion.checkNotNull(measures);
		Assertion.checkNotNull(dataFilter);
		Assertion.checkNotNull(timeFilter.getDim());// we check dim is not null because we need it
		//---
		final String q = buildQuery(measures, dataFilter, timeFilter)
				.append(" group by time(").append(timeFilter.getDim()).append(')')
				.toString();

		return executeTimedQuery(appName, q);

	}

	@Override
	public TimedDatas getClusteredTimeSeries(final String appName, final ClusteredMeasure clusteredMeasure, final DataFilter dataFilter, final TimeFilter timeFilter) {
		Assertion.checkNotNull(dataFilter);
		Assertion.checkNotNull(timeFilter);
		Assertion.checkNotNull(timeFilter.getDim()); // we check dim is not null because we need it
		Assertion.checkNotNull(clusteredMeasure);
		//---
		Assertion.checkArgNotEmpty(clusteredMeasure.getMeasure());
		Assertion.checkNotNull(clusteredMeasure.getThresholds());
		Assertion.checkState(!clusteredMeasure.getThresholds().isEmpty(), "For clustering the measure '{0}' you need to provide at least one threshold", clusteredMeasure.getMeasure());
		//we use the natural order
		clusteredMeasure.getThresholds().sort(Comparator.naturalOrder());
		//---
		final String fieldName = clusteredMeasure.getMeasure().split(":")[0];
		final String standardwhereClause = buildWhereClause(dataFilter, timeFilter);// the where clause is almost the same for each cluster
		final StringBuilder selectClause = new StringBuilder();
		final StringBuilder fromClause = new StringBuilder();
		Integer minThreshold = null;

		// for each cluster defined by the thresholds we add a subquery (after benchmark it's the fastest solution)
		for (int i = 0; i <= clusteredMeasure.getThresholds().size(); i++) {
			Integer maxThreshold = null;
			if (i < clusteredMeasure.getThresholds().size()) {
				maxThreshold = clusteredMeasure.getThresholds().get(i);
			}
			// we add the where clause of the cluster value > threshold_1 and value <= threshold_2
			appendMeasureThreshold(
					minThreshold,
					maxThreshold,
					fieldName,
					clusteredMeasure.getMeasure(),
					dataFilter.getMeasurement(),
					standardwhereClause,
					timeFilter.getDim(),
					fromClause,
					i);

			// we construct the top select clause. we use the max as the aggregate function. No conflict possible
			selectClause.append(" max(\"").append(fieldName).append('_').append(i)
					.append("\") as \"").append(clusterName(minThreshold, maxThreshold, clusteredMeasure.getMeasure())).append('"');
			if (i < clusteredMeasure.getThresholds().size()) {
				selectClause.append(',');
				fromClause.append(", ");
			}

			minThreshold = maxThreshold;
		}

		// the global query
		final StringBuilder request = new StringBuilder()
				.append("select ").append(selectClause)
				.append(" from ").append(fromClause)
				.append(" where time > ").append(timeFilter.getFrom()).append(" and time <").append(timeFilter.getTo())
				.append(" group by time(").append(timeFilter.getDim()).append(')');

		return executeTimedQuery(appName, request.toString());
	}

	private static String clusterName(
			final Integer minThreshold,
			final Integer maxThreshold,
			final String measure) {
		if (minThreshold == null) {
			return measure + '<' + maxThreshold;
		} else if (maxThreshold == null) {
			return measure + '>' + minThreshold;
		} else {
			return measure + '_' + maxThreshold;
		}
	}

	private static void appendMeasureThreshold(
			final Integer previousThreshold,
			final Integer currentThreshold,
			final String clusteredField,
			final String clusteredMeasure,
			final String measurement,
			final String standardwhereClause,
			final String timeDimension,
			final StringBuilder fromClauseBuilder,
			final int i) {
		fromClauseBuilder.append("(select ")
				.append(buildMeasureQuery(clusteredMeasure, clusteredField + "_" + i))
				.append(" from ").append(measurement)
				.append(standardwhereClause);
		if (previousThreshold != null) {
			fromClauseBuilder.append(" and \"").append(clusteredField).append('"').append(" > ").append(previousThreshold);
		}
		if (currentThreshold != null) {
			fromClauseBuilder.append(" and \"").append(clusteredField).append('"').append(" <= ").append(currentThreshold);
		}
		fromClauseBuilder.append(" group by time(").append(timeDimension).append(')');
		fromClauseBuilder.append(')');
	}

	private TimedDatas executeTimedQuery(final String appName, final String q) {
		final Query query = new Query(q, appName);
		final QueryResult queryResult = influxDB.query(query);

		final List<Series> seriesList = queryResult.getResults().get(0).getSeries();
		if (seriesList != null && !seriesList.isEmpty()) {

			final Series series = seriesList.get(0);
			final List<TimedDataSerie> dataSeries = series
					.getValues()
					.stream()
					.map(values -> new TimedDataSerie(LocalDateTime.parse(values.get(0).toString(), DateTimeFormatter.ISO_OFFSET_DATE_TIME).toEpochSecond(ZoneOffset.UTC), buildMapValue(series.getColumns(), values)))
					.collect(Collectors.toList());
			return new TimedDatas(dataSeries, series.getColumns().subList(1, series.getColumns().size()));//we remove the first one
		}
		return new TimedDatas(Collections.emptyList(), Collections.emptyList());
	}

	@Override
	public TimedDatas getTabularData(final String appName, final List<String> measures, final DataFilter dataFilter, final TimeFilter timeFilter, final boolean keepTime, final String... groupBy) {
		final StringBuilder queryBuilder = buildQuery(measures, dataFilter, timeFilter);

		final String groupByClause = Stream.of(groupBy)
				.collect(Collectors.joining("\", \"", "\"", "\""));

		queryBuilder.append(" group by ").append(groupByClause);
		final String queryString = queryBuilder.toString();

		return executeTabularQuery(appName, queryString, keepTime);
	}

	private TimedDatas executeTabularQuery(final String appName, final String queryString, final boolean keepTime) {
		final Query query = new Query(queryString, appName);
		final QueryResult queryResult = influxDB.query(query);

		final List<Series> series = queryResult.getResults().get(0).getSeries();

		if (series != null && !series.isEmpty()) {
			//all columns are the measures
			final List<String> seriesName = new ArrayList<>();
			seriesName.addAll(series.get(0).getColumns().subList(1, series.get(0).getColumns().size()));//we remove the first one
			seriesName.addAll(series.get(0).getTags().keySet());// + all the tags names (the group by)

			final List<TimedDataSerie> dataSeries = series
					.stream()
					.map(mySeries -> {
						final Map<String, Object> mapValues = buildMapValue(mySeries.getColumns(), mySeries.getValues().get(0));
						mapValues.putAll(mySeries.getTags());
						return new TimedDataSerie(keepTime ? LocalDateTime.parse(mySeries.getValues().get(0).get(0).toString(), DateTimeFormatter.ISO_OFFSET_DATE_TIME).toEpochSecond(ZoneOffset.UTC) : null, mapValues);
					})
					.collect(Collectors.toList());

			return new TimedDatas(dataSeries, seriesName);
		}
		return new TimedDatas(Collections.emptyList(), Collections.emptyList());
	}

	@Override
	public TimedDatas getTops(final String appName, final String measure, final DataFilter dataFilter, final TimeFilter timeFilter, final String groupBy, final int maxRows) {
		final StringBuilder queryBuilder = new StringBuilder();

		final String queryString = queryBuilder
				.append("select top(").append("\"top_").append(measure).append("\", \"").append(groupBy).append("\", ").append(maxRows).append(") as \"").append(measure).append('"')
				.append(" from ( select ").append(buildMeasureQuery(measure, "top_" + measure))
				.append(" from ").append(dataFilter.getMeasurement())
				.append(buildWhereClause(dataFilter, timeFilter))
				.append(" group by \"").append(groupBy).append('"')
				.append(')')
				.toString();

		return executeTimedQuery(appName, queryString);
	}

	private static StringBuilder buildQuery(final List<String> measures, final DataFilter dataFilter, final TimeFilter timeFilter) {
		Assertion.checkNotNull(measures);
		//---
		final StringBuilder queryBuilder = new StringBuilder("select ");
		String separator = "";
		for (final String measure : measures) {
			queryBuilder
					.append(separator)
					.append(buildMeasureQuery(measure, measure));
			separator = " ,";
		}
		queryBuilder.append(" from ").append(dataFilter.getMeasurement());
		queryBuilder.append(buildWhereClause(dataFilter, timeFilter));
		return queryBuilder;
	}

	private static String buildWhereClause(final DataFilter dataFilter, final TimeFilter timeFilter) {
		final StringBuilder queryBuilder = new StringBuilder()
				.append(" where time > ").append(timeFilter.getFrom()).append(" and time <").append(timeFilter.getTo());
		if (!"*".equals(dataFilter.getName())) {
			queryBuilder.append(" and \"name\"='").append(dataFilter.getName()).append('\'');
		}
		if (!"*".equals(dataFilter.getLocation())) {
			queryBuilder.append(" and \"location\"='").append(dataFilter.getLocation()).append('\'');
		}
		if (!"*".equals(dataFilter.getModule())) {
			queryBuilder.append(" and \"module\"='").append(dataFilter.getModule()).append('\'');
		}
		if (!"*".equals(dataFilter.getFeature())) {
			queryBuilder.append(" and \"feature\"='").append(dataFilter.getFeature()).append('\'');
		}
		if (dataFilter.getAdditionalWhereClause() != null) {
			queryBuilder.append(" and ").append(dataFilter.getAdditionalWhereClause());
		}
		return queryBuilder.toString();
	}

	private static String buildMeasureQuery(final String measure, final String alias) {
		Assertion.checkArgNotEmpty(measure);
		Assertion.checkArgNotEmpty(alias);
		//----
		final String[] measureDetails = measure.split(":");
		final Tuple2<String, List<String>> aggregateFunction = parseAggregateFunction(measureDetails[1]);
		// append function name
		final StringBuilder measureQueryBuilder = new java.lang.StringBuilder(aggregateFunction.getVal1()).append("(\"").append(measureDetails[0]).append("\"");
		// append parameters
		if (!aggregateFunction.getVal2().isEmpty()) {
			measureQueryBuilder.append(aggregateFunction.getVal2()
					.stream()
					.collect(Collectors.joining(",", ", ", "")));
		}
		// end measure and add alias
		measureQueryBuilder.append(") as \"").append(alias).append('"');
		return measureQueryBuilder.toString();
	}

	private static Tuple2<String, List<String>> parseAggregateFunction(final String aggregateFunction) {
		final int firstSeparatorIndex = aggregateFunction.indexOf('_');
		if (firstSeparatorIndex > -1) {
			return Tuples.of(
					aggregateFunction.substring(0, firstSeparatorIndex),
					Arrays.asList(aggregateFunction.substring(firstSeparatorIndex + 1).split("_")));
		}
		return Tuples.of(aggregateFunction, Collections.emptyList());

	}

	private static Map<String, Object> buildMapValue(final List<String> columns, final List<Object> values) {
		final Map<String, Object> valueMap = new HashMap<>();
		// we start at 1 because time is always the first row
		for (int i = 1; i < columns.size(); i++) {
			valueMap.put(columns.get(i), values.get(i));
		}
		return valueMap;
	}

	@Override
	public List<String> getTagValues(final String appName, final String measurement, final String tag) {
		final String queryString = new StringBuilder("show tag values on ")
				.append("\"").append(appName).append("\"")
				.append(" from ").append("\"").append(measurement).append("\"")
				.append("  with key= ").append("\"").append(tag).append("\"")
				.toString();

		final Query query = new Query(queryString, appName);
		final QueryResult queryResult = influxDB.query(query);

		final List<Series> seriesList = queryResult.getResults().get(0).getSeries();
		if (seriesList != null && !seriesList.isEmpty()) {
			final Series series = seriesList.get(0);
			return series
					.getValues()
					.stream()
					.map(values -> values.get(1).toString()) //always the second columns
					.collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

}
