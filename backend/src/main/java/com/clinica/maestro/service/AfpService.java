package com.clinica.maestro.service;

import com.clinica.maestro.dto.AfpResponse;
import com.clinica.maestro.repository.rrhh.AfpRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AfpService {

    private final AfpRepository afpRepository;

    public AfpService(AfpRepository afpRepository) {
        this.afpRepository = afpRepository;
    }

    public List<AfpResponse> findAll() {
        return afpRepository.findAllByActivoTrueOrderByCodigo()
            .stream()
            .map(AfpResponse::fromEntity)
            .toList();
    }
}
