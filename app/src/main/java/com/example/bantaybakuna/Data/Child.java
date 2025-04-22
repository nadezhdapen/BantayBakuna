package com.example.bantaybakuna.Data;
import com.google.firebase.firestore.Exclude;
import java.util.Date;

public class Child {

    @Exclude
    public String documentId;

    public String name;
    public Date dateOfBirth;


    public Child() {}

    // for when creating a new Child object
    public Child(String name, Date dateOfBirth) {
        this.name = name;
        this.dateOfBirth = dateOfBirth;
    }

    public String getDocumentId() {
        return documentId;
    }
    public String getName() {
        return name;
    }
    public Date getDateOfBirth() {
        return dateOfBirth;
    }
}