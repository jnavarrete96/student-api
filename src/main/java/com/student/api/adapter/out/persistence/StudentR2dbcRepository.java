package com.student.api.adapter.out.persistence;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface StudentR2dbcRepository extends R2dbcRepository<StudentEntity,String> {
    Flux<StudentEntity> findByStatus(String status);
}
