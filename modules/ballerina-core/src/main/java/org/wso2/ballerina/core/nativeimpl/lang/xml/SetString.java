/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 **/

package org.wso2.ballerina.core.nativeimpl.lang.xml;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMContainer;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.jaxen.JaxenException;
import org.jaxen.XPathSyntaxException;
import org.osgi.service.component.annotations.Component;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.model.values.XMLValue;
import org.wso2.ballerina.core.nativeimpl.AbstractNativeFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaFunction;
import org.wso2.ballerina.core.nativeimpl.lang.utils.ErrorHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Set the string value of a element that matches the given xPath.
 * If the xPath matches to an existing element, this method will update the value of it.
 * If the xPath does not match to an existing element, this method will add a new element
 * to match the xPath.
 */
@BallerinaFunction(
        packageName = "ballerina.lang.xml",
        functionName = "set",
        args = {@Argument(name = "xml", type = TypeEnum.XML),
                @Argument(name = "xPath", type = TypeEnum.STRING),
//                @Argument(name = "nameSpaces", type = TypeEnum.MAP),
                @Argument(name = "value", type = TypeEnum.STRING)},
        isPublic = true
)
@Component(
        name = "func.lang.xml_setString",
        immediate = true,
        service = AbstractNativeFunction.class
)
public class SetString extends AbstractNativeFunction {
    
    private static final String OPERATION = "set string in xml";

    @Override
    public BValue<?>[] execute(Context ctx) {
        try {
            // Accessing Parameters.
            XMLValue xml = (XMLValue) getArgument(ctx, 0).getBValue();
            String xPath = getArgument(ctx, 1).getString();
            // MapValue<String, String> nameSpaces = getArgument(ctx, 2).getMap();
            String value = getArgument(ctx, 2).getString();
            
            // Setting the value to XML
            AXIOMXPath axiomxPath = new AXIOMXPath(xPath);
            // set the namespaces
            /*if (nameSpaces != null && !nameSpaces.isEmpty()) {
                for (MapValue<String, String>.MapEntry<String, String> entry : nameSpaces.getValue()) {
                    axiomxPath.addNamespace(entry.getKey(), entry.getValue());

                }
            }*/
            Object ob = axiomxPath.evaluate(xml.getValue());
            if (ob instanceof ArrayList) {
                List list = (List) ob;
                for (Object obj : list) {
                    if (obj instanceof OMNode) {
                        OMNode omNode = (OMNode) obj;
                        OMContainer omContainer = omNode.getParent();
                        omNode.detach();
                        OMAbstractFactory.getOMFactory().createOMText(omContainer, value);
                    } else if (obj instanceof OMAttribute) {
                        OMAttribute omAttribute = (OMAttribute) obj;
                        omAttribute.setAttributeValue(value);
                    } else {
                        ErrorHandler.logWarn(OPERATION, "xPath '" + xPath + "' doesnot refers to a attribute or text.");
                    }
                }
            }
        } catch (XPathSyntaxException e) {
            ErrorHandler.handleInvalidXPath(OPERATION, e);
        } catch (JaxenException e) {
            ErrorHandler.handleXPathException(OPERATION, e);
        } catch (Throwable e) {
            ErrorHandler.handleXPathException(OPERATION, e);
        }
        
        return VOID_RETURN;
    }
}
