package com.taskmanager.exception.domain;

public class UserByEmailNotFoundException extends RuntimeException{
    // Конкретный пользователь не найдет по Email
    public UserByEmailNotFoundException(String email){
        super(String.format("Пользователь с email %s не найден", email));
    }
}
