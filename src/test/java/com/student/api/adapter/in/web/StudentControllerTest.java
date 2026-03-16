package com.student.api.adapter.in.web;

import com.student.api.domain.exception.DuplicateStudentException;
import com.student.api.domain.model.Student;
import com.student.api.domain.model.Student.StudentStatus;
import com.student.api.domain.port.in.StudentUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@WebFluxTest(StudentController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("StudentController unit tests")
class StudentControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private StudentUseCase studentUseCase;

    private StudentRequest validRequest;
    private Student student;

    @BeforeEach
    void setUp() {
        validRequest = new StudentRequest();
        validRequest.setId("test-id-001");
        validRequest.setName("John");
        validRequest.setLastName("Doe");
        validRequest.setStatus(StudentStatus.ACTIVE);
        validRequest.setAge(20);

        student = Student.builder()
                .id("test-id-001")
                .name("John")
                .lastName("Doe")
                .status(StudentStatus.ACTIVE)
                .age(20)
                .build();
    }

    // ── POST /api/students ────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/students — should return 201 when student is saved successfully")
    void saveStudent_whenValidRequest_shouldReturn201() {
        when(studentUseCase.saveStudent(any(Student.class))).thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/api/students")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody().isEmpty();

        verify(studentUseCase, times(1)).saveStudent(any(Student.class));
    }

    @Test
    @DisplayName("POST /api/students — should return 409 when student id already exists")
    void saveStudent_whenDuplicateId_shouldReturn409() {
        when(studentUseCase.saveStudent(any(Student.class)))
                .thenReturn(Mono.error(new DuplicateStudentException("test-id-001")));

        webTestClient.post()
                .uri("/api/students")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validRequest)
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.status").isEqualTo(409)
                .jsonPath("$.error").isEqualTo("Conflict")
                .jsonPath("$.message").isNotEmpty();
    }

    @Test
    @DisplayName("POST /api/students — should return 400 when id is blank")
    void saveStudent_whenIdIsBlank_shouldReturn400() {
        validRequest.setId("");

        webTestClient.post()
                .uri("/api/students")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validRequest)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.fieldErrors.id").isNotEmpty();

        verify(studentUseCase, never()).saveStudent(any());
    }

    @Test
    @DisplayName("POST /api/students — should return 400 when name is blank")
    void saveStudent_whenNameIsBlank_shouldReturn400() {
        validRequest.setName("");

        webTestClient.post()
                .uri("/api/students")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validRequest)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.fieldErrors.name").isNotEmpty();

        verify(studentUseCase, never()).saveStudent(any());
    }

    @Test
    @DisplayName("POST /api/students — should return 400 when age is negative")
    void saveStudent_whenAgeIsNegative_shouldReturn400() {
        validRequest.setAge(-1);

        webTestClient.post()
                .uri("/api/students")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validRequest)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.fieldErrors.age").isNotEmpty();

        verify(studentUseCase, never()).saveStudent(any());
    }

    @Test
    @DisplayName("POST /api/students — should return 400 when status is null")
    void saveStudent_whenStatusIsNull_shouldReturn400() {
        validRequest.setStatus(null);

        webTestClient.post()
                .uri("/api/students")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validRequest)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.fieldErrors.status").isNotEmpty();

        verify(studentUseCase, never()).saveStudent(any());
    }

    // ── GET /api/students/active ──────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/students/active — should return 200 with active students")
    void getActiveStudents_whenStudentsExist_shouldReturn200WithData() {
        when(studentUseCase.getActiveStudents()).thenReturn(Flux.just(student));

        webTestClient.get()
                .uri("/api/students/active")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo(200)
                .jsonPath("$.count").isEqualTo(1)
                .jsonPath("$.data[0].id").isEqualTo("test-id-001")
                .jsonPath("$.data[0].name").isEqualTo("John")
                .jsonPath("$.data[0].status").isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("GET /api/students/active — should return 200 with empty list when no active students")
    void getActiveStudents_whenNoStudents_shouldReturn200WithEmptyList() {
        when(studentUseCase.getActiveStudents()).thenReturn(Flux.empty());

        webTestClient.get()
                .uri("/api/students/active")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo(200)
                .jsonPath("$.count").isEqualTo(0)
                .jsonPath("$.message").isEqualTo("No active students found")
                .jsonPath("$.data").isArray();
    }
}