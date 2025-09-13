package com.example.HospitalManagementSystem.DTO;

public class PatientDTO {
    private Long id;
    private String name;
    private Integer age;
    private String gender;
    private String phone;
    private String email;
    private String address;

    public PatientDTO() 
    {
    	
    }
    
    //Constructors
    public PatientDTO(Long id, String name, Integer age,String gender, String phone, String email,String address) {
        this.id = id;
        this.name = name;
        this.age=age;
        this.gender = gender;
        this.phone = phone;
        this.email=email;
        this.address = address;
    }
    
    //Getters AND Setters
    
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getAge() {
		return age;
	}
	public void setAge(Integer age) {
		this.age = age;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}

}
