package net.m9studio.springrelay;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
исключение на type и config параметры, будут в вызывающем методе, а не тут!
так-же при вызове конструктора, не будет пустых строк,
а null и так-же при вызове конструктора будут убраны слэши в начале и в конце и убраны двойные слеши
 */
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

    public Boolean checkParameters(Map<String, String[]> parameters){
        int count = 0;

        //подсчет всех необязательных параметров
        for(Map.Entry<String, RelayParameter> entry : mapParameters.entrySet()){
            if(!entry.getValue().getRequired()){
                count++;
            }
        }

        for(Map.Entry<String, String[]> entry : parameters.entrySet()){
            if(mapParameters.containsKey(entry.getKey())){
                RelayParameter md = mapParameters.get(entry.getKey());
                //todo проверка на типы, если тип не подходит, то return false;

                //если это обязательный параметр, то считаем, все необязательные уже были подсчитаны ранее
                if(md.getRequired()){
                    count++;
                }
            }else{
                return false;
            }
        }
        //если кол-во необязательных + те что были найдены в обязательных в parameters, не равно mapParameters.size(), то есть расхождение и это не искомый мэппер
        return count == mapParameters.size();
    }
    public Boolean checkParameters(HttpServletRequest request){
        return checkParameters(request.getParameterMap());
    }

}