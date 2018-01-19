package com.xyz.reservations.engine;

import com.xyz.reservations.domain.Seat;
import com.xyz.reservations.domain.SeatHold;
import com.xyz.reservations.domain.SeatHoldFuture;
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

    final static Log logger = LogFactory.getLog(TicketServiceImpl.class);
    private static EmailValidator emv = new EmailValidator();

    private static Object synch = new Object();
    private static TicketServiceImpl serviceInstance = null;

    private TicketServiceCore ticketServiceCore = null;
    private Map<Integer, SeatHoldFuture> seatHoldMap = new HashMap<>();
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
        SeatHoldFuture shf = new SeatHoldFuture();
        SeatHold seatHold = new SeatHold(holdSeats, customerEmail);
        shf.seatHold = seatHold;
        if (!holdSeats.isEmpty()) {
            synchronized(seatHoldMap) {
                seatHoldMap.put(seatHold.seatHoldId, shf);
            }
            shf.future = scheduler.schedule(new Runnable() {
                public void run() {
                    releaseHold(shf);
                }
            }, holdTimeoutSeconds, TimeUnit.SECONDS);
            logger.info("Hold request succeeded HoldId: " + seatHold.seatHoldId);
        }
        return seatHold;
    }

    @Override
    public String reserveSeats(int seatHoldId, String customerEmail) {
        validateEmail(customerEmail);
        SeatHoldFuture shf = seatHoldMap.get(seatHoldId);
        String retval = "";
        if (shf != null) {
            SeatHold seatHold = shf.seatHold;
            if(!seatHold.customerEmail.equals(customerEmail)){
                throw new IllegalArgumentException("Emails specified for hold action and reserve action don't match");
            }
            synchronized(seatHoldMap){
                retval = ticketServiceCore.reserveSeats(seatHold.seatList);
                seatHoldMap.remove(seatHoldId);
            }
            //cancel the hold timeout
            if (shf.future != null && !shf.future.isDone()) {
                shf.future.cancel(true);
            }
        }else{
            logger.error("Reservation attempt unsuccessful. Seats were never held or hold has expired");
        }
        return retval;
    }

    public void releaseHold(int seatHoldId) {
        SeatHoldFuture shf = seatHoldMap.get(seatHoldId);
        releaseHold(shf);
    }

    private void releaseHold(SeatHoldFuture shf) {
        if (shf != null) {
            logger.info("Attempting to release hold: " + shf.seatHold.seatHoldId);
            ticketServiceCore.releaseHold(shf.seatHold.seatList);

            synchronized(seatHoldMap){
                SeatHold seatHold = shf.seatHold;
                if(seatHoldMap.get(seatHold.seatHoldId) != null) {
                    seatHoldMap.remove(seatHold.seatHoldId);
                    logger.info("Released hold: " + seatHold.seatHoldId);
                }
            }
        }
    }

    public String printSeatMap() {
        return ticketServiceCore.toString();
    }

    private static void validateEmail(String email){
        if(!emv.validate(email)){
            throw new IllegalArgumentException("Invalid Email format");
        }
    }
}
