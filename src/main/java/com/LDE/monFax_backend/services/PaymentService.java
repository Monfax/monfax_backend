package com.LDE.monFax_backend.services;

import com.LDE.monFax_backend.enumerations.PaymentStatus;
import com.LDE.monFax_backend.exception.PaymentException;
import com.LDE.monFax_backend.models.*;
import com.LDE.monFax_backend.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final WebClient webClient;

    private final AuthService authService;
    private final PaymentRepository paymentRepository;
    private final VideoRepository videoRepository;
    private final SubjectRepository subjectRepository;
    private final SemesterRepository semesterRepository;
    private final CorrectionRepository correctionRepository;
    private final LectureCourseRepository lectureCourseRepository;

    @Value("${paydunya.master-key}")
    private String masterKey;

    @Value("${paydunya.private-key}")
    private String privateKey;

    @Value("${paydunya.token}")
    private String token;

    @Value("${paydunya.url}")
    private String paydunyaUrl;

    @Value("${paydunya.softpay-url}")
    private String softpayUrl;

    public String buyVideo(Long videoId) {
        User user = authService.getCurrentUser();
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Vidéo introuvable"));

        return createAndSaveInvoice(
                video.getPrice(), "Paiement de la vidéo : " + video.getTitle(), user,
                video, null, null, null, null
        );
    }

    public String buySubject(Long subjectId) {
        User user = authService.getCurrentUser();
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Sujet introuvable"));

        return createAndSaveInvoice(
                subject.getPrice(), "Paiement du sujet : " + subject.getName(), user,
                null, subject, null, null, null
        );
    }

    public String buySemester(Long semesterId) {
        User user = authService.getCurrentUser();
        Semester semester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new RuntimeException("Semestre introuvable"));

        return createAndSaveInvoice(
                semester.getPrice(), "Paiement du semestre : " + semester.getName(), user,
                null, null, semester, null, null
        );
    }

    public String buyCorrection(Long correctionId) {
        User user = authService.getCurrentUser();
        Correction correction = correctionRepository.findById(correctionId)
                .orElseThrow(() -> new RuntimeException("Correction introuvable"));

        return createAndSaveInvoice(
                correction.getPrice(), "Paiement de la correction : " + correction.getTitle(), user,
                null, null, null, correction, null
        );
    }

    public String buyLectureCourse(Long lectureCourseId) {
        User user = authService.getCurrentUser();
        LectureCourse course = lectureCourseRepository.findById(lectureCourseId)
                .orElseThrow(() -> new RuntimeException("Cours introuvable"));

        return createAndSaveInvoice(
                course.getPrice(), "Paiement du cours : " + course.getTitle(), user,
                null, null, null, null, course
        );
    }

    private String createAndSaveInvoice(
            double amount, String description, User user,
            Video video, Subject subject, Semester semester,
            Correction correction, LectureCourse lectureCourse
    ) {
        Map<String, Object> invoiceMap = Map.of("total_amount", amount, "description", description);
        Map<String, Object> storeMap = Map.of("name", "MonFax");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("invoice", invoiceMap);
        requestBody.put("store", storeMap);

        Map<String, Object> response = webClient.post()
                .uri(paydunyaUrl)
                .header("PAYDUNYA-MASTER-KEY", masterKey)
                .header("PAYDUNYA-PRIVATE-KEY", privateKey)
                .header("PAYDUNYA-TOKEN", token)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response == null || !"00".equals(response.get("response_code"))) {
            log.error("Erreur PayDunya: code={}, message={}", response.get("response_code"), response.get("response_text"));
            throw new PaymentException("Échec de la création de la facture");
        }

        String invoiceUrl = (String) response.get("response_text");
        String invoiceToken = (String) response.get("token");

        Payment payment = new Payment();
        payment.setInvoiceToken(invoiceToken);
        payment.setAmount(amount);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setUser(user);
        payment.setVideo(video);
        payment.setSubject(subject);
        payment.setSemester(semester);
        payment.setCorrection(correction);
        payment.setLectureCourse(lectureCourse);

        paymentRepository.save(payment);

        log.info("Facture créée pour user {} - token {}", user.getId(), invoiceToken);

        return invoiceUrl;
    }

//    public String payWithSoftPay(String phoneNumber, String email, String password, String invoiceToken) {
//        Map<String, Object> body = Map.of(
//                "phone_number", phoneNumber,
//                "customer_email", email,
//                "password", password,
//                "invoice_token", invoiceToken
//        );
//
//        Map<String, Object> response = webClient.post()
//                .uri(softpayUrl)
//                .bodyValue(body)
//                .retrieve()
//                .bodyToMono(Map.class)
//                .block();
//
//        if (response == null || !Boolean.TRUE.equals(response.get("success"))) {
//            throw new PaymentException("Échec du paiement : " + response.get("message"));
//        }
//
//        Payment payment = paymentRepository.findByInvoiceToken(invoiceToken)
//                .orElseThrow(() -> new RuntimeException("Paiement introuvable"));
//
//        payment.setStatus(PaymentStatus.COMPLETED);
//        paymentRepository.save(payment);
//
//        return (String) response.get("message");
//    }

    public void updatePaymentStatus(String invoiceToken, PaymentStatus newStatus) {
        Payment payment = paymentRepository.findByInvoiceToken(invoiceToken)
                .orElseThrow(() -> new RuntimeException("Aucun paiement trouvé pour le token : " + invoiceToken));

        payment.setStatus(newStatus);
        paymentRepository.save(payment);
    }
}
