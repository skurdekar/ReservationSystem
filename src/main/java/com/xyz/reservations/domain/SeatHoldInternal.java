package com.xyz.reservations.domain;

import java.util.List;
import java.util.concurrent.Future;

public class SeatHoldInternal {

    public Future future;
    public List<Seat> seatList;
    public String customerEmail;
    public Integer seatHoldId = 0;

    private static Integer holdId = 1;

    public SeatHoldInternal(List<Seat> seatList, String customerEmail){
        synchronized(this) {
            if (seatList.isEmpty()) {
                holdId = -1;
            } else {
                seatHoldId = holdId++;
            }
        }

        this.seatList = seatList;
        this.customerEmail = customerEmail;
    }

    public SeatHold getSeatHold(){
        return new SeatHold(seatHoldId, seatList, customerEmail);
    }
}
