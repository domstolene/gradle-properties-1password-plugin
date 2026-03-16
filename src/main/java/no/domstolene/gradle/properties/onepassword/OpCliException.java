package no.domstolene.gradle.properties.onepassword;

public final class OpCliException extends RuntimeException {
    public OpCliException(String message) {
        super(message);
    }

    public OpCliException(String message, Throwable cause) {
        super(message, cause);
    }
}
