ALTER TABLE fichatecnica.historico_item DROP FOREIGN KEY historico_item_item_FK;
ALTER TABLE fichatecnica.historico_item ADD CONSTRAINT historico_item_item_FK FOREIGN KEY (codigo) REFERENCES fichatecnica.item(codigo) ON DELETE RESTRICT;
