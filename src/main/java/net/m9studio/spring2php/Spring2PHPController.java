package net.m9studio.spring2php;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.function.Consumer;
import java.util.function.Predicate;

@Component
@RequiredArgsConstructor
public class Spring2PHPController {
    @Autowired
    Config config;

    ProcedureAddData procedureCookie = null;
    ProcedureAddData procedureParameters = null;
    Predicate<MapperData> accept = null;

    private final WebClient webClient = WebClient.create();

    public ResponseEntity<String> handle(HttpServletRequest requestHttpServletRequest request, @RequestBody(required = false) String body) {

        String path = request.getRequestURI();
        String method = request.getMethod();
        MapperData md = null;

/*
todo поиск MapperData md и сравнение со всеми доступными

        if(!path.startsWith(config.getRouter())){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        MapperData md = props.getMappers().stream()
                             .filter(m -> path.equalsIgnoreCase(m.getMapperRouter()) && method.equalsIgnoreCase(m.getMapperType()))
                             .findFirst()
                             .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

*/

        if(accept != null && !accept.test(md)){
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE);
        }
        if(procedureCookie != null){
            procedureCookie.AddData(webClient, md, request);
        }
        if(procedureParameters != null){
            procedureParameters.AddData(webClient, md, request);
        }


        return null;//todo
    }
}
