package com.crabapple.spring.IOC.ResourceResolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

//资源解析器类，根据资源类查找出文件列表
//解析jar包和路径数据
public class ResourceResolver {
    private String basepackage;
    Logger logger = LoggerFactory.getLogger(getClass());
    public ResourceResolver(String packagename){
        this.basepackage=packagename;

    }

    public <T> List<T> scan(Function<Resource,T> mapper){
        String basepath=basepackage.replace(".","/");
        try{
            List<T> collector=new ArrayList<>();
            scan0(basepath,basepath,collector,mapper);
            return collector;
        }catch(IOException e){
            throw new UncheckedIOException(e);
        }catch(URISyntaxException e){
            throw new RuntimeException(e);
        }
//        ResourceResolver rr=new ResourceResolver("origin.package");
//        List<String> classList=rr.scan(resource -> {
//            String name=resource.name();
//            if(name.endsWith(".class")){
//                return name.substring(0,name.length()-6).replace("/",".").replace("\\",".");
//            }
//            return null;
//        });
//        return null;
    }
    <T> void scan0(String basePackagePath,String path,List<T> collector,Function<Resource,T> mapper) throws IOException, URISyntaxException {
       logger.atDebug().log("Scan path:{}",path);
        Enumeration<URL> en=getContextClassLoader().getResources(path);
        if(en.hasMoreElements()){
            URL url=en.nextElement();
            URI uri=url.toURI();
            String uriStr=removeTrailingSlash(uriToString(uri));
            String uriBaseStr = uriStr.substring(0, uriStr.length() - basePackagePath.length());
            if (uriBaseStr.startsWith("file:")) {
                uriBaseStr = uriBaseStr.substring(5);
            }
            if (uriStr.startsWith("jar:")) {
                scanFile(true, uriBaseStr, jarUriToPath(basePackagePath, uri), collector, mapper);
            } else {
                scanFile(false, uriBaseStr, Paths.get(uri), collector, mapper);
            }
        }
    }
    ClassLoader getContextClassLoader(){
        ClassLoader cl=null;
        cl=Thread.currentThread().getContextClassLoader();
        if(cl==null){
            cl=getClass().getClassLoader();
        }
        return cl;
    }
    Path jarUriToPath(String basepath, URI jarURI) throws IOException {
        return FileSystems.newFileSystem(jarURI, Map.of()).getPath(basepath);
    }
    <T> void scanFile(boolean isJar,String base,Path root,List<T> collector,Function<Resource,T> mapper) throws IOException {
        String basedir=removeTrailingSlash(base);
        Files.walk(root).filter(Files::isRegularFile).forEach(file->{
            Resource r=null;
            if(isJar){
                r=new Resource(basedir,removeLeadingSlash(file.toString()));
            }else{
                String path=file.toString();
                String name=removeLeadingSlash(path.toString().substring(basedir.length()));
                r=new Resource("file:"+path,name);
            }
            logger.atDebug().log("found resource:{}",r);
            T t=mapper.apply(r);
            if(r!=null){
                collector.add(t);
            }
        });
    }
//    把uri解码成可读的字符串
    String uriToString(URI uri){
        return URLDecoder.decode(uri.toString(), StandardCharsets.UTF_8);
    }

    String removeLeadingSlash(String s) {
        if (s.startsWith("/") || s.startsWith("\\")) {
            s = s.substring(1);
        }
        return s;
    }

    String removeTrailingSlash(String s) {
        if (s.endsWith("/") || s.endsWith("\\")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

}
