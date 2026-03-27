package com.taskmanager.exception.domain;

public class BadStatusProjectException extends RuntimeException{
    public BadStatusProjectException(String message){
        super(String.format("Статус проекта %s", message));
    }
}
