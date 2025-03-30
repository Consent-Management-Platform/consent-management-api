package com.consentframework.consentmanagement.api.infrastructure.entities;

import com.consentframework.consentmanagement.api.models.ConsentStatus;
import com.consentframework.shared.api.infrastructure.annotations.DynamoDbImmutableStyle;
import jakarta.annotation.Nullable;
import org.immutables.value.Value.Immutable;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbImmutable;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;

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
    public static final String CONSENT_BY_SERVICE_USER_GSI_NAME = "ConsentsByServiceUser";

    static Builder builder() {
        return new Builder();
    }

    /**
     * DynamoDbServiceUserConsent Builder class, intentionally empty.
     */
    class Builder extends ImmutableDynamoDbServiceUserConsent.Builder {}

    @DynamoDbPartitionKey
    String id();

    /**
     * The ConsentsByServiceUser GSI, which supports querying for all
     * consents for a given User or Service-User pair, uses the more
     * well-distributed "userId" attribute as its partition key to
     * mitigate hot-key issues when a single service has a large number
     * of users with different consents, as well as to support retrieving
     * all consents for a given user across all associated services.
     *
     * It uses "serviceId" as its sort key since service IDs are expected
     * to be less well-distributed for a given hosting company.
     */
    @DynamoDbSecondarySortKey(indexNames = { CONSENT_BY_SERVICE_USER_GSI_NAME })
    String serviceId();

    @DynamoDbSecondaryPartitionKey(indexNames = { CONSENT_BY_SERVICE_USER_GSI_NAME })
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
