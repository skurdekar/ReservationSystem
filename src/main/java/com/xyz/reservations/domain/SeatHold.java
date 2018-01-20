package com.xyz.reservations.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

public class SeatHold {

    public Seat[] seatArray;
    public String customerEmail;
    public Integer seatHoldId = null;

    public SeatHold(Integer seatHoldId, List<Seat> seatList, String customerEmail) {
        this.seatHoldId = seatHoldId;
        this.seatArray = convertListToArray(seatList);
        this.customerEmail = customerEmail;
    }

    public Seat[] convertListToArray(List<Seat> seatList){
        Seat[] seatArray = new Seat[seatList.size()];
        int index = 0;
        for(Seat seat: seatList){
            Seat copy = new Seat(seat.row, seat.column);
            copy.status = seat.status;
            seatArray[index++] = copy;
        }
        return seatArray;
    }
}
