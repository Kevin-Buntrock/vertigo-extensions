/*
 * Copyright 2017, Danny Rottstegge
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.vertigo.ui.impl.thymeleaf.composite;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

public class ResourcePathFinder {

	private static final Logger LOG = LoggerFactory.getLogger(ResourcePathFinder.class);

	private final String directory;
	private final ClassLoader loader;

	/**
	 * Constructor
	 *
	 * @param directory
	 *            Base directory to search resource files (e.g.
	 *            templates/components)
	 */
	public ResourcePathFinder(final String directory) {
		this.directory = directory;
		loader = Thread.currentThread().getContextClassLoader();
	}

	/**
	 * Searches for resource files
	 *
	 * Search files recursively
	 *
	 * @return List of files as strings
	 */
	public List<String> findResourceFiles() {
		return getResourceFiles(directory);
	}

	private List<String> getResourceFiles(final String dir) {
		final List<String> files = new ArrayList<>();
		try {
			final ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(loader);
			final Resource[] resources = resolver.getResources("classpath*:/" + dir + "/**/*.*");
			final String basePath = loader.getResource(dir).getPath();
			for (final Resource resource : resources) {

				final String pathRelativeToDir = dir + "/" + resource.getURL()
						.getPath().replace(basePath + "/", "");
				files.add(pathRelativeToDir);
			}

		} catch (final IOException ex) {
			LOG.error("Could not process resource pattern. {}", ex);
		}

		return files;
	}
}