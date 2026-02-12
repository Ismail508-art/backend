package com.example.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String contact;
    private String location;

    private String treatment;
    private String physioType;
    private String cuppingType;
    private String cuppingOption;
    private String dietPlan;
    private String labPackage;

    private int price;
    private String date;
    private String time;

    @Column(length = 500)
    private String reason;
     
    private String transactionId;
    private String status; // PENDING / CONFIRMED
    private String paymentStatus;

    public String getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(String paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	// ✅ REQUIRED
    public Appointment() {}

    // getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getTreatment() { return treatment; }
    public void setTreatment(String treatment) { this.treatment = treatment; }

    public String getPhysioType() { return physioType; }
    public void setPhysioType(String physioType) { this.physioType = physioType; }

    public String getCuppingType() { return cuppingType; }
    public void setCuppingType(String cuppingType) { this.cuppingType = cuppingType; }

    public String getCuppingOption() { return cuppingOption; }
    public void setCuppingOption(String cuppingOption) { this.cuppingOption = cuppingOption; }

    public String getDietPlan() { return dietPlan; }
    public void setDietPlan(String dietPlan) { this.dietPlan = dietPlan; }

    public String getLabPackage() { return labPackage; }
    public void setLabPackage(String labPackage) { this.labPackage = labPackage; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	
}
