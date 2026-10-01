package com.rota.restaurant.application.cache;

/**
 * Nomes dos caches do catálogo público.
 */
public final class CatalogCaches {

    /** Páginas da busca de restaurantes. Qualquer mudança em qualquer restaurante limpa tudo. */
    public static final String SEARCH = "restaurant-search";

    /** Detalhe (cardápio completo) por id de restaurante. */
    public static final String DETAIL = "restaurant-detail";

    private CatalogCaches() {
    }
}
