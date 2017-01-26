/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.wso2.ballerina.core.nativeimpl.lang.converters;

import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BDouble;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.AbstractNativeTypeConverter;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaTypeConverter;
import org.wso2.ballerina.core.nativeimpl.annotations.ReturnType;

/**
 * Convert String to Double
 */
@BallerinaTypeConverter(
        packageName = "ballerina.lang.converters",
        typeConverterName = "stringToDouble",
        args = {@Argument(name = "value", type = TypeEnum.STRING)},
        returnType = {@ReturnType(type = TypeEnum.DOUBLE)},
        isPublic = true
)
public class StringToDouble extends AbstractNativeTypeConverter {

    public BValue convert(Context ctx) {
        BString msg = (BString) getArgument(ctx, 0);
        BDouble result = new BDouble(msg.doubleValue());
        return result;
    }
}

