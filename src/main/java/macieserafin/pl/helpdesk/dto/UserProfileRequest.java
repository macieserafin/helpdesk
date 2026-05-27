package macieserafin.pl.helpdesk.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UserProfileRequest {
    @Size(max = 80, message = "First name must not exceed 80 characters")
    @Pattern(regexp = "^.*\\S.*$", message = "First name must not be blank")
    private String firstName;

    @Size(max = 80, message = "Last name must not exceed 80 characters")
    @Pattern(regexp = "^.*\\S.*$", message = "Last name must not be blank")
    private String lastName;

    @Size(max = 30, message = "Phone number must not exceed 30 characters")
    @Pattern(regexp = "^[+0-9 ()-]+$", message = "Phone number must be valid")
    private String phoneNumber;

    @Size(max = 80, message = "City must not exceed 80 characters")
    @Pattern(regexp = "^.*\\S.*$", message = "City must not be blank")
    private String city;

    @Size(max = 120, message = "Street address must not exceed 120 characters")
    @Pattern(regexp = "^.*\\S.*$", message = "Street address must not be blank")
    private String streetAddress;

    @Size(max = 20, message = "Postal code must not exceed 20 characters")
    @Pattern(regexp = "^[A-Za-z0-9 -]+$", message = "Postal code must be valid")
    private String postalCode;

    public UserProfileRequest() {
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
}
