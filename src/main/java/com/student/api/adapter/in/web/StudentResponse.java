package com.student.api.adapter.in.web;

import com.student.api.domain.model.Student;
import com.student.api.domain.model.Student.StudentStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StudentResponse {
    private String id;
    private String name;
    private String lastName;
    private StudentStatus status;
    private Integer age;

    public static StudentResponse fromDomain(Student student) {
        return StudentResponse.builder()
                .id(student.getId())
                .name(student.getName())
                .lastName(student.getLastName())
                .status(student.getStatus())
                .age(student.getAge())
                .build();
    }
}
