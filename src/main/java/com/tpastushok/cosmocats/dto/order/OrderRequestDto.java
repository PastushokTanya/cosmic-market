package com.tpastushok.cosmocats.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder(toBuilder = true)
public class OrderRequestDto {
    @Valid
    @NotNull(message = "Order entries cannot be null")
    @NotEmpty(message = "Order entries cannot be an empty list")
    List<@Valid OrderEntryDto> orderEntries; //enforce nested validation where each OrderEntryDto within the List is also validated

    @NotNull(message = "Address cannot be null")
    @Size(max = 512, message = "Address must be less than 512 characters long")
    String address;

    @Email(message = "Email should be valid")
    String email;

    @NotNull(message = "Name cannot be null")
    @Size(max = 128, message = "Name must be less than 128 characters long")
    String customerName;
}