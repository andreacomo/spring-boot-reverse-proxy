package it.codingjam.spring_boot_reverse_proxy.controllers;

import it.codingjam.spring_boot_reverse_proxy.repositories.HttpRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.io.IOException;

@RestController
public class ProxyController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyController.class);

    private final HttpRepository httpRepository;

    private final String proxyTo;

    public ProxyController(HttpRepository httpRepository, @Value("${config.proxy.url}") String proxyTo) {
        this.httpRepository = httpRepository;
        this.proxyTo = proxyTo;
    }

    @RequestMapping(value = "/**")
    // StreamingResponseBody
    public ResponseEntity<InputStreamResource> proxy(HttpServletRequest request) throws IOException {
        LOGGER.info("Ready to proxy (in virtual thread? {}) on thread id {}",
                Thread.currentThread().isVirtual(), Thread.currentThread().threadId());
        RestClient.RequestHeadersSpec.ConvertibleClientHttpResponse response = httpRepository.doProxy(proxyTo, request);
        LOGGER.info("Response retrieved with {} on thread id {}", response.getStatusCode(), Thread.currentThread().threadId());
        return new ResponseEntity<>(
                // out -> StreamUtils.copy(response.getBody(), out),
                new InputStreamResource(response.getBody()),
                response.getHeaders(),
                response.getStatusCode());
    }
}
