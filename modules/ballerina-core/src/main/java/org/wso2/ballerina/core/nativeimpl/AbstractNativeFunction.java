/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 **/

package org.wso2.ballerina.core.nativeimpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.Annotation;
import org.wso2.ballerina.core.model.Const;
import org.wso2.ballerina.core.model.Function;
import org.wso2.ballerina.core.model.Parameter;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.types.Type;
import org.wso2.ballerina.core.model.types.TypeC;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.model.values.BValueRef;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.Utils;
import org.wso2.ballerina.core.nativeimpl.exceptions.ArgumentOutOfRangeException;
import org.wso2.ballerina.core.nativeimpl.exceptions.MalformedEntryException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * {@code {@link AbstractNativeFunction}} represents a Abstract implementation of Native Ballerina Function.
 */
public abstract class AbstractNativeFunction implements NativeConstruct, Function {

    /* Void RETURN */
    public static final BValue[] VOID_RETURN = new BValue[0];
    private static final Logger log = LoggerFactory.getLogger(AbstractNativeFunction.class);
    private String packageName, functionName;
    private SymbolName symbolName;
    private List<Annotation> annotations;
    private List<Parameter> parameters;
    private List<Type> returnTypes;
    private boolean isPublicFunction;
    private List<Const> constants;

    public AbstractNativeFunction() {
        parameters = new ArrayList<>();
        returnTypes = new ArrayList<>();
        annotations = new ArrayList<>();
        constants = new ArrayList<>();
        buildModel();
    }

    /**
     * Build Native function Model using Java annotation.
     */
    private void buildModel() {
        BallerinaFunction function = this.getClass().getAnnotation(BallerinaFunction.class);
        packageName = function.packageName();
        functionName = function.functionName();

        Argument[] methodParams = function.args();
        Type[] params = new Type[methodParams.length];
        for (int i = 0; i < methodParams.length; i++) {
            params[i] = TypeC.getType(methodParams[i].type().getName());
        }
        symbolName = new SymbolName(packageName + "." + functionName, SymbolName.SymType.CALLABLE_UNIT, params);
        isPublicFunction = function.isPublic();
        Arrays.stream(methodParams).
                forEach(argument -> {
                    try {
                        parameters.add(new Parameter(TypeC.getType(argument.type().getName())
                                , new SymbolName(argument.name())));
                    } catch (RuntimeException e) {
                        // TODO: Fix this when TypeC.getType method is improved.
                        log.warn("Error while processing Parameters for Native ballerina function {}:{}.",
                                packageName, functionName, e);
                    }
                });
        Arrays.stream(function.returnType()).forEach(
                returnType -> {
                    try {
                        returnTypes.add(TypeC.getType(returnType.getName()));
                    } catch (RuntimeException e) {
                        // TODO: Fix this when TypeC.getType method is improved.
                        log.warn("Error while processing ReturnTypes for Native ballerina function {}:{}.",
                                packageName, functionName, e);
                    }
                });
        Arrays.stream(function.consts()).forEach(
                constant -> {
                    try {
                        constants.add(Utils.getConst(constant));
                    } catch (MalformedEntryException e) {
                        log.warn("Error while processing pre defined const {} for Native ballerina function {}:{}.",
                                constant.identifier(), packageName, functionName, e);
                    }
                }
        );
        // TODO: Handle Ballerina Annotations.
    }

    public String getPackageName() {
        return packageName;
    }

    @Override
    public String getName() {
        return symbolName.getName();
    }

    public SymbolName getSymbolName() {
        return symbolName;
    }

    @Override
    public Annotation[] getAnnotations() {
        return annotations.toArray(new Annotation[annotations.size()]);
    }

    @Override
    public Parameter[] getParameters() {
        return parameters.toArray(new Parameter[parameters.size()]);
    }

    @Override
    public Type[] getReturnTypes() {
        return returnTypes.toArray(new Type[returnTypes.size()]);
    }

    /**
     * Get Argument by index.
     *
     * @param context current {@code {@link Context}} instance.
     * @param index   index of the parameter.
     * @return BValue;
     */
    public BValueRef getArgument(Context context, int index) {
        if (index > -1 && index < parameters.size()) {
            return context.getControlStack().getCurrentFrame().values[index];
        }
        throw new ArgumentOutOfRangeException(index);
    }

    @Override
    public boolean isPublic() {
        return isPublicFunction;
    }

    @Override
    public void interpret(Context context) {
        BValue[] returnValues = execute(context);
        if (returnValues == null || returnValues.length == 0 || this.returnTypes.size() == 0) {
            context.getControlStack().getCurrentFrame().returnValue = null;
            return;
        }
        // TODO : Support for multiple return values.
        context.getControlStack().getCurrentFrame().returnValue.setBValue(returnValues[0]);
    }

    /**
     * Where Native Function logic is implemented.
     *
     * @param context Current Context instance
     * @return Native function return BValue array
     */
    public abstract BValue[] execute(Context context);

    /**
     * Util method to construct BValue array.
     *
     * @param values
     * @return
     */
    public BValue[] getBValues(BValue... values) {
        return values;
    }


    public Const[] getFunctionConstats() {
        return constants.toArray(new Const[constants.size()]);
    }

}
