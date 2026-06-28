package com.crabapple.spring.IOC.BeanDefinition.annotion;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface ComponentScan {
    String[] value() default {};
}
