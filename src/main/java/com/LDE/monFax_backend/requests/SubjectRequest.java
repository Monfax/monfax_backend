package com.LDE.monFax_backend.requests;

import lombok.Data;

@Data
public class SubjectRequest {
    private String name;
    private Double price;
    private Long semesterId;
}
