package com.crabapple.spring.IOC.BeanDefinition.annotion;


import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Value {
    String value();
}
