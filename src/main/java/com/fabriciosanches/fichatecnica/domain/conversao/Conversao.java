package com.fabriciosanches.fichatecnica.domain.conversao;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;


@Table(name = "conversao")
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Conversao {
    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long codigo;
    private Long unidadeDe;
    private Long unidadePara;
    private String operacao;
    private BigDecimal valor;

    public Conversao(DadosConversao dadosConversao) {
        this.codigo = dadosConversao.codigo();
        this.unidadeDe = dadosConversao.unidadeDe();
        this.unidadePara = dadosConversao.unidadePara();
        this.operacao = dadosConversao.operacao();
        this.valor = dadosConversao.valor();
    }
}
