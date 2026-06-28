package com.crabapple.spring.IOC.BeanDefinition.util;

import com.crabapple.spring.IOC.BeanDefinition.annotion.Bean;
import com.crabapple.spring.IOC.BeanDefinition.annotion.Component;
import com.crabapple.spring.IOC.BeanDefinition.exception.BeanDefinitionException;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ClassUtils {
    public static <A extends Annotation> A findAnnotation(Class<?> target,Class<A> annoClass){
        A a=target.getAnnotation(annoClass);
        for(Annotation anno:target.getAnnotations()){
            Class<? extends Annotation> annoType=anno.annotationType();
            if(!annoType.getPackageName().equals("java.lang.annotation")){
                A found=findAnnotation(annoType,annoClass);
                if(found!=null){
                    if(a!=null){
                        throw new BeanDefinitionException("Duplicate @" + annoClass.getSimpleName() + " found on class " + target.getSimpleName());
                    }
                    a=found;
                }
            }
        }
        return a;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <A extends Annotation> A getAnnoation(Annotation[] annos,Class<A> annoClass){
        for(Annotation anno:annos){
            if(annoClass.isInstance(anno)){
                return (A) anno;
            }
        }
        return null;
    }

    public static String getBeanName(Method method){
        Bean bean=method.getAnnotation(Bean.class);
        String name=bean.value();
        if(name.isEmpty()){
            name=method.getName();
        }
        return name;
    }

    public static String getBeanName(Class<?> clazz){
        String name="";
        Component component=clazz.getAnnotation(Component.class);
        if(component!=null){
            name=component.value();
        }else{
            for(Annotation anno:clazz.getAnnotations()){
                if(findAnnotation(anno.annotationType(), Component.class)!=null){
                    try{
                        name=(String) anno.annotationType().getMethod("value").invoke(anno);
                    }catch(ReflectiveOperationException e){
                        throw new BeanDefinitionException("Cannot get annotation value",e);
                    }
                }
            }
        }
        if(name.isEmpty()){
            name=clazz.getSimpleName();
            name=Character.toLowerCase(name.charAt(0))+name.substring(1);
        }
        return name;
    }

    @Nullable
    public static Method findAnnotationMethod(Class<?> clazz,Class<? extends Annotation> annoClass){
        List<Method> msgs= Arrays.stream(clazz.getDeclaredMethods()).filter(msg->msg.isAnnotationPresent(annoClass))
                .map(m->{
                    if(m.getParameterCount()!=0){
                        throw new BeanDefinitionException( String.format("Method '%s' with @%s must not have argument: %s", m.getName(), annoClass.getSimpleName(), clazz.getName()));
                    }
                    return m;
                }).collect(Collectors.toList());
        if(msgs.isEmpty()){
            return null;
        }
        if(msgs.size()==1){
            return msgs.get(0);
        }
        throw new BeanDefinitionException(String.format("Multiple methods with @%s found in class: %s", annoClass.getSimpleName(), clazz.getName()));
    }

    public static Method getNameMethod(Class<?> clazz,String name){
        try{
            return clazz.getDeclaredMethod(name);
        }catch (ReflectiveOperationException e){
            throw new BeanDefinitionException(String.format("Method '%s' not found in class: %s", name, clazz.getName()));
        }
    }
}
