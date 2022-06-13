import java.sql.*;
import java.util.ArrayList;

public class User extends Person {

//    ArrayList<String[]> notifications = new ArrayList();
    private ArrayList<String[]> notifications;


    User(){
        super("Anonymous");
    }


    public void book(ArrayList<String[]> passengers, ArrayList bookingInfo) throws SQLException{
        String date= (String) bookingInfo.get(0);
        char seatType = (char) bookingInfo.get(2);
        String flightId =(String) bookingInfo.get(3);

        passengers.forEach((passenger)->{
            try {
                statement.execute("INSERT INTO " +
                        "Bookings(flightId,bookedOnDate,bookedForDate,bookedBy,fullName,cnic,seatType) VALUES(" +
                        flightId +","+
                        "date('now'),"+
                        "'"+date+"',"+
                        "'"+username+"',"+
                        "'"+passenger[0]+"',"+
                        "'"+passenger[1]+"',"+
                        "'"+seatType+
                        "')");
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });

    }
    public ResultSet getBookings() throws SQLException{
        //iske andar query hogi jo user k wali bookings greater than or equals to hogi aaj ki date se
        //ye method return karega resultset.. jisko process karega driver
        // this method returns a resultSet:  bookings made only by a username and are greater or equals to today

        statement.execute("SELECT Bookings.*,Flights.travelTo,Flights.travelFrom,Flights.takeOffTime FROM Bookings " +
                "INNER JOIN Flights ON Bookings.flightId=Flights.flightId " +
                "WHERE Bookings.bookedBy = '"+username+"' AND bookedForDate>= date('now') " +
                "ORDER BY bookedForDate");
        return statement.getResultSet();
    }

    public void cancelBooking(String bookingId) throws SQLException{
        statement.execute("DELETE FROM Bookings WHERE BookingId="+bookingId+";");
    }

    public ResultSet getHistory() throws SQLException{
//        statement.execute("SELECT * FROM Bookings WHERE bookedBy = '"+username+"' AND bookedForDate<date('now')");
        statement.execute("SELECT Bookings.*,Flights.travelTo,Flights.travelFrom,Flights.takeOffTime FROM Bookings " +
                "INNER JOIN Flights ON Bookings.flightId=Flights.flightId " +
                "WHERE Bookings.bookedBy = '"+username+"' AND bookedForDate<date('now') " +
                "ORDER BY bookedForDate");
        return statement.getResultSet();
    }

    private void populateNotifications() throws SQLException{
        notifications = new ArrayList();
        String query = "SELECT notification,whenNotified FROM notifications WHERE username=" + "'"+username+"' ORDER BY whenNotified LIMIT 10;";
        statement.execute(query);
        ResultSet rset = statement.getResultSet();
        Boolean notificationsPresent=false;
            while(rset.next()){
                notificationsPresent=true;
                String [] notification = new String[2];
                notification[0] = rset.getString("notification");
                notification[1] = rset.getString("whenNotified");
                notifications.add(notification);
            }
            if(!notificationsPresent) notifications = null;
            //repopulate is liye nahii ho rha ke last sign in par wo
    }

    ArrayList<String[]> getNotifications() throws SQLException{
        populateNotifications();
        return notifications;
    }

}

