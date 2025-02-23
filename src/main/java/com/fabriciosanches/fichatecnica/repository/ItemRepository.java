package com.fabriciosanches.fichatecnica.repository;

import com.fabriciosanches.fichatecnica.domains.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ItemRepository extends JpaRepository<Item, Long> {
    @Query("SELECT COUNT(i) FROM Item i WHERE i.nome = :nome")
    long countByName(@Param("nome") String nome);
}
