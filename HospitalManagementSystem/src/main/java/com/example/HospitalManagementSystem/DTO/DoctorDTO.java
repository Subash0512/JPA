package com.example.HospitalManagementSystem.DTO;

public class DoctorDTO {
    private Long id;
    private String name;
    private String specialization;
    private String email;
    private String phone;
    
    //Constructors

    public DoctorDTO() 
    {
    	
    }
    public DoctorDTO(Long id, String name, String specialization, String email,String phone) 
    {
        this.id = id; 
        this.name = name; 
        this.specialization = specialization; 
        this.email=email;
        this.phone = phone;
    }
    
    // Getters and Setters
    
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
	public String getSpecialization() {
		return specialization;
	}
	public void setSpecialization(String specialization) {
		this.specialization = specialization;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
      
	
}
