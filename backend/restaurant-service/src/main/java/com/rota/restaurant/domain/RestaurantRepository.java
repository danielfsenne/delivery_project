package com.rota.restaurant.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    /**
     * Filtros vazios ("") são ignorados. Os valores devem chegar em minúsculas.
     */
    @Query("""
            select r from Restaurant r
            where r.active = true
              and (:city = '' or lower(r.address.city) = :city)
              and (:cuisine = '' or lower(r.cuisine) = :cuisine)
              and (:term = '' or lower(r.name) like concat('%', :term, '%')
                              or lower(r.cuisine) like concat('%', :term, '%'))
            """)
    Page<Restaurant> search(String city, String cuisine, String term, Pageable pageable);

    List<Restaurant> findByOwnerIdOrderByNameAsc(Long ownerId);
}
