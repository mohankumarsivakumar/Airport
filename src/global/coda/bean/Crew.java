package global.coda.bean;

public class Crew extends Person{
private int crewId,lop,pto;
private String designation;
public int getCrewId() {
	return crewId;
}
public void setCrewId(int crewId) {
	this.crewId = crewId;
}
public int getLop() {
	return lop;
}
public void setLop(int lop) {
	this.lop = lop;
}
public int getPto() {
	return pto;
}
public void setPto(int pto) {
	this.pto = pto;
}
public String getDesignation() {
	return designation;
}
public void setDesignation(String designation) {
	this.designation = designation;
}

}
