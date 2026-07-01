package com.crabapple.spring.IOC.BeanDefinition.annotion;

import java.lang.annotation.*;

@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Order {
    int value();
}
