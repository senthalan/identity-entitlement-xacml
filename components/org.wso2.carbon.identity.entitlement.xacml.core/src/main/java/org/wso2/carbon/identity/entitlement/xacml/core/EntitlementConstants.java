package org.wso2.carbon.identity.entitlement.xacml.core;

/**
 *
 */
public class EntitlementConstants {

    public static final String PDP_STORE = "PDP_STORE";

    public static final String PAP_STORE = "PAP_STORE";


    public static final int MAX_NO_OF_IN_MEMORY_POLICIES = 10;

    public static final String MAX_POLICY_REFERENCE_ENTRIES = "PDP.References.MaxPolicyEntries";

    public static final String POLICY_REFERENCE = "policyIdReferences";

    public static final String POLICY_SET_REFERENCE = "policySetIdReferences";

    public static final class PolicyPublish {

        public static final String ACTION_CREATE = "CREATE";

        public static final String ACTION_UPDATE = "UPDATE";

        public static final String ACTION_DELETE = "DELETE";

        public static final String ACTION_ENABLE = "ENABLE";

        public static final String ACTION_DISABLE = "DISABLE";

        public static final String ACTION_ORDER = "ORDER";
    }
}
