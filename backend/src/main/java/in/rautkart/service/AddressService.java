package in.rautkart.service;

import in.rautkart.dto.AddressDtos;
import in.rautkart.entity.Address;
import in.rautkart.entity.User;
import in.rautkart.exception.ApiException;
import in.rautkart.repository.AddressRepository;
import in.rautkart.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public AddressService(AddressRepository addressRepository, UserRepository userRepository) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<AddressDtos.AddressResponse> list(Long userId) {
        return addressRepository.findByUserIdOrderByIsDefaultDescIdDesc(userId).stream()
                .map(Mappers::toAddress)
                .toList();
    }

    @Transactional
    public AddressDtos.AddressResponse create(Long userId, AddressDtos.AddressRequest req) {
        User user = userRepository.getReferenceById(userId);
        boolean first = addressRepository.findByUserIdOrderByIsDefaultDescIdDesc(userId).isEmpty();
        boolean makeDefault = first || Boolean.TRUE.equals(req.isDefault());

        if (makeDefault) {
            clearExistingDefault(userId);
        }

        Address address = Address.builder()
                .user(user)
                .fullName(req.fullName().trim())
                .phone(req.phone())
                .line1(req.line1().trim())
                .line2(req.line2() == null || req.line2().isBlank() ? null : req.line2().trim())
                .city(req.city().trim())
                .state(req.state().trim())
                .pincode(req.pincode())
                .isDefault(makeDefault)
                .build();

        return Mappers.toAddress(addressRepository.save(address));
    }

    @Transactional
    public AddressDtos.AddressResponse update(Long userId, Long addressId, AddressDtos.AddressRequest req) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> ApiException.notFound("Address"));

        if (Boolean.TRUE.equals(req.isDefault()) && !address.isDefault()) {
            clearExistingDefault(userId);
            address.setDefault(true);
        }

        address.setFullName(req.fullName().trim());
        address.setPhone(req.phone());
        address.setLine1(req.line1().trim());
        address.setLine2(req.line2() == null || req.line2().isBlank() ? null : req.line2().trim());
        address.setCity(req.city().trim());
        address.setState(req.state().trim());
        address.setPincode(req.pincode());

        return Mappers.toAddress(addressRepository.save(address));
    }

    @Transactional
    public void delete(Long userId, Long addressId) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> ApiException.notFound("Address"));
        addressRepository.delete(address);
    }

    @Transactional(readOnly = true)
    public Address requireOwned(Long userId, Long addressId) {
        return addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> ApiException.notFound("Address"));
    }

    private void clearExistingDefault(Long userId) {
        addressRepository.findByUserIdOrderByIsDefaultDescIdDesc(userId).stream()
                .filter(Address::isDefault)
                .forEach(a -> {
                    a.setDefault(false);
                    addressRepository.save(a);
                });
    }
}
