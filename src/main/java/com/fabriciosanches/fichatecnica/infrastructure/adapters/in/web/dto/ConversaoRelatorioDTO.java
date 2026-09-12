package com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto;

import java.math.BigDecimal;

/**
 * DTO de projeÃ§Ã£o usado exclusivamente para geraÃ§Ã£o de relatÃ³rios PDF de ConversÃ£o.
 * Os nomes das Unidades de Medida sÃ£o resolvidos via JPQL cross-join, sem chamadas extras ao serviÃ§o.
 * Implementado como classe (nÃ£o record) para garantir compatibilidade com SELECT new do JPQL.
 */
public class ConversaoRelatorioDTO {

    private final Long codigo;
    private final String unidadeDe;
    private final String unidadePara;
    private final String operacao;
    private final BigDecimal valor;

    /** Constructor explÃ­cito exigido pela expressÃ£o SELECT new do JPQL. */
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

    // Acessores no estilo record para manter compatibilidade com o cÃ³digo existente
    public Long      codigo()     { return codigo;     }
    public String    unidadeDe()  { return unidadeDe;  }
    public String    unidadePara(){ return unidadePara; }
    public String    operacao()   { return operacao;   }
    public BigDecimal valor()     { return valor;      }
}

