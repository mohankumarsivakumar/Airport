package global.coda.bean;

public class Passenger {
int pnr;
String passengerName,seatNo;
public int getPnr() {
	return pnr;
}
public void setPnr(int pnr) {
	this.pnr = pnr;
}
public String getPassengerName() {
	return passengerName;
}
public void setPassengerName(String passengerName) {
	this.passengerName = passengerName;
}
public String getSeatNo() {
	return seatNo;
}
public void setSeatNo(String seatNo) {
	this.seatNo = seatNo;
}
public void setPassenger(int pnr,String name) {
	setPnr(pnr);
	setPassengerName(name);
	setSeatNo("na");
}
}
