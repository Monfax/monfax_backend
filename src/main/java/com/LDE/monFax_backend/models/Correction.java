package com.LDE.monFax_backend.models;

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
    public class Correction extends Resource{

        private Double price;

        @OneToOne
        @JoinColumn(name = "exam_id", referencedColumnName = "id")
        @JsonManagedReference
        private Exam exam;

        @OneToMany(mappedBy = "correction", cascade = CascadeType.ALL, orphanRemoval = true)
        @JsonManagedReference
        private List<Payment> payments = new ArrayList<>();




    }
