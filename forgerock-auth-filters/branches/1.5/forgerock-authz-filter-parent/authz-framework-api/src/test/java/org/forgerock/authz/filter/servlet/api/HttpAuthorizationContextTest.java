/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2014 ForgeRock AS.
 */

package org.forgerock.authz.filter.servlet.api;

import org.forgerock.authz.filter.api.AuthorizationContext;
import org.testng.annotations.Test;

import javax.servlet.ServletRequest;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;

/**
 * Unit tests for {@link org.forgerock.authz.filter.api.AuthorizationContext}.
 *
 * @since 1.4.0
 */
public class HttpAuthorizationContextTest {

    @Test
    public void shouldPropagateUpdatesToTheRequestMap() {

        // Given
        ServletRequest request = mock(ServletRequest.class);
        Map<String, Object> contextMap = new LinkedHashMap<String, Object>();
        given(request.getAttribute(HttpAuthorizationContext.ATTRIBUTE_AUTHORIZATION_CONTEXT)).willReturn(contextMap);

        // When
        AuthorizationContext context = HttpAuthorizationContext.forRequest(request);
        context.setAttribute("one", 2);

        // Then the map in the request should also be updated
        assertEquals(contextMap, Collections.singletonMap("one", 2));
    }

    @Test
    public void shouldCreateContextMapOnRequest() {

        // Given
        ServletRequest request = mock(ServletRequest.class);
        given(request.getAttribute(HttpAuthorizationContext.ATTRIBUTE_AUTHORIZATION_CONTEXT)).willReturn(null);

        // When
        HttpAuthorizationContext.forRequest(request);

        // Then
        verify(request).setAttribute(eq(HttpAuthorizationContext.ATTRIBUTE_AUTHORIZATION_CONTEXT),
                anyMapOf(String.class, Object.class));
    }

    @Test
    public void shouldThrowClassCaseExceptionWhenContextNotMap() {

        // Given
        ServletRequest request = mock(ServletRequest.class);
        given(request.getAttribute(HttpAuthorizationContext.ATTRIBUTE_AUTHORIZATION_CONTEXT)).willReturn("STRING");

        // When
        HttpAuthorizationContext.forRequest(request);

        // Then
        verify(request).setAttribute(eq(HttpAuthorizationContext.ATTRIBUTE_AUTHORIZATION_CONTEXT),
                anyMapOf(String.class, Object.class));
    }
}
