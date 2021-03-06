package org.wso2.carbon.identity.entitlement.xacml.core.policy;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.wso2.balana.XACMLConstants;
import org.wso2.carbon.identity.entitlement.xacml.core.EntitlementConstants;
import org.wso2.carbon.identity.entitlement.xacml.core.EntitlementUtil;
import org.wso2.carbon.identity.entitlement.xacml.core.dto.AttributeDTO;
import org.wso2.carbon.identity.entitlement.xacml.core.exception.EntitlementException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * This class phrase the xml representation of policy and build the policy meta data such as
 * resource names, subject names action names and environment names, attribute ids and data types.
 */
public class PolicyAttributeBuilder {


    private int version;

    /**
     * This creates properties object which contains the policy meta data.
     *
     * @return properties object which contains the policy meta data
     * @throws EntitlementException throws
     */
    public AttributeDTO[] getPolicyMetaDataFromPolicy(String policy) throws EntitlementException {

        String version = EntitlementUtil.getPolicyVersion(policy);
        if (XACMLConstants.XACML_1_0_IDENTIFIER.equals(version)) {
            this.version = XACMLConstants.XACML_VERSION_1_0;
        } else if (XACMLConstants.XACML_2_0_IDENTIFIER.equals(version)) {
            this.version = XACMLConstants.XACML_VERSION_2_0;
        } else {
            this.version = XACMLConstants.XACML_VERSION_3_0;
        }

        List<AttributeDTO> attributeDTOs = new ArrayList<>();
        try {
            attributeDTOs = createPolicyMetaData(policy, attributeDTOs);
        } catch (EntitlementException e) {
            throw new EntitlementException("Can not create Policy MetaData for given policy");
        }

        return attributeDTOs.toArray(new AttributeDTO[attributeDTOs.size()]);
    }

    /**
     * This creates the OMElement from the policy xml and create the the meta data for hole policy
     *
     * @param policy        policy as a String
     * @param attributeDTOs object which holds the policy meta data in String format
     * @return list of AttributeDTO object which holds the policy meta data in String format
     * @throws EntitlementException throws if OMElement can not be created
     */
    public List<AttributeDTO> createPolicyMetaData(String policy,
                                                   List<AttributeDTO> attributeDTOs) throws EntitlementException {

        OMElement omElement;

        try {
            omElement = AXIOMUtil.stringToOM(policy);
        } catch (XMLStreamException e) {
            throw new EntitlementException("Policy xml can not be converted to OMElement");
        }

        if (omElement != null) {
            Iterator iterator1 = omElement.getChildrenWithLocalName(EntitlementConstants.TARGET_ELEMENT);
            if (iterator1 != null) {
                iterator1.forEachRemaining(targetElement -> {
                    if (version == XACMLConstants.XACML_VERSION_3_0) {
                        createMetaDataFromXACML3TargetElement((OMElement) targetElement, attributeDTOs);
                    } else {
                        createMetaDataFromTargetElement((OMElement) targetElement, attributeDTOs);
                    }
                });
            }

            Iterator iterator2 = omElement.getChildrenWithLocalName(EntitlementConstants.RULE_ELEMENT);
            iterator2.forEachRemaining(targetElement -> createMetaDataFromRuleElement((OMElement) targetElement,
                    attributeDTOs));

            Iterator iterator3 = omElement.getChildrenWithLocalName(EntitlementConstants.POLICY_ELEMENT);
            while (iterator3.hasNext()) {
                OMElement targetElement = (OMElement) iterator3.next();
                createPolicyMetaData(targetElement.toString(), attributeDTOs);
            }
        }

        return attributeDTOs;
    }

    /**
     * This extract policy meta data from target element in the policy
     *
     * @param omElement     target element as an OMElement
     * @param attributeDTOs list of AttributeDTO object which holds the policy meta data
     *                      in String format
     * @return list of AttributeDTO object which holds the policy meta data in String format
     */
    public List<AttributeDTO> createMetaDataFromTargetElement(OMElement omElement, List<AttributeDTO> attributeDTOs) {

        if (omElement != null) {

            Iterator iterator1 = omElement.
                    getChildrenWithLocalName(EntitlementConstants.RESOURCE_ELEMENT + "s");
            while (iterator1.hasNext()) {
                OMElement resourceElements = (OMElement) iterator1.next();

                Iterator iterator2 = resourceElements.
                        getChildrenWithLocalName(EntitlementConstants.RESOURCE_ELEMENT);
                while (iterator2.hasNext()) {
                    OMElement resourceElement = (OMElement) iterator2.next();

                    Iterator iterator3 = resourceElement.
                            getChildrenWithLocalName(EntitlementConstants.RESOURCE_ELEMENT +
                                    EntitlementConstants.MATCH_ELEMENT);

                    while (iterator3.hasNext()) {
                        OMElement resourceMatch = (OMElement) iterator3.next();
                        List<AttributeDTO> attributeDTOList =
                                createMetaDataFromMatchElement(resourceMatch,
                                        EntitlementConstants.RESOURCE_ELEMENT);
                        for (AttributeDTO attributeDTO : attributeDTOList) {
                            attributeDTOs.add(attributeDTO);
                        }
                    }
                }
            }

            Iterator iterator4 = omElement.
                    getChildrenWithLocalName(EntitlementConstants.SUBJECT_ELEMENT + "s");
            while (iterator4.hasNext()) {
                OMElement resourceElements = (OMElement) iterator4.next();

                Iterator iterator2 = resourceElements.
                        getChildrenWithLocalName(EntitlementConstants.SUBJECT_ELEMENT);
                while (iterator2.hasNext()) {
                    OMElement resourceElement = (OMElement) iterator2.next();

                    Iterator iterator3 = resourceElement.
                            getChildrenWithLocalName(EntitlementConstants.SUBJECT_ELEMENT +
                                    EntitlementConstants.MATCH_ELEMENT);

                    while (iterator3.hasNext()) {
                        OMElement resourceMatch = (OMElement) iterator3.next();
                        List<AttributeDTO> attributeDTOList =
                                createMetaDataFromMatchElement(resourceMatch,
                                        EntitlementConstants.SUBJECT_ELEMENT);
                        for (AttributeDTO attributeDTO : attributeDTOList) {
                            attributeDTOs.add(attributeDTO);
                        }
                    }
                }

            }

            Iterator iterator5 = omElement.
                    getChildrenWithLocalName(EntitlementConstants.ACTION_ELEMENT + "s");

            while (iterator5.hasNext()) {
                OMElement resourceElements = (OMElement) iterator5.next();

                Iterator iterator2 = resourceElements.
                        getChildrenWithLocalName(EntitlementConstants.ACTION_ELEMENT);
                while (iterator2.hasNext()) {
                    OMElement resourceElement = (OMElement) iterator2.next();

                    Iterator iterator3 = resourceElement.
                            getChildrenWithLocalName(EntitlementConstants.ACTION_ELEMENT +
                                    EntitlementConstants.MATCH_ELEMENT);

                    while (iterator3.hasNext()) {
                        OMElement resourceMatch = (OMElement) iterator3.next();
                        List<AttributeDTO> attributeDTOList =
                                createMetaDataFromMatchElement(resourceMatch,
                                        EntitlementConstants.ACTION_ELEMENT);
                        for (AttributeDTO attributeDTO : attributeDTOList) {
                            attributeDTOs.add(attributeDTO);
                        }
                    }
                }

            }

            Iterator iterator6 = omElement.
                    getChildrenWithLocalName(EntitlementConstants.ENVIRONMENT_ELEMENT + "s");

            while (iterator6.hasNext()) {
                OMElement resourceElements = (OMElement) iterator6.next();

                Iterator iterator2 = resourceElements.
                        getChildrenWithLocalName(EntitlementConstants.ENVIRONMENT_ELEMENT);
                while (iterator2.hasNext()) {
                    OMElement resourceElement = (OMElement) iterator2.next();

                    Iterator iterator3 = resourceElement.
                            getChildrenWithLocalName(EntitlementConstants.ENVIRONMENT_ELEMENT +
                                    EntitlementConstants.MATCH_ELEMENT);

                    while (iterator3.hasNext()) {
                        OMElement resourceMatch = (OMElement) iterator3.next();

                        List<AttributeDTO> attributeDTOList =
                                createMetaDataFromMatchElement(resourceMatch,
                                        EntitlementConstants.ENVIRONMENT_ELEMENT);
                        for (AttributeDTO attributeDTO : attributeDTOList) {
                            attributeDTOs.add(attributeDTO);
                        }
                    }
                }

            }

        }

        return attributeDTOs;
    }


    /**
     * This extract policy meta data from target element in the policy
     *
     * @param omElement     target element as an OMElement
     * @param attributeDTOs list of AttributeDTO object which holds the policy meta data
     *                      in String format
     * @return list of AttributeDTO object which holds the policy meta data in String format
     */
    public List<AttributeDTO> createMetaDataFromXACML3TargetElement(OMElement omElement,
                                                                    List<AttributeDTO> attributeDTOs) {

        if (omElement != null) {

            Iterator iterator1 = omElement.
                    getChildrenWithLocalName(EntitlementConstants.ANY_OF);
            while (iterator1.hasNext()) {

                OMElement anyOff = (OMElement) iterator1.next();

                Iterator iterator2 = anyOff.
                        getChildrenWithLocalName(EntitlementConstants.ALL_OF);

                while (iterator2.hasNext()) {
                    OMElement allOf = (OMElement) iterator2.next();

                    Iterator iterator3 = allOf.
                            getChildrenWithLocalName(EntitlementConstants.MATCH_ELEMENT);

                    while (iterator3.hasNext()) {
                        OMElement resourceMatch = (OMElement) iterator3.next();
                        List<AttributeDTO> attributeDTOList =
                                createMetaDataFromXACML3MatchElement(resourceMatch);
                        for (AttributeDTO attributeDTO : attributeDTOList) {
                            attributeDTOs.add(attributeDTO);
                        }
                    }
                }
            }
        }

        return attributeDTOs;
    }

    /**
     * This extract policy meta data from match element in the policy
     *
     * @param omElement      match element  as an OMElement
     * @param subElementName match element name
     * @return AttributeValueDTO object which holds the policy meta data in String format
     */
    public List<AttributeDTO> createMetaDataFromMatchElement(OMElement omElement,
                                                             String subElementName) {

        List<AttributeDTO> attributeDTOs = new ArrayList<AttributeDTO>();
        String attributeId = null;
        String dataType = null;

        if (omElement != null) {
            Iterator iterator1 = omElement.
                    getChildrenWithLocalName(subElementName + EntitlementConstants.ATTRIBUTE_DESIGNATOR);
            while (iterator1.hasNext()) {
                OMElement attributeDesignator = (OMElement) iterator1.next();
                if (attributeDesignator != null) {
                    attributeId = attributeDesignator.
                            getAttributeValue(new QName(EntitlementConstants.ATTRIBUTE_ID));
                    dataType = attributeDesignator.
                            getAttributeValue(new QName(EntitlementConstants.DATA_TYPE));
                }
            }

            Iterator iterator2 = omElement.
                    getChildrenWithLocalName(EntitlementConstants.ATTRIBUTE_SELECTOR);
            while (iterator2.hasNext()) {
                OMElement attributeDesignator = (OMElement) iterator2.next();
                if (attributeDesignator != null) {
                    attributeId = attributeDesignator.
                            getAttributeValue(new QName(EntitlementConstants.REQUEST_CONTEXT_PATH));
                    dataType = attributeDesignator.
                            getAttributeValue(new QName(EntitlementConstants.DATA_TYPE));
                }
            }

            Iterator iterator3 = omElement.
                    getChildrenWithLocalName(EntitlementConstants.ATTRIBUTE_VALUE);
            while (iterator3.hasNext()) {
                OMElement attributeElement = (OMElement) iterator3.next();
                if (attributeElement != null) {
                    AttributeDTO attributeDTO = new AttributeDTO();
                    attributeDTO.setAttributeValue(attributeElement.getText());
                    attributeDTO.setAttributeId(attributeId);
                    attributeDTO.setAttributeDataType(dataType);
                    attributeDTO.setCategory(subElementName);
                    attributeDTOs.add(attributeDTO);
                }
            }

        }
        return attributeDTOs;
    }


    /**
     * This extract policy meta data from match element in the policy
     *
     * @param omElement match element  as an OMElement
     * @return AttributeValueDTO object which holds the policy meta data in String format
     */
    public List<AttributeDTO> createMetaDataFromXACML3MatchElement(OMElement omElement) {

        List<AttributeDTO> attributeDTOs = new ArrayList<AttributeDTO>();
        String attributeId = null;
        String category = null;

        if (omElement != null) {
            Iterator iterator1 = omElement.
                    getChildrenWithLocalName(EntitlementConstants.ATTRIBUTE_DESIGNATOR);
            while (iterator1.hasNext()) {
                OMElement attributeDesignator = (OMElement) iterator1.next();
                if (attributeDesignator != null) {
                    attributeId = attributeDesignator.
                            getAttributeValue(new QName(EntitlementConstants.ATTRIBUTE_ID));
                    category = attributeDesignator.
                            getAttributeValue(new QName(EntitlementConstants.CATEGORY));
                }
            }

            Iterator iterator3 = omElement.
                    getChildrenWithLocalName(EntitlementConstants.ATTRIBUTE_VALUE);
            while (iterator3.hasNext()) {
                OMElement attributeElement = (OMElement) iterator3.next();
                if (attributeElement != null) {
                    String dataType = attributeElement.
                            getAttributeValue(new QName(EntitlementConstants.DATA_TYPE));
                    AttributeDTO attributeDTO = new AttributeDTO();
                    attributeDTO.setAttributeValue(attributeElement.getText());
                    attributeDTO.setAttributeId(attributeId);
                    attributeDTO.setAttributeDataType(dataType);
                    attributeDTO.setCategory(category);
                    attributeDTOs.add(attributeDTO);
                }
            }
        }
        return attributeDTOs;
    }

    /**
     * This extract policy meta data from apply element in the policy
     *
     * @param omElement     apply element as an OMElement
     * @param attributeDTOs list of AttributeDTO object which holds the policy meta data
     *                      in String format
     * @return list of AttributeDTO object which holds the policy meta data in String format
     */
    public List<AttributeDTO> createMetaDataFromApplyElement(OMElement omElement,
                                                             List<AttributeDTO> attributeDTOs) {

        //TODO check with function id and decide whether search can be done or not
        if (omElement != null) {
            Iterator iterator1 = omElement.
                    getChildrenWithLocalName(EntitlementConstants.RESOURCE_ELEMENT +
                            EntitlementConstants.ATTRIBUTE_DESIGNATOR);
            while (iterator1.hasNext()) {
                OMElement attributeDesignator = (OMElement) iterator1.next();
                if (attributeDesignator != null) {
                    String attributeId = attributeDesignator.
                            getAttributeValue(new QName(EntitlementConstants.ATTRIBUTE_ID));
                    String dataType = attributeDesignator.
                            getAttributeValue(new QName(EntitlementConstants.DATA_TYPE));
                    List<String> attributeValues = searchAttributeValues(omElement,
                            new ArrayList<String>(), true);
                    if (attributeValues == null) {
                        AttributeDTO attributeDTO = new AttributeDTO();
                        attributeDTO.setCategory(EntitlementConstants.UNKNOWN);
                        attributeDTO.setAttributeValue(EntitlementConstants.SEARCH_WARNING_MESSAGE1 +
                                " for " + EntitlementConstants.RESOURCE_ELEMENT +
                                " Designator Element ");
                        attributeDTOs.add(attributeDTO);
                    } else if (attributeValues.isEmpty()) {
                        AttributeDTO attributeDTO = new AttributeDTO();
                        attributeDTO.setCategory(EntitlementConstants.UNKNOWN);
                        attributeDTO.setAttributeValue(EntitlementConstants.SEARCH_WARNING_MESSAGE2 +
                                " for " + EntitlementConstants.RESOURCE_ELEMENT +
                                " Designator Element ");

                    } else {
                        for (String value : attributeValues) {
                            AttributeDTO attributeDTO = new AttributeDTO();
                            attributeDTO.setAttributeValue(value);
                            attributeDTO.setAttributeDataType(dataType);
                            attributeDTO.setCategory(EntitlementConstants.RESOURCE_ELEMENT);
                            attributeDTO.setAttributeId(attributeId);
                            attributeDTOs.add(attributeDTO);
                        }
                    }
                }
            }

            Iterator iterator2 = omElement.
                    getChildrenWithLocalName(EntitlementConstants.SUBJECT_ELEMENT +
                            EntitlementConstants.ATTRIBUTE_DESIGNATOR);
            while (iterator2.hasNext()) {
                OMElement attributeDesignator = (OMElement) iterator2.next();
                if (attributeDesignator != null) {
                    String attributeId = attributeDesignator.
                            getAttributeValue(new QName(EntitlementConstants.ATTRIBUTE_ID));
                    String dataType = attributeDesignator.
                            getAttributeValue(new QName(EntitlementConstants.DATA_TYPE));
                    List<String> attributeValues = searchAttributeValues(omElement,
                            new ArrayList<String>(), true);
                    if (attributeValues == null) {
                        AttributeDTO attributeDTO = new AttributeDTO();
                        attributeDTO.setCategory(EntitlementConstants.UNKNOWN);
                        attributeDTO.setAttributeValue(EntitlementConstants.SEARCH_WARNING_MESSAGE1 +
                                " for " + EntitlementConstants.RESOURCE_ELEMENT +
                                " Designator Element ");

                        attributeDTOs.add(attributeDTO);
                    } else if (attributeValues.isEmpty()) {
                        AttributeDTO attributeDTO = new AttributeDTO();
                        attributeDTO.setCategory(EntitlementConstants.UNKNOWN);
                        attributeDTO.setAttributeValue(EntitlementConstants.SEARCH_WARNING_MESSAGE2 +
                                " for " + EntitlementConstants.RESOURCE_ELEMENT +
                                " Designator Element ");

                    } else {
                        for (String value : attributeValues) {
                            AttributeDTO attributeDTO = new AttributeDTO();
                            attributeDTO.setAttributeValue(value);
                            attributeDTO.setAttributeDataType(dataType);
                            attributeDTO.setCategory(EntitlementConstants.SUBJECT_ELEMENT);
                            attributeDTO.setAttributeId(attributeId);
                            attributeDTOs.add(attributeDTO);
                        }
                    }
                }
            }

            Iterator iterator3 = omElement.
                    getChildrenWithLocalName(EntitlementConstants.ACTION_ELEMENT +
                            EntitlementConstants.ATTRIBUTE_DESIGNATOR);
            while (iterator3.hasNext()) {
                OMElement attributeDesignator = (OMElement) iterator3.next();
                if (attributeDesignator != null) {
                    String attributeId = attributeDesignator.
                            getAttributeValue(new QName(EntitlementConstants.ATTRIBUTE_ID));
                    String dataType = attributeDesignator.
                            getAttributeValue(new QName(EntitlementConstants.DATA_TYPE));
                    List<String> attributeValues = searchAttributeValues(omElement,
                            new ArrayList<String>(), true);
                    if (attributeValues == null) {
                        AttributeDTO attributeDTO = new AttributeDTO();
                        attributeDTO.setCategory(EntitlementConstants.UNKNOWN);
                        attributeDTO.setAttributeValue(EntitlementConstants.SEARCH_WARNING_MESSAGE1 +
                                " for " + EntitlementConstants.RESOURCE_ELEMENT +
                                " Designator Element ");

                        attributeDTOs.add(attributeDTO);
                    } else if (attributeValues.isEmpty()) {
                        AttributeDTO attributeDTO = new AttributeDTO();
                        attributeDTO.setCategory(EntitlementConstants.UNKNOWN);
                        attributeDTO.setAttributeValue(EntitlementConstants.SEARCH_WARNING_MESSAGE2 +
                                " for " + EntitlementConstants.RESOURCE_ELEMENT +
                                " Designator Element ");

                    } else {
                        for (String value : attributeValues) {
                            AttributeDTO attributeDTO = new AttributeDTO();
                            attributeDTO.setAttributeValue(value);
                            attributeDTO.setAttributeDataType(dataType);
                            attributeDTO.setCategory(EntitlementConstants.ACTION_ELEMENT);
                            attributeDTO.setAttributeId(attributeId);
                            attributeDTOs.add(attributeDTO);
                        }
                    }
                }
            }

            Iterator iterator4 = omElement.
                    getChildrenWithLocalName(EntitlementConstants.ENVIRONMENT_ELEMENT +
                            EntitlementConstants.ATTRIBUTE_DESIGNATOR);
            while (iterator4.hasNext()) {
                OMElement attributeDesignator = (OMElement) iterator4.next();
                if (attributeDesignator != null) {
                    String attributeId = attributeDesignator.
                            getAttributeValue(new QName(EntitlementConstants.ATTRIBUTE_ID));
                    String dataType = attributeDesignator.
                            getAttributeValue(new QName(EntitlementConstants.DATA_TYPE));
                    List<String> attributeValues = searchAttributeValues(omElement,
                            new ArrayList<String>(), true);
                    if (attributeValues == null) {
                        AttributeDTO attributeDTO = new AttributeDTO();
                        attributeDTO.setCategory(EntitlementConstants.UNKNOWN);
                        attributeDTO.setAttributeValue(EntitlementConstants.SEARCH_WARNING_MESSAGE1 +
                                " for " + EntitlementConstants.RESOURCE_ELEMENT +
                                " Designator Element ");

                        attributeDTOs.add(attributeDTO);
                    } else if (attributeValues.isEmpty()) {
                        AttributeDTO attributeDTO = new AttributeDTO();
                        attributeDTO.setCategory(EntitlementConstants.UNKNOWN);
                        attributeDTO.setAttributeValue(EntitlementConstants.SEARCH_WARNING_MESSAGE2 +
                                " for " + EntitlementConstants.RESOURCE_ELEMENT +
                                " Designator Element ");

                    } else {
                        for (String value : attributeValues) {
                            AttributeDTO attributeDTO = new AttributeDTO();
                            attributeDTO.setAttributeValue(value);
                            attributeDTO.setAttributeDataType(dataType);
                            attributeDTO.setCategory(EntitlementConstants.ENVIRONMENT_ELEMENT);
                            attributeDTO.setAttributeId(attributeId);
                            attributeDTOs.add(attributeDTO);
                        }
                    }
                }
            }

            Iterator iterator5 = omElement.
                    getChildrenWithLocalName(EntitlementConstants.ATTRIBUTE_SELECTOR);
            while (iterator5.hasNext()) {
                OMElement attributeSelector = (OMElement) iterator5.next();
                if (attributeSelector != null) {
                    String attributeId = attributeSelector.
                            getAttributeValue(new QName(EntitlementConstants.REQUEST_CONTEXT_PATH));
                    String subElementName = attributeId;  //TODO  Fix finding element name from Xpath 
                    String dataType = attributeSelector.
                            getAttributeValue(new QName(EntitlementConstants.DATA_TYPE));
                    List<String> attributeValues = searchAttributeValues(omElement,
                            new ArrayList<String>(), true);
                    if (attributeValues == null) {
                        AttributeDTO attributeDTO = new AttributeDTO();
                        attributeDTO.setCategory(EntitlementConstants.UNKNOWN);
                        attributeDTO.setAttributeValue(EntitlementConstants.SEARCH_WARNING_MESSAGE3);

                        attributeDTOs.add(attributeDTO);
                    } else if (attributeValues.isEmpty()) {
                        AttributeDTO attributeDTO = new AttributeDTO();
                        attributeDTO.setCategory(EntitlementConstants.UNKNOWN);
                        attributeDTO.setAttributeValue(EntitlementConstants.SEARCH_WARNING_MESSAGE3);

                    } else {
                        for (String value : attributeValues) {
                            AttributeDTO attributeDTO = new AttributeDTO();
                            attributeDTO.setAttributeValue(value);
                            attributeDTO.setAttributeDataType(dataType);
                            attributeDTO.setCategory(subElementName);
                            attributeDTO.setAttributeId(attributeId);
                            attributeDTOs.add(attributeDTO);
                            // Remove following after fixing XPath issues
                            attributeDTO.setCategory(EntitlementConstants.UNKNOWN);
                            attributeDTO.setAttributeValue(EntitlementConstants.SEARCH_WARNING_MESSAGE3);
                        }
                    }
                }
            }

            Iterator iterator6 = omElement.
                    getChildrenWithLocalName(EntitlementConstants.ATTRIBUTE_VALUE);
            if (iterator6.hasNext()) {
                List<String> attributeValues = searchAttributeValues(omElement, new ArrayList<String>(), false);
                if (attributeValues == null) {
                    AttributeDTO attributeDTO = new AttributeDTO();
                    attributeDTO.setCategory(EntitlementConstants.UNKNOWN);
                    attributeDTO.setAttributeValue(EntitlementConstants.SEARCH_WARNING_MESSAGE1 +
                            " for " + EntitlementConstants.RESOURCE_ELEMENT +
                            " Designator Element ");
                    attributeDTOs.add(attributeDTO);
                } else if (attributeValues.isEmpty()) {
                    AttributeDTO attributeDTO = new AttributeDTO();
                    attributeDTO.setCategory(EntitlementConstants.UNKNOWN);
                    attributeDTO.setAttributeValue(EntitlementConstants.SEARCH_WARNING_MESSAGE2 +
                            " for " + EntitlementConstants.RESOURCE_ELEMENT +
                            " Designator Element ");
                } else {
                    for (String values : attributeValues) {
                        AttributeDTO attributeDTO = new AttributeDTO();
                        attributeDTO.setAttributeValue(values);
                        Iterator iterator8 = omElement.
                                getChildrenWithLocalName(EntitlementConstants.APPLY_ELEMENT);
                        while (iterator8.hasNext()) {
                            OMElement applyElement = (OMElement) iterator8.next();
                            if (version == XACMLConstants.XACML_VERSION_3_0) {
                                searchXACML3Designator(applyElement, attributeDTO);
                            } else {
                                searchDesignatorOrSelector(applyElement, attributeDTO);
                            }
                        }
                        if (attributeDTO.getCategory() != null ||
                                "".equals(attributeDTO.getCategory())) {
                            attributeDTOs.add(attributeDTO);
                        }
                    }
                }
            }

            Iterator iterator7 = omElement.getChildrenWithLocalName(EntitlementConstants.APPLY_ELEMENT);
            while (iterator7.hasNext()) {
                OMElement applyElement = (OMElement) iterator7.next();
                createMetaDataFromApplyElement(applyElement, attributeDTOs);
            }
        }
        return attributeDTOs;
    }


    /**
     * This extract policy meta data from apply element in the policy
     *
     * @param omElement     apply element as an OMElement
     * @param attributeDTOs list of AttributeDTO object which holds the policy meta data
     *                      in String format
     * @return list of AttributeDTO object which holds the policy meta data in String format
     */
    public List<AttributeDTO> createMetaDataFromXACML3ApplyElement(OMElement omElement,
                                                                   List<AttributeDTO> attributeDTOs) {

        //TODO check with function id and decide whether search can be done or not
        if (omElement != null) {
            Iterator iterator1 = omElement.
                    getChildrenWithLocalName(EntitlementConstants.ATTRIBUTE_DESIGNATOR);
            while (iterator1.hasNext()) {
                OMElement attributeDesignator = (OMElement) iterator1.next();
                if (attributeDesignator != null) {
                    String attributeId = attributeDesignator.
                            getAttributeValue(new QName(EntitlementConstants.ATTRIBUTE_ID));
                    String category = attributeDesignator.
                            getAttributeValue(new QName(EntitlementConstants.CATEGORY));
                    String dataType = attributeDesignator.
                            getAttributeValue(new QName(EntitlementConstants.DATA_TYPE));   //TODO
                    List<String> attributeValues = searchAttributeValues(omElement,
                            new ArrayList<String>(), true);
                    if (attributeValues == null) {
                        AttributeDTO attributeDTO = new AttributeDTO();
                        attributeDTO.setCategory(EntitlementConstants.UNKNOWN);
                        attributeDTO.setAttributeValue(EntitlementConstants.SEARCH_WARNING_MESSAGE1 +
                                " for " + EntitlementConstants.RESOURCE_ELEMENT +
                                " Designator Element ");
                        attributeDTOs.add(attributeDTO);
                    } else if (attributeValues.isEmpty()) {
                        AttributeDTO attributeDTO = new AttributeDTO();
                        attributeDTO.setCategory(EntitlementConstants.UNKNOWN);
                        attributeDTO.setAttributeValue(EntitlementConstants.SEARCH_WARNING_MESSAGE2 +
                                " for " + EntitlementConstants.RESOURCE_ELEMENT +
                                " Designator Element ");

                    } else {
                        for (String value : attributeValues) {
                            AttributeDTO attributeDTO = new AttributeDTO();
                            attributeDTO.setAttributeValue(value);
                            attributeDTO.setAttributeDataType(dataType);
                            attributeDTO.setCategory(category);
                            attributeDTO.setAttributeId(attributeId);
                            attributeDTOs.add(attributeDTO);
                        }
                    }
                }
            }


            Iterator iterator2 = omElement.
                    getChildrenWithLocalName(EntitlementConstants.ATTRIBUTE_VALUE);
            if (iterator2.hasNext()) {
                List<String> attributeValues = searchAttributeValues(omElement, new ArrayList<String>(), false);
                if (attributeValues == null) {
                    AttributeDTO attributeDTO = new AttributeDTO();
                    attributeDTO.setCategory(EntitlementConstants.UNKNOWN);
                    attributeDTO.setAttributeValue(EntitlementConstants.SEARCH_WARNING_MESSAGE1 +
                            " for " + EntitlementConstants.RESOURCE_ELEMENT +
                            " Designator Element ");
                    attributeDTOs.add(attributeDTO);
                } else if (attributeValues.isEmpty()) {
                    AttributeDTO attributeDTO = new AttributeDTO();
                    attributeDTO.setCategory(EntitlementConstants.UNKNOWN);
                    attributeDTO.setAttributeValue(EntitlementConstants.SEARCH_WARNING_MESSAGE2 +
                            " for " + EntitlementConstants.RESOURCE_ELEMENT +
                            " Designator Element ");
                } else {
                    for (String values : attributeValues) {
                        AttributeDTO attributeDTO = new AttributeDTO();
                        attributeDTO.setAttributeValue(values);
                        Iterator iterator8 = omElement.
                                getChildrenWithLocalName(EntitlementConstants.APPLY_ELEMENT);
                        while (iterator8.hasNext()) {
                            OMElement applyElement = (OMElement) iterator8.next();
                            searchXACML3Designator(applyElement, attributeDTO);
                        }
                        if (attributeDTO.getCategory() != null ||
                                "".equals(attributeDTO.getCategory())) {
                            attributeDTOs.add(attributeDTO);
                        }
                    }
                }
            }

            Iterator iterator7 = omElement.getChildrenWithLocalName(EntitlementConstants.APPLY_ELEMENT);
            while (iterator7.hasNext()) {
                OMElement applyElement = (OMElement) iterator7.next();
                createMetaDataFromXACML3ApplyElement(applyElement, attributeDTOs);
            }
        }
        return attributeDTOs;
    }

    /**
     * This searches through  attribute values in the attribute elements to extract the policy meta data
     *
     * @param omElement         apply element as an OMElement
     * @param values            set of String objects
     * @param searchDesignators states where,  to find designators which are involved in creating
     *                          attribute values
     * @return AttributeValueDTO object which holds the policy meta data in String format
     */
    public List<String> searchAttributeValues(OMElement omElement, List<String> values,
                                              boolean searchDesignators) {

        if (values != null) {
            Iterator iterator = omElement.
                    getChildrenWithLocalName(EntitlementConstants.ATTRIBUTE_VALUE);
            while (iterator.hasNext()) {
                OMElement attributeElement = (OMElement) iterator.next();
                if (attributeElement != null) {
                    String dataType = attributeElement.
                            getAttributeValue(new QName(EntitlementConstants.DATA_TYPE));   // TODO
                    values.add(attributeElement.getText());
                }
            }
        }

        Iterator iterator1 = omElement.getChildrenWithLocalName(EntitlementConstants.APPLY_ELEMENT);
        while (iterator1.hasNext()) {
            OMElement applyElement = (OMElement) iterator1.next();
            searchAttributeValues(applyElement, values, searchDesignators);

            AttributeDTO attributeDTO = new AttributeDTO();
            if (searchDesignators) {
                if (version == XACMLConstants.XACML_VERSION_3_0) {
                    searchXACML3Designator(applyElement, attributeDTO);
                } else {
                    searchDesignatorOrSelector(applyElement, attributeDTO);
                }
            }
            if (attributeDTO.getCategory() != null || attributeDTO.getAttributeId() != null ||
                    attributeDTO.getAttributeDataType() != null) {
                values = null;
            }
        }

        return values;
    }

    /**
     * This searches through  designator and selector values in the attribute elements to extract
     * the policy meta data
     *
     * @param omElement    apply element as an OMElement
     * @param attributeDTO AttributeDTO object which holds the policy meta data in String format
     * @return AttributeValueDTO object which holds the policy meta data in String format
     */
    public AttributeDTO searchDesignatorOrSelector(OMElement omElement,
                                                   AttributeDTO attributeDTO) {


        Iterator iterator1 = omElement.
                getChildrenWithLocalName(EntitlementConstants.RESOURCE_ELEMENT +
                        EntitlementConstants.ATTRIBUTE_DESIGNATOR);
        while (iterator1.hasNext()) {
            OMElement attributeDesignator = (OMElement) iterator1.next();
            if (attributeDesignator != null) {
                String attributeId = attributeDesignator.
                        getAttributeValue(new QName(EntitlementConstants.ATTRIBUTE_ID));
                String dataType = attributeDesignator.
                        getAttributeValue(new QName(EntitlementConstants.DATA_TYPE));
                attributeDTO.setAttributeDataType(dataType);
                attributeDTO.setCategory(EntitlementConstants.RESOURCE_ELEMENT);
                attributeDTO.setAttributeId(attributeId);
            }
        }

        Iterator iterator2 = omElement.
                getChildrenWithLocalName(EntitlementConstants.SUBJECT_ELEMENT +
                        EntitlementConstants.ATTRIBUTE_DESIGNATOR);
        while (iterator2.hasNext()) {
            OMElement attributeDesignator = (OMElement) iterator2.next();
            if (attributeDesignator != null) {
                String attributeId = attributeDesignator.
                        getAttributeValue(new QName(EntitlementConstants.ATTRIBUTE_ID));
                String dataType = attributeDesignator.
                        getAttributeValue(new QName(EntitlementConstants.DATA_TYPE));
                attributeDTO.setAttributeDataType(dataType);
                attributeDTO.setCategory(EntitlementConstants.SUBJECT_ELEMENT);
                attributeDTO.setAttributeId(attributeId);
            }
        }

        Iterator iterator3 = omElement.
                getChildrenWithLocalName(EntitlementConstants.ACTION_ELEMENT +
                        EntitlementConstants.ATTRIBUTE_DESIGNATOR);
        while (iterator3.hasNext()) {
            OMElement attributeDesignator = (OMElement) iterator3.next();
            if (attributeDesignator != null) {
                String attributeId = attributeDesignator.
                        getAttributeValue(new QName(EntitlementConstants.ATTRIBUTE_ID));
                String dataType = attributeDesignator.
                        getAttributeValue(new QName(EntitlementConstants.DATA_TYPE));
                attributeDTO.setAttributeDataType(dataType);
                attributeDTO.setCategory(EntitlementConstants.ACTION_ELEMENT);
                attributeDTO.setAttributeId(attributeId);
            }
        }

        Iterator iterator4 = omElement.
                getChildrenWithLocalName(EntitlementConstants.ENVIRONMENT_ELEMENT +
                        EntitlementConstants.ATTRIBUTE_DESIGNATOR);
        while (iterator4.hasNext()) {
            OMElement attributeDesignator = (OMElement) iterator4.next();
            if (attributeDesignator != null) {
                String attributeId = attributeDesignator.
                        getAttributeValue(new QName(EntitlementConstants.ATTRIBUTE_ID));
                String dataType = attributeDesignator.
                        getAttributeValue(new QName(EntitlementConstants.DATA_TYPE));
                attributeDTO.setAttributeDataType(dataType);
                attributeDTO.setCategory(EntitlementConstants.ENVIRONMENT_ELEMENT);
                attributeDTO.setAttributeId(attributeId);
            }
        }

        Iterator iterator5 = omElement.
                getChildrenWithLocalName(EntitlementConstants.ATTRIBUTE_SELECTOR);
        while (iterator5.hasNext()) {
            OMElement attributeDesignator = (OMElement) iterator5.next();
            if (attributeDesignator != null) {
                String attributeId = attributeDesignator.
                        getAttributeValue(new QName(EntitlementConstants.REQUEST_CONTEXT_PATH));
                String subElementName = attributeId;  //TODO  Fix finding element name from Xpath
                String dataType = attributeDesignator.
                        getAttributeValue(new QName(EntitlementConstants.DATA_TYPE));
                attributeDTO.setAttributeDataType(dataType);
                attributeDTO.setCategory(EntitlementConstants.UNKNOWN);
                attributeDTO.setAttributeValue(EntitlementConstants.SEARCH_WARNING_MESSAGE3);
                attributeDTO.setAttributeId(attributeId);
            }
        }

        return attributeDTO;
    }

    /**
     * This searches through  designator and selector values in the attribute elements to extract
     * the policy meta data
     *
     * @param omElement    apply element as an OMElement
     * @param attributeDTO AttributeDTO object which holds the policy meta data in String format
     * @return AttributeValueDTO object which holds the policy meta data in String format
     */
    public AttributeDTO searchXACML3Designator(OMElement omElement,
                                               AttributeDTO attributeDTO) {

        Iterator iterator1 = omElement.
                getChildrenWithLocalName(EntitlementConstants.ATTRIBUTE_DESIGNATOR);
        while (iterator1.hasNext()) {
            OMElement attributeDesignator = (OMElement) iterator1.next();
            if (attributeDesignator != null) {
                String attributeId = attributeDesignator.
                        getAttributeValue(new QName(EntitlementConstants.ATTRIBUTE_ID));
                String category = attributeDesignator.
                        getAttributeValue(new QName(EntitlementConstants.CATEGORY));
                String dataType = attributeDesignator.
                        getAttributeValue(new QName(EntitlementConstants.DATA_TYPE));
                attributeDTO.setCategory(category);
                attributeDTO.setAttributeId(attributeId);
                attributeDTO.setAttributeDataType(dataType);
            }
        }

        return attributeDTO;
    }

    /**
     * This extract policy meta data from condition element in the policy
     *
     * @param omElement     condition element as an OMElement
     * @param attributeDTOs list of AttributeDTO object which holds the policy meta data
     *                      in String format
     * @return list of AttributeDTO object which holds the policy meta data in String format
     */
    public List<AttributeDTO> createMetaDataFromConditionElement(OMElement omElement,
                                                                 List<AttributeDTO> attributeDTOs) {

        Iterator iterator = omElement.getChildrenWithLocalName(EntitlementConstants.APPLY_ELEMENT);
        if (iterator.hasNext()) {
            if (version == XACMLConstants.XACML_VERSION_3_0) {
                createMetaDataFromXACML3ApplyElement(omElement, attributeDTOs);
            } else {
                createMetaDataFromApplyElement(omElement, attributeDTOs);
            }
        } else {
            AttributeDTO attributeDTO = new AttributeDTO();
            attributeDTO.setCategory(EntitlementConstants.UNKNOWN);
            attributeDTO.setAttributeValue(EntitlementConstants.SEARCH_WARNING_MESSAGE4);
        }

        // TODO currently only search meta data on Apply Element, support for other elements 
        return attributeDTOs;
    }

    /**
     * This extract policy meta data from each rule element in the policy
     *
     * @param omElement     rule element as an OMElement
     * @param attributeDTOs list of AttributeDTO object which holds the policy meta data
     *                      in String format
     * @return list of AttributeDTO object which holds the policy meta data in String format
     */
    public List<AttributeDTO> createMetaDataFromRuleElement(OMElement omElement,
                                                            List<AttributeDTO> attributeDTOs) {

        if (omElement != null) {

            Iterator iterator1 = omElement.getChildrenWithLocalName(EntitlementConstants.
                    TARGET_ELEMENT);
            while (iterator1.hasNext()) {
                OMElement targetElement = (OMElement) iterator1.next();
                if (version == XACMLConstants.XACML_VERSION_3_0) {
                    createMetaDataFromXACML3TargetElement(targetElement, attributeDTOs);
                } else {
                    createMetaDataFromTargetElement(targetElement, attributeDTOs);
                }
            }

            Iterator iterator2 = omElement.getChildrenWithLocalName(EntitlementConstants.
                    CONDITION_ELEMENT);
            while (iterator2.hasNext()) {
                OMElement conditionElement = (OMElement) iterator2.next();
                createMetaDataFromConditionElement(conditionElement, attributeDTOs);
            }
        }

        return attributeDTOs;
    }
}
