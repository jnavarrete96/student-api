package com.student.api.domain.service;

import com.student.api.domain.exception.DuplicateStudentException;
import com.student.api.domain.model.Student;
import com.student.api.domain.port.in.StudentUseCase;
import com.student.api.domain.port.out.StudentRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public class StudentService implements StudentUseCase {

    private final StudentRepositoryPort repositoryPort;

    @Override
    public Mono<Void> saveStudent(Student student) {
        log.debug("Attempting to save student with id: {}", student.getId());

        return repositoryPort.existsById(student.getId())
                .flatMap(exists -> {
                    if (exists) {
                        log.warn("Student with id '{}' already exists, save rejected", student.getId());
                        return Mono.error(new DuplicateStudentException(student.getId()));
                    }
                    return repositoryPort.save(student)
                            .doOnSuccess(s -> log.info("Student saved successfully with id: {}", s.getId()))
                            .then();
                });
    }

    @Override
    public Flux<Student> getActiveStudents() {
        log.debug("Fetching all students with status ACTIVE");
        return repositoryPort.findAllActive()
                .doOnComplete(() -> log.debug("Active students query completed"));
    }
}
