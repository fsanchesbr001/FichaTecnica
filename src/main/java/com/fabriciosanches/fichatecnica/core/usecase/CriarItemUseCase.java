package com.fabriciosanches.fichatecnica.core.usecase;

import com.fabriciosanches.fichatecnica.core.domain.Item;
import com.fabriciosanches.fichatecnica.core.ports.in.CriarItemPort;
import com.fabriciosanches.fichatecnica.core.ports.in.RegistrarHistoricoItemPort;
import com.fabriciosanches.fichatecnica.core.ports.out.ItemRepositoryPort;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.out.persistence.UnidadeMedidaEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class CriarItemUseCase implements CriarItemPort {
    private final ItemRepositoryPort itemRepositoryPort;
    private final RegistrarHistoricoItemPort registrarHistoricoItemPort;

    public CriarItemUseCase(ItemRepositoryPort itemRepositoryPort, RegistrarHistoricoItemPort registrarHistoricoItemPort) {
        this.itemRepositoryPort = Objects.requireNonNull(itemRepositoryPort, "Item repository port não pode ser nulo");
        this.registrarHistoricoItemPort = Objects.requireNonNull(registrarHistoricoItemPort, "Port de histórico não pode ser nulo");
    }

    @Override
    public Item criar(String nome, UnidadeMedidaEntity unidadeMedida, BigDecimal valor) {
        validar(nome, unidadeMedida, valor);

        if (itemRepositoryPort.contarPorNome(nome) > 0) {
            throw new FichaTecnicaException("Item já cadastrado");
        }

        Item novoItem = new Item(null, nome, unidadeMedida, valor);
        Item itemSalvo = itemRepositoryPort.salvar(novoItem);

        registrarHistoricoItemPort.registrar(itemSalvo.getCodigo(), valor, LocalDate.now());
        return itemSalvo;
    }

    private void validar(String nome, UnidadeMedidaEntity unidadeMedida, BigDecimal valor) {
        Objects.requireNonNull(nome, "Nome do item não pode ser nulo");
        Objects.requireNonNull(unidadeMedida, "Unidade de medida não pode ser nula");
        Objects.requireNonNull(valor, "Valor do item não pode ser nulo");
    }
}

