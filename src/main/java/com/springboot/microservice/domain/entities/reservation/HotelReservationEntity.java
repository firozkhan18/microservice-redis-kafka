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
package com.springboot.microservice.domain.entities.reservation;

import java.time.LocalDate;

import com.springboot.microservice.domain.entities.core.springdata.AbstractBaseEntityWithUUID;
import com.springboot.microservice.domain.statemachine.reservation.ReservationState;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * @author: Firoz Khan
 * @version:
 * @date:
 */

@Entity
@Table(name = "reservation_hotel_tx")
public class HotelReservationEntity extends AbstractBaseEntityWithUUID {

    @Column(name = "hotelId")
    private String hotelId;

    @Column(name = "hotelName")
    private String hotelName;

    @Column(name = "days")
    private Integer days;

    @Column(name = "persons")
    private Integer persons;

    @Column(name ="startDate")
    private LocalDate startDate;

    @Column(name ="endDate")
    private LocalDate endDate;

    @Column(name = "rate")
    private Integer rate;

    @Column(name = "reservationStatus")
    @Enumerated(EnumType.STRING)
    private ReservationState reservationState;

    @Column(name = "hotelReservationNo")
    private String hotelReservationNo;

    public HotelReservationEntity() {}

    /**
     * Create Hotel Reservation
     *
     * @param _hotelId
     * @param _hotelName
     * @param _days
     * @param _rate
     */
    public HotelReservationEntity(String _hotelId, String _hotelName, Integer _days, Integer _rate) {
        this.hotelId = _hotelId;
        this.hotelName = _hotelName;
        this.days = _days;
        this.rate = _rate;
    }

    /**
     * Returns Hotel ID
     * @return
     */
    public String getHotelId() {
        return hotelId;
    }

    /**
     * Returns Hotel Name
     * @return
     */
    public String getHotelName() {
        return hotelName;
    }

    /**
     * Returns Quantity
     * @return
     */
    public Integer getDays() {
        return days;
    }

    /**
     * Returns the Price
     * @return
     */
    public Integer getRate() {
        return rate;
    }

    /**
     * Returns the Total Cost
     * @return
     */
    public Integer getTotalCost() {
        return days * rate;
    }

    /**
     * Returns the No. of Persons staying
     * @return
     */
    public Integer getPersons() {
        return persons;
    }

    /**
     * Rental Start Date
     * @return
     */
    public LocalDate getStartDate() {
        return startDate;
    }

    /**
     * Rental End Date
     * @return
     */
    public LocalDate getEndDate() {
        return endDate;
    }

    /**
     * Returns the Reservation Status
     * @return
     */
    public ReservationState getReservationState() {
        return reservationState;
    }

    public String getHotelReservationNo() {
        return hotelReservationNo;
    }
}
