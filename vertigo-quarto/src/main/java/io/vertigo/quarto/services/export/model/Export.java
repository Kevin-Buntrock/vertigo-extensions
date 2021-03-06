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
package io.vertigo.quarto.services.export.model;

import java.util.ArrayList;
import java.util.List;

import io.vertigo.lang.Assertion;

/**
 * Données à exporter.
 * @author pchretien
 */
public final class Export {
	/**
	 * Orientation des documents.
	 */
	public enum Orientation {
		/**Paysage.*/
		Landscape,
		/**Portait.*/
		Portait;
	}

	private final List<ExportSheet> sheets;

	private final ExportFormat format;
	private final String fileName;
	private final String title;
	private final String author;
	private final Export.Orientation orientation;

	/**
	 * Constructor.
	 * @param format output file type. This determine the best handler to use.
	 * @param fileName output file name.
	 * @param title Title
	 * @param author Author
	 * @param orientation Orientation
	 * @param sheets Sheets list
	 */
	Export(final ExportFormat format, final String fileName, final String title, final String author, final Export.Orientation orientation, final List<ExportSheet> sheets) {
		Assertion.checkNotNull(format);
		Assertion.checkArgNotEmpty(fileName, "a fileName is required");
		Assertion.checkNotNull(orientation);
		Assertion.checkNotNull(sheets);
		//-----
		this.format = format;
		this.fileName = fileName;
		this.title = title;
		this.orientation = orientation;
		this.author = author;
		this.sheets = new ArrayList<>(sheets);
	}

	/**
	 * @return format de sortie du document
	 */
	public ExportFormat getFormat() {
		return format;
	}

	/**
	 * @return Nom du fichier
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @return Titre du document (Facultatif)
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @return Auteur du document (Facultatif)
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 * @return Orientation du document (mode portrait si non renseigné)
	 */
	public Orientation getOrientation() {
		return orientation;
	}

	/**
	 * @return Liste des paramètres de données à exporter
	 */
	public List<ExportSheet> getSheets() {
		return sheets;
	}
}
