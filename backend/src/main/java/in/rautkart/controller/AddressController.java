package in.rautkart.controller;

import in.rautkart.dto.AddressDtos;
import in.rautkart.security.AuthUser;
import in.rautkart.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping
    public List<AddressDtos.AddressResponse> list(@AuthenticationPrincipal AuthUser user) {
        return addressService.list(user.getId());
    }

    @PostMapping
    public ResponseEntity<AddressDtos.AddressResponse> create(@AuthenticationPrincipal AuthUser user,
                                                              @Valid @RequestBody AddressDtos.AddressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(addressService.create(user.getId(), request));
    }

    @PutMapping("/{id}")
    public AddressDtos.AddressResponse update(@AuthenticationPrincipal AuthUser user,
                                              @PathVariable Long id,
                                              @Valid @RequestBody AddressDtos.AddressRequest request) {
        return addressService.update(user.getId(), id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal AuthUser user, @PathVariable Long id) {
        addressService.delete(user.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
