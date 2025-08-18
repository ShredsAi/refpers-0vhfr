package ai.shreds.shared.value_objects;

import ai.shreds.application.dtos.ApplicationMoneyValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.Objects;

/**
 * Shared value object representing a monetary amount with currency.
 * This is an immutable value object that ensures consistency across layers.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SharedMoneyValue {
    
    @NotBlank(message = "Amount cannot be blank")
    @Pattern(regexp = "^\\d{1,16}(\\.\\d{1,4})?$", message = "Amount must be a valid decimal number with up to 4 decimal places")
    private String amount;
    
    @NotBlank(message = "Currency cannot be blank")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid 3-letter ISO 4217 currency code")
    private String currency;

    /**
     * Converts this shared value object to an application layer value object.
     *
     * @return The ApplicationMoneyValue with values from this shared value object
     */
    public ApplicationMoneyValue toApplicationMoneyValue() {
        return ApplicationMoneyValue.builder()
                .amount(this.amount)
                .currency(this.currency)
                .build();
    }

    /**
     * Creates a shared value object from an application layer value object.
     *
     * @param value The application layer value object to convert from
     * @return A new SharedMoneyValue with values from the application value object, or null if input is null
     */
    public static SharedMoneyValue fromApplicationMoneyValue(ApplicationMoneyValue value) {
        if (value == null) {
            return null;
        }
        return SharedMoneyValue.builder()
                .amount(value.getAmount())
                .currency(value.getCurrency())
                .build();
    }
    
    /**
     * Creates a new SharedMoneyValue with the specified amount and currency.
     *
     * @param amount The monetary amount as a string
     * @param currency The ISO 4217 currency code
     * @return A new SharedMoneyValue instance
     */
    public static SharedMoneyValue of(String amount, String currency) {
        return SharedMoneyValue.builder()
                .amount(amount)
                .currency(currency)
                .build();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SharedMoneyValue that = (SharedMoneyValue) obj;
        return Objects.equals(amount, that.amount) && Objects.equals(currency, that.currency);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }
    
    @Override
    public String toString() {
        return currency + " " + amount;
    }
}