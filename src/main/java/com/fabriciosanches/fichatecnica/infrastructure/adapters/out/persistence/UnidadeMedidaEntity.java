package com.fabriciosanches.fichatecnica.infrastructure.adapters.out.persistence;

import com.fabriciosanches.fichatecnica.dtos.UnidadeMedidaDTO;
import jakarta.persistence.*;
import lombok.*;


@Table(name = "unidade_medida")
@Entity(name = "UnidadeMedida")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UnidadeMedidaEntity {
    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long codigo;
    private String nome;
    private String sigla;

    public UnidadeMedidaEntity(UnidadeMedidaDTO unidadeMedidaDTO) {
        this.codigo = unidadeMedidaDTO.codigo();
        this.nome = unidadeMedidaDTO.nome();
        this.sigla = unidadeMedidaDTO.sigla();
    }
}
