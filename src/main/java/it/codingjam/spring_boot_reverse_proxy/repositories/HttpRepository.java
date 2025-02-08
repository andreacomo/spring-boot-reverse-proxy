package it.codingjam.spring_boot_reverse_proxy.repositories;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;

@Repository
public class HttpRepository {

    public RestClient.RequestHeadersSpec.ConvertibleClientHttpResponse doProxy(String downstreamHost, HttpServletRequest request) throws IOException {
        return createProxyRequest(downstreamHost, request)
                .exchange((req, resp) -> resp, false);
    }

    private static RestClient.RequestBodySpec createProxyRequest(String downstreamHost, HttpServletRequest request) throws IOException {
        RestClient.RequestBodySpec bodySpec = RestClient.create(downstreamHost)
                .method(HttpMethod.valueOf(request.getMethod()))
                .uri(uriBuilder -> createUri(request, uriBuilder))
                .headers(headers -> createHeaders(request, headers));

        if (request.getInputStream().available() > 0) {
            bodySpec.body(new InputStreamResource(request.getInputStream()));
        }
        return bodySpec;
    }

    private static void createHeaders(HttpServletRequest request, HttpHeaders headers) {
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, Collections.list(request.getHeaders(headerName)));
        }
    }

    private static URI createUri(HttpServletRequest request, UriBuilder uriBuilder) {
        return uriBuilder.path(request.getRequestURI())
                .query(request.getQueryString()).build();
    }
}
