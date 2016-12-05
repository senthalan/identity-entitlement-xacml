/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.entitlement.xacml.core.pdp;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.balana.Balana;
import org.wso2.balana.PDP;
import org.wso2.balana.PDPConfig;
import org.wso2.balana.finder.AttributeFinder;
import org.wso2.balana.finder.AttributeFinderModule;
import org.wso2.balana.finder.PolicyFinder;
import org.wso2.balana.finder.PolicyFinderModule;
import org.wso2.balana.finder.ResourceFinder;
import org.wso2.balana.finder.ResourceFinderModule;
import org.wso2.carbon.identity.entitlement.xacml.core.EntitlementUtil;
import org.wso2.carbon.identity.entitlement.xacml.core.policy.finder.CarbonPolicyFinder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component(
        name = "org.wso2.carbon.identity.entitlement.xacml.core.pdp.EntitlementEngine",
        immediate = true
)
public class EntitlementEngine {

    private PDP pdp;
    private Balana balana;
    private PolicyFinder carbonPolicyFinder;
    private static final Object lock = new Object();
    private List<AttributeFinderModule> attributeModules = new ArrayList<>();
    private List<ResourceFinderModule> resourceModules = new ArrayList<>();
    private static EntitlementEngine entitlementEngine;


    private static final Logger logger = LoggerFactory.getLogger(EntitlementEngine.class);


    @Activate
    protected void start(BundleContext bundleContext) {
        entitlementEngine = new EntitlementEngine();
    }
    /**
     * Get a EntitlementEngine instance. This method will return an
     * EntitlementEngine instance if exists, or creates a new one
     *
     * @return EntitlementEngine instance for that tenant
     */
    public static EntitlementEngine getInstance() {


        if (entitlementEngine == null) {
            synchronized (lock) {
                if (entitlementEngine == null) {
                    entitlementEngine = new EntitlementEngine();
                }
            }
        }

        return entitlementEngine;
    }

    private EntitlementEngine() {

        // if PDP config file is not configured, then balana instance is created from default configurations
        balana = Balana.getInstance();


        AttributeFinder attributeFinder = new AttributeFinder();
        attributeFinder.setModules(attributeModules);

        ResourceFinder resourceFinder = new ResourceFinder();
        resourceFinder.setModules(resourceModules);

        setUPPolicyFinder();

        PDPConfig pdpConfig =
                new PDPConfig(attributeFinder, carbonPolicyFinder, resourceFinder, false);
        pdp = new PDP(pdpConfig);


    }


    /**
     * Test request for PDP
     *
     * @param xacmlRequest XACML request as String
     * @return response as String
     */
    public String evaluate(String xacmlRequest) {

        logger.debug("XACML Request : " + xacmlRequest);

        String xacmlResponse = pdp.evaluate(xacmlRequest);

        logger.debug("XACML Response : " + xacmlResponse);

        return xacmlResponse;
    }

    public String testPolicy(String action, String resource, String subject, String environment) {
        String xacmlRequest = EntitlementUtil.createSimpleXACMLRequest(subject, resource, action, environment);
        return evaluate(xacmlRequest);
    }


    private void setUPPolicyFinder() {

        carbonPolicyFinder = new PolicyFinder();
        Set<PolicyFinderModule> policyModules = new HashSet<>();
        CarbonPolicyFinder tmpCarbonPolicyFinder = new CarbonPolicyFinder();
        policyModules.add(tmpCarbonPolicyFinder);
        carbonPolicyFinder.setModules(policyModules);
        carbonPolicyFinder.init();

    }
}