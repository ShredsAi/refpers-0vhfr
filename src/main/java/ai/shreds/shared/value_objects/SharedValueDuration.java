package ai.shreds.shared.value_objects;

public class SharedValueDuration {
    private final long seconds;

    public SharedValueDuration(long seconds) {
        this.seconds = seconds;
        validate();
    }

    public long getSeconds() {
        return seconds;
    }

    public long toMillis() {
        return seconds * 1000;
    }

    public void validate() {
        if (seconds < 0) {
            throw new IllegalArgumentException("Duration cannot be negative");
        }
    }

    public static SharedValueDuration fromMinutes(long minutes) {
        return new SharedValueDuration(minutes * 60);
    }

    public static SharedValueDuration fromHours(long hours) {
        return new SharedValueDuration(hours * 3600);
    }

    public static SharedValueDuration fromMillis(long millis) {
        return new SharedValueDuration(millis / 1000);
    }

    public static SharedValueDuration fromSeconds(long seconds) {
        return new SharedValueDuration(seconds);
    }

    @Override
    public String toString() {
        return "SharedValueDuration{" +
                "seconds=" + seconds +
                '}';
    }
}