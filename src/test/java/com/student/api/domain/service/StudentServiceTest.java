package com.student.api.domain.service;

import com.student.api.domain.exception.DuplicateStudentException;
import com.student.api.domain.model.Student;
import com.student.api.domain.model.Student.StudentStatus;
import com.student.api.domain.port.out.StudentRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StudentService unit tests")
class StudentServiceTest {

    @Mock
    private StudentRepositoryPort repositoryPort;

    @InjectMocks
    private StudentService studentService;

    private Student student;

    @BeforeEach
    void setUp() {
        student = Student.builder()
                .id("test-id-001")
                .name("John")
                .lastName("Doe")
                .status(StudentStatus.ACTIVE)
                .age(20)
                .build();
    }

    // ── saveStudent ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("saveStudent — should save successfully when id does not exist")
    void saveStudent_whenIdNotExists_shouldSaveSuccessfully() {
        when(repositoryPort.existsById(student.getId())).thenReturn(Mono.just(false));
        when(repositoryPort.save(any(Student.class))).thenReturn(Mono.just(student));

        StepVerifier.create(studentService.saveStudent(student))
                .verifyComplete();

        verify(repositoryPort, times(1)).existsById(student.getId());
        verify(repositoryPort, times(1)).save(student);
    }

    @Test
    @DisplayName("saveStudent — should throw DuplicateStudentException when id already exists")
    void saveStudent_whenIdExists_shouldThrowDuplicateStudentException() {
        when(repositoryPort.existsById(student.getId())).thenReturn(Mono.just(true));

        StepVerifier.create(studentService.saveStudent(student))
                .expectErrorMatches(ex ->
                        ex instanceof DuplicateStudentException &&
                                ex.getMessage().contains(student.getId()))
                .verify();

        verify(repositoryPort, times(1)).existsById(student.getId());
        verify(repositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("saveStudent — should never call save when student is duplicate")
    void saveStudent_whenDuplicate_shouldNeverCallSave() {
        when(repositoryPort.existsById(student.getId())).thenReturn(Mono.just(true));

        StepVerifier.create(studentService.saveStudent(student))
                .expectError(DuplicateStudentException.class)
                .verify();

        verify(repositoryPort, never()).save(any());
    }

    // ── getActiveStudents ─────────────────────────────────────────────────────

    @Test
    @DisplayName("getActiveStudents — should return active students")
    void getActiveStudents_shouldReturnActiveStudents() {
        Student student2 = Student.builder()
                .id("test-id-002")
                .name("Jane")
                .lastName("Smith")
                .status(StudentStatus.ACTIVE)
                .age(22)
                .build();

        when(repositoryPort.findAllActive()).thenReturn(Flux.just(student, student2));

        StepVerifier.create(studentService.getActiveStudents())
                .expectNext(student)
                .expectNext(student2)
                .verifyComplete();

        verify(repositoryPort, times(1)).findAllActive();
    }

    @Test
    @DisplayName("getActiveStudents — should return empty flux when no active students")
    void getActiveStudents_whenNoActiveStudents_shouldReturnEmpty() {
        when(repositoryPort.findAllActive()).thenReturn(Flux.empty());

        StepVerifier.create(studentService.getActiveStudents())
                .verifyComplete();

        verify(repositoryPort, times(1)).findAllActive();
    }
}