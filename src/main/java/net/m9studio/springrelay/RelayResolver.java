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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        Pattern pattern = Pattern.compile("^(?:array\\[(.+?)\\]|(.+))$");

        File folder = new File(config.getConfigPath());
        if (!folder.exists() || !folder.isDirectory()) {
            //todo later исключение, что папки нет.... или не стоит и создать папку самим????
            throw new ConfigPathNotFoundException(config.getConfigPath());
        }

        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml") || name.endsWith(".yaml"));
        if (files != null) {
            Yaml yaml = new Yaml();

            cycleFile:
            for (File file : files) {

                Object rawRows;
                try(FileInputStream fis = new FileInputStream(file)){
                    rawRows = yaml.load(fis);
                } catch (IOException e) {
                    handleInvalid(e.getMessage());
                    continue;
                }


                if(!(rawRows instanceof List<?>)){
                    handleInvalid("YAML root in file '" + file.getName() + "' must be a list of mappings");
                    continue;
                }
                List<Object> listRawRows = (List<Object>)rawRows;


                cycleEntry:
                for(Object rawRow : listRawRows){
                    if(!(rawRow instanceof Map<?, ?>)){
                        handleInvalid("YAML element in file '" + file.getName() + "' is not a mapping (expected key-value pairs)");
                        continue;
                    }

                    Map<String, Object> mapRow = new HashMap<>();
                    for(Map.Entry<Object, Object> entry : ((Map<Object, Object>)rawRow).entrySet()){
                        mapRow.put(entry.getKey().toString(), entry.getValue());
                    }

                    String path     = replace(mapRow.getOrDefault("path", "").toString());
                    String basePath = replace(mapRow.getOrDefault("base-path", "").toString());
                    String fullPath = replace(mapRow.getOrDefault("full-path", "").toString());
                    if(path == null && fullPath == null){
                        handleInvalid("missing path/fullPath in file '" + file.getName() + "'");
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
                        handleInvalid("missing targetUrl/fullTargetUrl in file '" + file.getName() + "'");
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
                        handleInvalid("missing httpMethod in file '" + file.getName() + "'");
                        continue;
                    }

                    RelayEntry relayEntry = new RelayEntry(httpMethod, thisPath, thisTargetUrl);

                    Object rawParams = mapRow.getOrDefault("parameters", null);

                    if(rawParams != null){
                        if(!(rawParams instanceof List<?>)) {
                            handleInvalid("'parameters' must be a list of maps in file '" + file.getName() + "'");
                            continue;
                        }
                        cycleParameter:
                        for (Object rawItem : (List<Object>)rawParams) {
                            if (!(rawItem instanceof Map<?, ?> mapParams)) {
                                handleInvalid("'parameters' contains non-map item in file '" + file.getName() + "'");
                                continue cycleEntry;
                            }
                            Map<String, Object> item = new HashMap<>();
                            for(Map.Entry<Object, Object> entry : ((Map<Object, Object>)mapParams).entrySet()){
                                item.put(entry.getKey().toString(), entry.getValue());
                            }

                            Object oType = item.getOrDefault("type", null);
                            Object oName = item.getOrDefault("name", null);
                            Object oRequired = item.getOrDefault("required", null);

                            if(oType == null || oName == null || oRequired == null){
                                handleInvalid("parameter missing type/name/required in file '" + file.getName() + "'");
                                continue cycleEntry;
                            }

                            RelayParameter rp = new RelayParameter();

                            rp.setName(oName.toString().toLowerCase());
                            rp.setRequired(oRequired.toString().equalsIgnoreCase("true"));
                            Matcher m = pattern.matcher(oType.toString().toLowerCase());

                            if (m.find()) {
                                if (m.group(1) != null) {
                                    // Совпадение с array[...]
                                    rp.setType(m.group(1));
                                    rp.setArray(true);
                                } else {
                                    // Просто текст
                                    rp.setType(m.group(2));
                                    rp.setArray(false);
                                }
                            }else{
                                //todo распарелилть на более подробную ошибку?
                                handleInvalid("invalid 'type' in parameter in file '" + file.getName() + "'");
                                continue cycleEntry;
                            }

                            relayEntry.addParameter(rp);
                        }


                    }


                    List<RelayEntry> list = map.getOrDefault(relayEntry.getPath(), null);
                    if(list == null){
                        list = new ArrayList<>();
                        map.put(relayEntry.getPath(), list);
                    }
                    list.add(relayEntry);
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
