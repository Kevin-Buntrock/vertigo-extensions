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
package io.vertigo.quarto.services.converter;

import io.vertigo.core.component.Manager;
import io.vertigo.dynamo.file.model.VFile;

/**
 * Gestionnaire centralisé des conversions de documents.
 *
 * Exemple :
 *  - doc-->pdf
 *  - odt-->doc
 *
 * @author pchretien, npiedeloup
 */
public interface ConverterManager extends Manager {
	/**
	 * Conversion d'un document à un format cible.
	 *
	 * @param inputFile Document source à convertir
	 * @param format Format du document à cible
	 * @return Document converti au format passé en paramètre.
	 */
	VFile convert(VFile inputFile, String format);
}
