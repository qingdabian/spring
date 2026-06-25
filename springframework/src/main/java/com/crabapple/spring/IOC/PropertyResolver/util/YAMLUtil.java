package com.crabapple.spring.IOC.PropertyResolver.util;

import java.io.FileNotFoundException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.resolver.Resolver;

@SuppressWarnings("unused")
public class YAMLUtil {
    @SuppressWarnings("unchecked")
    public static Map<String, Object> loadYaml(String path) {
        var loaderOptions = new LoaderOptions();
        var dumperOptions = new DumperOptions();
        var representer = new Representer(dumperOptions);
        var resolver = new NoImplicitResolver();
        var yaml = new Yaml(new Constructor(loaderOptions), representer, dumperOptions, loaderOptions, resolver);
        return ClassPathUtils.readInputStream(path, (input) -> {
            return (Map<String, Object>) yaml.load(input);
        });
    }

    public Map<String,Object> loadYamlAsMap(String path){
      Map<String,Object> data=loadYaml(path);
      Map<String,Object> plain=new LinkedHashMap<>();
      convertTo(data,"",plain);
      return plain;
    }

    private void convertTo(Map<String,Object> data,String prefix,Map<String,Object> plain){
        for(String key:data.keySet()){
            Object obj=data.get(key);
            if(obj instanceof Map){
                convertTo((Map<String,Object>)obj,prefix+key+".",plain);
            }else if(obj instanceof List){
                plain.put(prefix+key,obj);
            }else{
                plain.put(prefix+key,obj.toString());
            }
        }
    }

}
class NoImplicitResolver extends Resolver {

    public NoImplicitResolver() {
        super();
        super.yamlImplicitResolvers.clear();
    }
}

