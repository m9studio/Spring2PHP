package net.m9studio.springrelay;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import net.m9studio.springrelay.exception.ConfigPathNotFoundException;
import net.m9studio.springrelay.exception.InvalidEntryException;
import net.m9studio.springrelay.exception.MultipleMatchesException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
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
        if (folder.exists() && folder.isDirectory()) {
            //папка есть
        } else {
            //todo later исключение, что папки нет.... или не стоит и создать папку самим????
            throw new ConfigPathNotFoundException(config.getConfigPath());
        }

        //todo заполняем из файлов с конфигами

        while ("todo дописать выборку из файла" == null){
            String path     = replace(null);
            String basePath = replace(null);
            String fullPath = replace(null);
            if(path == null && fullPath == null){
                if(config.isIgnoreInvalidEntries()){
                    //todo log
                    continue;
                }else{
                    throw new InvalidEntryException("missing path/fullPath");
                }
            }

            String targetUrl     = replace(null);
            String baseTargetUrl = httpNormalize(replace(null));
            String fullTargetUrl = httpNormalize(replace(null));
            if(targetUrl == null && fullTargetUrl == null){
                if(config.isIgnoreInvalidEntries()){
                    //todo log
                    continue;
                }else{
                    throw new InvalidEntryException("missing targetUrl/fullTargetUrl");
                }
            }

            String httpMethod = null;
            if(httpMethod == null){
                if(config.isIgnoreInvalidEntries()){
                    //todo log
                    continue;
                }else{
                    throw new InvalidEntryException("missing httpMethod");
                }
            }


            String thisPath = fullPath;
            if(thisPath == null){
                thisPath = basePath;
                if(thisPath == null){
                    thisPath = config.getBasePath();
                }
                thisPath += "/" + path;
            }

            String thisTargetUrl = fullTargetUrl;
            if(thisTargetUrl == null){
                thisTargetUrl = baseTargetUrl;
                if(thisTargetUrl == null){
                    thisTargetUrl = config.getBaseTargetUrl();
                }
                thisTargetUrl += "/" + targetUrl;
            }

            RelayEntry md = new RelayEntry(httpMethod, thisPath, thisTargetUrl);

            //todo заполнение md.list


            List<RelayEntry> list = map.getOrDefault(md.getPath(), null);
            if(list == null){
                list = new ArrayList<>();
                map.put(md.getPath(), list);
            }
            list.add(md);
        }

        this.map = map;
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
