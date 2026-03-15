package com.student.api.domain.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Student {
    private String id;
    private String name;
    private String lastName;
    private StudentStatus status;
    private Integer age;

    public enum StudentStatus {
        ACTIVE, INACTIVE
    }
}
