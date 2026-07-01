package com.crabapple.spring.IOC.BeanDefinition;

import com.crabapple.spring.IOC.BeanDefinition.annotion.*;
import com.crabapple.spring.IOC.BeanDefinition.exception.BeanDefinitionException;
import com.crabapple.spring.IOC.BeanDefinition.exception.NoUniqueBeanDefinitionException;
import com.crabapple.spring.IOC.BeanDefinition.util.ClassUtils;
import com.crabapple.spring.IOC.PropertyResolver.PropertyResolver;
import com.crabapple.spring.IOC.ResourceResolver.ResourceResolver;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class AnnotationConfigApplicationContext {
     Map<String,BeanDefinition> beans;
     Logger logger= LoggerFactory.getLogger(this.getClass());
     public AnnotationConfigApplicationContext(Class<?> configClass, PropertyResolver propertyResolver){
         Set<String> beanClassNames=scanForClassNames(configClass);
         this.beans=createBeanDefinitions(beanClassNames);
     }
     /*
     扫描配置类中指定的包中包含的类
      */
     private Set<String> scanForClassNames(Class<?> configClass){
         ComponentScan scan= ClassUtils.findAnnotation(configClass, ComponentScan.class);
         String[] scanPackages=scan==null||scan.value().length==0?new String[]{configClass.getPackage().getName()}:scan.value();
         Set<String> classNameSet=new HashSet<>();
         for(String pkg:scanPackages){
           logger.atDebug().log("扫描到包：{}",pkg);
           var rr=new ResourceResolver(pkg);
           List<String> classList=rr.scan(res->{
               String name= res.name();
               if(name.endsWith(".class")){
                   return name.substring(0,name.length()-6).replace("/",".").replace("\\",".");
               }
               return null;
           });
             classNameSet.addAll(classList);
         }
         Import importClass=configClass.getAnnotation(Import.class);
         if(importClass!=null){
             for(Class<?> clazz:importClass.value()){
                 String name=clazz.getName();
                 classNameSet.add(name);
             }
         }
        return classNameSet;
     }
     Map<String,BeanDefinition> createBeanDefinitions(Set<String> classNames){
         Map<String,BeanDefinition> beans=new HashMap<>();
         for(String className:classNames){
             Class<?> clazz=null;
             try{
                 clazz=Class.forName(className);
             }catch(ClassNotFoundException e){
                 e.printStackTrace();
             }
             Component component=ClassUtils.findAnnotation(clazz, Component.class);
             if(component!=null){
                 String beanName=ClassUtils.getBeanName(clazz);
                 var beanDefinition=new BeanDefinition(beanName,clazz,getSuitableConstructor(clazz),
                         getOrder(clazz),clazz.isAnnotationPresent(Primary.class),
                         null,null,
                         ClassUtils.findAnnotationMethod(clazz,PostConstruct.class),
                         ClassUtils.findAnnotationMethod(clazz,PreDestroy.class));
                 addBeanDefinition(beans,beanDefinition);
                 Configuration configuration=ClassUtils.findAnnotation(clazz, Configuration.class);
                 if(configuration!=null){
                     scanFactoryMethods(beanName,clazz,beans);
                 }
             }
         }
         return beans;
     }
     /*
     获取构造函数
      */
     private Constructor<?> getSuitableConstructor(Class<?> clazz){
             Constructor<?>[] constructors=clazz.getConstructors();
             if(constructors.length==0){
                 constructors=clazz.getDeclaredConstructors();
                 if(constructors.length!=1){
                     throw new BeanDefinitionException("More than one constructor found in class"+clazz.getName()+".");
                 }
             }
             if(constructors.length!=1){
                 throw new BeanDefinitionException("More than one constructor found in class"+clazz.getName()+".");
             }
         return constructors[0];
     }

     /*
     解析@Bean标注的工厂方法
      */
     private void scanFactoryMethods(String beanName,Class<?> clazz,Map<String,BeanDefinition> map){
         for(Method method : clazz.getDeclaredMethods()){
             Bean bean=method.getAnnotation(Bean.class);
             if(bean!=null){
                 Class<?> beanClass=method.getReturnType();
                 var beanDefinition=new BeanDefinition(
                         ClassUtils.getBeanName(beanClass),beanClass,
                         beanName,method,
                         getOrder(method),
                         method.isAnnotationPresent(Primary.class),
                         bean.initMethod().isEmpty()?null:bean.initMethod(),
                         bean.destoryMethod().isEmpty()?null:bean.destoryMethod(),
                         null,null
                 );
                 addBeanDefinition(map,beanDefinition);
             }
         }
     }
     private int getOrder(Class<?> clazz){
         Order order=clazz.getDeclaredAnnotation(Order.class);
         return order!=null?order.value():Integer.MAX_VALUE;
     }
     private int getOrder(Method method){
         Order order=method.getAnnotation(Order.class);
         return order!=null?order.value():Integer.MAX_VALUE;
     }
     private void addBeanDefinition(Map<String,BeanDefinition> map,BeanDefinition beanDefinition){
         map.put(beanDefinition.getName(),beanDefinition);
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
