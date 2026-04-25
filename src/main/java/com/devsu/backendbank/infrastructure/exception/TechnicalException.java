package com.devsu.backendbank.infrastructure.exception;

import com.devsu.backendbank.infrastructure.exception.message.TechnicalErrorMessage;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class TechnicalException extends RuntimeException{
    final TechnicalErrorMessage technicalErrorMessage;

    public TechnicalException(Throwable cause, TechnicalErrorMessage technicalErrorMessage){
        super(technicalErrorMessage.getDetail(), cause);
        this.technicalErrorMessage = technicalErrorMessage;
    }

    public TechnicalException(TechnicalErrorMessage technicalErrorMessage){
        super(technicalErrorMessage.getDetail());
        this.technicalErrorMessage = technicalErrorMessage;
    }
}
