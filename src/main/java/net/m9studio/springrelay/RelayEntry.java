package net.m9studio.springrelay;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class RelayEntry {
    private final Map<String, RelayParameter> mapParameters = new ConcurrentHashMap<>();

    private final String httpMethod;
    private final String path;
    private final String targetUrl;

    public RelayEntry(String httpMethod, String path, String targetUrl){
        this.httpMethod = httpMethod;
        this.path = path;
        this.targetUrl = targetUrl;
    }

    public void addParameter(RelayParameter parameter){
        mapParameters.put(parameter.getName().toLowerCase(), parameter);
    }

    public boolean checkParameters(Map<String, String[]> parameters){
        int count = 0;

        //подсчет всех необязательных параметров
        for(Map.Entry<String, RelayParameter> entry : mapParameters.entrySet()){
            if(!entry.getValue().isRequired()){
                count++;
            }
        }

        //todo распаралелить или не стоит?
        for(Map.Entry<String, String[]> entry : parameters.entrySet()){
            if(mapParameters.containsKey(entry.getKey())){
                RelayParameter md = mapParameters.get(entry.getKey());

                if(!md.check(entry.getValue())){
                    return false;
                }

                //если это обязательный параметр, то считаем, все необязательные уже были подсчитаны ранее
                if(md.isRequired()){
                    count++;
                }
            }else{
                return false;
            }
        }
        //если кол-во необязательных + те что были найдены в обязательных в parameters, не равно mapParameters.size(), то есть расхождение и это не искомый мэппер
        return count == mapParameters.size();
    }
    public boolean checkParameters(HttpServletRequest request){
        return checkParameters(request.getParameterMap());
    }
}