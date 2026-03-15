package com.student.api.domain.port.in;

import com.student.api.domain.model.Student;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface StudentUseCase {

    Mono<Void> saveStudent(Student student);

    Flux<Student> getActiveStudents();
}
