package com.fabriciosanches.fichatecnica.domain.historicoItem;

import com.fabriciosanches.fichatecnica.domain.itens.Item;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "historico_item")
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class HistoricoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long codigo;

    @OneToOne(optional = false)
    @JoinColumn(name = "cd_item")
    private Item item;

    private BigDecimal valor;

    private LocalDate dataInicio;

    public HistoricoItem(DadosHistoricoItem dadosHistoricoItem) {
        this.codigo = dadosHistoricoItem.codigo();
        this.item = dadosHistoricoItem.item();
        this.valor = dadosHistoricoItem.valor();
        this.dataInicio = dadosHistoricoItem.dataInicio();
    }
}
