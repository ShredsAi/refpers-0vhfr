package ai.shreds.domain.value_objects;

import java.util.Objects;
import java.util.UUID;

public class DomainChallengeIdValue {

    private final UUID id;

    public DomainChallengeIdValue(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("Challenge ID cannot be null");
        }
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    @Override
    public String toString() {
        return id.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DomainChallengeIdValue that = (DomainChallengeIdValue) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
