package global.coda.bean;
import java.util.Date;

public class Schedule {
private int scheduleNo,flightNo,economyCost,businessCost;
private Date date,departureTime;
public int getScheduleNo() {
	return scheduleNo;
}
public void setScheduleNo(int scheduleNo) {
	this.scheduleNo = scheduleNo;
}
public int getFlightNo() {
	return flightNo;
}
public void setFlightNo(int flightNo) {
	this.flightNo = flightNo;
}
public int getEconomyCost() {
	return economyCost;
}
public void setEconomyCost(int economyCost) {
	this.economyCost = economyCost;
}
public int getBusinessCost() {
	return businessCost;
}
public void setBusinessCost(int businessCost) {
	this.businessCost = businessCost;
}
public Date getDate() {
	return date;
}
public void setDate(Date date) {
	this.date = date;
}
public Date getDepartureTime() {
	return departureTime;
}
public void setDepartureTime(Date departureTime) {
	this.departureTime = departureTime;
}

}
