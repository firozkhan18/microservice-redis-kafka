/**
 * (C) Copyright 2023 Firoz Khan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.springboot.microservice.adapters.repository;

import com.springboot.microservice.domain.entities.reservation.ReservationEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author: Firoz Khan
 * @version:
 * @date:
 */
@Repository
public interface ReservationRepository extends PagingAndSortingRepository<ReservationEntity, UUID> {


    /**
     * Find Reservation by Customer ID
     * @param customerId
     * @return
     */
    public List<ReservationEntity> findByCustomerId(String customerId);

    /**
     * Find by Reservation ID
     * @param reservationId
     * @return
     */
    @Query("SELECT reservation FROM ReservationEntity reservation WHERE reservation.uuid = :reservationId ")
    public Optional<ReservationEntity> findByReservationId(@Param("reservationId") UUID reservationId);

    /**
     * Find by Customer ID and Reservation ID
     * @param customerId
     * @param reservationId
     * @return
     */
    @Query("SELECT reservation FROM ReservationEntity reservation WHERE reservation.customerId = :customerId AND reservation.uuid = :reservationId ")
    public Optional<ReservationEntity> findByCustomerIdAndReservationId(
            @Param("customerId") String customerId,
            @Param("reservationId") UUID reservationId);

	public ReservationEntity save(ReservationEntity reservation);

	public List<ReservationEntity> findAll();


}
