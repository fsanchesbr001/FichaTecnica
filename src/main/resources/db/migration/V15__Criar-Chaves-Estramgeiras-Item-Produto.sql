ALTER TABLE fichatecnica.item_produto DROP FOREIGN KEY item_pedido_conversao_FK;
ALTER TABLE fichatecnica.item_produto ADD CONSTRAINT item_produto_pk PRIMARY KEY (codigo);
ALTER TABLE fichatecnica.item_produto ADD CONSTRAINT item_produto_produto_FK FOREIGN KEY (cd_produto) REFERENCES fichatecnica.produto(codigo);
ALTER TABLE fichatecnica.item_produto ADD CONSTRAINT item_produto_item_FK FOREIGN KEY (cd_item) REFERENCES fichatecnica.item(codigo);
