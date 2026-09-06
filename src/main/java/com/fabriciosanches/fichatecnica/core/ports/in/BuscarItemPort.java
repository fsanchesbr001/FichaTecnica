package com.fabriciosanches.fichatecnica.core.ports.in;

import com.fabriciosanches.fichatecnica.core.domain.Item;

import java.util.List;

public interface BuscarItemPort {
    List<Item> listar();

    Item buscarPorId(Long id);
}

