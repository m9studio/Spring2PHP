package net.m9studio.spring2php;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/*
исключение на type и config параметры, будут в вызывающем методе, а не тут!
так-же при вызове конструктора, не будет пустых строк,
а null и так-же при вызове конструктора будут убраны слэши в начале и в конце и убраны двойные слеши
 */
@Getter
public class MapperData {
    private final List<MapperParameter> listParameters = new ArrayList<>();

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

    public Boolean check(String path, String method){
        if(!method.equalsIgnoreCase(type)){
            return false;
        }
        return this.path.equalsIgnoreCase(path);
    }
    public Boolean check(HttpServletRequest request){
        return check(request.getRequestURI(), request.getMethod());
    }
}