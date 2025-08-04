package com.LDE.monFax_backend.controllers;

import com.LDE.monFax_backend.enumerations.PaymentStatus;
import com.LDE.monFax_backend.services.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/buy/videos/{id}")
    public ResponseEntity<String> buyVideo(@PathVariable Long id) {
        String invoiceUrl = paymentService.buyVideo(id);
        return ResponseEntity.ok(invoiceUrl);
    }

    @PostMapping("/buy/subjects/{id}")
    public ResponseEntity<String> buySubject(@PathVariable Long id) {
        String invoiceUrl = paymentService.buySubject(id);
        return ResponseEntity.ok(invoiceUrl);
    }

    @PostMapping("/buy/semesters/{id}")
    public ResponseEntity<String> buySemester(@PathVariable Long id) {
        String invoiceUrl = paymentService.buySemester(id);
        return ResponseEntity.ok(invoiceUrl);
    }

    @PostMapping("/buy/corrections/{id}")
    public ResponseEntity<String> buyCorrection(@PathVariable Long id) {
        String invoiceUrl = paymentService.buyCorrection(id);
        return ResponseEntity.ok(invoiceUrl);
    }

    @PostMapping("/buy/lecture-courses/{id}")
    public ResponseEntity<String> buyLectureCourse(@PathVariable Long id) {
        String invoiceUrl = paymentService.buyLectureCourse(id);
        return ResponseEntity.ok(invoiceUrl);
    }

    @PostMapping("/ipn")
    public ResponseEntity<String> handleIpn(@RequestParam Map<String, String> payload) {
        log.info("IPN reçu : {}", payload);

        String status = payload.get("data[status]");
        String invoiceToken = payload.get("data[invoice][token]");

        if (status != null && invoiceToken != null) {
            PaymentStatus newStatus = switch (status.toLowerCase()) {
                case "completed" -> PaymentStatus.COMPLETED;
                case "cancelled", "expired", "failed" -> PaymentStatus.FAILED;
                default -> PaymentStatus.PENDING;
            };

            try {
                paymentService.updatePaymentStatus(invoiceToken, newStatus);
                return ResponseEntity.ok("IPN traité avec succès");
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur : " + e.getMessage());
            }
        }

        return ResponseEntity.badRequest().body("Données IPN incomplètes");
    }

}
