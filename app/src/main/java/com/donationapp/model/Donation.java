package com.donationapp.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

public class Donation {

    @DocumentId
    private String id;

    private String  itemName;
    private String  description;
    private int     quantity;
    private String  pickupLocation;
    private String  contactInfo;
    private boolean pickedUp;

    @ServerTimestamp
    private Timestamp createdAt;

    public Donation() {}

    public Donation(String itemName, String description, int quantity,
                    String pickupLocation, String contactInfo) {
        this.itemName       = itemName;
        this.description    = description;
        this.quantity       = quantity;
        this.pickupLocation = pickupLocation;
        this.contactInfo    = contactInfo;
        this.pickedUp       = false;
    }

    public String    getId()                      { return id; }
    public void      setId(String id)             { this.id = id; }
    public String    getItemName()                { return itemName; }
    public void      setItemName(String v)        { this.itemName = v; }
    public String    getDescription()             { return description; }
    public void      setDescription(String v)     { this.description = v; }
    public int       getQuantity()                { return quantity; }
    public void      setQuantity(int v)           { this.quantity = v; }
    public String    getPickupLocation()          { return pickupLocation; }
    public void      setPickupLocation(String v)  { this.pickupLocation = v; }
    public String    getContactInfo()             { return contactInfo; }
    public void      setContactInfo(String v)     { this.contactInfo = v; }
    public boolean   isPickedUp()                 { return pickedUp; }
    public void      setPickedUp(boolean v)       { this.pickedUp = v; }
    public Timestamp getCreatedAt()               { return createdAt; }
    public void      setCreatedAt(Timestamp v)    { this.createdAt = v; }
}