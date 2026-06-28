package com.crabapple.spring.IOC.BeanDefinition.exception;

public class BeanCreateionException extends BeansException{
    public BeanCreateionException(){}

    public BeanCreateionException(String message){
        super(message);
    }
    public BeanCreateionException(Throwable cause){
        super(cause);
    }
    public BeanCreateionException(String message,Throwable cause){
        super(message,cause);
    }
}
