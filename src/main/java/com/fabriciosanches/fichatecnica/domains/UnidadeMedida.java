package com.fabriciosanches.fichatecnica.domains;

import com.fabriciosanches.fichatecnica.dtos.UnidadeMedidaDTO;
import jakarta.persistence.*;
import lombok.*;


@Table(name = "unidade_medida")
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UnidadeMedida {
    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long codigo;
    private String nome;
    private String sigla;

    public UnidadeMedida(UnidadeMedidaDTO unidadeMedidaDTO) {
        this.codigo = unidadeMedidaDTO.codigo();
        this.nome = unidadeMedidaDTO.nome();
        this.sigla = unidadeMedidaDTO.sigla();
    }
}
