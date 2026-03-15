package com.student.api.config;

import com.student.api.domain.port.in.StudentUseCase;
import com.student.api.domain.port.out.StudentRepositoryPort;
import com.student.api.domain.service.StudentService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean
    public StudentUseCase studentUseCase(StudentRepositoryPort repositoryPort) {
        return new StudentService(repositoryPort);
    }
}
