package com.xyz.reservations.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

public class SeatHold {
    private static Integer holdId = 1;

    public List<Seat> seatList;
    public String customerEmail;
    public Integer seatHoldId = null;

    public SeatHold(List<Seat> seatList, String customerEmail) {
        if(!seatList.isEmpty()) {
            seatHoldId = holdId++;
            this.seatList = seatList;
            this.customerEmail = customerEmail;
        }else{
            seatHoldId = -1;
        }
    }
}
