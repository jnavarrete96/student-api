package com.student.api.adapter.out;

import com.student.api.adapter.out.persistence.StudentEntity;
import com.student.api.adapter.out.persistence.StudentR2dbcRepository;
import com.student.api.adapter.out.persistence.StudentRepositoryAdapter;
import com.student.api.domain.model.Student;
import com.student.api.domain.model.Student.StudentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import reactor.test.StepVerifier;

@DataR2dbcTest
@Import(StudentRepositoryAdapter.class)
@DisplayName("StudentRepositoryAdapter unit tests")
class StudentRepositoryAdapterTest {

    @Autowired
    private StudentRepositoryAdapter repositoryAdapter;

    @Autowired
    private StudentR2dbcRepository r2dbcRepository;

    @BeforeEach
    void setUp() {
        r2dbcRepository.deleteAll().block();
    }

    // ── existsById ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("existsById — should return true when student exists")
    void existsById_whenStudentExists_shouldReturnTrue() {
        StudentEntity entity = buildEntity("id-001", "ACTIVE");
        r2dbcRepository.save(entity).block();

        StepVerifier.create(repositoryAdapter.existsById("id-001"))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    @DisplayName("existsById — should return false when student does not exist")
    void existsById_whenStudentNotExists_shouldReturnFalse() {
        StepVerifier.create(repositoryAdapter.existsById("non-existent"))
                .expectNext(false)
                .verifyComplete();
    }

    // ── save ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("save — should persist student and return domain object")
    void save_shouldPersistAndReturnStudent() {
        Student student = buildStudent("id-002", StudentStatus.ACTIVE);

        StepVerifier.create(repositoryAdapter.save(student))
                .expectNextMatches(saved ->
                        saved.getId().equals("id-002") &&
                                saved.getName().equals("John") &&
                                saved.getStatus() == StudentStatus.ACTIVE)
                .verifyComplete();

        StepVerifier.create(r2dbcRepository.existsById("id-002"))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    @DisplayName("save — should correctly map all fields to domain")
    void save_shouldMapAllFieldsCorrectly() {
        Student student = buildStudent("id-003", StudentStatus.INACTIVE);

        StepVerifier.create(repositoryAdapter.save(student))
                .expectNextMatches(saved ->
                        saved.getId().equals("id-003") &&
                                saved.getName().equals("John") &&
                                saved.getLastName().equals("Doe") &&
                                saved.getStatus() == StudentStatus.INACTIVE &&
                                saved.getAge().equals(20))
                .verifyComplete();
    }

    // ── findAllActive ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("findAllActive — should return only active students")
    void findAllActive_shouldReturnOnlyActiveStudents() {
        r2dbcRepository.save(buildEntity("id-004", "ACTIVE")).block();
        r2dbcRepository.save(buildEntity("id-005", "ACTIVE")).block();
        r2dbcRepository.save(buildEntity("id-006", "INACTIVE")).block();

        StepVerifier.create(repositoryAdapter.findAllActive())
                .expectNextMatches(s -> s.getId().equals("id-004"))
                .expectNextMatches(s -> s.getId().equals("id-005"))
                .verifyComplete();
    }

    @Test
    @DisplayName("findAllActive — should return empty when no active students")
    void findAllActive_whenNoActiveStudents_shouldReturnEmpty() {
        r2dbcRepository.save(buildEntity("id-007", "INACTIVE")).block();

        StepVerifier.create(repositoryAdapter.findAllActive())
                .verifyComplete();
    }

    @Test
    @DisplayName("findAllActive — should return empty when no students at all")
    void findAllActive_whenNoStudents_shouldReturnEmpty() {
        StepVerifier.create(repositoryAdapter.findAllActive())
                .verifyComplete();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private StudentEntity buildEntity(String id, String status) {
        return StudentEntity.builder()
                .id(id)
                .name("John")
                .lastName("Doe")
                .status(status)
                .age(20)
                .isNew(true)
                .build();
    }

    private Student buildStudent(String id, StudentStatus status) {
        return Student.builder()
                .id(id)
                .name("John")
                .lastName("Doe")
                .status(status)
                .age(20)
                .build();
    }
}