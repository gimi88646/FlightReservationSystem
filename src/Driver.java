import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.sql.ResultSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import Exceptions.InputOutOfBound;
import Exceptions.InvalidDateException;
import Exceptions.InvalidInput;
import Exceptions.UsernameException;
import Validations.FiniteAutomata;

public class Driver {
    public static Scanner input = new Scanner(System.in);
    static Airline airline;

    public static void main(String[] args) {

        int choice;
        try {
            airline = new Airline();
        } catch (SQLException throwables) {
            System.out.println(throwables);
            System.out.println("Something really went wrong");
        }
        do {
            try {
                if (airline.user.getSignedInStatus()) {
                    //how should i change the options if the user has performed sign in
                    //when user signs in and he is a regular user he should be able to see following options

                    System.out.print(
                            """
                                    1. See Flights
                                    2. View Notifications
                                    3. View Bookings
                                    4. Cancel Booking
                                    5. View History
                                    6. Log out
                                    7. Exit
                                    Please choose any of the above:\s"""
                    );
                    choice = input.nextInt();
                    if (choice > 6 || choice < 1)
                        throw new InputOutOfBound("Please make sure you made a right selection.");
                    switch (choice) {
                        case 1: {
                            // see flights
                            // after the flights are displayed there has to be option for booking
                            // that booking actually invokes airline.user.book()
                            ArrayList bookingInfo = new ArrayList();

                            String response = seeFlights(bookingInfo);
                            if(response.equals("notFound") ){
                                System.out.println("Sorry no Flights Available, Please choose another date. ");
                            }else if ( response.equals("goBack")){}
                            else  {
                                //start taking information.. iske liye method likhna chahiye kyuu user logedin me bhi yahii kam ho rha hoga
                                //static take info method should be implemented... which returns Arraylist of string[] each string array represents a passenger
                                ArrayList<String[]> passengers = takeInfoForPassengers((int)bookingInfo.get(1));
                                if(passengers!=null){
                                    while(true){
                                        try{
                                            System.out.println("\nCalculated Bill PKR: " + bookingInfo.get(bookingInfo.size()-1) + "\n");
                                            System.out.print("Confirm Booking?\n1. Yes\n2. No\nYour Choice: ");
                                            int cchoice = input.nextInt();
                                            if(cchoice != 1 && cchoice !=2) throw new InputOutOfBound("Please input from given choices");
                                            if(cchoice == 1){
                                                airline.user.book(passengers,bookingInfo);
                                                System.out.println("Booked Successfully!\n ");
                                            }
                                            break;
                                        }
                                        catch(InputOutOfBound eob){
                                            System.out.println(eob.getMessage());

                                        }
                                        catch(InputMismatchException emm){
                                            System.out.println("Please choose between 1 OR 2");
                                            input.nextLine();
                                        }

                                    }
                                }
                            }
                            break;
                        }
                        case 2:{
                            // view notifications....
                            ArrayList<String[]> notifications = airline.user.getNotifications();
                            if(notifications==null) System.out.println("No notifications!");
                            else {
                                // sout(headers)// datetime = XXXX-XX-XX HH:MM:SS
                                System.out.println(String.format("%-14s","Date")+String.format("%-14s","Time")+"Notification");
                                for(int i=0; i<=notifications.size()-1; i++){
                                    String[] notification = notifications.get(i);
                                    String[] datetime = notification[1].split(" ");

                                    System.out.println(String.format("%-14s",datetime[0]) + String.format("%-14s",datetime[1])+notification[0]);
                                }
                            }

                            break;
                        }

                        case 3: {
                            ResultSet bookings = airline.user.getBookings();
                            displayBookings(bookings,false);
                            break;
                        }
                        case 4: {
                            String bookingId = displayBookings(airline.user.getBookings(),true);
                            airline.user.cancelBooking(bookingId);
                            break;
                        }
                        case 5: {
                            // display booking history of the user
                            System.out.println("HISTORY");
                            displayBookings(airline.user.getHistory(),false);
                            break;
                        }
                        case 6: {
                            //logout ...airline class me
                            airline.user.logOut();
                            break;
                        }
                        case 7: {
                            System.out.println("Exiting Flight Reservation System");
                            System.exit(0);
                        }
                    }
                }
                else if (airline.admin.getSignedInStatus()) {
                    //if the user is an admin he should be able to following options
                    System.out.print(
                            """
                                    1. Add a route
                                    2. Cancel a route
                                    3. Cancel a flight
                                    4. Log out
                                    5. Exit
                             
                                    Please choose any of the above:\s"""
                    );
                    choice = input.nextInt();
                    if (choice > 5 || choice < 1) throw new InputOutOfBound("Make sure your input is correct");

                    switch (choice) {
                        case 1: {

                            // the admin is supposed to enter a new flight and information relevant to the fight..
                            // and that information is inserted into the flights table in database
                            System.out.print("Enter Flight ID: ");
                            input.nextLine();
                            String fid = input.nextLine();
                            System.out.print("Enter Travel To: ");
                            String trvto = input.nextLine();
                            System.out.print("Enter travel From: ");
                            String trvfrom = input.nextLine();
                            System.out.print("Enter Number of Business Seats: ");
                            String bseats = input.nextLine();
                            System.out.print("Enter Number of Economy Seats: ");
                            String eseats = input.nextLine();
                            String time = inputTime();
                            System.out.println("Enter Price for Economy: ");
                            String priceE = input.nextLine();
                            System.out.println("Enter Price for Business: ");
                            String priceB = input.nextLine();


                            ArrayList<String> flightinfo = new ArrayList<String>();
                            flightinfo.add(fid);
                            flightinfo.add(trvto);
                            flightinfo.add(trvfrom);
                            flightinfo.add(bseats);
                            flightinfo.add(eseats);
                            flightinfo.add(time);
                            flightinfo.add(priceE);
                            flightinfo.add(priceB);
                            try {
                                airline.admin.addRoute(flightinfo);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;

                        }
                        // admin cancels a route
                        case 2: {
                            //cancel a route
                            // show admin to and froms .. display the flights of that to and from..
                            // ask admin for selection to grab flight ID and then call airline.cancelRoute
                            ArrayList<String> froms = airline.getFroms();
                            String from = inputFrom(froms);

                            ArrayList<String> destinations = airline.getDestinations(from);
                            String destination = inputDestination(destinations);
                            //ab mujhe isme se flight id select krni he ,
                            // unlike user ki tarah number of passengers and seatType , isme matter nahi krty
                            ResultSet resultSet=airline.getFlights(from,destination);
                            String response =  showFlights(resultSet);
                            if(response.equals("notFound")){
                                System.out.println("No active route between "+destination+" and "+from+'.');
                            }else if(response.equals("goBack")){}
                            else {
                                String mostRecentlyBookedDate=airline.admin.cancelRoute(response);
                                if(!(mostRecentlyBookedDate==null))
                                System.out.println("\nFlight "+response+ " is only taking bookings till "+mostRecentlyBookedDate+".\n");
                                else System.out.println("\nFlight "+response+ " is no longer available.");
                            }
                            break;
                        }
                        // admin cancels a route for a date
                        case 3: {
                            //cancel a flight
                            // the admin is supposed to enter a date and to-from , and on that specific date all the bookings of a to-from gets cancelled
                            /* admin should see the flights whose active till column is null
                            * */

                            String from = inputFrom(airline.getFroms());
                            String destination = inputDestination(airline.getDestinations(from));
                            String date = inputBookingDate();
                            ResultSet resultSet = airline.getFlights(from,destination,date);

                            String response =  showFlights(resultSet);
                            if(response.equals("notFound")){
                                System.out.println("No active route between "+destination+" and "+from+" on "+date+".");
                            }else if(response.equals("goBack")){}
                            else {
                                System.out.print("Enter a message for passengers \n Message: ");
                                input.nextLine();
                                String message = input.nextLine();
                                airline.admin.cancelFlight(response,date,message);
                                System.out.println("The flight "+ response + " operation for date "+date+" have been cancelled!");
                            }
                            break;
                        }
                        case 4: {
                            //log out
                            //when the admin logs out. the admin attribute in airline is assigned to null again
                            //airline.
                            airline.admin.logOut();
                            break;
                        }
                        case 5: {
                            System.out.println("Exiting Flight Reservation System");
                            System.exit(0);
                        }
                    }
                }
                //once the program starts the neither admin nor user is signed in.. and he gets to see following options
                //if !airline.user.isSigned ..if user is null that means it not yet signed in..
                else {
                    System.out.print(
                        "1. See flights\n" +
                        "2. Log in\n" +
                        "3. Sign up\n" +
                        "4. Exit\n\n" +
                        "Please choose any of the above: "
                    );
                    choice = input.nextInt();
                    if (choice > 4 || choice < 1) throw new InputOutOfBound("Make sure your input is correct");
                    switch (choice) {

                        // all the flight operations gets displayed
                        case 1: {
                            ArrayList bookingInfo = new ArrayList();
                            String flight = seeFlights(bookingInfo);
                            if(flight.equals("goBack")){
                                break;
                            }else if (flight.equals("notFound")){
                                System.out.println("Sorry no Flights Available, Please choose another date. ");
                                break;
                            }
                            else  {
                                ArrayList<String[]> passengers = takeInfoForPassengers((int)bookingInfo.get(1));
                                if(passengers!=null){

                                    while(true){
                                        try{
                                            System.out.println("\nCalculated Bill PKR: " + bookingInfo.get(bookingInfo.size()-1) + "\n");
                                            System.out.print("Confirm Booking?\n1. Yes\n2. No\nYour Choice: ");
                                            int cchoice = input.nextInt();
                                            if(cchoice != 1 && cchoice !=2) throw new InputOutOfBound("Please input from given choices");

                                            if(cchoice == 1){
                                                airline.user.book(passengers,bookingInfo);
                                                System.out.println("Booked Successfully!\n ");
                                            }
                                            break;

                                        }
                                        catch(InputOutOfBound eob){
                                            System.out.println(eob.getMessage());

                                        }
                                        catch(InputMismatchException emm){
                                            System.out.println("Please choose between 1 OR 2");
                                            input.nextLine();
                                        }

                                    }

                                }
                            }
                            break;
                        }


                        case 2: {
                            System.out.print("Username: ");
                            String username = input.next();
                            System.out.print("\nPassword: ");
                            String password = input.next();
                            airline.login(username, password);
                            break;
                        }

                        case 3: {
                            System.out.println("Sign Up");
                            String[] signUpData = collectSignUp();
                            airline.signUp(signUpData);
                            System.out.println("Signing up successful.");
                            break;
                        } //complete

                        //End Program
                        case 4: {
                            System.out.println("\n\nExiting Flight Reservation System");
                            System.exit(0);
                        }
                    }
                }
            } catch (InputOutOfBound ex) {
                System.out.println(ex);
            } catch (InputMismatchException ex) {
                input.nextLine();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } while (true);
    }

    public static String displayBookings(ResultSet resultSet,boolean wantsToCancelBooking) throws SQLException{
        while(true){
            //headers
            System.out.println(
                    "_".repeat(147)+'\n'+
                    String.format("%-4s","")+
                            String.format("%-13s","Flight ID")+
                            String.format("%-15s","From")+
                            String.format("%-15s","To")+
                            String.format("%-20s","Name")+
                            String.format("%-19s","CNIC")+
                            String.format("%-9s","Class")+
                            String.format("%-18s","Departure Time")+
                            String.format("%-18s","Departure Date")+
                            String.format("%-16s","Booking Date")+"|\n"+
                            "=".repeat(147)+"|\n"+
                            String.format("%148s",'|')
            );
            int count=1;
            ArrayList<String> bookingIds = new ArrayList<>();
            while (resultSet.next()){

                bookingIds.add(resultSet.getString("bookingId"));
                //  148
                System.out.println(
                                String.format("%-4s",count+".")+
                                String.format("%-13s",resultSet.getString("flightId"))+
                                String.format("%-15s",resultSet.getString("travelFrom"))+
                                String.format("%-15s",resultSet.getString("travelTo"))+
                                String.format("%-20s",resultSet.getString("fullName"))+
                                String.format("%-19s",resultSet.getString("cnic"))+
                                String.format("%-9s",resultSet.getString("seatType"))+
                                String.format("%-18s",resultSet.getString("takeOffTime"))+
                                String.format("%-18s",resultSet.getString("bookedForDate"))+
                                String.format("%-16s",resultSet.getString("bookedOnDate"))+ "|"
                );
                count++;
            }
            if(count==1){
                System.out.println(String.format("%89s","You do not have any Bookings!")+
                        String.format("%59s",'|'));
            }
            System.out.println();

            if(wantsToCancelBooking){
                System.out.println(String.format("%-4s",count+".")+"Go Back");
                System.out.print("Choose a booking that you want to cancel: ");
                try{
                    int choice = input.nextInt();
                    if(choice>count|| choice<1) throw  new InputOutOfBound("Select From 1 to "+count);
                    if(choice==count){return "goBack";}
                    else return bookingIds.get(choice-1);
                }catch (InputMismatchException inputMismatchException){
                    System.out.println("Please Input Integers Only!");
                    input.nextLine();
                    continue;
                }catch (InputOutOfBound inputOutOfBound){
                    System.out.println(inputOutOfBound);
                    continue;
                }
            }
            return "";
        }

    }

    private static String showFlights(ResultSet resultSet) throws SQLException {
        boolean flightFound =false;
        ArrayList<String[]> flights = new ArrayList<>();
        while(resultSet.next()){
            if(!flightFound) System.out.println("\nActive Flights from "+resultSet.getString("travelFrom")+" to "+resultSet.getString("travelTo")+"\n");
            flightFound=true;
            String[] flight = new String[2];
            flight[0]=resultSet.getString("flightId");
            flight[1]=resultSet.getString("takeOffTime");
            flights.add(flight);
        }
        if (flightFound){
            //sout(headers)
            System.out.println(String.format("%-4s","")+String.format("%-20s","Flight ID")+ String.format("%-20s","Time"));
            for(int i=0;i<flights.size();i++){
                //sout(info) of each flight
                String[] flight = flights.get(i);
                System.out.println(
                        String.format("%-4s",(i+1)+".")+
                        String.format("%-20s",flight[0])+
                        String.format("%-20s",flight[1])
                );
            }
            System.out.print(String.format("%-4s",(flights.size()+1))
                    +"Go back\n" +
                    "please make a choice between 1 and "+(flights.size()+1)+
                    "\nChoice: "
            );
            int choice = input.nextInt()-1;
            if (choice==flights.size()) {
                return "goBack";
            }
            else {
                return flights.get(choice)[0];
            }
        }
        else return "notFound";
        // possible strings flightId .. go back .. notFound
        //yahan jaa kr see flights kaa pura hua
        // mujhe return karani chahiye string.. agar user peeche jana chahta he tou go back ki string jaegi.. or jahan se method call hua he wahan if go back then break ki instruction chale
    }

    private static String seeFlights(ArrayList bookingInfo) {

        while (true) {
            try {

                String date = inputBookingDate();
                ArrayList<String> froms = airline.getFroms();
                String from = inputFrom(froms);
                ArrayList<String> destinations = airline.getDestinations(from);
                String destination = inputDestination(destinations);
                int numberOfPassengers;
                while (true){
                    try {
                        System.out.print("How many passengers? ");
                         numberOfPassengers = input.nextInt();
                        break;
                    }catch (InputMismatchException ime){
                        System.out.println("Please provide an integer!");
                        input.nextLine();
                    }
                }
                char seatType;
                while (true) {
                    try{
                        
                    System.out.print("Enter Type \n" +
                    "B for Business\n" +
                    "E for Economy\n" +
                    "Your Choice: ");
                    seatType = input.next().toUpperCase(Locale.ROOT).charAt(0);
                    if (!(seatType == 'B' || seatType == 'E')) throw new InvalidInput("Please choose between B and E");
                    break;

                    }catch(InvalidInput invinp){
                        System.out.println(invinp.getMessage());
                    }
                }

                ResultSet resultSet = airline.getFlights(date,destination,from,numberOfPassengers,seatType);
                if (resultSet==null) return "notFound";
                double price = resultSet.getDouble("price"+seatType)*numberOfPassengers;
                String flight = showFlights(resultSet);

                bookingInfo.add(date);
                bookingInfo.add(numberOfPassengers);
                bookingInfo.add(seatType);
                bookingInfo.add(flight);
                bookingInfo.add(price);


                return flight;
            } catch (java.text.ParseException ex) {
                System.out.print("date input type mismatched\n" +
                        "Please re-enter correctly: ");
            } catch (InputMismatchException ex) {
                System.out.println("Please enter legal values!");
                input.nextLine();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            } catch (InputOutOfBound inputOutOfBound) {
                System.out.println(inputOutOfBound);
            }
        }
    }

    private static ArrayList<String[]> takeInfoForPassengers(int numberOfPassengers){
        ArrayList<String[]> passengers = new ArrayList<>();

        //full name and cnic
        for(int i=0;i<numberOfPassengers;i++){
            String[] passenger = new String[2];
            System.out.println("Information for passenger "+(i+1));
            input.nextLine();
            passenger[0] = inputName();
            passenger[1] = inputCnic();
            passengers.add(passenger);

            if(i+1<numberOfPassengers){
                int choice;
                while (true){
                    try {
                        System.out.print(
                                "1. Continue for next passenger\n" +
                                        "2. drop\n" +
                                        "choice: ");
                        choice = input.nextInt();
                        if (!(choice==1 || choice==2)) throw new InputOutOfBound("");
                        break;
                    }

                    catch (InputMismatchException ex){
                        System.out.println("Please input integers only");
                        input.nextLine();
                    }
                    catch (InputOutOfBound ex){
                        System.out.println("Please choose between 1 and 2.");
                    }

                }
                if(choice==2) return null;
            }
        }
        return passengers;
    }

    public static String inputFrom(ArrayList<String> froms) throws InputOutOfBound,InputMismatchException{
        while(true){
            try{
                System.out.println("Please Select an Origin: ");
        int fromsLength = froms.size();
        for (int i=0;i<fromsLength;i++){
            System.out.println((i+1)+". "+froms.get(i));
        }
        System.out.print("please make a choice from 1 to "+fromsLength+"\n" +
                "Fly From: ");
        int choice = input.nextInt();
        if (choice<1 ||choice>fromsLength) throw new InputOutOfBound("Please make sure your input lies in range");
        return froms.get(choice-1);

            }catch(InputOutOfBound iob){
                System.out.println("Please provide the value from given range");
                
            }catch(InputMismatchException imm){
                System.out.println("Please provide an integer only");
                input.nextLine();
            }
        }
        
    }

    public static String inputDestination(ArrayList<String> destinations) throws InputOutOfBound,InputMismatchException{
    while(true){
        try{
                System.out.println("Please Select a Destination ");
        int destinationsLength = destinations.size();
        for (int i=0;i<destinationsLength;i++){
            System.out.println((i+1)+". "+destinations.get(i));
        }
        System.out.print("please make a choice from 1 to "+destinationsLength+"\n" +
                "Fly To: ");
        int choice = input.nextInt();
        if (choice<1 ||choice>destinationsLength) throw new InputOutOfBound("Please make sure your input lies in range");
        return destinations.get(choice-1);
    }catch(InputOutOfBound iob){
        System.out.println("Please provide the value from given range");
        
    }catch(InputMismatchException imm){
        System.out.println("Please provide an integer only");
        input.nextLine();
    }}
    }

    // VALIDATIONS

    public static String inputTime(){
        String pattern = "(([0-1][0-9])|([2][0-3])):([0-5][0-9]):([0-5][0-9])";
        Pattern timePattern = Pattern.compile(pattern);
        while (true){
            try {
            System.out.println("Time Format Should be HH:MM:SS in 24 Hours. ");
            System.out.print("Input Time: ");
            String time =input.nextLine();
            Matcher matcher = timePattern.matcher(time);
            boolean matches = matcher.matches();
            if(!matches) throw  new InputMismatchException("Please Enter a Valid Time!");
            return time;
            }
            catch (InputMismatchException ex){
                System.out.println(ex.getMessage());
            }
        }
    }

    private static String inputDate(String msg, String exceptionMsg)  {

        int Leap = 21;
        int NL= 22;
        int Days28 = 19;
        int Days29 = 13;
        int Days30 = 24;
        int Days31 = 25;
        int Trap = 0;
        int Final = 23;

        char[] allowedChars = {'0','1','2','3','4','5','6','7','8','9'};
        int[] finalStates = {Final};
        int initialState = 1;
        int[][] transitionTable = {
                {Trap, Trap, Trap, Trap, Trap, Trap, Trap, Trap, Trap, Trap},
                {2 ,3, 2, 3, 2, 3, 2, 3, 2, 3},
                {4,	5, 5, 5, 4, 5, 5, 5, 4, 5},
                {5, 5, 4, 5, 5, 5, 4, 5, 5, 5},
                {8, 10, 9, 10 ,9 ,10 ,9 ,10 ,9, 10},
                {7,6,7,6,7,6,7,6, 7, 6},
                {NL, NL, Leap, NL, NL, NL, Leap, NL, NL, NL},
                {Leap, NL, NL, NL, Leap, NL, NL, NL, Leap, NL},
                {NL, NL, NL, NL, Leap, NL, NL, NL, Leap, NL},
                {Leap, NL, NL, NL, Leap, NL, NL, NL, Leap, NL},
                {NL, NL, Leap, NL, NL, NL, Leap, NL, NL, NL},
                {Trap, Days31, Days29, Days31, Days30, Days31, Days30, Days31, Days31, Days30},
                {Days31, Days30, Days31, Trap, Trap, Trap, Trap, Trap ,Trap, Trap},
                {14, 15, 15,Trap, Trap, Trap, Trap, Trap, Trap, Trap},
                {Trap, Final, Final, Final, Final, Final, Final, Final, Final, Final},
                {Final, Final, Final, Final, Final, Final, Final, Final, Final, Final},
                {Final, Trap, Trap, Trap, Trap, Trap, Trap, Trap, Trap, Trap},
                {Final, Final, Trap, Trap, Trap, Trap, Trap, Trap, Trap, Trap},
                {Trap, Days31, Days28, Days31, Days30, Days31, Days30, Days31, Days31, Days30},
                {14, 15, 20,Trap, Trap, Trap, Trap, Trap, Trap, Trap},
                {Final, Final, Final, Final, Final, Final, Final, Final, Final, Trap},
                {11, 12,Trap, Trap, Trap, Trap, Trap, Trap, Trap, Trap},
                {18, 12, Trap, Trap, Trap, Trap, Trap, Trap, Trap, Trap},
                {Trap, Trap, Trap, Trap, Trap, Trap, Trap, Trap, Trap, Trap},
                {14, 15, 15, 16, Trap, Trap, Trap, Trap, Trap, Trap},
                {14, 15, 15, 17, Trap, Trap, Trap, Trap, Trap, Trap},
        };
        FiniteAutomata dateDFA = new FiniteAutomata(allowedChars,initialState,finalStates,transitionTable);
        Pattern formatPattern = Pattern.compile("....-..-..");
        while (true){
            try {
                System.out.print("Date format should be YYYY-MM-DD\n" +
                        msg +": ");
                String date =input.next();
                if(!(dateDFA.validate(date.replaceAll("-","")) && formatPattern.matcher(date).matches()))
                    throw  new InvalidDateException("Please enter a valid date!");
                return date;
            }catch(InvalidDateException ide){
                System.out.println(ide.getMessage());
            }
        }
    }

    public static String inputBookingDate() throws ParseException {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE,60);
        Date datePlus60 = cal.getTime();
        while (true){
            String dateStr = inputDate("Book For Date","Date doesn't make sense");
            Date dateIn = new SimpleDateFormat("yyyy-MM-dd").parse(dateStr);
            try {
                if(dateIn.compareTo(new Date())<0) throw new InvalidDateException("The date you entered has already past");
                if(datePlus60.compareTo(dateIn)<0) throw new InvalidDateException("Sorry, The system takes bookings within 60 days only! ");
                return dateStr;
            } catch (InvalidDateException e) {
                System.out.println(e.getMessage());
            }

        }
    }

    private static String inputDob() {
        while(true){
            try {
                String date = inputDate("Birth Date","Date doesn't make sense!");
                Date dob = new SimpleDateFormat("yyyy-MM-dd").parse(date);
                Date dateToday = new Date();
                if (dateToday.getYear() - dob.getYear() < 18) {
                    throw new Exception("you must be 18 or older to sign up");
                }
                return date;
            } catch (ParseException e) {
                System.out.println("Invalid date format, please re-enter");
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }


    // SIGN UP
    private static String[] collectSignUp(){
        String[] signUpData = new String[9];
        try {
            input.nextLine();
            signUpData[0] = inputName();
            signUpData[1] = inputUsername();
            input.nextLine();
            signUpData[2] = inputPassword();
            signUpData[3] = inputCnic();
            signUpData[4] = inputPhone();
            input.nextLine();
            signUpData[5] = inputAddress();
            signUpData[6] = inputGender();
            signUpData[7] = inputDob();
            input.nextLine();
            signUpData[8] = inputEmail();

        }catch (Exception ex){
            System.out.println(ex.getMessage());
            return null;
        }
        return signUpData;
    }

    private static String inputName(){

        Pattern pattern = Pattern.compile(new String ("^[a-zA-Z]+[\\-'\\s]?[a-zA-Z ]+$"));
        String name;
        while(true){
            try {
                System.out.print("Full Name: ");
                name = input.nextLine();
                Matcher matcher = pattern.matcher(name);
                if(!matcher.matches()) throw new InputMismatchException();
                break;

            }
            catch (InputMismatchException e){
                System.out.println("Please enter a valid name!");
                input.nextLine();
            }
        }
        String capitalized="";
        boolean convertNext = true;
        for (char ch : name.toCharArray()) {
            if (Character.isSpaceChar(ch)) {
                convertNext = true;
            } else if (convertNext) {
                ch = Character.toTitleCase(ch);
                convertNext = false;
            } else {
                ch = Character.toLowerCase(ch);
            }
            capitalized+=ch;
        }
        return capitalized;
    }

    private static String inputCnic() {
        String cnic;
        while (true) {
            try {
                System.out.print("cnic should be separated by \"-\"\nCNIC Number: ");
                cnic = input.nextLine();
                String cnicPatternStructure = "^[1-7][0-9]{4}-\\d{7}-\\d{1}$";
                Pattern cnicPattern = Pattern.compile(cnicPatternStructure);
                Matcher cnicPatternMatcher = cnicPattern.matcher(cnic);
                if (!cnicPatternMatcher.matches()) throw new InvalidInput("Please Enter a Valid CNIC number! ");
                return cnic;
            } catch (InvalidInput ex) {
                System.out.println(ex.getMessage());
                input.nextLine();
            }
        }
    }

    private static String inputPhone() {
        String phone;
        while (true) {
            try {
                System.out.print("Phone: ");
                phone = input.next();
                String phonePatternStructure = "[\\d]{6,16}";
                Pattern cnicPattern = Pattern.compile(phonePatternStructure);
                Matcher cnicPatternMatcher = cnicPattern.matcher(phone);
                if (!cnicPatternMatcher.matches()) throw new InputMismatchException();
                return phone;
            } catch (InputMismatchException ex) {
                System.out.print("Please Enter a valid phone number! ");
            }
        }
    }

    private static String inputAddress(){
        while (true){
            System.out.print("address: ");
            String address = input.nextLine();
            if(address.length()<5){
                System.out.println("Invalid address! Enter a valid one. ");
                continue;
            }
            return address;
        }
    }

    private static String inputGender(){
        while (true){
            try{
                System.out.print("Gender:\n" +
                        "1. Male\n" +
                        "2. Female\n" +
                        "Choice: ");
                int choice = input.nextInt();
                if (choice<1 || choice>2) throw  new InputOutOfBound();
                if(choice==1) return "M";
                else return "F";
            }catch (InputMismatchException ime){
                System.out.println("Select between Integers Only");
                input.nextLine();
            }catch (InputOutOfBound iob){
                System.out.println(" Please select either 1 or 2");
            }
        }

    }

    private static String inputEmail(){
        Pattern emailPattern = Pattern.compile("[a-z]+[0-9]*([.][a-z0-9]+)*[a-z0-9]*@[a-z]+[.](com|org|edu|net|io)([.]([a-z]{2}))?");
        while(true){
            try{
                System.out.print("email: ");
                String email = input.nextLine();
                Matcher matcher = emailPattern.matcher(email);
                if(!matcher.matches()) throw new Exception("Email not valid, re-enter a valid one!");
                return email;
            }
            catch (Exception ex){
                System.out.println(ex.getMessage());
            }
        }
    }

    private static String inputUsername() throws SQLException{
        while(true) {
            try {
                System.out.print("username: ");
                String username = input.next();
                Pattern usernamePattern= Pattern.compile("^[a-zA-Z0-9]([._-](?![._-])|[a-zA-Z0-9]){3,18}[a-zA-Z0-9]$");
                Matcher matcher = usernamePattern.matcher(username);
                if(!matcher.matches()) throw new UsernameException("Please enter a valid username.");
                if (airline.usernameAlreadyExists(username)) throw new UsernameException("Username not available! Please re-enter a different one");
                return username;
            }catch (UsernameException ue){
                System.out.println(ue.getMessage());
            }
        }
    }

    public static String inputPassword(){

        String passwordRe =
                "[a-zA-Z0-9]*[a-z][a-zA-Z0-9]*[A-Z][a-zA-Z0-9]*[0-9][a-zA-Z0-9]*|"+
                        "[a-zA-Z0-9]*[a-z][a-zA-Z0-9]*[0-9][a-zA-Z0-9]*[A-Z][a-zA-Z0-9]*|"+
                        "[a-zA-Z0-9]*[A-Z][a-zA-Z0-9]*[a-z][a-zA-Z0-9]*[0-9][a-zA-Z0-9]*|"+
                        "[a-zA-Z0-9]*[A-Z][a-zA-Z0-9]*[0-9][a-zA-Z0-9]*[a-z][a-zA-Z0-9]*|"+
                        "[a-zA-Z0-9]*[0-9][a-zA-Z0-9]*[a-z][a-zA-Z0-9]*[A-Z][a-zA-Z0-9]*|"+
                        "[a-zA-Z0-9]*[0-9][a-zA-Z0-9]*[A-Z][a-zA-Z0-9]*[a-z][a-zA-Z0-9]*";
        Pattern passwordPattern = Pattern.compile(passwordRe);
        while (true){

            System.out.print("password: ");
            String password =input.nextLine();
            Matcher matcher = passwordPattern.matcher(password);
            if(matcher.matches()) return password;
            else{
                System.out.println("""
                   
                   ********  Password guidelines  ********
                   Password must include at least 
                   one upper case letter,
                   one lower case letter, 
                   and one numeric digit.
                    """);
            }
        }
    }




}

