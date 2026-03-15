package com.student.api.adapter.out.persistence;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("student")
public class StudentEntity {
    @Id
    @Column("id")
    private String id;

    @Column("name")
    private String name;

    @Column("last_name")
    private String lastName;

    @Column("status")
    private String status;

    @Column("age")
    private Integer age;
}
