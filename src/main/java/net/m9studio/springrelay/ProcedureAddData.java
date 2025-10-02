package net.m9studio.springrelay;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.reactive.function.client.WebClient;

@FunctionalInterface
public interface ProcedureAddData {
    void AddData(WebClient.RequestBodySpec webClient, MapperData mapperData, HttpServletRequest request);
}
