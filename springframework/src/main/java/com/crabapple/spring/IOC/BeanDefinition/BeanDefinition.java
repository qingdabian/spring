package com.crabapple.spring.IOC.BeanDefinition;

import lombok.Data;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
/*
保存bean对象的相关信息
 */
@Data
public class BeanDefinition {
    String name;
    Class<?> beanClass;
    /*bean实例对象*/
    Object instance=null;
    Constructor<?> constructor;
    String factoryname;
    Method factoryMethod;
    int order;
    boolean primary;
    String initMethodName;
    String destoryMethodName;
    Method initMethod;
    Method destoryMethod;
}
