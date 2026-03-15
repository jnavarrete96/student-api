package com.student.api.domain.port.out;

import com.student.api.domain.model.Student;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface StudentRepositoryPort {

    Mono<Boolean> existsById(String id);

    Mono<Student> save(Student student);

    Flux<Student> findAllActive();
}
