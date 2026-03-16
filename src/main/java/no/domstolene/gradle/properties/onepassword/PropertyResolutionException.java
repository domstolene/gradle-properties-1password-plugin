package no.domstolene.gradle.properties.onepassword;

public final class PropertyResolutionException extends RuntimeException {
    public PropertyResolutionException(String message) {
        super(message);
    }

    public PropertyResolutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
