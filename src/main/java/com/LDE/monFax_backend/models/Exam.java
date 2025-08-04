package com.LDE.monFax_backend.models;

import com.LDE.monFax_backend.enumerations.ExamType;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;



@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Exam extends Resource {

    private ExamType type;

    private int year;


    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "subject_id")
    private Subject subject;


    @OneToOne(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference
    private Correction correction;

}
