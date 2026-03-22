package com.a3m.studyassistant.backend.common.exceptions;

import com.a3m.studyassistant.backend.features.user.UserNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public void userNotFoundException(UserNotFoundException ex) {
        System.out.println(ex.getMessage());
    }

}
