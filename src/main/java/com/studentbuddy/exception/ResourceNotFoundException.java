package com.studentbuddy.exception;

/**
 * Thrown when a requested resource does not exist, or exists but does not
 * belong to the current user. We deliberately do not distinguish the two cases
 * to the client, so users cannot probe for the existence of others' data.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException of(String resource, Long id) {
        return new ResourceNotFoundException(resource + " not found with id: " + id);
    }
}
