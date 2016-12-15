/*
*   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerina.core.model;

import org.testng.Assert;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.interpreter.ControlStack;
import org.wso2.ballerina.core.model.expressions.Expression;
import org.wso2.ballerina.core.model.expressions.FunctionInvocationExpr;
import org.wso2.ballerina.core.model.expressions.VariableRefExpr;
import org.wso2.ballerina.core.model.statements.AssignStmt;
import org.wso2.ballerina.core.model.statements.BlockStmt;
import org.wso2.ballerina.core.model.statements.ReturnStmt;
import org.wso2.ballerina.core.model.statements.Statement;
import org.wso2.ballerina.core.model.types.StringType;
import org.wso2.ballerina.core.model.types.Type;
import org.wso2.ballerina.core.model.values.BValueRef;
import org.wso2.ballerina.core.model.values.StringValue;
import org.wso2.ballerina.core.nativeimpl.lang.system.Print;
import org.wso2.ballerina.core.nativeimpl.lang.system.Println;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Test Native function invocation.
 */
public class NativeFunctionInvocationTest {

    private static final String ECHO_STRING = "Hello World...!!!";

    private static final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    private PrintStream original;

//    @BeforeClass
    public void setup() {
        original = System.out;
        System.setOut(new PrintStream(outContent));
    }

//    @AfterClass
    public void cleanup() throws IOException {
        System.setOut(original);
        outContent.close();
    }

    /**
     * function nestedNative(string echo) (string) {
     * string value ;
     * value = echoString(echo);
     * return value;
     * }
     *
     * @return BallerinaFunction.
     */
    private Function getFunctionNestedNative() {

        Annotation[] annotations = new Annotation[0];

        Parameter echo = new Parameter(new StringType(), new SymbolName("echo"));
        Parameter[] parameters = new Parameter[1];
        parameters[0] = echo;

        Type[] returnTypes = new Type[1];
        returnTypes[0] = new StringType();

        ConnectorDcl[] connectorDcls = new ConnectorDcl[0];

        Worker[] workers = new Worker[0];

        VariableDcl varDclValue = new VariableDcl(new StringType(), new SymbolName("value"), "");
        VariableDcl[] variableDcls = new VariableDcl[1];
        variableDcls[0] = varDclValue;


        // BallerinaFunction body
        SymbolName idEcho = new SymbolName("echo");
        VariableRefExpr varRefEcho = new VariableRefExpr(idEcho);
        varRefEcho.setEvalFunction(VariableRefExpr.createGetParamValueFunc(0));

        SymbolName idValue = new SymbolName("value");
        VariableRefExpr varRefExprValue = new VariableRefExpr(idValue);
        varRefExprValue.setEvalFunction(VariableRefExpr.createGetLocalValueFunc(0));

        List<Expression> expressions = new ArrayList<>(1);
        expressions.add(varRefEcho);

        FunctionInvocationExpr functionInvocationExpression = new FunctionInvocationExpr(
                new SymbolName("echoString"), expressions);
        // Creating new test function.
//        AbstractNativeFunction nativeFunction = new EchoStringNativeFunction();
//        functionInvocationExpression.setFunction(nativeFunction);

        AssignStmt assignStmt = new AssignStmt(varRefExprValue, functionInvocationExpression);
        ReturnStmt returnStmt = new ReturnStmt(varRefExprValue);

        Statement[] statements = new Statement[2];
        statements[0] = assignStmt;
        statements[1] = returnStmt;

        BlockStmt funcBody = new BlockStmt(statements);

        // Creating new function

        BallerinaFunction function = new BallerinaFunction(
                new SymbolName("nestedNative"),
                false,
                annotations,
                parameters,
                returnTypes,
                connectorDcls,
                variableDcls,
                workers,
                funcBody);

        return function;

    }

//    @Test
    public void testNativeFunction() {
        Context ctx = new Context();
        ControlStack controlStack = ctx.getControlStack();

        BValueRef[] localVariable = new BValueRef[1];
        localVariable[0] = new BValueRef(new StringValue(ECHO_STRING));
//        StackFrame stackFrame = new StackFrame(new BValueRef[0], null, localVariable);
//        controlStack.pushFrame(stackFrame);


        // Creating FunctionInvocation for Ballerina Function.
        /*
            string original = "Hello World...!"
            string echo = nestedNative(original)
          */

        SymbolName original = new SymbolName("original");
        VariableRefExpr varRefExprOriginal = new VariableRefExpr(original);
        varRefExprOriginal.setEvalFunction(VariableRefExpr.createGetLocalValueFunc(0));

        List<Expression> nestedFunctionInvokeExpr = new ArrayList<>();
        nestedFunctionInvokeExpr.add(varRefExprOriginal);

        FunctionInvocationExpr invocationExpr = new FunctionInvocationExpr(
                new SymbolName("nestedNative"), nestedFunctionInvokeExpr);
        invocationExpr.setFunction(getFunctionNestedNative());

        BValueRef returnValue = invocationExpr.evaluate(ctx);

        String returnVal = ((StringValue) returnValue.getBValue()).getValue();
        Assert.assertEquals(returnVal, ECHO_STRING);
    }

//    @Test
    public void testNativeSystemPrint() {
        outContent.reset();
        Context ctx = new Context();
        ControlStack controlStack = ctx.getControlStack();

        BValueRef[] localVariable = new BValueRef[1];
        localVariable[0] = new BValueRef(new StringValue(ECHO_STRING));
//        StackFrame stackFrame = new StackFrame(new BValueRef[0], null, localVariable);
//        controlStack.pushFrame(stackFrame);


        // Creating FunctionInvocation for Ballerina Function.
        /*
            string original = "Hello World...!"
            system:print(original);
            system:println(original);
            // Out is -> Hello World...!Hello World...!\n
          */

        SymbolName original = new SymbolName("original");
        VariableRefExpr varRefExprOriginal = new VariableRefExpr(original);
        varRefExprOriginal.setEvalFunction(VariableRefExpr.createGetLocalValueFunc(0));

        List<Expression> nestedFunctionInvokeExpr = new ArrayList<>();
        nestedFunctionInvokeExpr.add(varRefExprOriginal);

        FunctionInvocationExpr invocationExprPrint = new FunctionInvocationExpr(
                new SymbolName("print"), nestedFunctionInvokeExpr);
        invocationExprPrint.setFunction(new Print());

        invocationExprPrint.evaluate(ctx);

        nestedFunctionInvokeExpr.add(varRefExprOriginal);
        FunctionInvocationExpr invocationExprPrintln = new FunctionInvocationExpr(
                new SymbolName("println"), nestedFunctionInvokeExpr);
        invocationExprPrintln.setFunction(new Println());

        invocationExprPrintln.evaluate(ctx);

        Assert.assertEquals(outContent.toString(), ECHO_STRING + ECHO_STRING + "\n");
    }

}
