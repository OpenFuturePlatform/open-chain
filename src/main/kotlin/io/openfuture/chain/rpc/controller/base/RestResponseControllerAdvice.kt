package io.openfuture.chain.rpc.controller.base

import io.openfuture.chain.network.property.NodeProperties
import io.openfuture.chain.rpc.domain.RestResponse
import org.springframework.core.MethodParameter
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice


@ControllerAdvice
class RestResponseControllerAdvice(
    private val nodeProperties: NodeProperties
) : ResponseBodyAdvice<Any> {

    override fun supports(returnType: MethodParameter, converterType: Class<out HttpMessageConverter<*>>): Boolean {
        val parameterType = returnType.parameterType

        return !parameterType.isAssignableFrom(RestResponse::class.java) && !parameterType.isAssignableFrom(String::class.java)
    }

    override fun beforeBodyWrite(body: Any?, returnType: MethodParameter, selectedContentType: MediaType,
                                 selectedConverterType: Class<out HttpMessageConverter<*>>, request: ServerHttpRequest,
                                 response: ServerHttpResponse): RestResponse<Any?> {
        return RestResponse(System.currentTimeMillis(), nodeProperties.protocolVersion!!, body)
    }

}
