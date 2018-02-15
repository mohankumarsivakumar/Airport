package global.coda.airport;

import global.coda.connection.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;
import global.coda.bean.Customer;
import global.coda.bean.Flight;
import global.coda.bean.Passenger;
import global.coda.bean.Schedule;
import global.coda.bean.Transaction;

import com.mysql.jdbc.Connection;

public class Implementation {
	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

	public int verifyLogin(String phoneNo, String password) throws SQLException {
		Connection co = (Connection) ConnectionClass.establish();
		String query = "select * from customer where contact_number=(?) and password=(?)";
		PreparedStatement preparedStmt = co.prepareStatement(query);
		preparedStmt.setString(1, phoneNo);
		preparedStmt.setString(2, password);
		ResultSet rs = preparedStmt.executeQuery();
		int rowCount = 0;
		while (rs.next())
			rowCount++;
		ConnectionClass.closeConnection(co);
		return rowCount;
	}

	public void createLogin(int phoneNo, String name, String gender, String password) throws SQLException {
		Connection co = (Connection) ConnectionClass.establish();
		String query = "insert into customer values (?,?,?,?)";
		PreparedStatement preparedStmt = co.prepareStatement(query);
		preparedStmt.setInt(1, phoneNo);
		preparedStmt.setString(2, name);
		preparedStmt.setString(3, gender);
		preparedStmt.setString(4, password);
		int rs = preparedStmt.executeUpdate();
		if (rs == 1) {
			System.out.println("insert performed successfully!!!");
			Customer c = new Customer();
			c.createCustomer(phoneNo, name, gender, password);
		} else
			System.out.println("try again!!!");
	}

	public void search(String source, String destination, Date date, String type, int number, String filter)
			throws SQLException {
		Connection co = (Connection) ConnectionClass.establish();
		String query = null;
		if (type.equalsIgnoreCase("e"))
			query = "select flight_schedule.flight_schedule_no,flight.airline,flight.boarding_place,flight.destination,flight_schedule.date,flight_schedule.departure,flight_schedule.economy_fare,flight_schedule.economy_seats"
					+ " from flight inner join flight_schedule on flight.id = flight_schedule.flight_id and flight.boarding_place=(?) "
					+ "and flight.destination=(?) and flight_schedule.date=(?)and flight_schedule.economy_seats>="
					+ number + " order by " + filter;
		else if (type.equalsIgnoreCase("b"))
			query = "select flight_schedule.flight_schedule_no,flight.airline,flight.boarding_place,flight.destination,flight_schedule.date,flight_schedule.departure,flight_schedule.business_fare,flight_schedule.business_seats"
					+ " from flight inner join flight_schedule on flight.id = flight_schedule.flight_id and flight.boarding_place=(?) "
					+ "and flight.destination=(?) and flight_schedule.date=(?)and flight_schedule.business_seats>="
					+ number + " and order by " + filter;

		PreparedStatement preparedStmt = co.prepareStatement(query);
		preparedStmt.setString(1, source);
		preparedStmt.setString(2, destination);
		preparedStmt.setString(3, df.format(date));
		ResultSet rs = preparedStmt.executeQuery();
		if (rs.next() == false)
			System.out.println("no match found for your request");
		else {
			System.out.println("flight_schedule_no,airline,boarding_place,destination,date,departure,fare,seats");
			do {
				System.out.println(rs.getInt(1) + "\t" + rs.getString(2) + "\t" + rs.getString(3) + "\t"
						+ rs.getString(4) + "\t" + df.format(rs.getDate(5)) + "\t" + rs.getTime(6) + "\t"
						+ rs.getFloat(7) + "\t" + rs.getInt(8));

			} while (rs.next());

		}
		ConnectionClass.closeConnection(co);
	}

	public void book(int pnr, String cid, int scheduleNo, String types, int noofSeat) throws SQLException {
		Connection co = (Connection) ConnectionClass.establish();
		String q = null;
		if (types.equals("e")) {
			q = " select (?)*economy_fare from flight_schedule where flight_schedule_no=(?)";
		} else if (types.equals("b")) {
			q = " select (?)*business_fare from flight_schedule where flight_schedule_no=(?)";
		}
		PreparedStatement smt = co.prepareStatement(q);
		smt.setInt(1, noofSeat);
		smt.setInt(2, scheduleNo);
		ResultSet rs = smt.executeQuery();
		float amnt = 0;
		while (rs.next()) {
			amnt = rs.getInt(1);
		}
		String query = "insert into transaction values (?,?,?,?,?,?,?)";
		PreparedStatement preparedStmt = co.prepareStatement(query);
		preparedStmt.setInt(1, pnr);
		preparedStmt.setString(2, cid);
		preparedStmt.setInt(3, scheduleNo);
		preparedStmt.setString(4, types);
		preparedStmt.setInt(5, noofSeat);
		preparedStmt.setString(6, "booked");
		preparedStmt.setFloat(7, amnt);
		int value = preparedStmt.executeUpdate();
		if (value == 1) {

			Transaction t = new Transaction();
			t.setTransaction(pnr, cid, scheduleNo, types, noofSeat, amnt);
			String names;
			Scanner s = new Scanner(System.in);
			Passenger p = new Passenger();
			for (int j = 0; j < noofSeat; j++) {
				System.out.println("passenger:" + (j + 1));
				System.out.println("enter name:");
				names = s.next();
				String qry = "insert into passenger(pnr_no,passenger_name) values(?,?)";
				PreparedStatement Stmt = co.prepareStatement(qry);
				Stmt.setInt(1, pnr);
				Stmt.setString(2, names);
				Stmt.execute();
				p.setPassenger(pnr, names);

			}
			if (types.equalsIgnoreCase("e")) {
				query = "update flight_schedule set economy_seats=economy_seats-(?) where flight_schedule_no=(?)";
				preparedStmt = co.prepareStatement(query);
				preparedStmt.setInt(1, noofSeat);
				preparedStmt.setInt(2, scheduleNo);
				preparedStmt.executeUpdate();
			} else if (types.equalsIgnoreCase("b")) {
				query = "update flight_schedule set business_seats=business_seats-(?) where flight_schedule_no=(?)";
				preparedStmt = co.prepareStatement(query);
				preparedStmt.setInt(1, noofSeat);
				preparedStmt.setInt(2, scheduleNo);
				preparedStmt.executeUpdate();
			}
			System.out.println("booked successfully!!! your pnr is" + pnr);
		} else
			System.out.println("try again!!!");
		ConnectionClass.closeConnection(co);
	}

	public void checkin(int pnrno) throws SQLException {
		Connection co = (Connection) ConnectionClass.establish();
		String query = null;
		query = "select flight_schedule_no,flight_class,no_of_seats from transaction where pnr=(?) and status=(?)";
		PreparedStatement preparedStmt = co.prepareStatement(query);
		preparedStmt.setInt(1, pnrno);
		preparedStmt.setString(2, "booked");
		ResultSet r = preparedStmt.executeQuery();
		int scheduleNo, noofSeat, flightNo;
		String clas;
		if (r.next() == false)
			System.out.println("enter valid pnr!!!");
		else {
			do {
				scheduleNo = r.getInt(1);
				noofSeat = r.getInt(3);
				clas = r.getString(2);
			} while (r.next());
			query = "select flight_id from flight_schedule where flight_schedule_no=(?)";
			preparedStmt = co.prepareStatement(query);
			preparedStmt.setInt(1, scheduleNo);
			r = preparedStmt.executeQuery();
			ArrayList a = new ArrayList(100);
			ArrayList b = new ArrayList(100);
			if (r.next()) {
				flightNo = r.getInt(1);
				query = "select start_seat_no,ending_seat_no from flight inner join seats on flight.id=seats.flight_id and flight.id=(?) and seats.flight_class=(?)";
				preparedStmt = co.prepareStatement(query);
				preparedStmt.setInt(1, flightNo);
				preparedStmt.setString(2, clas);
				r = preparedStmt.executeQuery();
				int start = 0, end = 0;
				while (r.next()) {
					start = r.getInt(1);
					end = r.getInt(2);
				}

				for (; start <= end; start++) {
					a.add(start);
				}

				query = "select seat_no from passenger";
				preparedStmt = co.prepareStatement(query);
				r = preparedStmt.executeQuery();

				while (r.next()) {
					b.add(r.getInt(1));
				}
				a.removeAll(b);
				int j = 0;
				while (j < noofSeat) {
					System.out.println("select seat number alone for : " + j + "th passenger");
					for (int i = 0; i < a.size(); i++) {
						if ((int) a.get(i) % 2 == 0)
							System.out.println("Normal:" + a.get(i));
						else
							System.out.println("Window:" + a.get(i));
					}
					Scanner sc = new Scanner(System.in);
					int seat = sc.nextInt();
					if (!a.contains(seat)) {

						continue;
					}

					else {
						query = "update passenger set seat_no=(?) where pnr_no=(?) and seat_no=0 limit 1";
						preparedStmt = co.prepareStatement(query);
						preparedStmt.setInt(1, seat);
						preparedStmt.setInt(2, pnrno);
						preparedStmt.executeUpdate();
						a.remove(seat);
					}
					j++;
				}
				query = "update transaction set status=checkedin where pnr=(?)";
				preparedStmt = co.prepareStatement(query);
				preparedStmt.setInt(1, pnrno);
				preparedStmt.executeUpdate();
			} else
				System.out.println("error .Try again!!!");

		}

		ConnectionClass.closeConnection(co);
	}

	public void cancel(int pnrno) throws SQLException {
		Connection co = (Connection) ConnectionClass.establish();
		String query = null;
		query = "select no_of_seats,flight_class,flight_schedule_no from transaction where pnr=(?) and status=(?)";
		PreparedStatement preparedStmt = co.prepareStatement(query);
		preparedStmt.setInt(1, pnrno);
		preparedStmt.setString(2, "booked");
		ResultSet r = preparedStmt.executeQuery();
		int seatcount = 0;
		String cls = null;
		int flight_schedule_no = 0;
		if (r.next() == false) {
			System.out.println("invalid pnr.try again");

		}
		do {
			cls = r.getString(2);
			seatcount = r.getInt(1);
			flight_schedule_no = r.getInt(3);
			query = "update transaction set status=(?) where pnr=(?)";
			PreparedStatement tmt = co.prepareStatement(query);
			tmt.setString(1, "cancelled");
			tmt.setInt(2, pnrno);
			tmt.executeUpdate();
			if (cls.equals("e")) {
				query = "update flight_schedule set economy_seats=economy_seats+(?) where flight_schedule_no=(?)";
				preparedStmt = co.prepareStatement(query);
				preparedStmt.setInt(1, seatcount);
				preparedStmt.setInt(2, flight_schedule_no);
				preparedStmt.executeUpdate();
			} else if (cls.equals("b")) {
				query = "update flight_schedule set business_seats=business_seats+(?) where flight_schedule_no=(?)";
				preparedStmt = co.prepareStatement(query);
				preparedStmt.setInt(1, seatcount);
				preparedStmt.setInt(2, flight_schedule_no);
				preparedStmt.executeUpdate();
			}

		} while (r.next());
		System.out.println("your booking has been cancelled");
		ConnectionClass.closeConnection(co);
	}

	public void viewHistory(String cid) throws SQLException {
		Connection co = (Connection) ConnectionClass.establish();
		String query = null;
		String seats[] = new String[] {};
		query = "select * from transaction where customer_id=(?) ";
		PreparedStatement mt = co.prepareStatement(query);
		mt.setString(1, cid);
		ResultSet set = mt.executeQuery();
		if (set.next() == false) {
			System.out.println("no history available");
		} else {
			System.out.println("pnr\tcid\tscheduleno\tclass\tnoofseats\tstatus\tprice");
			while (set.next()) {
				System.out.println(set.getInt(1) + " " + set.getString(2) + " " + set.getInt(3) + " " + set.getString(4)
						+ " " + set.getInt(5) + " " + set.getString(6) + " " + set.getDouble(7));
			}
			ConnectionClass.closeConnection(co);
		}
	}

	public void status(int pnr) throws SQLException {
		Connection co = (Connection) ConnectionClass.establish();
		String query = "select * from transaction where pnr=(?)";
		PreparedStatement preparedStmt = co.prepareStatement(query);
		preparedStmt.setInt(1, pnr);
		ResultSet r = preparedStmt.executeQuery();
		if (r.next() == false)
			System.out.println("invalid pnr number");
		else {
			System.out.println("pnr\tcid\tscheduleno\tclass\tnoofseats\tstatus\tprice");
			do {
				System.out.println(r.getInt(1) + " " + r.getString(2) + " " + r.getInt(3) + " " + r.getString(4) + " "
						+ r.getInt(5) + " " + r.getString(6) + " " + r.getDouble(7));
			} while (r.next());
			ConnectionClass.closeConnection(co);
		}
	}

	public int verifyCrewLogin(int id, String password) throws SQLException {
		Connection co = (Connection) ConnectionClass.establish();
		String query = "select * from crew where id=(?) and password=(?)";
		PreparedStatement preparedStmt = co.prepareStatement(query);
		preparedStmt.setInt(1, id);
		preparedStmt.setString(2, password);
		ResultSet rs = preparedStmt.executeQuery();
		int rowCount = 0;
		while (rs.next())
			rowCount++;
		ConnectionClass.closeConnection(co);
		return rowCount;
	}

	public void viewCrewProfile(int cid) throws SQLException {
		Connection co = (Connection) ConnectionClass.establish();
		String query = "select * from crew where id=(?)";
		PreparedStatement preparedStmt = co.prepareStatement(query);
		preparedStmt.setInt(1, cid);
		ResultSet r = preparedStmt.executeQuery();
		String qery = "select language from languages where crew_id=(?)";
		PreparedStatement prparedStmt = co.prepareStatement(qery);
		prparedStmt.setInt(1, cid);
		ResultSet re = prparedStmt.executeQuery();
		if (r.next() == false)
			System.out.println("invalid unique id");
		else {
			System.out.println("crewid\tcrewName\tdesignation\tcontactNumber\tpassword\tgender\tlanguages");
			do {
				System.out.print(r.getInt(1) + "\t" + r.getString(2) + "\t" + r.getString(3) + "\t" + r.getString(4)
						+ "\t" + r.getString(5) + "\t" + r.getString(6) + "\t");
				while (re.next()) {
					System.out.print(re.getString(1) + " ");
				}
				System.out.println("");
			} while (r.next());
			ConnectionClass.closeConnection(co);
		}
	}

	public void viewAllotedFlight(int cid) throws SQLException {
		Connection co = (Connection) ConnectionClass.establish();
		String query = "select f.flight_schedule_no,f.flight_id,f.date,f.departure,f.arrival from flight_schedule f inner join  slot s on f.flight_schedule_no=s.flight_schedule_no and s.crew_id=(?)";
		PreparedStatement preparedStmt = co.prepareStatement(query);
		preparedStmt.setInt(1, cid);
		ResultSet r = preparedStmt.executeQuery();
		if (r.next() == false)
			System.out.println("you are not assigned with any flight");
		else {
			System.out.println("scheduleno\tflightid\tdate\tdeparture\tarrival");
			do {
				System.out.println(r.getInt(1) + "\t\t" + r.getInt(2) + "\t" + r.getDate(3) + "\t" + r.getTime(4) + "\t"
						+ r.getTime(5));
			} while (r.next());
			ConnectionClass.closeConnection(co);
		}
	}

	public void viewOtherCrew(int scheduleNo) throws SQLException {
		Connection co = (Connection) ConnectionClass.establish();
		String query = "select crew_id from slot where flight_schedule_no=(?)";
		PreparedStatement preparedStmt = co.prepareStatement(query);
		preparedStmt.setInt(1, scheduleNo);
		ResultSet r = preparedStmt.executeQuery();
		if (r.next() == false)
			System.out.println("crews are yet to alloted");
		else {

			do {
				String query1 = "select * from crew where id=(?)";
				PreparedStatement preparedStmt1 = co.prepareStatement(query1);
				preparedStmt1.setInt(1, r.getInt(1));
				ResultSet r1 = preparedStmt1.executeQuery();
				String qery = "select language from languages where crew_id=(?)";
				PreparedStatement prparedStmt = co.prepareStatement(qery);
				prparedStmt.setInt(1, r.getInt(1));
				ResultSet re = prparedStmt.executeQuery();
				if (r1.next() == false)
					System.out.println("invalid unique id");
				else {
					System.out.println("crewid\tcrewName\tdesignation\tcontactNumber\tpassword\tgender\tlanguages");

					do {
						System.out.print(r1.getInt(1) + "\t" + r1.getString(2) + "\t" + r1.getString(3) + "\t"
								+ r1.getString(4) + "\t" + r1.getString(5) + "\t" + r1.getString(6) + "\t");
						while (re.next()) {
							System.out.print(re.getString(1) + " ");
						}
						System.out.println("");
					} while (r1.next());

				}
			} while (r.next());
			ConnectionClass.closeConnection(co);
		}
	}

	public void requestSlot(int cid, String dest, Date date) throws SQLException {
		Connection co = (Connection) ConnectionClass.establish();
		String query = "select distinct f.flight_schedule_no,f.date,f.departure,fl.destination from flight_schedule f inner join flight fl where f.date=(?) and fl.destination=(?)";
		PreparedStatement preparedStmt = co.prepareStatement(query);
		preparedStmt.setString(1, df.format(date));
		preparedStmt.setString(2, dest);
		ResultSet r = preparedStmt.executeQuery();
		if (r.next() == false)
			System.out.println("invalid unique id");
		else {
			System.out.println("choose a schedule nmber from the below list:");
			System.out.println("scheduleno\tdate\tdeparture\tdestination");

			do {
				System.out.println(r.getInt(1) + "\t" + r.getString(2) + "\t" + r.getString(3) + "\t" + r.getString(4));

			} while (r.next());
		}
		Scanner sc = new Scanner(System.in);
		int schdlno = sc.nextInt();
		query = "insert into slot_requests (crew_id,status,flight_schedule_no) values(?,?,?)";
		preparedStmt = co.prepareStatement(query);
		preparedStmt.setInt(1, cid);// cid is crew id
		preparedStmt.setString(2, "req");
		preparedStmt.setInt(3, schdlno);
		int re = preparedStmt.executeUpdate();
		if (re == 1)
			System.out.println("requested successfully!!");
		else
			System.out.println("request has not been made");
		ConnectionClass.closeConnection(co);
	}

	public void leaveRequests(int cid, Date dat, int count) throws SQLException, ParseException {
		Connection co = (Connection) ConnectionClass.establish();
		System.out.println("enter 1.leaveRequest 2.compensation ");
		Scanner s = new Scanner(System.in);
		int opts = s.nextInt();
		switch (opts) {
		case 1:
			String query = "insert into leave_requests (crew_id,status,no_of_days,date) values(?,?,?,?)";
			PreparedStatement preparedStmt = co.prepareStatement(query);
			preparedStmt.setInt(1, cid);// crew id as cid
			preparedStmt.setString(2, "req");
			preparedStmt.setInt(3, count);
			preparedStmt.setDate(4, new java.sql.Date(dat.getTime()));
			int re = preparedStmt.executeUpdate();

			if (re == 1) {
				System.out.println("your request has been recorded!!!");
			} else
				System.out.println("improper request!!!");
			break;
		case 2:
			Scanner s1 = new Scanner(System.in);
			System.out.println("enter the alternative days on which you will be available");
			for (int c = 1; c <= count; c++) {
				Date d = df.parse(s1.next());
				query = "insert into crew_availability values(?,?,?)";
				preparedStmt = co.prepareStatement(query);
				preparedStmt.setInt(1, cid);
				preparedStmt.setDate(2, new java.sql.Date(d.getTime()));
				preparedStmt.setString(3, "available");
				preparedStmt.executeUpdate();
			}
			System.out.println("your request has been recorded");
			for (int c = 0; c < count; c++) {
				query = "insert into crew_availability values(?,?,?)";
				preparedStmt = co.prepareStatement(query);
				preparedStmt.setInt(1, cid);
				preparedStmt.setDate(2, new java.sql.Date(dat.getTime() + c * (1000 * 60 * 60 * 24)));
				preparedStmt.setString(3, "unavailable");
				preparedStmt.executeUpdate();

			}
			break;
		default:
			System.out.println("choose correct option");
		}
		ConnectionClass.closeConnection(co);
	}

	public int verifyAdminLogin(int aid, String paswd) throws SQLException {
		Connection co = (Connection) ConnectionClass.establish();
		String query = "select * from admin where id=(?) and password=(?)";
		PreparedStatement preparedStmt = co.prepareStatement(query);
		preparedStmt.setInt(1, aid);
		preparedStmt.setString(2, paswd);
		ResultSet rs = preparedStmt.executeQuery();
		int rowCount = 0;
		while (rs.next())
			rowCount++;
		ConnectionClass.closeConnection(co);
		return rowCount;
	}

	public void addFlight(String flno, String airline, String boarding, String dest) throws SQLException {
		Connection co = (Connection) ConnectionClass.establish();
		String query = "insert into flight(flight_no,airline,boarding_place,destination) values(?,?,?,?)";
		PreparedStatement preparedStmt = co.prepareStatement(query);
		preparedStmt.setString(1, flno);
		preparedStmt.setString(2, airline);
		preparedStmt.setString(3, boarding);
		preparedStmt.setString(4, dest);
		int r = preparedStmt.executeUpdate();
		if (r == 1) {
			System.out.println("insert is performed successfully!!!");
			Flight f = new Flight();
			f.setFlight(flno, airline, boarding, dest);
		} else
			System.out.println("there is an error in insertion");
		ConnectionClass.closeConnection(co);
	}

	private void addSchedule(int flid, Date date, Date dept, Date arrival, int ecSeats, Float ecCost, int bsSeats,
			Float bsCost) throws SQLException {
		Connection co = (Connection) ConnectionClass.establish();
		String query = "insert into flight_schedule(flight_id,date,departure,arrival,economy_seats,economy_fare,business_seats,business_fare) values(?,?,?,?,?,?,?,?)";
		PreparedStatement preparedStmt = co.prepareStatement(query);
		preparedStmt.setInt(1, flid);
		preparedStmt.setDate(2, new java.sql.Date(date.getTime()));
		preparedStmt.setTime(3, new java.sql.Time(dept.getTime()));
		preparedStmt.setTime(4, new java.sql.Time(arrival.getTime()));
		preparedStmt.setInt(5, ecSeats);
		preparedStmt.setFloat(6, ecCost);
		preparedStmt.setInt(7, bsSeats);
		preparedStmt.setFloat(8, bsCost);
		int r = preparedStmt.executeUpdate();
		if (r == 1) {
			System.out.println("insert of schedule performed successfully!!!");
			Schedule s = new Schedule();
		} else
			System.out.println("there is an error in insertion of schedule");

		ConnectionClass.closeConnection(co);
	}

	public void addSeats(int flid1, String clas, int start, int end) throws SQLException {
		Connection co = (Connection) ConnectionClass.establish();
		String query = "insert into seats(flight_id,flight_class,start_seat_no,ending_seat_no) values(?,?,?,?)";
		PreparedStatement preparedStmt = co.prepareStatement(query);
		preparedStmt.setInt(1, flid1);
		preparedStmt.setString(2, clas);
		preparedStmt.setInt(3, start);
		preparedStmt.setInt(4, end);
		int r = preparedStmt.executeUpdate();
		if (r == 1)
			System.out.println("insert of seats  performed successfully!!!");
		else
			System.out.println("there is an error in insertion of seats");

		ConnectionClass.closeConnection(co);
	}

	private void addCrew(String name, String designation, String contactno, String password, String gender)
			throws SQLException {
		Connection co = (Connection) ConnectionClass.establish();
		String query = "insert into crew(crew_name,designation,contact_no,password,gender) values(?,?,?,?,?)";
		PreparedStatement preparedStmt = co.prepareStatement(query);
		preparedStmt.setString(1, name);
		preparedStmt.setString(2, designation);
		preparedStmt.setString(3, contactno);
		preparedStmt.setString(4, password);
		preparedStmt.setString(5, gender);
		int r = preparedStmt.executeUpdate();
		if (r == 1)
			System.out.println("insertion of crew was  performed successfully!!!");
		else
			System.out.println("there is an error in insertion of crew");

		ConnectionClass.closeConnection(co);

	}

	public void addLanguage(int cid1, String next) throws SQLException {
		Connection co = (Connection) ConnectionClass.establish();
		String query = "insert into languages values(?,?)";
		PreparedStatement preparedStmt = co.prepareStatement(query);
		preparedStmt.setInt(1, cid1);
		preparedStmt.setString(2, next);
		int r = preparedStmt.executeUpdate();
		if (r == 1)
			System.out.println("insertion of language was  performed successfully!!!");
		else
			System.out.println("there is an error in insertion of language");

		ConnectionClass.closeConnection(co);

	}

	public void leaveAction() throws SQLException {
		Connection co = (Connection) ConnectionClass.establish();
		String query = "select * from leave_requests where status='req'";
		PreparedStatement preparedStmt = co.prepareStatement(query);
		ResultSet rs = preparedStmt.executeQuery();
		int days = 0;
		Date date = null;
		if (rs.next() == false)
			System.out.println("no record is available");
		else {
			System.out.println("sno crew_id no_of_days startDate ");

			do {
				System.out.println(
						rs.getInt(1) + " " + rs.getInt(2) + " " + (days = rs.getInt(4)) + " " + (date = rs.getDate(5)));

			} while (rs.next());
		}

		Scanner sc = new Scanner(System.in);
		int op = 1;
		do {
			System.out.println("enter the leave id to grant leave");
			int crew = sc.nextInt();
			query = "update crew  set leave_count=leave_count+(?) where id=(?)";
			preparedStmt = co.prepareStatement(query);
			preparedStmt.setInt(1, days);
			preparedStmt.setInt(2, crew);
			preparedStmt.executeUpdate();
			query = "update leave_requests set status='granted' where id=(?) and status=(?)";
			preparedStmt = co.prepareStatement(query);
			preparedStmt.setInt(1, crew);
			preparedStmt.setString(2, "req");
			int r = preparedStmt.executeUpdate();

			if (r > 0) {

				for (int k = 0; k < days; k++) {
					query = "insert into crew_availability values(?,?,?)";
					preparedStmt = co.prepareStatement(query);
					preparedStmt.setInt(1, crew);
					preparedStmt.setDate(2, new java.sql.Date(date.getTime() + k * (1000 * 60 * 60 * 24)));
					preparedStmt.setString(3, "unavailable");
					preparedStmt.executeUpdate();
				}
				System.out.println("leave has been granted!!!");
			} else
				System.out.println("enter  valid leave no!!!");
			System.out.println("1.continue 2.exit");
			op = sc.nextInt();
		} while (op == 1);
		ConnectionClass.closeConnection(co);
	}

	public void slotAllocation(int sid) throws SQLException {
		int opts = 0;
		Connection co = (Connection) ConnectionClass.establish();
		int f = 0;
		while (f == 0) {
			String query = "select date from flight_schedule where flight_schedule_no=(?)";
			PreparedStatement preparedStmt = co.prepareStatement(query);
			preparedStmt.setInt(1, sid);
			ResultSet rs = preparedStmt.executeQuery();
			Date d = null;
			while (rs.next())
				d = rs.getDate(1);
			query = "select c.id,c.crew_name,c.designation,c.contact_no from crew c inner join crew_availability a on c.id=a.crew_id "
					+ "and status='available' and date=(?) and id not in(select crew_id from slot where flight_schedule_no=(?)) union select id,crew_name,designation,contact_no from crew where id not in(select crew_id from crew_availability where status='unavailable' and date=(?) union select crew_id from slot where flight_schedule_no=(?))";
			preparedStmt = co.prepareStatement(query);
			preparedStmt.setDate(1, new java.sql.Date(d.getTime()));
			preparedStmt.setInt(2, sid);
			preparedStmt.setDate(3, new java.sql.Date(d.getTime()));
			preparedStmt.setInt(4, sid);
			rs = preparedStmt.executeQuery();
			int count = 0;
			while (rs.next()) {
				System.out
						.println(rs.getInt(1) + " " + rs.getString(2) + " " + rs.getString(3) + " " + rs.getString(4));
				count++;
			}
			if (count > 0) {
				System.out.println("enter the crew id to allot");
				Scanner sc = new Scanner(System.in);
				int cid = sc.nextInt();
				query = "insert into slot values(?,?) ";
				preparedStmt = co.prepareStatement(query);
				preparedStmt.setInt(1, sid);
				preparedStmt.setInt(2, cid);
				preparedStmt.executeUpdate();
				System.out.println("enter 1.continue 2.exit");
				Scanner sc1 = new Scanner(System.in);
				opts = sc1.nextInt();
			} else {
				System.out.println("no crew available for this schedule");
				f = 1;
			}
			if (opts == 2)
				f = 1;
		}
		ConnectionClass.closeConnection(co);
	}

	public static void main(String args[]) throws SQLException, ParseException {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat tf = new SimpleDateFormat("hh:mm:ss");
		Implementation i = new Implementation();
		Scanner sc = new Scanner(System.in);
		while (true) {
			System.out.println("enter 1.customerLogin 2.crewLogin 3.adminlogin");
			switch (sc.nextInt()) {
			case 1:
				System.out.println("enter 1.login 2.register");
				switch (sc.nextInt()) {
				case 1:

					String cid, pwd;// contactNo and password
					System.out.println("enter your contact number");
					cid = sc.next();
					System.out.println("enter your password");
					pwd = sc.next();
					int val = i.verifyLogin(cid, pwd);
					if (val == 1) {
						// System.out.println("valid ");
						while (true) {
							System.out.println(
									"enter 1.searchwithfilter 2.book 3.checkin 4.cancel 5.viewhistory 6.checkstatus 7.exit");
							switch (sc.nextInt()) {
							case 1:
								String source, destination, date, type;
								int number;
								System.out.println("enter your source name");
								source = sc.next();
								System.out.println("enter your destination name");
								destination = sc.next();
								System.out.println("enter the date of journey");
								date = sc.next();

								Date d = df.parse(date);
								System.out.println("enter economy(e)/business(b)");
								type = sc.next();
								System.out.println("enter the number of seats to be booked");
								number = sc.nextInt();
								while (true) {
									System.out.println(
											"enter 1.nofilter 2.costasc 3.costdesc 4.timeearlier 5.timelater 6.exit");
									int opts = sc.nextInt();
									if (opts == 1)
										i.search(source, destination, d, type, number, "flight.flight_no");
									if (opts == 2 && type.equals("e"))
										i.search(source, destination, d, type, number, "flight_schedule.economy_fare");
									if (opts == 3 && type.equals("e"))
										i.search(source, destination, d, type, number,
												"flight_schedule.economy_fare desc");
									if (opts == 2 && type.equals("b"))
										i.search(source, destination, d, type, number, "flight_schedule.business_fare");
									if (opts == 3 && type.equals("b"))
										i.search(source, destination, d, type, number,
												"flight_schedule.business_fare desc");
									if (opts == 4)
										i.search(source, destination, d, type, number, "flight_schedule.departure");
									if (opts == 5)
										i.search(source, destination, d, type, number,
												"flight_schedule.departure desc");
									if (opts == 6)
										break;
								}

							case 2:
								int pnr, scheduleNo, noofSeat;
								Float price;
								String types;
								System.out.println("enter the schedule number for which you are booking the ticket");
								scheduleNo = sc.nextInt();
								System.out.println("enter the type economy(e)/business(b)");
								types = sc.next();
								System.out.println("enter the number of seats");
								noofSeat = sc.nextInt();
								Random rand = new Random();
								pnr = rand.nextInt(100000);
								i.book(pnr, cid, scheduleNo, types, noofSeat);
								break;
							case 3:
								System.out.println("enter the pnr number for checkin in");
								pnr = sc.nextInt();
								i.checkin(pnr);
								break;
							case 4:
								System.out.println("enter the pnr to cancel booking");
								pnr = sc.nextInt();
								i.cancel(pnr);
								break;
							case 5:
								i.viewHistory(cid);
								break;
							case 6:
								System.out.println("enter the pnr to view status");
								pnr = sc.nextInt();
								i.status(pnr);
								break;
							case 7:
								System.exit(0);
							}
						}
					} else
						System.out.println("not valid.tryAgain!!!");
					break;
				case 2:
					int phoneNo;
					String password, name, gender;// contactNo and password
					System.out.println("enter your contact number");
					phoneNo = sc.nextInt();
					System.out.println("enter your name");
					name = sc.next();
					System.out.println("enter your gender");
					gender = sc.next();
					System.out.println("enter your password");
					password = sc.next();
					i.createLogin(phoneNo, name, gender, password);
					break;

				default:
					System.out.println("enter proper option number");
				}
				break;

			case 2:
				String pwd;
				int cid;// contactNo and password
				System.out.println("enter your unique id");
				cid = sc.nextInt();
				System.out.println("enter your password");
				pwd = sc.next();
				int val = i.verifyCrewLogin(cid, pwd);
				if (val == 1) {
					while (true) {
						System.out.println(
								"enter 1.viewProfile 2.slotRequest 3.leaveRequest 4.viewAlottedFlight 5.viewOtherCrew 6.exit");
						switch (sc.nextInt()) {
						case 1:
							i.viewCrewProfile(cid);
							break;
						case 2:
							String dest;
							Date date;
							System.out.println("enter the destination");
							dest = sc.next();
							System.out.println("enter the date");
							date = df.parse(sc.next());
							i.requestSlot(cid, dest, date);
						case 3:
							Date dat;
							int count;
							System.out.println("enter the date from which you are unavailable");
							dat = df.parse(sc.next());
							System.out.println("enter the number of days");
							count = sc.nextInt();
							i.leaveRequests(cid, dat, count);
							break;
						case 4:
							i.viewAllotedFlight(cid);
							break;
						case 5:
							System.out.println("enter the schedule number to see others");
							i.viewOtherCrew(sc.nextInt());
							break;
						case 6:
							System.exit(0);
						}
					}
				}
				break;

			case 3:
				String paswd;
				int aid;// contactNo and password
				System.out.println("enter your unique id");
				aid = sc.nextInt();
				System.out.println("enter your password");
				paswd = sc.next();
				val = i.verifyAdminLogin(aid, paswd);
				if (val == 1) {
					while (true) {
						System.out.println(
								"enter 1.addFlight 2.addSchedule 3.addSeat 4.addCrew 5.leaveRequest 6.slotRequest 7.addLanguage 8.exit ");
						switch (sc.nextInt()) {
						case 1:
							String flno, airline, boarding, dest;
							System.out.println("enter the flight id");
							flno = sc.next();
							System.out.println("enter the airlines name");
							airline = sc.next();
							System.out.println("enter the starting place");
							boarding = sc.next();
							System.out.println("enter the destination");
							dest = sc.next();
							i.addFlight(flno, airline, boarding, dest);
							break;
						case 2:
							int flid, ecSeats, bsSeats;
							Float ecCost, bsCost;
							Date date, dept, arrival;
							System.out.println("enter the flight id");
							flid = sc.nextInt();
							System.out.println("enter the date of journey in yyyy-mm-dd");
							date = df.parse(sc.next());
							System.out.println("enter the departure time in hh:mm:ss");
							dept = tf.parse(sc.next());
							System.out.println("enter the arrival time in hh:mm:ss");
							arrival = tf.parse(sc.next());
							System.out.println("enter the number of economy seats");
							ecSeats = sc.nextInt();
							System.out.println("enter the economy seat cost ");
							ecCost = sc.nextFloat();
							System.out.println("enter the number of business seats");
							bsSeats = sc.nextInt();
							System.out.println("enter the business seat cost ");
							bsCost = sc.nextFloat();
							i.addSchedule(flid, date, dept, arrival, ecSeats, ecCost, bsSeats, bsCost);
							break;
						case 3:
							int flid1, start, end;
							String clas;
							System.out.println("enter the flight id to add seats");
							flid1 = sc.nextInt();
							System.out.println("enter the class to add seats");
							clas = sc.next().toLowerCase();
							System.out.println("enter the start seat number");
							start = sc.nextInt();
							System.out.println("enter the end seat number");
							end = sc.nextInt();
							i.addSeats(flid1, clas, start, end);
							break;

						case 4:
							String name, designation, contactno, password, gender;
							System.out.println("enter your name");
							name = sc.next();
							System.out.println("1.pilot 2.ground 3.air");
							int opt = sc.nextInt();
							if (opt == 1)
								designation = "pilot";
							else if (opt == 2)
								designation = "ground";
							else if (opt == 3)
								designation = "air";
							else {
								System.out.println("your selection was wrong.temporarily designation set as NA");
								designation = "NA";
							}
							System.out.println("enter your conatct number");
							contactno = sc.next();
							System.out.println("enter password");
							password = sc.next();
							System.out.println("enter gender");
							gender = sc.next();
							i.addCrew(name, designation, contactno, password, gender);

						case 7:
							int cid1;
							String language;
							System.out.println("enter the crew id");
							cid1 = sc.nextInt();
							System.out.println("enter the number of languages known");
							int count = sc.nextInt();
							for (int j = 1; j <= count; j++) {
								System.out.println("enter the language :" + j);
								i.addLanguage(cid1, sc.next());
							}
						case 5:
							i.leaveAction();
							break;
						case 6:
							System.out.println("enter the schedule number to allocate crew");
							int sid = sc.nextInt();
							i.slotAllocation(sid);
							break;
						case 8:
							System.exit(0);
						default:
							System.out.println("enter a valid option.Try again!!!");
						}
					}
				} else
					System.out.println("not valid.tryAgain!!!");
				break;

			default:
				System.out.println("enter proper options");
			}
		}

	}

}
