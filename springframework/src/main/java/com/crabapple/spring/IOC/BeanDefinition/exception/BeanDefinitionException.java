package com.crabapple.spring.IOC.BeanDefinition.exception;

public class BeanDefinitionException extends BeansException{
    public BeanDefinitionException(){}

    public BeanDefinitionException(String message){
        super(message);
    }
    public BeanDefinitionException(Throwable cause){
        super(cause);
    }
    public BeanDefinitionException(String message,Throwable cause){
        super(message,cause);
    }
}
