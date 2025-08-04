package com.LDE.monFax_backend.models;

import com.LDE.monFax_backend.enumerations.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true)
    private String invoiceToken;

    private Double amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private LocalDateTime createdAt;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "video_id")
    private Video video;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "correction_id")
    private Correction correction;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "lectureCourse_id")
    private LectureCourse lectureCourse;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "semester_id")
    private Semester semester;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}

