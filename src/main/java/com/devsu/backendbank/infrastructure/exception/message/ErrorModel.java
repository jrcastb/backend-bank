package com.devsu.backendbank.infrastructure.exception.message;

import lombok.Getter;

import java.util.List;

@Getter
public class ErrorModel {

    private String timestamp;
    private Integer status;
    private String code;
    private String title;
    private String instance;
    private String message;
    private String detail;
    private String type;
    private String path;
    private String traceId;
    private List<FieldErrorItem> fieldErrors;

    public ErrorModel timestamp(String timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public ErrorModel status(Integer status) {
        this.status = status;
        return this;
    }

    public ErrorModel code(String code) {
        this.code = code;
        return this;
    }

    public ErrorModel title(String title) {
        this.title = title;
        return this;
    }

    public ErrorModel instance(String instance) {
        this.instance = instance;
        return this;
    }

    public ErrorModel message(String message) {
        this.message = message;
        return this;
    }

    public ErrorModel detail(String detail) {
        this.detail = detail;
        return this;
    }

    public ErrorModel type(String type) {
        this.type = type;
        return this;
    }

    public ErrorModel path(String path) {
        this.path = path;
        return this;
    }

    public ErrorModel traceId(String traceId) {
        this.traceId = traceId;
        return this;
    }

    public ErrorModel fieldErrors(List<FieldErrorItem> fieldErrors) {
        this.fieldErrors = fieldErrors;
        return this;
    }

    @Getter
    public static class FieldErrorItem {
        private String field;
        private String message;

        public FieldErrorItem field(String field) {
            this.field = field;
            return this;
        }

        public FieldErrorItem message(String message) {
            this.message = message;
            return this;
        }
    }
}
