package com.fabriciosanches.fichatecnica.repository;

import com.fabriciosanches.fichatecnica.domains.HistoricoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface HistoricoItemRepository extends JpaRepository<HistoricoItem, Long> {
    @Modifying
    @Transactional
    @Query("DELETE FROM HistoricoItem h WHERE h.item.codigo = :codigoItem")
    void deleteByItemCodigo(Long codigoItem);
}
