-- Contexto W3C do trace que gravou o evento; o relay publica como continuação dele.
ALTER TABLE outbox_events ADD COLUMN trace_parent VARCHAR(55);
