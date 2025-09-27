package net.m9studio.spring2php;

import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;

public class MapperCollect {
    private Config config;
    private List<MapperData> list;
    public MapperData search(HttpServletRequest request){
        return list.stream()
                   .filter(row -> row.check(request))
                   .filter(row -> row.getType() != null/*проверка по аргументам*/)
                   .findFirst()
                   .orElse(null);
    }
    public void update(){
        List<MapperData> list = new ArrayList();

        //todo заполняем из файлов с конфигами
        {
            String Router     = replace(null);
            String baseRouter = replace(null);
            String fullRouter = replace(null);
            if(Router == null && fullRouter == null){
                //todo runtime
            }

            String phpUrl     = replace(null);
            String phpBaseUrl = httpNormalize(replace(null));
            String phpFullUrl = httpNormalize(replace(null));
            if(phpUrl == null && phpFullUrl == null){
                //todo runtime
            }

            String type = null;
            if(type == null){
                //todo runtime
            }

            MapperData md = new MapperData(Router, baseRouter, fullRouter,
                                           phpUrl, phpBaseUrl, phpFullUrl,
                                           type, config.getBaseRouter(), config.getPhpBaseUrl());

            //todo заполнение md.list

            list.add(md);
        }

        this.list = list;
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
        return t;
    }
    private static String httpNormalize(String s){
        if(s == null){
            return null;
        }
        return s.replace(":/", "://");
    }
}
