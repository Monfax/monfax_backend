package com.LDE.monFax_backend.services;

import com.LDE.monFax_backend.models.Niveau;
import com.LDE.monFax_backend.repositories.NiveauRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NiveauService {

    private final NiveauRepository niveauRepository;

    public List<Niveau> getAllNiveaux() {
        return niveauRepository.findAll();
    }

    public Optional<Niveau> getNiveauById(Long id) {
        return niveauRepository.findById(id);
    }

    public Niveau createNiveau(Niveau niveau) {
        return niveauRepository.save(niveau);
    }

    public Niveau updateNiveau(Long id, Niveau updatedNiveau) {
        return niveauRepository.findById(id)
                .map(niveau -> {
                    niveau.setNumero(updatedNiveau.getNumero());
                    niveau.setFilieres(updatedNiveau.getFilieres());
                    return niveauRepository.save(niveau);
                })
                .orElseThrow(() -> new RuntimeException("Niveau introuvable"));
    }

    public void deleteNiveau(Long id) {
        niveauRepository.deleteById(id);
    }
}
