package com.crabapple.spring.IOC.BeanDefinition.exception;

public class NoUniqueBeanDefinitionException extends BeanDefinitionException{
    public NoUniqueBeanDefinitionException(){}

    public NoUniqueBeanDefinitionException(String message){
        super(message);
    }
    public NoUniqueBeanDefinitionException(Throwable cause){
        super(cause);
    }
    public NoUniqueBeanDefinitionException(String message,Throwable cause){
        super(message,cause);
    }

}
