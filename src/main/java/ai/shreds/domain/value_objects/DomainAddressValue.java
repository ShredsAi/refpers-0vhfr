package ai.shreds.domain.value_objects;

import java.util.Objects;

public class DomainAddressValue {
    private final String line1;
    private final String line2;
    private final String city;
    private final String state;
    private final String postalCode;
    private final String country;

    public DomainAddressValue(String line1,
                              String line2,
                              String city,
                              String state,
                              String postalCode,
                              String country) {
        this.line1 = line1;
        this.line2 = line2;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.country = country;
    }

    public String getLine1() {
        return line1;
    }

    public String getLine2() {
        return line2;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getCountry() {
        return country;
    }

    public boolean validate() {
        if (line1 == null || line1.isEmpty()) return false;
        if (city == null || city.isEmpty()) return false;
        if (state == null || state.isEmpty()) return false;
        if (postalCode == null || postalCode.isEmpty()) return false;
        if (country == null || country.isEmpty()) return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(line1);
        if (line2 != null && !line2.isEmpty()) {
            sb.append(", ").append(line2);
        }
        sb.append(", ").append(city)
          .append(", ").append(state)
          .append(", ").append(postalCode)
          .append(", ").append(country);
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DomainAddressValue)) return false;
        DomainAddressValue that = (DomainAddressValue) o;
        return Objects.equals(line1, that.line1) &&
               Objects.equals(line2, that.line2) &&
               Objects.equals(city, that.city) &&
               Objects.equals(state, that.state) &&
               Objects.equals(postalCode, that.postalCode) &&
               Objects.equals(country, that.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(line1, line2, city, state, postalCode, country);
    }
}