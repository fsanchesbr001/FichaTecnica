package com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web;

import com.fabriciosanches.fichatecnica.core.ports.in.ObterValoresConversaoPort;
import com.fabriciosanches.fichatecnica.core.ports.out.ItemRepositoryPort;
import com.fabriciosanches.fichatecnica.core.ports.out.UnidadeMedidaRepositoryPort;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.out.persistence.ItemProdutoPersistenceAdapter;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.out.persistence.ProdutoPersistenceAdapter;
import com.fabriciosanches.fichatecnica.infrastructure.config.ItemProdutoConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.properties.hibernate.check_nullability=false",
        "spring.jpa.show-sql=false"
})
@Import({ItemProdutoController.class, ItemProdutoConfig.class, ItemProdutoPersistenceAdapter.class,
        ProdutoPersistenceAdapter.class, ItemProdutoExclusaoJpaTest.Config.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class ItemProdutoExclusaoJpaTest {

    @Autowired private ItemProdutoController controller;
    @Autowired private JdbcTemplate jdbc;

    @BeforeEach
    void preparar() {
        jdbc.execute("ALTER TABLE produto DROP CONSTRAINT IF EXISTS teste_custo_minimo");
        jdbc.update("DELETE FROM item_produto");
        jdbc.update("DELETE FROM produto");
        jdbc.update("DELETE FROM item");
        jdbc.update("DELETE FROM unidade_medida");
        jdbc.update("INSERT INTO unidade_medida (codigo, nome, sigla) VALUES (1, 'Grama', 'g'), (2, 'Quilo', 'kg')");
        jdbc.update("INSERT INTO item (codigo, nome, cd_unidade_medida, valor) VALUES " +
                "(5, 'Sal', 1, 2.50), (6, 'Carne', 2, 5.00)");
        jdbc.update("INSERT INTO produto (codigo, nome, descricao, imagem, valor_venda, valor_itens) VALUES " +
                "(1, 'Prato', 'Descricao', 'produtos/1/foto.png', 20.00, 7.50), " +
                "(2, 'Outro prato', 'Descricao', NULL, 30.00, 9.00)");
        jdbc.update("INSERT INTO item_produto (cd_produto, cd_item, cd_unidade_para, quantidade, valor) VALUES " +
                "(1, 5, 1, 1, 2.50), (1, 6, 2, 1, 5.00), (2, 5, 1, 1, 9.00)");
    }

    @Test
    void exclusaoSemTransacaoExternaRemoveSomenteAssociacaoEAtualizaCusto() {
        assertThat(controller.deletarItemProduto(1L, 5L).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        assertThat(associacoes(1L, 5L)).isZero();
        assertThat(associacoes(1L, 6L)).isEqualTo(1);
        assertThat(associacoes(2L, 5L)).isEqualTo(1);
        assertThat(custo(1L)).isEqualByComparingTo("5.00");
        assertThat(custo(2L)).isEqualByComparingTo("9.00");
        assertThat(jdbc.queryForObject("SELECT imagem FROM produto WHERE codigo=1", String.class))
                .isEqualTo("produtos/1/foto.png");
    }

    @Test
    void falhaNoRecalculoDesfazExclusaoInclusiveQuandoErroOcorreNoCommit() {
        jdbc.execute("ALTER TABLE produto ADD CONSTRAINT teste_custo_minimo CHECK (valor_itens >= 7.50)");

        assertThatThrownBy(() -> controller.deletarItemProduto(1L, 5L))
                .isInstanceOf(DataIntegrityViolationException.class);

        assertThat(associacoes(1L, 5L)).isEqualTo(1);
        assertThat(associacoes(2L, 5L)).isEqualTo(1);
        assertThat(custo(1L)).isEqualByComparingTo("7.50");
    }

    private int associacoes(Long produto, Long item) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM item_produto WHERE cd_produto=? AND cd_item=?",
                Integer.class, produto, item);
    }

    private BigDecimal custo(Long produto) {
        return jdbc.queryForObject("SELECT valor_itens FROM produto WHERE codigo=?", BigDecimal.class, produto);
    }

    @TestConfiguration
    static class Config {

        @Bean
        ItemRepositoryPort itemRepositoryPort() {
            return mock(ItemRepositoryPort.class);
        }

        @Bean
        UnidadeMedidaRepositoryPort unidadeMedidaRepositoryPort() {
            return mock(UnidadeMedidaRepositoryPort.class);
        }

        @Bean
        ObterValoresConversaoPort obterValoresConversaoPort() {
            return mock(ObterValoresConversaoPort.class);
        }
    }
}
