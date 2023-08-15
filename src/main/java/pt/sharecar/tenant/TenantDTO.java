package pt.sharecar.tenant;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public class TenantDTO {

    @NotEmpty
    public String company;

    @NotEmpty
    @Size(min = 3, max = 15, message = "Subdomain must be between 3 and 15 characters")
    public String subdomain;

    @NotEmpty
    public String firstName;

    @NotEmpty
    public String lastName;

    @NotEmpty
    @Email(message = "Email should be valid")
    public String email;

}
