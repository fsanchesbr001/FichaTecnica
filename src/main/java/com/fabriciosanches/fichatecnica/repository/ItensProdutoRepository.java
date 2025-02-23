package com.fabriciosanches.fichatecnica.repository;

import com.fabriciosanches.fichatecnica.domains.ItensProduto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItensProdutoRepository extends JpaRepository<ItensProduto, Long> {
}
