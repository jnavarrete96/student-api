package com.student.api.domain.exception;

public class DuplicateStudentException extends RuntimeException {
    public DuplicateStudentException(String id) {
        super("Could not save student: id '" + id + "' already exists");
    }
}
