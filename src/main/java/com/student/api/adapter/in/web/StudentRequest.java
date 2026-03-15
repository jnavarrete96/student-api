package com.student.api.adapter.in.web;

import com.student.api.domain.model.Student;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class StudentRequest {
    @NotBlank(message = "Id is required")
    private String id;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
    private String lastName;

    @NotNull(message = "Status is required")
    private Student.StudentStatus status;

    @NotNull(message = "Age is required")
    @Min(value = 0, message = "Age must be greater than or equal to 0")
    @Max(value = 120, message = "Age must be less than or equal to 120")
    private Integer age;

    public Student toDomain() {
        return Student.builder()
                .id(this.id.trim())
                .name(this.name.trim())
                .lastName(this.lastName.trim())
                .status(this.status)
                .age(this.age)
                .build();
    }
}
