package com.crabapple.spring.IOC.BeanDefinition;

import com.crabapple.spring.IOC.BeanDefinition.annotion.ComponentScan;
import com.crabapple.spring.IOC.BeanDefinition.exception.NoUniqueBeanDefinitionException;
import com.crabapple.spring.IOC.BeanDefinition.util.ClassUtils;
import com.crabapple.spring.IOC.PropertyResolver.PropertyResolver;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class AnnotationConfigApplicationContext {
     Map<String,BeanDefinition> beans;

     public AnnotationConfigApplicationContext(Class<?> configClass, PropertyResolver propertyResolver){
         Set<String> beanClassNames=scanForClassNames(configClass);
         this.beans=createBeanDefinitions(beanClassNames);
     }
     private Set<String> scanForClassNames(Class<?> config){
         ComponentScan scan= ClassUtils.findAnnotation(config, ComponentScan.class);



     }
     @Nullable
    public BeanDefinition findBeanDefinition(String name){
         return this.beans.get(name);
     }

     public List<BeanDefinition> findBeanDefinitions(Class<?> type){
         return beans.values().stream().filter(b->type.isAssignableFrom(b.getBeanClass()))
                 .sorted().collect(Collectors.toList());
     }

     @Nullable
     public BeanDefinition findBeanDefinition(Class<?> type){
         List<BeanDefinition> defs=findBeanDefinitions(type);
         if(defs.isEmpty()){
             return null;
         }
         if(defs.size()==1){
             return defs.get(0);
         }
         List<BeanDefinition> pdefs=defs.stream().filter(def->def.isPrimary()).toList();
         if(pdefs.size()==1){
             return pdefs.get(0);
         }
         if (pdefs.isEmpty()) { // 不存在@Primary
             throw new NoUniqueBeanDefinitionException(String.format("Multiple bean with type '%s' found, but no @Primary specified.", type.getName()));
         } else { // @Primary不唯一
             throw new NoUniqueBeanDefinitionException(String.format("Multiple bean with type '%s' found, and multiple @Primary specified.", type.getName()));
         }
     }
}
