package com.student.api.adapter.out.persistence;

import com.student.api.domain.model.Student;
import com.student.api.domain.model.Student.StudentStatus;
import com.student.api.domain.port.out.StudentRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class StudentRepositoryAdapter implements StudentRepositoryPort {

    private final StudentR2dbcRepository r2dbcRepository;

    @Override
    public Mono<Boolean> existsById(String id) {
        return r2dbcRepository.existsById(id);
    }

    @Override
    public Mono<Student> save(Student student) {
        StudentEntity entity = toEntity(student);
        entity.setNew(true);
        return r2dbcRepository.save(entity)
                .map(this::toDomain);
    }

    @Override
    public Flux<Student> findAllActive() {
        return r2dbcRepository.findByStatus(StudentStatus.ACTIVE.name())
                .map(this::toDomain);
    }

    private StudentEntity toEntity(Student student) {
        return StudentEntity.builder()
                .id(student.getId())
                .name(student.getName())
                .lastName(student.getLastName())
                .status(student.getStatus().name())
                .age(student.getAge())
                .build();
    }

    private Student toDomain(StudentEntity entity) {
        return Student.builder()
                .id(entity.getId())
                .name(entity.getName())
                .lastName(entity.getLastName())
                .status(StudentStatus.valueOf(entity.getStatus()))
                .age(entity.getAge())
                .build();
    }
}
