package com.example.venn.repository;

import com.example.venn.models.LoadEvent;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface LoadRepository extends CrudRepository<LoadEvent, Long> {
    @Query("""
            SELECT * FROM load_event 
                     WHERE customer_id = :customerId
                       AND accepted = :accepted
            AND time >= :startTime AND time < :endTime
            """)
    List<LoadEvent> findEventsByCustomerIdAndTimerange(@Param("customerId") Long customerId,
                                                       @Param("accepted") Boolean accepted,
                                                       @Param("startTime") Instant startTime,
                                                       @Param("endTime") Instant endTime);

    @Query("""
            SELECT * FROM load_event 
                     WHERE load_id = :loadId
                       AND customer_id = :customerId
            """)
    List<LoadEvent> findEventsByLoadIdAndCustomerId(
            @Param("loadId") Long loadId,
            @Param("customerId") Long customerId);

}
