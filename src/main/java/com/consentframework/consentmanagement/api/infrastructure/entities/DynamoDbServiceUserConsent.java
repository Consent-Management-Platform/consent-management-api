package com.consentframework.consentmanagement.api.infrastructure.entities;

import com.consentframework.consentmanagement.api.infrastructure.annotations.DynamoDbImmutableStyle;
import com.consentframework.consentmanagement.api.models.ConsentStatus;
import jakarta.annotation.Nullable;
import org.immutables.value.Value.Immutable;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbImmutable;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.util.Map;

/**
 * ServiceUserConsent DynamoDB data class representing a consent object to store/retrieve.
 */
@Immutable
@DynamoDbImmutableStyle
@DynamoDbImmutable(builder = DynamoDbServiceUserConsent.Builder.class)
public interface DynamoDbServiceUserConsent {
    public static final String TABLE_NAME = "ServiceUserConsent";
    public static final String PARTITION_KEY = "id";

    static Builder builder() {
        return new Builder();
    }

    /**
     * DynamoDbServiceUserConsent Builder class, intentionally empty.
     */
    class Builder extends ImmutableDynamoDbServiceUserConsent.Builder {}

    @DynamoDbPartitionKey
    String id();

    String serviceId();

    String userId();

    String consentId();

    Integer consentVersion();

    ConsentStatus consentStatus();

    @Nullable
    String consentType();

    @Nullable
    Map<String, String> consentData();

    @Nullable
    String expiryTime();
}
