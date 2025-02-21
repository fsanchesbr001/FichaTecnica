package com.fabriciosanches.fichatecnica.domain.itens;

import com.fabriciosanches.fichatecnica.domain.medidas.UnidadeMedida;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "item")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private  Long codigo;
    private String nome;

    @OneToOne(optional = false)
    @JoinColumn(name = "cd_unidade_medida")
    private UnidadeMedida unidadeMedida;
    private BigDecimal valor;

    public Item(DadosItem dadosItem) {
        this.codigo = dadosItem.codigo();
        this.nome = dadosItem.nome();
        this.unidadeMedida = dadosItem.unidadeMedida();
        this.valor = dadosItem.valor();
    }
}
