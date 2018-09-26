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

package io.vertigo.ui.impl.thymeleaf.composite.processor;

import static java.util.Collections.singleton;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeNames;
import org.thymeleaf.model.IAttribute;
import org.thymeleaf.model.IElementTag;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.model.ITemplateEvent;
import org.thymeleaf.processor.element.AbstractElementModelProcessor;
import org.thymeleaf.processor.element.IElementModelStructureHandler;
import org.thymeleaf.standard.StandardDialect;
import org.thymeleaf.templatemode.TemplateMode;

import io.vertigo.ui.impl.thymeleaf.composite.helper.FragmentHelper;
import io.vertigo.ui.impl.thymeleaf.composite.helper.WithHelper;

public class ComponentNamedElementProcessor
		extends AbstractElementModelProcessor {

	private static final String FRAGMENT_ATTRIBUTE = "fragment";
	private final String REPLACE_CONTENT_TAG;

	private static final int PRECEDENCE = 350;

	private final Set<String> excludeAttributes = singleton("params");
	private final String fragmentName;

	/**
	 * Constructor
	 *
	 * @param dialectPrefix
	 *            Dialect prefix (tc)
	 * @param tagName
	 *            Tag name to search for (e.g. panel)
	 * @param fragmentName
	 *            Fragment to search for
	 */
	public ComponentNamedElementProcessor(final String dialectPrefix,
			final String tagName, final String fragmentName) {
		super(TemplateMode.HTML, dialectPrefix, tagName, true, null, false,
				PRECEDENCE);
		REPLACE_CONTENT_TAG = dialectPrefix + ":content";
		this.fragmentName = fragmentName;
	}

	@Override
	protected void doProcess(final ITemplateContext context, final IModel model,
			final IElementModelStructureHandler structureHandler) {
		final IProcessableElementTag tag = processElementTag(context, model);
		final Map<String, String> attributes = processAttribute(tag);

		final String param = attributes.get("params");

		final IModel componentModel = model.cloneModel();
		componentModel.remove(0);

		if (componentModel.size() > 1) {
			componentModel.remove(componentModel.size() - 1);
		}

		final IModel fragmentModel = FragmentHelper.getFragmentModel(context,
				fragmentName + (param == null ? "" : "(" + param + ")"),
				structureHandler, StandardDialect.PREFIX, FRAGMENT_ATTRIBUTE);

		model.reset();

		final IModel replacedFragmentModel = replaceAllAttributeValues(attributes, context, fragmentModel);
		model.addModel(mergeModels(replacedFragmentModel, componentModel, REPLACE_CONTENT_TAG));

		processVariables(attributes, context, structureHandler, excludeAttributes);
	}

	private IProcessableElementTag processElementTag(final ITemplateContext context,
			final IModel model) {
		final ITemplateEvent firstEvent = model.get(0);
		for (final IProcessableElementTag tag : context.getElementStack()) {
			if (locationMatches(firstEvent, tag)) {
				return tag;
			}
		}
		return null;
	}

	private boolean locationMatches(final ITemplateEvent a, final ITemplateEvent b) {
		return Objects.equals(a.getTemplateName(), b.getTemplateName())
				&& Objects.equals(a.getLine(), b.getLine())
				&& Objects.equals(a.getCol(), b.getCol());
	}

	private void processVariables(final Map<String, String> attributes,
			final ITemplateContext context,
			final IElementModelStructureHandler structureHandler,
			final Set<String> excludeAttr) {
		for (final Map.Entry<String, String> entry : attributes.entrySet()) {
			if (excludeAttr.contains(entry.getKey()) || isDynamicAttribute(
					entry.getKey(), getDialectPrefix())) {
				continue;
			}
			String attributeValue = entry.getValue();
			if (attributeValue == null) {
				attributeValue = "${true}";
			}
			WithHelper.processWith(context, entry.getKey() + "=" + attributeValue,
					structureHandler);
		}
	}

	private Map<String, String> processAttribute(final IProcessableElementTag tag) {
		final Map<String, String> attributes = new HashMap<>();
		if (tag != null) {
			for (final IAttribute attribute : tag.getAllAttributes()) {
				final String completeName = attribute.getAttributeCompleteName();
				if (!isDynamicAttribute(completeName, StandardDialect.PREFIX)) {
					attributes.put(completeName, attribute.getValue());
				}
			}
		}

		return attributes;
	}

	private boolean isDynamicAttribute(final String attribute, final String prefix) {
		return attribute.startsWith(prefix + ":")
				|| attribute.startsWith("data-" + prefix + "-");
	}

	private IModel mergeModels(final IModel base, final IModel insert, final String replaceTag) {
		IModel mergedModel = insertModel(base, insert, replaceTag);
		mergedModel = removeTag(mergedModel, replaceTag);
		mergedModel = removeTag(mergedModel, replaceTag);
		return mergedModel;
	}

	private IModel insertModel(final IModel base, final IModel insert, final String replaceTag) {
		final IModel clonedModel = base.cloneModel();
		final int index = findTagIndex(base, replaceTag, IElementTag.class);
		if (index > -1) {
			clonedModel.insertModel(index, insert);
		}
		return clonedModel;
	}

	private IModel removeTag(final IModel model, final String tag) {
		final IModel clonedModel = model.cloneModel();
		final int index = findTagIndex(model, tag, IElementTag.class);
		if (index > -1) {
			clonedModel.remove(index);
		}
		return clonedModel;
	}

	private int findTagIndex(final IModel model, final String search, final Class<?> clazz) {
		final int size = model.size();
		ITemplateEvent event = null;
		for (int i = 0; i < size; i++) {
			event = model.get(i);
			if ((clazz == null || clazz.isInstance(event))
					&& event.toString().contains(search)) {
				return i;
			}
		}
		return -1;
	}

	private IModel replaceAllAttributeValues(final Map<String, String> attributes,
			final ITemplateContext context, final IModel model) {
		final Map<String, String> replaceAttributes = findAllAttributesStartsWith(
				attributes, super.getDialectPrefix(), "repl-", true);

		if (replaceAttributes.isEmpty()) {
			return model;
		}
		final IModel clonedModel = model.cloneModel();
		final int size = model.size();
		for (int i = 0; i < size; i++) {
			final ITemplateEvent replacedEvent = replaceAttributeValue(context,
					clonedModel.get(i), replaceAttributes);
			if (replacedEvent != null) {
				clonedModel.replace(i, replacedEvent);
			}

		}
		return clonedModel;
	}

	private ITemplateEvent replaceAttributeValue(final ITemplateContext context,
			final ITemplateEvent model, final Map<String, String> replaceValueMap) {
		IProcessableElementTag firstEvent = null;
		if (!replaceValueMap.isEmpty()
				&& model instanceof IProcessableElementTag) {
			final IModelFactory modelFactory = context.getModelFactory();

			firstEvent = (IProcessableElementTag) model;
			for (final Map.Entry<String, String> entry : firstEvent.getAttributeMap()
					.entrySet()) {
				final String oldAttrValue = entry.getValue();
				final String replacePart = getReplaceAttributePart(oldAttrValue);
				if (replacePart != null
						&& replaceValueMap.containsKey(replacePart)) {
					final String newStringValue = oldAttrValue.replace("?[" + replacePart + "]",
							replaceValueMap.get(replacePart));
					firstEvent = modelFactory.replaceAttribute(firstEvent,
							AttributeNames.forTextName(entry.getKey()),
							entry.getKey(), newStringValue);
				}
			}
		}
		return firstEvent;
	}

	private Map<String, String> findAllAttributesStartsWith(
			final Map<String, String> attributes, final String prefix,
			final String attributeName, final boolean removeStart) {
		final Map<String, String> matchingAttributes = new HashMap<>();
		for (final Map.Entry<String, String> entry : attributes.entrySet()) {
			String key = entry.getKey();
			final String value = entry.getValue();
			if (key.startsWith(prefix + ":" + attributeName)
					|| key.startsWith("data-" + prefix + "-" + attributeName)) {
				if (removeStart) {
					key = key.replaceAll("^" + prefix + ":" + attributeName,
							"");
					key = key.replaceAll(
							"^data-" + prefix + "-" + attributeName, "");
				}
				matchingAttributes.put(key, value);
			}
		}
		return matchingAttributes;
	}

	private String getReplaceAttributePart(final String attributeValue) {
		final Pattern pattern = Pattern.compile(".*\\?\\[([\\w|\\d|.|\\-|_]*)\\].*");
		final Matcher matcher = pattern.matcher(attributeValue);
		while (matcher.find()) {
			if (matcher.group(1) != null && !matcher.group(1).isEmpty()) {
				return matcher.group(1);
			}
		}
		return null;
	}
}