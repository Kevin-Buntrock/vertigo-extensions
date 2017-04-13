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
package io.vertigo.x.notification;

import io.vertigo.app.config.AppConfig;
import io.vertigo.app.config.AppConfigBuilder;
import io.vertigo.commons.impl.CommonsFeatures;
import io.vertigo.core.plugins.resource.classpath.ClassPathResourceResolverPlugin;
import io.vertigo.dynamo.impl.DynamoFeatures;
import io.vertigo.dynamo.plugins.environment.loaders.java.AnnotationLoaderPlugin;
import io.vertigo.dynamo.plugins.environment.registries.domain.DomainDynamicRegistryPlugin;
import io.vertigo.persona.impl.security.PersonaFeatures;
import io.vertigo.vega.VegaFeatures;
import io.vertigo.x.connectors.ConnectorsFeatures;
import io.vertigo.x.impl.account.AccountFeatures;
import io.vertigo.x.impl.notification.NotificationFeatures;
import io.vertigo.x.notification.data.TestUserSession;
import io.vertigo.x.plugins.account.memory.MemoryAccountStorePlugin;
import io.vertigo.x.plugins.notification.memory.MemoryNotificationPlugin;
import io.vertigo.x.webapi.notification.NotificationWebServices;

public final class MyAppConfig {
	public static final int WS_PORT = 8088;

	private static AppConfigBuilder createAppConfigBuilder(final boolean redis) {
		final String redisHost = "redis-pic.part.klee.lan.net";
		final int redisPort = 6379;
		final int redisDatabase = 15;

		// @formatter:off
		final AppConfigBuilder appConfigBuilder =  new AppConfigBuilder()
			.beginBootModule("fr")
				.beginPlugin( ClassPathResourceResolverPlugin.class).endPlugin()
				.beginPlugin(AnnotationLoaderPlugin.class).endPlugin()
				.beginPlugin(DomainDynamicRegistryPlugin.class).endPlugin()
			.endModule()
			.beginBoot()
				.silently()
			.endBoot()
			.beginModule(PersonaFeatures.class).withUserSession(TestUserSession.class).endModule()
			.beginModule(CommonsFeatures.class).endModule()
			.beginModule(DynamoFeatures.class).endModule();
		if (redis){
			return  appConfigBuilder
			.beginModule(ConnectorsFeatures.class).withRedis(redisHost, redisPort, redisDatabase).endModule()
			.beginModule(AccountFeatures.class).withRedis().endModule()
			.beginModule(NotificationFeatures.class).withRedis().endModule();
		}
		//else we use memory
		return  appConfigBuilder
				.beginModule(AccountFeatures.class).getModuleConfigBuilder().addPlugin(MemoryAccountStorePlugin.class).endModule()
				.beginModule(NotificationFeatures.class).getModuleConfigBuilder().addPlugin(MemoryNotificationPlugin.class).endModule();
		// @formatter:on
	}

	public static AppConfig config(final boolean redis) {
		// @formatter:off
		return createAppConfigBuilder(redis).build();
	}

	public static AppConfig vegaConfig() {
		// @formatter:off
		return createAppConfigBuilder(true)
			.beginModule(VegaFeatures.class)
				.withSecurity()
				.withEmbeddedServer(WS_PORT).endModule()
			.beginModule("ws-comment").withNoAPI()
				.addComponent(NotificationWebServices.class)
				.addComponent(TestLoginWebServices.class)
			.endModule()
			.build();
		// @formatter:on
	}
}
