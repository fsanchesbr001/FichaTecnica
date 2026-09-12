package com.fabriciosanches.fichatecnica.infrastructure.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SpringDataHistoricoItemRepository extends JpaRepository<HistoricoItemEntity, Long> {
    @Modifying
    @Transactional
    void deleteHistoricoItemByCdItem(Long codigoItem);

    List<HistoricoItemEntity> findByCdItem(Long codigoItem);

    List<HistoricoItemEntity> findByCdItemOrderByCdItemAscDataInicioAscCodigoAsc(Long codigoItem);
}


