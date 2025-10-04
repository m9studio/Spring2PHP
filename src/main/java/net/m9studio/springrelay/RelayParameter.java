package net.m9studio.springrelay;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.function.Function;


@Getter
@Setter
public class RelayParameter {

    //Array[Type] -> {array:true; type:Type}
    //Type -> {array:false; type:Type}
    private boolean array;
    private String type;

    private String name;
    private boolean required = true;

    public boolean check(String name, String[] values){
        if(this.name.equalsIgnoreCase(name)){
            return check(values);
        }
        return false;
    }

    public boolean check(String[] values){

        if(values.length > 1){
            if(array){
                //todo распаралелить или не стоит?
                for (String value : values) {
                    if (!check(value)) {
                        return false;
                    }
                }
                return true;
            }else{
                return false;
            }
        }

        return check(values[0]);
    }
    public boolean check(Map.Entry<String, String[]> entry){
        return check(entry.getKey(), entry.getValue());
    }

    private boolean check(String value){
        //todo стоит ли добавлять unsigned number и все подтипы?

        return switch (type) {
            case "any", "string" -> true;
            case "b", "byte" -> isByte(value);
            case "s", "short" -> isShort(value);
            case "i", "int", "integer" -> isInt(value);
            case "l", "long" -> isLong(value);
            case "f", "float" -> isFloat(value);
            case "d", "double" -> isDouble(value);
            case "bool", "boolean" -> isBoolean(value);
            case "number" -> isNumber(value);
            default -> false;
        };
    }


    private boolean isByte(String value) {
        return parseCheck(value, Byte::valueOf);
    }

    private boolean isShort(String value) {
        return parseCheck(value, Short::valueOf);
    }

    private boolean isInt(String value) {
        return parseCheck(value, Integer::valueOf);
    }

    private boolean isLong(String value) {
        return parseCheck(value, Long::valueOf);
    }

    private boolean isFloat(String value) {
        return parseCheck(value, Float::valueOf);
    }

    private boolean isDouble(String value) {
        return parseCheck(value, Double::valueOf);
    }

    private boolean isBoolean(String value) {
        return value != null && (
                value.equalsIgnoreCase("true") ||
                        value.equalsIgnoreCase("false")
        );
    }

    private <T> boolean parseCheck(String value, Function<String, T> parser) {
        if (value == null || value.isEmpty()) return false;
        try {
            parser.apply(value.trim());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isNumber(String value) {
        return isByte(value) || isShort(value) || isInt(value) ||
                isLong(value) || isFloat(value) || isDouble(value);
    }
}
