package com.fabriciosanches.fichatecnica.repository;

import com.fabriciosanches.fichatecnica.domains.HistoricoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface HistoricoItemRepository extends JpaRepository<HistoricoItem, Long> {
    @Modifying
    @Transactional
    @Query("DELETE FROM HistoricoItem h WHERE h.cdItem = :codigoItem")
    void deleteByItemCodigo(Long codigoItem);

    List<HistoricoItem> findByCdItem(Long codigoItem);
}
