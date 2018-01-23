package com.xyz.reservations.engine;

import com.xyz.reservations.domain.Seat;
import com.xyz.reservations.domain.SeatHold;
import com.xyz.reservations.domain.SeatHoldInternal;
import com.xyz.reservations.service.TicketService;
import com.xyz.reservations.util.EmailValidator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TicketServiceImpl implements TicketService {

    private final static Log logger = LogFactory.getLog(TicketServiceImpl.class);
    private static EmailValidator emv = new EmailValidator();
    private static Object synch = new Object();
    private static TicketServiceImpl serviceInstance = null;

    private TicketServiceCore ticketServiceCore = null;
    private Map<Integer, SeatHoldInternal> seatHoldMap = new HashMap<>();
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
    private int holdTimeoutSeconds = 30;

    private TicketServiceImpl(int seatCount, int rowCount, int holdTimeoutSeconds) {
        ticketServiceCore = new TicketServiceCore(seatCount, rowCount);
        if (holdTimeoutSeconds <= 120 && holdTimeoutSeconds >= 10) {
            this.holdTimeoutSeconds = holdTimeoutSeconds;
        } else {
            logger.debug("TicketServiceImpl: Invalid value for hold timeout. defaulting to 30 seconds");
        }
        logger.info("Created TicketService numSeats: " + seatCount + ", rows: " + rowCount + ", hold timeout(s): " + holdTimeoutSeconds);
    }

    /**
     * Return instance of service
     *
     * @param seatCount
     * @param rowCount
     * @return
     */
    public static TicketService getInstance(int seatCount, int rowCount, int holdTimeoutSeconds) {
        synchronized (synch) {
            if (serviceInstance == null) {
                serviceInstance = new TicketServiceImpl(seatCount, rowCount, holdTimeoutSeconds);
            }
        }
        return serviceInstance;
    }

    /**
     * Returns the number of seats available
     * @return the number of available seats
     */
    @Override
    public int numSeatsAvailable() {
        return ticketServiceCore.numSeatsAvailable();
    }

    @Override
    public SeatHold findAndHoldSeats(int numSeats, String customerEmail) {

        validateEmail(customerEmail);
        List<Seat> holdSeats = ticketServiceCore.findAndHoldSeats(numSeats);
        SeatHoldInternal si = new SeatHoldInternal(holdSeats, customerEmail);
        if (!holdSeats.isEmpty()) {
            synchronized(seatHoldMap) {
                seatHoldMap.put(si.seatHoldId, si);
            }
            si.future = scheduler.schedule(new Runnable() {
                public void run() {
                    releaseHold(si);
                }
            }, holdTimeoutSeconds, TimeUnit.SECONDS);
            logger.info("Hold succeeded HoldId: " + si.seatHoldId + ", notifying " + customerEmail);
        }
        logger.debug("Hold Seats Assigned {row,column}: " + si.getSeatHold());

        return si.getSeatHold();
    }

    @Override
    public String reserveSeats(int seatHoldId, String customerEmail) {

        validateEmail(customerEmail);
        SeatHoldInternal si = seatHoldMap.get(seatHoldId);
        String retval = "";
        if (si != null) {
            if(!si.customerEmail.equals(customerEmail)){
                throw new IllegalArgumentException("Emails specified for hold action and reserve action don't match");
            }
            synchronized(seatHoldMap){
                retval = ticketServiceCore.reserveSeats(si.seatList);
                seatHoldMap.remove(seatHoldId);
            }
            //cancel the hold timeout
            if (si.future != null && !si.future.isDone()) {
                si.future.cancel(true);
            }
        }else{
            logger.error("Reservation failed. Seats were never held or hold has expired, notifying " + customerEmail);
        }
        return retval;
    }

    public void releaseHold(int seatHoldId) {
        SeatHoldInternal si = seatHoldMap.get(seatHoldId);
        releaseHold(si);
    }

    private void releaseHold(SeatHoldInternal si) {
        if (si != null) {
            logger.info("Attempting to release hold: " + si.seatHoldId);

            ticketServiceCore.releaseHold(si.seatList);
            synchronized(seatHoldMap){
                if(seatHoldMap.get(si.seatHoldId) != null) {
                    seatHoldMap.remove(si.seatHoldId);
                    logger.info("Released hold: " + si.seatHoldId + ", notifying " + si.customerEmail);
                }
            }
        }
    }

    public String printSeatMap() {
        return ticketServiceCore.toString();
    }

    private static void validateEmail(String email){
        if(!emv.validate(email)){
            throw new IllegalArgumentException("Invalid Email format " + email);
        }
    }
}
