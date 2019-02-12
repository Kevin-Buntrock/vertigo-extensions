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
package io.vertigo.geo.plugins.geocoder.ban;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import com.google.gson.Gson;

import io.vertigo.geo.impl.services.geocoder.GeoCoderPlugin;
import io.vertigo.geo.plugins.geocoder.ban.BanGeoCoderPlugin.BanResponse.GeoJsonFeature;
import io.vertigo.geo.services.geocoder.GeoLocation;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;

/**
 * @author spoitrenaud
 *
 */
public final class BanGeoCoderPlugin implements GeoCoderPlugin {
	// Début de la requête http
	private static final String GEOCODE_REQUEST_PREFIX = "https://api-adresse.data.gouv.fr/search/";
	// Expression XPath permettant de récupérer la latitude, la longitude et
	// l'adresse formatée de
	private final Proxy proxy;

	@Inject
	public BanGeoCoderPlugin(
			final @Named("proxyHost") Optional<String> proxyHost,
			final @Named("proxyPort") Optional<String> proxyPort) {
		Assertion.checkNotNull(proxyHost);
		Assertion.checkNotNull(proxyPort);
		Assertion.checkArgument((proxyHost.isPresent() && proxyPort.isPresent()) || (!proxyHost.isPresent() && !proxyPort.isPresent()), "les deux paramètres host et port doivent être tous les deux remplis ou vides");
		//-----
		if (proxyHost.isPresent()) {
			proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost.get(), Integer.parseInt(proxyPort.get())));
		} else {
			proxy = Proxy.NO_PROXY;
		}
	}

	/**
	 * Méthode de connexion au service Google.
	 *
	 * @param address Chaîne de caractères contenant l'adresse à geocoder
	 * @return Document
	 */
	private BanResponse geoCode(final String address) {
		Assertion.checkNotNull(address);
		//-----
		final String urlString;
		try {
			urlString = GEOCODE_REQUEST_PREFIX + "?q=" + URLEncoder.encode(address, StandardCharsets.UTF_8.name()) + "&limit=1";
		} catch (final UnsupportedEncodingException e) {
			throw new RuntimeException("Erreur lors de l'encodage de l'adresse", e);
		}

		final URL url;
		try {
			url = new URL(urlString);
		} catch (final MalformedURLException e) {
			throw new RuntimeException("Erreur lors de la creation de l'URL", e);
		}

		//-----
		Assertion.checkNotNull(url);
		try {
			final HttpURLConnection connection = (HttpURLConnection) url.openConnection(proxy);
			connection.setConnectTimeout(500); //500 ms timeout
			connection.setDoOutput(true);
			// Connexion et récupération des résultats
			connection.connect();

			final Gson gson = new Gson();
			try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"))) {
				final BanResponse response = gson.fromJson(bufferedReader, BanResponse.class);
				return response;
			}
		} catch (final IOException e) {
			throw WrappedException.wrap(e);
		}
	}

	public static String toString(final Document doc) {
		try {
			final StringWriter sw = new StringWriter();
			final TransformerFactory tf = TransformerFactory.newInstance();
			final Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

			transformer.transform(new DOMSource(doc), new StreamResult(sw));
			return sw.toString();
		} catch (final Exception ex) {
			throw new RuntimeException("Error converting to String", ex);
		}
	}

	/** {@inheritDoc} */
	@Override
	public GeoLocation findLocation(final String address) {
		Assertion.checkNotNull(address);
		//-----
		final BanResponse banResponse = geoCode(address);
		if (banResponse == null) {
			throw new RuntimeException("Pas de réponse du service");
		}
		//-----
		// 0- Vérification du status
		Assertion.checkState(banResponse.features.size() <= 1, "Only one address when looking for a single location");
		if (banResponse.features.size() == 0) {
			return GeoLocation.UNDEFINED;
		}

		final GeoJsonFeature geoJsonFeature = banResponse.features.get(0);

		final Double longitude = geoJsonFeature.geometry.coordinates[0];
		final Double latitude = geoJsonFeature.geometry.coordinates[1];
		//-----
		final String countryCode = "FR";

		final String[] contextLevels = geoJsonFeature.properties.context.split(", ");

		final String level1 = contextLevels[contextLevels.length - 1];
		final String level2 = contextLevels[contextLevels.length - 2];
		final String locality = geoJsonFeature.properties.city;

		return new GeoLocation(latitude, longitude, countryCode, level1, level2, locality);
	}

	protected static class BanResponse {
		public String attribution;
		public String licence;
		public Integer limit;
		public String query;
		public String type;
		public String version;

		public List<GeoJsonFeature> features;

		protected static class GeoJsonFeature {
			public GeoJsonGeometry geometry;
			public GeoJsonProperties properties;

			protected static class GeoJsonGeometry {
				public String type;
				public Double[] coordinates;
			}

			protected static class GeoJsonProperties {
				public Integer adm_weight;
				public String city;
				public String citycode;
				public String context;
				public String id;
				public Double importance;
				public String label;
				public String name;
				public String Double;
				public String postcode;
				public Double score;
				public String type;
				public Double x;
				public Double y;
			}

		}
	}
}
