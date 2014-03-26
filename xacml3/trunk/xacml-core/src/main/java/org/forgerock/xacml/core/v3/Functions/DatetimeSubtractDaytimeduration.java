/**
 *
 ~ DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 ~
 ~ Copyright (c) 2011-2013 ForgeRock AS. All Rights Reserved
 ~
 ~ The contents of this file are subject to the terms
 ~ of the Common Development and Distribution License
 ~ (the License). You may not use this file except in
 ~ compliance with the License.
 ~
 ~ You can obtain a copy of the License at
 ~ http://forgerock.org/license/CDDLv1.0.html
 ~ See the License for the specific language governing
 ~ permission and limitations under the License.
 ~
 ~ When distributing Covered Code, include this CDDL
 ~ Header Notice in each file and include the License file
 ~ at http://forgerock.org/license/CDDLv1.0.html
 ~ If applicable, add the following below the CDDL Header,
 ~ with the fields enclosed by brackets [] replaced by
 ~ your own identifying information:
 ~ "Portions Copyrighted [year] [name of copyright owner]"
 *
 */
package org.forgerock.xacml.core.v3.Functions;

/**
 * urn:oasis:names:tc:xacml:3.0:function:dateTime-subtract-dayTimeDuration
 This function SHALL take two arguments, the first SHALL be a
 “http://www.w3.org/2001/XMLSchema#dateTime” and the second
 SHALL be a “http://www.w3.org/2001/XMLSchema#dayTimeDuration”.
 It SHALL return a result of “http://www.w3.org/2001/XMLSchema#dateTime”.

 If the second argument is a positive duration, then this function SHALL return the value by adding
 the corresponding negative duration, as per the specification [XS] Appendix E.

 If the second argument is a negative duration, then the result SHALL be as if the function
 “urn:oasis:names:tc:xacml:1.0:function:dateTime-add-dayTimeDuration” had been applied to the corresponding
 positive duration.
 */

import org.forgerock.xacml.core.v3.engine.XACML3EntitlementException;
import org.forgerock.xacml.core.v3.engine.XACMLEvalContext;
import org.forgerock.xacml.core.v3.model.DataType;
import org.forgerock.xacml.core.v3.model.DataValue;
import org.forgerock.xacml.core.v3.model.FunctionArgument;
import org.forgerock.xacml.core.v3.model.XACMLFunction;

import java.util.Calendar;
import java.util.Date;

/**
 * urn:oasis:names:tc:xacml:3.0:function:dateTime-subtract-dayTimeDuration
 */
public class DatetimeSubtractDaytimeduration extends XACMLFunction {

    public DatetimeSubtractDaytimeduration()  {
    }

    public FunctionArgument evaluate( XACMLEvalContext pip) throws XACML3EntitlementException {

        if ( getArgCount() != 2) {
            throw new XACML3EntitlementException("Function Requires 2 Arguments");
        }

        Date date = getArg(0).asDateTime(pip);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        Long duration = getArg(1).asDayTimeDuration(pip);

        // Check Duration...
        if (duration < 0) {
            // Negative Duration.
            calendar.setTimeInMillis(calendar.getTimeInMillis() + duration.longValue());
        } else {
            // Positive Duration.
            calendar.setTimeInMillis(calendar.getTimeInMillis() - duration.longValue());
        }
        // Return Calculated DateTime Data Type.
        return new DataValue(DataType.XACMLDATETIME, calendar.getTime(), true);
    }

}
