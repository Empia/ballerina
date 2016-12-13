/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerina.core.parser.visitor;

import org.wso2.ballerina.core.interpreter.SymbolTable;
import org.wso2.ballerina.core.model.Annotation;
import org.wso2.ballerina.core.model.Resource;
import org.wso2.ballerina.core.model.Service;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.VariableDcl;
import org.wso2.ballerina.core.parser.BallerinaBaseVisitor;
import org.wso2.ballerina.core.parser.BallerinaParser;
import org.wso2.ballerina.core.utils.BValueFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Visitor for service
 */
public class ServiceVisitor extends BallerinaBaseVisitor {

    private SymbolTable serviceSymbolTable;

    public ServiceVisitor(SymbolTable parentSymbolTable) {
        this.serviceSymbolTable = new SymbolTable(parentSymbolTable);
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitServiceDefinition(BallerinaParser.ServiceDefinitionContext ctx) {
        // Create the service object
        String serviceName = ctx.Identifier().getText();
        SymbolName symbolName = new SymbolName(serviceName);
        Service serviceObject = new Service(symbolName);

        // Read the Annotations
        AnnotationVisitor annotationVisitor = new AnnotationVisitor();
        for (BallerinaParser.AnnotationContext annotationContext : ctx.annotation()) {
            serviceObject.addAnnotation((Annotation) annotationContext.accept(annotationVisitor));
        }

//        // Read the Connector declarations
//        ConnectorVisitor connectorVisitor = new ConnectorVisitor(serviceSymbolTable);
//        for (BallerinaParser.ConnectorDeclarationContext cdc :
//                ctx.serviceBody().serviceBodyDeclaration().connectorDeclaration()) {
//            Connection connection = (Connection) cdc.accept(connectorVisitor);
//            serviceObject.addConnector();
//            serviceSymbolTable.put(variableDcl.getIdentifier(),
//                    BValueFactory.createBValueFromVariableDeclaration(variableDcl));
//        }

        // Read the variable declarations
        VariableDeclarationVisitor variableDeclarationVisitor = new VariableDeclarationVisitor(serviceSymbolTable);
        for (BallerinaParser.VariableDeclarationContext variableDeclarationContext :
                ctx.serviceBody().serviceBodyDeclaration().variableDeclaration()) {
            VariableDcl variableDcl = (VariableDcl) variableDeclarationContext.accept(variableDeclarationVisitor);
            serviceObject.addVariable(variableDcl);
            serviceSymbolTable.put(variableDcl.getSymbolName(),
                    BValueFactory.createBValueFromVariableDeclaration(variableDcl));
        }

        // Read the resources
        List<Resource> resources = (List<Resource>) this.
                visitServiceBodyDeclaration(ctx.serviceBody().serviceBodyDeclaration());
        /* ToDo : Fix the listener properly to handle the new Model API.  */
        Resource[] resourceArray = new Resource[resources.size()];
        serviceObject.setResources(resources.toArray(resourceArray));

        return serviceObject;
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitServiceBody(BallerinaParser.ServiceBodyContext ctx) {
        return super.visitServiceBody(ctx);
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitServiceBodyDeclaration(BallerinaParser.ServiceBodyDeclarationContext ctx) {
        // Read the resources
        List<Resource> resources = new ArrayList<>();
        ResourceVisitor resourceVisitor = new ResourceVisitor(serviceSymbolTable);
        for (BallerinaParser.ResourceDefinitionContext rdc : ctx.resourceDefinition()) {
            Resource resourceObject = (Resource) rdc.accept(resourceVisitor);
            resources.add(resourceObject);
            //serviceObject.addResource(resourceObject);
        }
        return resources;
    }

    /**
     * Base method for retrieving the symbol table
     *
     * @return symbol table for this instance
     */
    @Override
    public SymbolTable getSymbolTable() {
        return this.serviceSymbolTable;
    }
}
