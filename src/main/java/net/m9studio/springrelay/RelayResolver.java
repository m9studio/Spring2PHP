package net.m9studio.springrelay;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import net.m9studio.springrelay.exception.ConfigPathNotFoundException;
import net.m9studio.springrelay.exception.InvalidEntryException;
import net.m9studio.springrelay.exception.MultipleMatchesException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RelayResolver {
    @Autowired
    private SpringRelayConfig config;
    @PostConstruct
    private void init(){
        config.setConfigPath(replace(config.getConfigPath()));
        config.setBasePath(replace(config.getBasePath()));
        config.setBaseTargetUrl(httpNormalize(replace(config.getBaseTargetUrl())));

        update();
    }


    private Map<String, List<RelayEntry>> map;



    public RelayEntry search(HttpServletRequest request){
        List<RelayEntry> list = map.getOrDefault(request.getRequestURI(), null);

        if(list == null){
            return null;
        }

        list = list.stream()
                   .filter(row -> row.getHttpMethod().equalsIgnoreCase(request.getMethod()))
                   .filter(row -> row.checkParameters(request))//todo проверка на передаваемые файлы, если есть
                   .toList();

        if(list.isEmpty()){
            return null;
        }
        if(list.size() > 1){
            if(config.isFailOnMultipleMatches()){
                //не удалось выбрать вызывающий метод
                throw new MultipleMatchesException(request.getRequestURI(), request.getMethod(), list.size());
            }else{
                //todo log
            }
        }
        return list.getFirst();
    }
    public void update(){
        Map<String, List<RelayEntry>> map = new ConcurrentHashMap<>();

        File folder = new File(config.getConfigPath());
        if (!folder.exists() || !folder.isDirectory()) {
            //todo later исключение, что папки нет.... или не стоит и создать папку самим????
            throw new ConfigPathNotFoundException(config.getConfigPath());
        }

        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml") || name.endsWith(".yaml"));
        if (files != null) {
            Yaml yaml = new Yaml();
            for (File file : files) {

                Object rawRows;
                try(FileInputStream fis = new FileInputStream(file)){
                    rawRows = yaml.load(fis);
                } catch (IOException e) {
                    handleInvalid(e.getMessage());
                    continue;
                }

                List<Object> listRawRows;
                if(rawRows instanceof List<?>){
                    listRawRows = (List<Object>)rawRows;
                }else{
                    handleInvalid("YAML root in file '" + file.getName() + "' must be a list of mappings");
                    continue;
                }



                for(Object rawRow : listRawRows){
                    Map<String, Object> mapRow = new HashMap<>();
                    if(rawRow instanceof Map<?, ?>){
                        Map<Object, Object> mapRawRow = (Map<Object, Object>)rawRow;
                        for(Map.Entry<Object, Object> entry : mapRawRow.entrySet()){
                            mapRow.put(entry.getKey().toString(), entry.getValue());
                        }
                    }else{
                        handleInvalid("YAML element in file '" + file.getName() + "' is not a mapping (expected key-value pairs)");
                        continue;
                    }

                    String path     = replace(mapRow.getOrDefault("path", "").toString());
                    String basePath = replace(mapRow.getOrDefault("base-path", "").toString());
                    String fullPath = replace(mapRow.getOrDefault("full-path", "").toString());
                    if(path == null && fullPath == null){
                        handleInvalid("missing path/fullPath");
                        continue;
                    }
                    String thisPath = fullPath;
                    if(thisPath == null){
                        thisPath = basePath;
                        if(thisPath == null){
                            thisPath = config.getBasePath();
                        }
                        thisPath += "/" + path;
                    }

                    String targetUrl     = replace(mapRow.getOrDefault("target-url", "").toString());
                    String baseTargetUrl = replace(mapRow.getOrDefault("base-target-url", "").toString());
                    String fullTargetUrl = replace(mapRow.getOrDefault("full-target-url", "").toString());
                    if(targetUrl == null && fullTargetUrl == null){
                        handleInvalid("missing targetUrl/fullTargetUrl");
                        continue;
                    }
                    String thisTargetUrl = fullTargetUrl;
                    if(thisTargetUrl == null){
                        thisTargetUrl = baseTargetUrl;
                        if(thisTargetUrl == null){
                            thisTargetUrl = config.getBaseTargetUrl();
                        }
                        thisTargetUrl += "/" + targetUrl;
                    }

                    String httpMethod = replace(mapRow.getOrDefault("method", "").toString());
                    if(httpMethod == null){
                        handleInvalid("missing httpMethod");
                        continue;
                    }

                    RelayEntry md = new RelayEntry(httpMethod, thisPath, thisTargetUrl);

                    Object rawParams = mapRow.getOrDefault("parameters", null);
                    List<Map<String, String>> listParameters = null;


                    if(rawParams instanceof List<?> listParams){
                        for (Object item : listParams) {
                            if (!(item instanceof Map<?, ?> mapParams)) {
                                handleInvalid("'parameters' contains non-map item");
                                continue;
                            }

                        }
                    }else{

                    }


                    if(objectParameters != null){
                        try{
                            listParameters = (List<Map<String, String>>)objectParameters;
                        }catch (Exception e){
                            handleInvalid(e.getMessage());
                            continue;
                        }
                    }

                    if(listParameters != null){
                        //todo заполнение md.list

                    }



                    List<RelayEntry> list = map.getOrDefault(md.getPath(), null);
                    if(list == null){
                        list = new ArrayList<>();
                        map.put(md.getPath(), list);
                    }
                    list.add(md);
                }
            }
        }

        this.map = map;
    }

    private void handleInvalid(String reason) {
        if (config.isIgnoreInvalidEntries()) {
            //todo log.warn("Ignoring invalid entry: {}", reason);
        } else {
            throw new InvalidEntryException(reason);
        }
    }

    private static String replace(String s){
        if(s == null){
            return null;
        }
        String t = s.trim().replaceAll("/+", "/");

        if(t.startsWith("/")){
            t = t.substring(1);
        }
        if(t.endsWith("/")){
            t = t.substring(0, t.length() - 1);
        }

        if(t.isEmpty()){
            return null;
        }
        return t.toLowerCase();
    }
    private static String httpNormalize(String s){
        if(s == null){
            return null;
        }
        return s.replace(":/", "://");
    }
}
