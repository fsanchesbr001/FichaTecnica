package com.fabriciosanches.fichatecnica.domain.medidas;

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

    public UnidadeMedida(DadosUnidadeMedida dadosUnidadeMedida) {
        this.codigo = dadosUnidadeMedida.codigo();
        this.nome = dadosUnidadeMedida.nome();
        this.sigla = dadosUnidadeMedida.sigla();
    }
}
