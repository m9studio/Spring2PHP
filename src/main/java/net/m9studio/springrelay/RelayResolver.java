package net.m9studio.springrelay;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RelayResolver {
    @Autowired
    private SpringRelayConfig config;
    @PostConstruct
    private void init(){
        config.setConfigPath(replace(config.getConfigPath()));
        config.setBasePath(replace(config.getBasePath()));
        config.setBaseTargetUrl(httpNormalize(replace(config.getBaseTargetUrl())));

        if(config.getConfigPath() == null){
            //todo runtime
        }
        if(config.getBasePath() == null){
            //todo runtime
        }
        if(config.getBaseTargetUrl() == null){
            //todo runtime
        }

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
                   .filter(row -> row.checkParameters(request))
                   .toList();

        if(list.isEmpty()){
            return null;
        }
        if(list.size() > 1){
            //todo runtime не удалось выбрать вызывающий метод
        }
        return list.getFirst();
    }
    public void update(){
        Map<String, List<RelayEntry>> map = new ConcurrentHashMap<>();

        //todo заполняем из файлов с конфигами
        {
            String path     = replace(null);
            String basePath = replace(null);
            String fullPath = replace(null);
            if(path == null && fullPath == null){
                //todo runtime
            }

            String targetUrl     = replace(null);
            String baseTargetUrl = httpNormalize(replace(null));
            String fullTargetUrl = httpNormalize(replace(null));
            if(targetUrl == null && fullTargetUrl == null){
                //todo runtime
            }

            String httpMethod = null;
            if(httpMethod == null){
                //todo runtime
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
