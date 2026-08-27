package com.fabriciosanches.fichatecnica.core.usecase;

import com.fabriciosanches.fichatecnica.core.domain.Conversao;
import com.fabriciosanches.fichatecnica.core.ports.in.ObterValoresConversaoPort;
import com.fabriciosanches.fichatecnica.core.ports.out.ConversaoRepositoryPort;
import com.fabriciosanches.fichatecnica.domains.Item;
import com.fabriciosanches.fichatecnica.dtos.ConversaoValoresDTO;
import com.fabriciosanches.fichatecnica.dtos.ItemDTO;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;

import java.math.BigDecimal;
import java.util.Objects;

public class ObterValoresConversaoUseCase implements ObterValoresConversaoPort {
    private final ConversaoRepositoryPort repositoryPort;

    public ObterValoresConversaoUseCase(ConversaoRepositoryPort repositoryPort) {
        this.repositoryPort = Objects.requireNonNull(repositoryPort, "Repository port não pode ser nulo");
    }

    @Override
    public ConversaoValoresDTO obterValoresConversao(Item item, Double quantidade, Long idUnidade) {
        validaValoresConversao(item, quantidade, idUnidade);

        var itemDto = new ItemDTO(item);
        var idUnidadeMedidaCompra = itemDto.unidadeMedida().getCodigo();
        var valorCompra = itemDto.valor();

        Conversao conversao = repositoryPort.buscarPorUnidadeDeEUnidadePara(idUnidadeMedidaCompra, idUnidade)
                .orElseThrow(() -> new FichaTecnicaException("Conversão não encontrada"));

        return converterValores(conversao, valorCompra, quantidade);
    }

    private void validaValoresConversao(Item item, Double quantidade, Long idUnidade) {
        if (item == null || item.getCodigo() == null || quantidade == null || idUnidade == null) {
            throw new FichaTecnicaException("Valores de conversão inválidos");
        }
        if (quantidade <= 0) {
            throw new FichaTecnicaException("Quantidade deve ser maior que zero");
        }
    }

    private ConversaoValoresDTO converterValores(Conversao conversao, BigDecimal valorCompra, Double quantidade) {
        var valorConvertido = switch (conversao.getOperacao()) {
            case "MULTIPLICA" -> valorCompra.multiply(conversao.getValor()).multiply(BigDecimal.valueOf(quantidade));
            case "DIVIDE" -> valorCompra.divide(conversao.getValor()).multiply(BigDecimal.valueOf(quantidade));
            default -> throw new FichaTecnicaException("Operação inválida");
        };

        return new ConversaoValoresDTO(quantidade, conversao.getUnidadePara(), valorConvertido);
    }
}
