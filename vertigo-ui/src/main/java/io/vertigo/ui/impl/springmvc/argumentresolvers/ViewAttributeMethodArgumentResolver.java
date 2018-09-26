package io.vertigo.ui.impl.springmvc.argumentresolvers;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.lang.Assertion;
import io.vertigo.ui.core.ViewContext;
import io.vertigo.ui.impl.springmvc.util.UiRequestUtil;
import io.vertigo.vega.webservice.model.UiObject;
import io.vertigo.vega.webservice.validation.DefaultDtObjectValidator;
import io.vertigo.vega.webservice.validation.DtObjectValidator;
import io.vertigo.vega.webservice.validation.UiMessageStack;
import io.vertigo.vega.webservice.validation.ValidationUserException;

public final class ViewAttributeMethodArgumentResolver implements HandlerMethodArgumentResolver {

	private final List<DtObjectValidator<DtObject>> defaultDtObjectValidators = Collections.singletonList(new DefaultDtObjectValidator<>());

	@Override
	public boolean supportsParameter(final MethodParameter parameter) {
		return parameter.hasParameterAnnotation(ViewAttribute.class);
	}

	@Override
	public Object resolveArgument(final MethodParameter parameter, final ModelAndViewContainer mavContainer, final NativeWebRequest webRequest, final WebDataBinderFactory binderFactory) throws Exception {
		final ViewContext viewContext = UiRequestUtil.getCurrentViewContext();
		final UiMessageStack uiMessageStack = UiRequestUtil.getCurrentUiMessageStack();
		Assertion.checkNotNull(viewContext);
		//---
		final String contextKey = parameter.getParameterAnnotation(ViewAttribute.class).value();
		//---
		if (UiObject.class.isAssignableFrom(parameter.getParameterType())) {
			return viewContext.getUiObject(() -> contextKey);
		} else if (DtObject.class.isAssignableFrom(parameter.getParameterType()) || DtList.class.isAssignableFrom(parameter.getParameterType())) {
			Assertion.checkNotNull(uiMessageStack);
			//---
			final Object value;
			if (DtObject.class.isAssignableFrom(parameter.getParameterType())) {
				//object
				if (viewContext.getUiObject(()->contextKey).checkFormat(uiMessageStack)) {
					value = viewContext.getUiObject(()->contextKey).mergeAndCheckInput(defaultDtObjectValidators, uiMessageStack);
				} else {
					value = null;
				}
			} else {
				//list
				if (viewContext.getUiList(()->contextKey).checkFormat(uiMessageStack)) {
					value = viewContext.getUiList(()->contextKey).mergeAndCheckInput(defaultDtObjectValidators, uiMessageStack);
				} else {
					value = null;
				}
			}
			if (!isNotLastDt(parameter) && uiMessageStack.hasErrors()) {
				// if we are the last one
				throw new ValidationUserException();
			}
			return value;
		}
		return viewContext.get(contextKey);// for primitive or other objects
	}

	private static boolean isNotLastDt(final MethodParameter parameter) {
		return Stream.of(parameter.getMethod().getParameters())
				.skip(parameter.getParameterIndex() + 1)
				.anyMatch(remainingParam -> DtObject.class.isAssignableFrom(remainingParam.getType()) || DtList.class.isAssignableFrom(remainingParam.getType()));
	}

}