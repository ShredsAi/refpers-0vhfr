package ai.shreds.domain.value_objects;

import java.util.Objects;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class DomainCurrencyValue {
    private final String code;
    private final String symbol;

    public DomainCurrencyValue(String code, String symbol) {
        if (code == null || !code.matches("^[A-Z]{3}$")) {
            throw new IllegalArgumentException("Currency code must be ISO 4217 three-letter uppercase");
        }
        if (symbol == null || symbol.isEmpty()) {
            throw new IllegalArgumentException("Currency symbol cannot be null or empty");
        }
        this.code = code;
        this.symbol = symbol;
    }

    public boolean validate() {
        return code != null && code.matches("^[A-Z]{3}$") && symbol != null && !symbol.isEmpty();
    }

    public boolean equals(DomainCurrencyValue other) {
        if (other == null) {
            return false;
        }
        return code.equals(other.code) && symbol.equals(other.symbol);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DomainCurrencyValue)) return false;
        DomainCurrencyValue that = (DomainCurrencyValue) o;
        return Objects.equals(code, that.code) && Objects.equals(symbol, that.symbol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, symbol);
    }
}