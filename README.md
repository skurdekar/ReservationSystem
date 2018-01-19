## Reservation Service

Implements a simple ticket service that facilitates discovery, temporary hold and final reservation of seats within a high 
demand performance venue. The application will allow end users to hold and reserve the specified number of seats. Every hold will
timeout within a configurable number of seconds. Both the number of seats and rows in the venue are configurable via 
application.conf file located in resources folder. The range for numberofseats is(100-5000), rowCount is (10-100) and
hold timeout is (10-120) seconds. All in input variables are assumed to be have integer range upper bounds.

The application accepts input via Standard Console. It uses log4j for logging and typesafe-config for loading application properties

## Using the Service
The service TicketService provides 3 apis to hold, release and get information on the number of seats available
 - `numSeatsAvailable()` returns the number of seats currently available in the venue. This is the total number of seats
 minus the number of seats held and reserved
 
 - `findAndHoldSeats(numSeats, email)` will hold the specified number of seats. Returns a SeatHold object which contains 
 a list of Seat objects(seatList), customer email(customerEmail) and seatHoldId which is a unique identifier (integer) for each 
 successful hold operation. If the hold operation is unsuccessful a -1 is returned for seatHoldId
 
 - `reserveSeats(seatHoldId, customerEmail)` will reserve the specified number of already held based on seatHoldId and email.
 The customerEmail should match the value specified for for hold operation otherwise the reserve operation will fail. The operation
 returns a confirmation id (String) for the reservation. If the reservation is unsuccessful an empty string "" is returned.

## API Example
    import com.xyz.reservations.engine.ReservationApp
    import com.xyz.reservations.engine.TicketService
    import com.xyz.reservations.engine.TicketServiceImpl

    TicketService service = TicketServiceImpl.getInstance(
                Settings.seatCount, Settings.rowCount, Settings.holdTimeout);
                
    impl.numSeatsAvailable()
    
    SeatHold sh = impl.findAndHoldSeats(numSeats, email)
    
    String confId = impl.reserveSeats(sh.seatHoldId, email)
    
    (TicketServiceImpl)impl.printSeatMap()
    
## Building and Running the application
The application can be downloaded using the following command
 - `git clone ....`
 
The application is packaged with the Gradle Build Tool. It can be built using the following command
 - `gradle build`
     
The application can be run as follows
 - `gradle run`
 
Once run, the application will prompt you with an input prompt for entering commands to process the reservation
 
2018-01-19 00:58:16 INFO  TicketServiceImpl: - Created TicketService numSeats: 100, rows: 10, hold timeout(s): 30
Enter Command( hold [numseats email], reserve [holdId email], available, print, end ): 

The application implements logging using the log4j library. The log configuration is available in log4j.properties
file in resources directory.
   
The application implements loading configurations using typesafe-config library. The configurations are stored in
application.conf file in resources directory

Alternatively the application can be packaged as a fat jar
 - `gradle fatJar`
     
Once packaged as a fat jar it can be run as a standalone java application
 - `java -classpath build/libs/ReservationSystem-all-1.0.jar com.xyz.reservations.engine.ReservationApp`

    
## Tests
Describe and show how to run the tests with code examples.

## Contributors
Shailesh Kurdekar (skurdekar@gmail.com)

## License
Open Source Free to use and distribute without warranty or liability from the original author.
