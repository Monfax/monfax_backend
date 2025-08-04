package com.LDE.monFax_backend.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LectureCourse extends Resource {

    private String description;

    private Double price ;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @OneToMany(mappedBy = "lectureCourse", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Payment> payments = new ArrayList<>();
}
