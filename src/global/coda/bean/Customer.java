package global.coda.bean;

public class Customer extends Person{
	
	public void createCustomer(int phoneNo,String name,String gender,String password) {
		setContactNumber(phoneNo);
		setName(name);
		setGender(gender);
		setPassword(password);
	}
	

}
