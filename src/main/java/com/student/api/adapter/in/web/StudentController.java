package com.student.api.adapter.in.web;

import com.student.api.domain.port.in.StudentUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentUseCase studentUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Void> saveStudent(@Valid @RequestBody StudentRequest request) {
        log.debug("POST /api/students - saving student with id: {}", request.getId());
        return studentUseCase.saveStudent(request.toDomain());
    }

    @GetMapping("/active")
    public Mono<ApiResponse<StudentResponse>> getActiveStudents() {
        log.debug("GET /api/students/active - fetching active students");
        return studentUseCase.getActiveStudents()
                .map(StudentResponse::fromDomain)
                .collectList()
                .map(students -> ApiResponse.ok(
                        students,
                        students.isEmpty()
                                ? "No active students found"
                                : "Active students retrieved successfully"
                ));
    }
}