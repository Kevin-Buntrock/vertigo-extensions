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

package io.vertigo.ui.impl.thymeleaf.components;

import static java.util.Collections.singleton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.thymeleaf.context.IEngineContext;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeNames;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.model.IAttribute;
import org.thymeleaf.model.ICloseElementTag;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
import org.thymeleaf.model.IOpenElementTag;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.model.IStandaloneElementTag;
import org.thymeleaf.model.ITemplateEvent;
import org.thymeleaf.model.IText;
import org.thymeleaf.processor.element.AbstractElementModelProcessor;
import org.thymeleaf.processor.element.IElementModelStructureHandler;
import org.thymeleaf.standard.StandardDialect;
import org.thymeleaf.standard.expression.Assignation;
import org.thymeleaf.standard.expression.AssignationSequence;
import org.thymeleaf.standard.expression.AssignationUtils;
import org.thymeleaf.standard.expression.IStandardExpression;
import org.thymeleaf.standard.expression.VariableExpression;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.util.StringUtils;

import io.vertigo.lang.Assertion;
import io.vertigo.util.StringUtil;

public class ThymeleafComponentNamedElementProcessor extends AbstractElementModelProcessor {
	private static final String VARIABLE_PLACEHOLDER_SEPARATOR = "_";
	private static final String ATTRS_SUFFIX = "attrs";
	private static final String CONTENT_TAGS = "contentTags";
	public static final String CONTENT_VAR_NAME = "content";

	private static final int PRECEDENCE = 350;

	private final Set<String> excludeAttributes = singleton("params");
	private final String componentName;
	private final Optional<VariableExpression> selectionExpression;
	private final List<String> parameterNames;
	private final List<String> placeholderPrefixes;
	private final Optional<String> unnamedPlaceholderPrefix;
	private final String frag;

	/**
	 * Constructor
	 *
	 * @param dialectPrefix Dialect prefix (tc)
	 * @param tagName Tag name to search for (e.g. panel)
	 * @param componentName Fragment to search for
	 */
	public ThymeleafComponentNamedElementProcessor(final String dialectPrefix, final ThymeleafComponent thymeleafComponent) {
		super(TemplateMode.HTML, dialectPrefix, thymeleafComponent.getName(), true, null, false, PRECEDENCE);
		componentName = thymeleafComponent.getFragmentTemplate();
		frag = thymeleafComponent.getFrag();

		selectionExpression = thymeleafComponent.getSelectionExpression();
		parameterNames = thymeleafComponent.getParameters();
		placeholderPrefixes = parameterNames.stream()
				.filter((parameterName) -> parameterName.endsWith(VARIABLE_PLACEHOLDER_SEPARATOR + ATTRS_SUFFIX))
				.map((parameterName) -> parameterName.substring(0, parameterName.length() - ATTRS_SUFFIX.length()))
				.collect(Collectors.toList());
		unnamedPlaceholderPrefix = placeholderPrefixes.isEmpty() ? Optional.empty() : Optional.of(placeholderPrefixes.get(placeholderPrefixes.size() - 1));
	}

	@Override
	protected void doProcess(final ITemplateContext context, final IModel model, final IElementModelStructureHandler structureHandler) {
		if (!selectionExpression.isPresent() //no selector
				|| (boolean) selectionExpression.get().execute(context)) { //or selector valid

			final IProcessableElementTag tag = processElementTag(context, model);
			final Map<String, String> attributes = processAttribute(model, context, structureHandler);

			final String param = attributes.get("params");

			final IModel contentModel = cloneAndCleanModel(model);

			removeCurrentTag(contentModel);

			if (parameterNames.contains(CONTENT_TAGS)) {
				structureHandler.setLocalVariable(CONTENT_TAGS, tag instanceof IStandaloneElementTag ? Collections.emptyList() : asList(contentModel));
			}
			structureHandler.setLocalVariable(CONTENT_VAR_NAME, tag instanceof IStandaloneElementTag ? Optional.empty() : Optional.ofNullable(contentModel));

			final String fragmentToUse = "~{" + componentName + " :: " + frag + "}";
			final IModel fragmentModel = FragmentHelper.getFragmentModel(context, fragmentToUse + (param == null ? "" : "(" + param + ")"), structureHandler);
			final IModel replacedFragmentModel = replaceAllAttributeValues(attributes, context, fragmentModel);

			//We replace the whole model
			model.reset();
			model.addModel(replacedFragmentModel);

			processVariables(attributes, context, structureHandler, excludeAttributes);
		} // else nothing

	}

	private void removeCurrentTag(final IModel model) {
		model.remove(0);
		if (model.size() > 1) {
			model.remove(model.size() - 1);
		}
	}

	private static IModel cloneAndCleanModel(final IModel model) {
		final IModel cleanerModel = model.cloneModel();
		final int size = cleanerModel.size();
		for (int i = size - 1; i > 0; i--) { //We loop decreasly for remove by index
			if (cleanerModel.get(i) instanceof IText) {
				final IText innerText = (IText) cleanerModel.get(i);
				if (StringUtil.isEmpty(innerText.getText())) {
					cleanerModel.remove(i);
				}
			}
		}
		return cleanerModel;
	}

	private List<ThymeleafContentComponent> asList(final IModel componentModel) {
		final List<ThymeleafContentComponent> asList = new ArrayList<>();
		final IModel buildingModel = componentModel.cloneModel(); //contains each first level tag (and all it's sub-tags)
		buildingModel.reset();
		int tapDepth = 0;
		for (int i = 0; i < componentModel.size(); i++) {
			final ITemplateEvent templateEvent = componentModel.get(i);
			buildingModel.add(templateEvent);
			if (templateEvent instanceof IOpenElementTag) {
				tapDepth++;
			} else if (templateEvent instanceof ICloseElementTag) {
				tapDepth--;
			}
			if (tapDepth == 0) {
				//Si on est à la base, on ajout que le model qu'on a préparé, on le close et on reset pour la boucle suivante
				final IModel firstLevelTagModel = buildingModel.cloneModel();
				asList.add(new ThymeleafContentComponent(firstLevelTagModel));
				buildingModel.reset();
			}
		}
		return asList;
	}

	private static IProcessableElementTag processElementTag(final ITemplateContext context, final IModel model) {
		final ITemplateEvent firstEvent = model.get(0);
		if (firstEvent instanceof IOpenElementTag) {
			final String elementCompleteName = ((IOpenElementTag) firstEvent).getElementCompleteName();
			final ITemplateEvent lastEvent = model.get(model.size() - 1);
			Assertion.checkArgument(lastEvent instanceof ICloseElementTag
					&& !((ICloseElementTag) lastEvent).isSynthetic()
					&& elementCompleteName.equals(((ICloseElementTag) lastEvent).getElementCompleteName()),
					"Can't find endTag of {0} in {1} line {2} col {3}", elementCompleteName, firstEvent.getTemplateName(), firstEvent.getLine(), firstEvent.getCol());
		}
		for (final IProcessableElementTag tag : context.getElementStack()) {
			if (locationMatches(firstEvent, tag)) {
				return tag;
			}
		}
		return null;
	}

	private static boolean locationMatches(final ITemplateEvent a, final ITemplateEvent b) {
		return Objects.equals(a.getTemplateName(), b.getTemplateName())
				&& Objects.equals(a.getLine(), b.getLine())
				&& Objects.equals(a.getCol(), b.getCol());
	}

	private void processVariables(final Map<String, String> attributes,
			final ITemplateContext context,
			final IElementModelStructureHandler structureHandler,
			final Set<String> excludeAttr) {

		final Map<String, Map<String, Object>> placeholders = new HashMap<>();

		for (final Map.Entry<String, String> entry : attributes.entrySet()) {
			if (excludeAttr.contains(entry.getKey()) || isDynamicAttribute(entry.getKey(), getDialectPrefix())) {
				continue;
			}
			final Object attributeValue = encodeAttributeValue(entry.getValue());
			processWith(context, entry.getKey() + "=" + attributeValue, structureHandler, placeholders);
		}

		//we set placeholders as localvariables (inner components shouldn't affect these in case of name conflict)
		setLocalPlaceholderVariables(structureHandler, placeholders);
	}

	private static Object encodeAttributeValue(final Object attributeValue) {
		if (attributeValue == null) {
			return "${true}";
		} else if (attributeValue instanceof String
				&& ((String) attributeValue).matches("^[a-zA-Z]+[^$#|]*")) {
			return "'" + attributeValue + "'";
		}
		return attributeValue;
	}

	private void setLocalPlaceholderVariables(final IElementModelStructureHandler structureHandler, final Map<String, Map<String, Object>> placeholders) {
		for (final String placeholderPrefix : placeholderPrefixes) {
			final String placeholder = placeholderPrefix + ATTRS_SUFFIX;
			final String affectationString;
			if (placeholders.containsKey(placeholder)) {
				affectationString = placeholders.get(placeholder)
						.entrySet().stream()
						.map(entry1 -> entry1.getKey() + "=" + entry1.getValue())
						.collect(Collectors.joining(", "));
			} else {
				affectationString = "noOp=_";
			}
			structureHandler.setLocalVariable(placeholder, affectationString);
		}
	}

	private Map<String, String> processAttribute(final IModel model, final ITemplateContext context, final IElementModelStructureHandler structureHandler) {
		final ITemplateEvent firstEvent = model.get(0);
		final Map<String, String> attributes = new HashMap<>();

		final Map<String, String> contentAttrs = (Map<String, String>) context.getVariable("contentAttrs");
		if (contentAttrs != null && !contentAttrs.isEmpty()) {
			structureHandler.removeLocalVariable("contentAttrs");
			attributes.putAll(contentAttrs);
		}

		if (firstEvent instanceof IProcessableElementTag) {
			final IProcessableElementTag processableElementTag = (IProcessableElementTag) firstEvent;
			for (final IAttribute attribute : processableElementTag.getAllAttributes()) {
				final String completeName = attribute.getAttributeCompleteName();
				if (!isDynamicAttribute(completeName, StandardDialect.PREFIX)) {
					attributes.put(completeName, attribute.getValue());
				}
			}
		}
		return attributes;
	}

	private void addPlaceholderVariable(final Map<String, Map<String, Object>> placeholders, final String prefixedVariableName, final Object value) {
		for (final String placeholderPrefix : placeholderPrefixes) {
			if (prefixedVariableName.startsWith(placeholderPrefix)) {
				final String attributeName = prefixedVariableName.substring(placeholderPrefix.length());
				addPlaceholderVariable(placeholders, placeholderPrefix, attributeName, encodeAttributeValue(value));
			}
		}
	}

	private static void addPlaceholderVariable(final Map<String, Map<String, Object>> placeholders, final String placeholderPrefix, final String attributeName, final Object value) {
		Map<String, Object> previousPlaceholderValues = placeholders.get(placeholderPrefix + ATTRS_SUFFIX);
		if (previousPlaceholderValues == null) {
			previousPlaceholderValues = new HashMap<>();
			placeholders.put(placeholderPrefix + ATTRS_SUFFIX, previousPlaceholderValues);
		}
		previousPlaceholderValues.put(attributeName, encodeAttributeValue(value));
	}

	private boolean isPlaceholder(final String prefixedVariableName) {
		for (final String placeholderPrefix : placeholderPrefixes) {
			if (prefixedVariableName.startsWith(placeholderPrefix)) {
				return true;
			}
		}
		return false;
	}

	private static boolean isDynamicAttribute(final String attribute, final String prefix) {
		return attribute.startsWith(prefix + ":") || attribute.startsWith("data-" + prefix + "-");
	}

	private IModel replaceAllAttributeValues(final Map<String, String> attributes, final ITemplateContext context, final IModel model) {
		final Map<String, String> replaceAttributes = findAllAttributesStartsWith(attributes, super.getDialectPrefix(), "repl-", true);

		if (replaceAttributes.isEmpty()) {
			return model;
		}
		final IModel clonedModel = model.cloneModel();
		final int size = model.size();
		for (int i = 0; i < size; i++) {
			final ITemplateEvent replacedEvent = replaceAttributeValue(context, clonedModel.get(i), replaceAttributes);
			if (replacedEvent != null) {
				clonedModel.replace(i, replacedEvent);
			}
		}
		return clonedModel;
	}

	private static ITemplateEvent replaceAttributeValue(final ITemplateContext context, final ITemplateEvent model, final Map<String, String> replaceValueMap) {
		IProcessableElementTag firstEvent = null;
		if (!replaceValueMap.isEmpty() && model instanceof IProcessableElementTag) {
			final IModelFactory modelFactory = context.getModelFactory();

			firstEvent = (IProcessableElementTag) model;
			for (final Map.Entry<String, String> entry : firstEvent.getAttributeMap().entrySet()) {
				final String oldAttrValue = entry.getValue();
				final String replacePart = getReplaceAttributePart(oldAttrValue);
				if (replacePart != null && replaceValueMap.containsKey(replacePart)) {
					final String newStringValue = oldAttrValue.replace("?[" + replacePart + "]", replaceValueMap.get(replacePart));
					firstEvent = modelFactory.replaceAttribute(firstEvent,
							AttributeNames.forTextName(entry.getKey()),
							entry.getKey(), newStringValue);
				}
			}
		}
		return firstEvent;
	}

	private static Map<String, String> findAllAttributesStartsWith(final Map<String, String> attributes, final String prefix, final String attributeName, final boolean removeStart) {
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

	private static String getReplaceAttributePart(final String attributeValue) {
		final Pattern pattern = Pattern.compile(".*\\?\\[([\\w|\\d|.|\\-|_]*)\\].*");
		final Matcher matcher = pattern.matcher(attributeValue);
		while (matcher.find()) {
			if (matcher.group(1) != null && !matcher.group(1).isEmpty()) {
				return matcher.group(1);
			}
		}
		return null;
	}

	private void processWith(final ITemplateContext context, final String attributeValue, final IElementModelStructureHandler structureHandler, final Map<String, Map<String, Object>> placeholders) {

		final AssignationSequence assignations = AssignationUtils.parseAssignationSequence(context, attributeValue, false /* no parameters without value */);
		if (assignations == null) {
			throw new TemplateProcessingException("Could not parse value as attribute assignations: \"" + attributeValue + "\"");
		}

		// Normally we would just allow the structure handler to be in charge of declaring the local variables
		// by using structureHandler.setLocalVariable(...) but in this case we want each variable defined at an
		// expression to be available for the next expressions, and that forces us to cast our ITemplateContext into
		// a more specific interface --which shouldn't be used directly except in this specific, special case-- and
		// put the local variables directly into it.
		IEngineContext engineContext = null;
		if (context instanceof IEngineContext) {
			// NOTE this interface is internal and should not be used in users' code
			engineContext = (IEngineContext) context;
		}

		final List<Assignation> assignationValues = assignations.getAssignations();
		final int assignationValuesLen = assignationValues.size();

		for (int i = 0; i < assignationValuesLen; i++) {

			final Assignation assignation = assignationValues.get(i);

			final IStandardExpression leftExpr = assignation.getLeft();
			final Object leftValue = leftExpr.execute(context);

			final String newVariableName = leftValue == null ? null : leftValue.toString();
			if (StringUtils.isEmptyOrWhitespace(newVariableName)) {
				throw new TemplateProcessingException("Variable name expression evaluated as null or empty: \"" + leftExpr + "\"");
			}

			final IStandardExpression rightExpr = assignation.getRight();
			final Object rightValue = rightExpr.execute(context);

			if (isPlaceholder(newVariableName)) {
				//We prepared prefixed placeholders variables.
				addPlaceholderVariable(placeholders, newVariableName, rightValue);
			} else if (!parameterNames.contains(newVariableName)) {
				Assertion.checkState(unnamedPlaceholderPrefix.isPresent(), "Component '{0}' can't accept this parameter : '{1}' (accepted params : {2})", componentName, newVariableName, parameterNames);
				//We prepared unnamed placeholder variable
				addPlaceholderVariable(placeholders, unnamedPlaceholderPrefix.get(), newVariableName, rightValue);
			}

			if (engineContext != null) {
				// The advantage of this vs. using the structure handler is that we will be able to
				// use this newly created value in other expressions in the same 'th:with'
				engineContext.setVariable(newVariableName, rightValue);
			} else {
				// The problem is, these won't be available until we execute the next processor
				structureHandler.setLocalVariable(newVariableName, rightValue);
			}
		}
	}
}