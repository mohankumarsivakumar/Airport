package global.coda.bean;

public class Transaction {
int pnrNo,flightNo,numberOfSeat;
String flightClass,status,customerId;
Float price;
public int getPnrNo() {
	return pnrNo;
}
public void setPnrNo(int pnrNo) {
	this.pnrNo = pnrNo;
}
public String getCustomerId() {
	return customerId;
}
public void setCustomerId(String customerId) {
	this.customerId = customerId;
}
public int getFlightNo() {
	return flightNo;
}
public void setFlightNo(int flightNo) {
	this.flightNo = flightNo;
}
public int getNumberOfSeat() {
	return numberOfSeat;
}
public void setNumberOfSeat(int numberOfSeat) {
	this.numberOfSeat = numberOfSeat;
}
public String getFlightClass() {
	return flightClass;
}
public void setFlightClass(String flightClass) {
	this.flightClass = flightClass;
}
public String getStatus() {
	return status;
}
public void setStatus(String status) {
	this.status = status;
}
public Float getPrice() {
	return price;
}
public void setPrice(Float price) {
	this.price = price;
}

public void setTransaction(int pnr, String cid, int scheduleNo, String types, int noofSeat,Float price) {
	setPnrNo(pnr);
	setCustomerId(cid);
	setFlightNo(scheduleNo);
	setNumberOfSeat(noofSeat);
	setFlightClass(types);
	setStatus("booked");
	setPrice(price);
}

}
