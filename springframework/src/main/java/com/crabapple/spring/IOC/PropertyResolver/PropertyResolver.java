package com.crabapple.spring.IOC.PropertyResolver;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.*;
import java.util.*;
import java.util.function.Function;

public class PropertyResolver {
    Logger logger= LoggerFactory.getLogger(getClass());
    Map<Class<?>, Function<String,Object>> converters=new HashMap<>();
    Map<String,String> properties=new HashMap<>();

    //构造函数
    //注册环境变量
    //注册传入参数
    //注册转换器
    //转换器的作用是把读取到的数据转换成目标的类型
    /*
      @Value(${app.port})
      int number;
      这时就需要把map中读取到的String类型转换成int类型
     */
    public PropertyResolver(Properties props){
        this.properties.putAll(System.getenv());
        Set<String> names=props.stringPropertyNames();
        for(String name:names){
            this.properties.put(name,props.getProperty(name));
        }
        if(logger.isDebugEnabled()){
            List<String> keys=new ArrayList(this.properties.keySet());
            Collections.sort(keys);
            for(String key:keys){
                String value=this.properties.get(key);
                logger.atDebug().log("解析器解析到参数："+key+"  值："+value);
            }
        }

        converters.put(String.class, s -> s);
        converters.put(boolean.class, s -> Boolean.parseBoolean(s));
        converters.put(Boolean.class, s -> Boolean.valueOf(s));

        converters.put(byte.class, s -> Byte.parseByte(s));
        converters.put(Byte.class, s -> Byte.valueOf(s));

        converters.put(short.class, s -> Short.parseShort(s));
        converters.put(Short.class, s -> Short.valueOf(s));

        converters.put(int.class, s -> Integer.parseInt(s));
        converters.put(Integer.class, s -> Integer.valueOf(s));

        converters.put(long.class, s -> Long.parseLong(s));
        converters.put(Long.class, s -> Long.valueOf(s));

        converters.put(float.class, s -> Float.parseFloat(s));
        converters.put(Float.class, s -> Float.valueOf(s));

        converters.put(double.class, s -> Double.parseDouble(s));
        converters.put(Double.class, s -> Double.valueOf(s));

        converters.put(LocalDate.class, s -> LocalDate.parse(s));
        converters.put(LocalTime.class, s -> LocalTime.parse(s));
        converters.put(LocalDateTime.class, s -> LocalDateTime.parse(s));
        converters.put(ZonedDateTime.class, s -> ZonedDateTime.parse(s));
        converters.put(Duration.class, s -> Duration.parse(s));
        converters.put(ZoneId.class, s -> ZoneId.of(s));
    }

    public boolean containsProperty(String key){
        return this.properties.containsKey(key);
    }

    //注册类型转换器
    public void registerConverter(Class<?> clazz,Function<String,Object> function){
        converters.put(clazz,function);
    }


    /*
    获取属性
     */
    @Nullable
    public String getProperty(String key){
        PropertyExpr pe=parsePropertyExpr(key);
        if(pe!=null){
            if(pe.defaultValue()!=null){
                return getProperty(pe.key(),pe.defaultValue());
            }else{
                return getRequiredProperty(pe.key());
            }
        }
        String value=this.properties.get(key);
        if(value!=null){
            return parseValue(value);
        }
        return value;
    }

    /*
    解析包含默认值的注入
     */
    private String getProperty(String key,String defaultvalue){
        String value=this.properties.get(key);
        if(value!=null){
            return value;
        }else{
            return parseValue(defaultvalue);
        }
    }
    /*
    使用转换器转换成目标类型
     */
    public <T> T getProperty(String key,Class<T> clazz){
        String value=this.properties.get(key);
        if(value==null){
            return null;
        }else{
            return convert(clazz,value);
        }
    }

    public <T> T getProperty(String key,Class<T> clazz,T defaultvalue){
        String value=this.properties.get(key);
        if(value==null){
            return defaultvalue;
        }else{
            return convert(clazz,value);
        }
    }

    private <T> T convert(Class<T> clazz,String value){
        Function<String,Object> func=converters.get(clazz);
        if(func==null){
            throw new IllegalArgumentException("Unsupported value type: " + clazz.getName());
        }else{
            Object o=func.apply(value);
            return (T)o;
        }
    }

    public String getRequiredProperty(String key) {
        String value = getProperty(key);
        return Objects.requireNonNull(value, "Property '" + key + "' not found.");
    }

    public <T> T getRequiredProperty(String key, Class<T> targetType) {
        T value = getProperty(key, targetType);
        return Objects.requireNonNull(value, "Property '" + key + "' not found.");
    }
    /*
    判断value是否是嵌套的格式
     */
    String parseValue(String value) {
        PropertyExpr expr = parsePropertyExpr(value);
        if (expr == null) {
            return value;
        }
        if (expr.defaultValue() != null) {
            return getProperty(expr.key(), expr.defaultValue());
        } else {
            return getRequiredProperty(expr.key());
        }
    }

    PropertyExpr parsePropertyExpr(String key){
        if(key.startsWith("${")&&key.endsWith("}")){
            int n=key.indexOf(":");
            if(n==(-1)){
               String k=key.substring(2,key.length()-1);
               return new PropertyExpr(key,null);
            }else{
                String k=key.substring(2,n);
                return new PropertyExpr(k,key.substring(n+1,key.length()-1));
            }
        }
        return null;
    }
    String notEmpty(String key) {
        if (key.isEmpty()) {
            throw new IllegalArgumentException("Invalid key: " + key);
        }
        return key;
    }

}
