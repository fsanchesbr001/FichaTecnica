package com.fabriciosanches.fichatecnica.dtos;

import java.math.BigDecimal;

/**
 * DTO de projeção usado exclusivamente para geração de relatórios PDF de Conversão.
 * Os nomes das Unidades de Medida são resolvidos via JPQL cross-join, sem chamadas extras ao serviço.
 * Implementado como classe (não record) para garantir compatibilidade com SELECT new do JPQL.
 */
public class ConversaoRelatorioDTO {

    private final Long codigo;
    private final String unidadeDe;
    private final String unidadePara;
    private final String operacao;
    private final BigDecimal valor;

    /** Constructor explícito exigido pela expressão SELECT new do JPQL. */
    public ConversaoRelatorioDTO(Long codigo, String unidadeDe, String unidadePara, String operacao, BigDecimal valor) {
        this.codigo     = codigo;
        this.unidadeDe  = unidadeDe;
        this.unidadePara = unidadePara;
        this.operacao   = operacao;
        this.valor      = valor;
    }

    public Long      getCodigo()     { return codigo;     }
    public String    getUnidadeDe()  { return unidadeDe;  }
    public String    getUnidadePara(){ return unidadePara; }
    public String    getOperacao()   { return operacao;   }
    public BigDecimal getValor()     { return valor;      }

    // Acessores no estilo record para manter compatibilidade com o código existente
    public Long      codigo()     { return codigo;     }
    public String    unidadeDe()  { return unidadeDe;  }
    public String    unidadePara(){ return unidadePara; }
    public String    operacao()   { return operacao;   }
    public BigDecimal valor()     { return valor;      }
}
