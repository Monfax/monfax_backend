package com.LDE.monFax_backend.services;


import com.LDE.monFax_backend.models.Subject;
import com.LDE.monFax_backend.models.Video;
import com.LDE.monFax_backend.repositories.SubjectRepository;
import com.LDE.monFax_backend.repositories.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VideoService {

    private final VideoRepository videoRepository;
    private final SubjectRepository subjectRepository;
    private final ResourceService resourceService;

    public List<Video> getAllVideos() {
        return videoRepository.findAll();
    }

    public Optional<Video> getVideoById(Long id) {
        Optional<Video> video =  videoRepository.findById(id);
        video.ifPresent(foundVideo -> {
            resourceService.increaseNumberOfViews(foundVideo);
            videoRepository.save(foundVideo);
        });
        return video;
    }

    public Video createVideo(String title, String description, Double duration, Double price, Long subjectId, MultipartFile file) throws IOException {
        // On stocke le fichier dans /uploads/videos
        String filename = (file.getOriginalFilename());
        String ext = resourceService.getExtension(filename);
        if (!ext.equals("mp4")) {
            throw new IOException("vous devez envoyer la video en mp4");
        }
        String fileUrl = resourceService.storeFile(file, "videos");

        Subject subject = subjectRepository.findById(subjectId).orElseThrow(() -> new IllegalArgumentException("Matière introuvable avec l'id : " + subjectId));

        Video video = new Video();
        video.setTitle(title);
        video.setDescription(description);
        video.setDuration(duration);
        video.setPrice(price);
        video.setSubject(subject);
        video.setResourceUrl(fileUrl);
        video.setSize(file.getSize());
        video.setCreatedAt(LocalDate.now());
        video.setNumberOfDownload(0L);
        video.setNumberOfView(0L);

        return videoRepository.save(video);

    }

    public void deleteVideo(Long id) {
        videoRepository.deleteById(id);
    }


    public void updateVideo(Long id, String title, String description, MultipartFile file) throws Exception{
        Optional<Video> optionalVideo = videoRepository.findById(id);
        if (optionalVideo.isEmpty()) {
            throw new Exception("Vidéo non trouvée");
        }
        Video video = optionalVideo.get();

        if (title != null) video.setTitle(title);
        if (description != null) video.setDescription(description);

        // Si un nouveau fichier est uploadé, on remplace l'ancien
        if (file != null && !file.isEmpty()) {
            // Supprimer l’ancien fichier si existant
            if (video.getResourceUrl() != null) {
                resourceService.deleteFile(video.getResourceUrl());
            }

            String filename=(file.getOriginalFilename());
            String ext =resourceService.getExtension(filename);
            if (!ext.equals("mp4")) {

                throw new IOException("vous devez envoyer la video en mp4");

            }
            // Stocker le nouveau fichier dans un dossier dédié "videos"
            String newFilePath = resourceService.storeFile(file, "videos");
            video.setResourceUrl(newFilePath);
            video.setSize(file.getSize());
        }

        videoRepository.save(video);
    }
    public long getTotalVideo() {
        return videoRepository.count();
    }
    }

