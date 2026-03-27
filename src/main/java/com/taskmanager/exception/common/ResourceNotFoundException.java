package com.taskmanager.exception.common;

public class ResourceNotFoundException extends RuntimeException{
    public ResourceNotFoundException(String message){
        super(message);
    }
    // Конкретный пользователь не найден по Id.
    public ResourceNotFoundException(String message, Long id){
        super(String.format("%s с id %d не найден", message, id));
    }

}
