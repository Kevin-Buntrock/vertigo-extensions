package io.vertigo.ui.impl.springmvc.argumentresolvers;

import java.util.Collections;

import org.springframework.core.MethodParameter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.AbstractMessageConverterMethodProcessor;

import io.vertigo.ui.core.ViewContext;
import io.vertigo.ui.impl.springmvc.util.UiRequestUtil;

public class ViewContextReturnValueAndArgumentResolver extends AbstractMessageConverterMethodProcessor {

	public ViewContextReturnValueAndArgumentResolver() {
		super(Collections.singletonList(new GsonHttpMessageConverter()));
	}

	@Override
	public boolean supportsReturnType(final MethodParameter returnType) {
		return ViewContext.class.isAssignableFrom(returnType.getParameterType());
	}

	@Override
	public void handleReturnValue(final Object returnValue, final MethodParameter returnType, final ModelAndViewContainer mavContainer, final NativeWebRequest webRequest) throws Exception {
		mavContainer.setRequestHandled(true);
		final ServletServerHttpRequest inputMessage = createInputMessage(webRequest);
		final ServletServerHttpResponse outputMessage = createOutputMessage(webRequest);

		// Try even with null return value. ResponseBodyAdvice could get involved.
		writeWithMessageConverters(((ViewContext) returnValue).asUpdatesMap(), returnType, inputMessage, outputMessage);

	}

	@Override
	public boolean supportsParameter(final MethodParameter parameter) {
		return ViewContext.class.isAssignableFrom(parameter.getParameterType());
	}

	@Override
	public Object resolveArgument(final MethodParameter parameter, final ModelAndViewContainer mavContainer, final NativeWebRequest webRequest, final WebDataBinderFactory binderFactory) throws Exception {
		return UiRequestUtil.getCurrentViewContext();
	}

}