package net.m9studio.spring2php;

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
public class MapperData {
    private final Map<String, MapperParameter> mapParameters = new ConcurrentHashMap<>();


    private final String type;

    private final String path;
    private final String phpUrl;

    public MapperData(
                      //либо Router либо fullRouter - null
                      String Router,
                      String baseRouter, //Если не указано, то береться из Config.baseUrl
                      String fullRouter, //Если указано, то является приоритетом

                      //либо phpUrl либо phpFullUrl - null
                      String phpUrl,
                      String phpBaseUrl, //Если не указано, то береться из Config.phpBaseUrl
                      String phpFullUrl, //Если указано, то является приоритетом

                      String type,

                      String configBaseRouter,//не допускает null
                      String configPhpBaseUrl //не допускает null
    ){
        String thisPath = fullRouter;
        if(thisPath == null){
            thisPath = baseRouter;
            if(thisPath == null){
                thisPath = configBaseRouter;
            }
            thisPath += "/" + Router;
        }

        String thisPhpUrl = phpFullUrl;
        if(thisPhpUrl == null){
            thisPhpUrl = phpBaseUrl;
            if(thisPhpUrl == null){
                thisPhpUrl = configPhpBaseUrl;
            }
            thisPhpUrl += "/" + phpUrl;
        }

        this.type = type;
        this.path = thisPath;
        this.phpUrl = thisPhpUrl;
    }

    public Boolean checkMethod(String path, String method){
        if(!method.equalsIgnoreCase(type)){
            return false;
        }
        return this.path.equalsIgnoreCase(path);
    }
    public Boolean checkMethod(HttpServletRequest request){
        return checkMethod(request.getRequestURI(), request.getMethod());
    }

    public Boolean checkParameters(Map<String, String[]> parameters){
        int count = 0;

        //подсчет всех необязательных параметров
        for(Map.Entry<String, MapperParameter> entry : mapParameters.entrySet()){
            if(!entry.getValue().getRequired()){
                count++;
            }
        }

        for(Map.Entry<String, String[]> entry : parameters.entrySet()){
            if(mapParameters.containsKey(entry.getKey())){
                MapperParameter md = mapParameters.get(entry.getKey());
                //todo проверка на типы, если тип не подходит, то return false;

                //если это обязательный параметр, то считаем, необязательные уже были подсчитаны ранее
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