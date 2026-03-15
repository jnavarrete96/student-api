package com.student.api.adapter.out.persistence;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("student")
public class StudentEntity implements Persistable<String> {
    @Id
    @Column("id")
    private String id;

    @Column("name")
    private String name;

    @Column("last_name")
    private String lastName;

    @Column("status")
    private String status;

    @Transient
    private boolean isNew;

    @Column("age")
    private Integer age;

    @Override
    public boolean isNew() {
        return isNew;
    }
}
