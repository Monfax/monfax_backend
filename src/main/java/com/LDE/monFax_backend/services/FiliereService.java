package com.LDE.monFax_backend.services;

import com.LDE.monFax_backend.models.Filiere;
import com.LDE.monFax_backend.repositories.FiliereRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FiliereService {

    private final FiliereRepository filiereRepository;

    public List<Filiere> getAllFilieres() {
        return filiereRepository.findAll();
    }

    public Optional<Filiere> getFiliereById(Long id) {
        return filiereRepository.findById(id);
    }

    public Filiere createFiliere(Filiere filiere) {
        return filiereRepository.save(filiere);
    }

    public Filiere updateFiliere(Long id, Filiere updatedFiliere) {
        return filiereRepository.findById(id)
                .map(filiere -> {
                    filiere.setName(updatedFiliere.getName());
                    filiere.setPrograms(updatedFiliere.getPrograms());
                    filiere.setNiveau(updatedFiliere.getNiveau());
                    return filiereRepository.save(filiere);
                })
                .orElseThrow(() -> new RuntimeException("Filière introuvable"));
    }

    public void deleteFiliere(Long id) {
        filiereRepository.deleteById(id);
    }
}

