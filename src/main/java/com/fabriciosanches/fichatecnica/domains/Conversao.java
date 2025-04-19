package com.fabriciosanches.fichatecnica.domains;

import com.fabriciosanches.fichatecnica.dtos.ConversaoDTO;
import com.fabriciosanches.fichatecnica.serializers.BigDecimalCurrencySerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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

    @Column(name = "valor", precision = 10, scale = 2)
    @JsonSerialize(using = BigDecimalCurrencySerializer.class)
    private BigDecimal valor;

    public Conversao(ConversaoDTO conversaoDTO) {
        this.codigo = conversaoDTO.codigo();
        this.unidadeDe = conversaoDTO.unidadeDe();
        this.unidadePara = conversaoDTO.unidadePara();
        this.operacao = conversaoDTO.operacao();
        this.valor = conversaoDTO.valor();
    }
}
