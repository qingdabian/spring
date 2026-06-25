package com.crabapple.spring.IOC.PropertyResolver.util;

import com.crabapple.spring.IOC.PropertyResolver.InputStreamCallback;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Function;

public class ClassPathUtils {
    public static <T> T readInputStream(String path, InputStreamCallback<T> callback) {
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        //这个部分使用类加载器加载配置文件，加载的是classpath中的配置文件，而不是使用FileInputStream等加载磁盘文件
        //加载磁盘文件的方式很可能因为主机的不同导致找不到文件，配置文件一般都是写在resource中的，使用类加载器是可以查看到的
        try(InputStream input=getContextClassLoader().getResourceAsStream(path)){
            if(input==null){
                throw new FileNotFoundException("找不到配置文件"+path);
            }
            T t=callback.doWithInputStream(input);
            return t;
        }catch(IOException e){
            e.printStackTrace();
            throw new UncheckedIOException(e);
        }
    }

    public static String readString(String path){
        return readInputStream(path,(input)->{
            byte[] bytes=input.readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        });
    }

    private static ClassLoader getContextClassLoader(){
        ClassLoader cl=null;
        cl=Thread.currentThread().getContextClassLoader();
        if(cl==null){
            cl = ClassPathUtils.class.getClassLoader();
        }
        return cl;
    }
}
