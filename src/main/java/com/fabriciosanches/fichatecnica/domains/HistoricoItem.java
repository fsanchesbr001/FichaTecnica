package com.fabriciosanches.fichatecnica.domains;

import com.fabriciosanches.fichatecnica.dtos.HistoricoItemDTO;
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

    public HistoricoItem(HistoricoItemDTO historicoItemDTO) {
        this.codigo = historicoItemDTO.codigo();
        this.item = historicoItemDTO.item();
        this.valor = historicoItemDTO.valor();
        this.dataInicio = historicoItemDTO.dataInicio();
    }
}
