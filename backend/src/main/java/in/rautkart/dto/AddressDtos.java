package in.rautkart.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class AddressDtos {

    private AddressDtos() {
    }

    public record AddressRequest(
            @NotBlank @Size(max = 100) String fullName,
            @NotBlank @Pattern(regexp = "^[0-9]{10}$", message = "phone must be 10 digits") String phone,
            @NotBlank @Size(max = 200) String line1,
            @Size(max = 200) String line2,
            @NotBlank @Size(max = 80) String city,
            @NotBlank @Size(max = 80) String state,
            @NotBlank @Pattern(regexp = "^[0-9]{6}$", message = "pincode must be 6 digits") String pincode,
            Boolean isDefault
    ) {
    }

    public record AddressResponse(
            Long id,
            String fullName,
            String phone,
            String line1,
            String line2,
            String city,
            String state,
            String pincode,
            boolean isDefault
    ) {
    }
}
