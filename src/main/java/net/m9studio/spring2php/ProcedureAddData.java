package net.m9studio.spring2php;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.reactive.function.client.WebClient;

@FunctionalInterface
public interface ProcedureAddData {
    void AddData(WebClient webClient, MapperData mapperData, HttpServletRequest request);
}
