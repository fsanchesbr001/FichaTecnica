package com.fabriciosanches.fichatecnica.repository;

import com.fabriciosanches.fichatecnica.domains.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ItemRepository extends JpaRepository<Item, Long> {
    @Query("SELECT COUNT(i) FROM Item i WHERE i.nome = :nome")
    long countByName(@Param("nome") String nome);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "DELETE FROM item i WHERE i.codigo = ?1")
    void deleteItem(@Param("codigo") Long codigo);
}
