package net.m9studio.springrelay;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import net.m9studio.springrelay.config.SpringRelayConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.function.Predicate;

@Service
public class SpringRelayController {
    @Autowired
    SpringRelayConfig config;
    @Autowired
    RelayResolver mapperCollect;

    ProcedureAddData procedure = null;

    Predicate<RelayEntry> accept = null;

    private final WebClient webClient = WebClient.create();

    public ResponseEntity<String> handle(HttpServletRequest request, @RequestBody(required = false) String body) {

        RelayEntry md = mapperCollect.search(request);
        if(md == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        if(accept != null && !accept.test(md)){
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE);
        }

        WebClient.RequestBodySpec wc = webClient.method(HttpMethod.valueOf(request.getMethod()
                                                                                  .toUpperCase()))
                                                .uri(uriBuilder -> {
                                                    UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(md.getTargetUrl());

                                                    request.getParameterMap().forEach((key, values) -> {
                                                        for (String value : values) {
                                                            builder.queryParam(key, value);
                                                        }
                                                    });

                                                    return builder.build(true).toUri();
                                                })
                                                .cookies(c -> {
                                                    for (Cookie cookie : request.getCookies()) {
                                                        c.add(cookie.getName(), cookie.getValue());
                                                    }
                                                })
                                                .headers(h -> {
                                                    String device = request.getHeader("X-Device");
                                                    if (device != null){
                                                        h.set("X-Device", device);
                                                    }

                                                    String client = request.getHeader("X-Client");
                                                    if (client != null){
                                                        h.set("X-Client", client);
                                                    }

                                                    String clientVersion = request.getHeader("X-Client-Version");
                                                    if (clientVersion != null){
                                                        h.set("X-Client-Version", clientVersion);
                                                    }

                                                    String userAgent = request.getHeader("User-Agent");
                                                    if (userAgent != null) {
                                                        h.set(HttpHeaders.USER_AGENT, userAgent);
                                                    }
                                                });

        //чтобы пользователь аддона, добавлял необходимые ему параметры
        if(procedure != null){
            procedure.AddData(wc, md, request);
        }

        return null;//todo
    }
}
