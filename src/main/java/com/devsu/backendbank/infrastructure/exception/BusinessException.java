package com.devsu.backendbank.infrastructure.exception;

import com.devsu.backendbank.infrastructure.exception.message.BusinessErrorMessage;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class BusinessException extends RuntimeException {
    final BusinessErrorMessage businessErrorMessage;
    final String extra;

    public BusinessException(BusinessErrorMessage businessErrorMessage, String extra){
        super(businessErrorMessage.getDetail());
        this.businessErrorMessage = businessErrorMessage;
        this.extra = extra;
    }

    public BusinessException(BusinessErrorMessage businessErrorMessage){
        super(businessErrorMessage.getDetail());
        this.businessErrorMessage = businessErrorMessage;
        this.extra = "";
    }
}
