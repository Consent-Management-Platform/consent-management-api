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

    /**
     * The ActiveConsentsWithExpiryTime GSI supports querying for all active
     * consents with non-null expiryTime values.
     *
     * The GSI partition key, "activeId", is an optional consent attribute
     * set to the consent's partition key value for active consents with
     * non-null expiryTime values.
     *
     * This makes the GSI a sparse index that only contains consents relevant
     * for the auto expiration workflow.
     *
     * The GSI sort key, "expiryTime", is used to scan in-scope consents
     * in ascending order of expiry time, to efficiently identify
     * consents that should be expired.
     *
     * The GSI only contains activeId and expiryTime attributes, which
     * are sufficient to identify consents to expire and submit DynamoDB
     * UpdateItem requests.
     */
    public static final String ACTIVE_CONSENTS_WITH_EXPIRY_TIME_GSI_NAME = "ActiveConsentsWithExpiryTime";

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
     *
     * The GSI contains all consent attributes, which allows for efficient
     * retrieval of all consent metadata for a given service user.
     */
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

    @DynamoDbSecondaryPartitionKey(indexNames = { ACTIVE_CONSENTS_WITH_EXPIRY_TIME_GSI_NAME })
    @Nullable
    String activeId();

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

    @DynamoDbSecondarySortKey(indexNames = { ACTIVE_CONSENTS_WITH_EXPIRY_TIME_GSI_NAME })
    @Nullable
    String expiryTime();
}
