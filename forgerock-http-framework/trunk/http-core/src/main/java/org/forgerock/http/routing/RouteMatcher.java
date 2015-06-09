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
 * Copyright 2015 ForgeRock AS.
 */

package org.forgerock.http.routing;

import org.forgerock.http.Context;

/**
 * A matcher for evaluating whether a route matches the incoming request.
 * Implementing class can either return a {@link RouteMatch}, if the route
 * matches, or {@code null}, if the route does not match, from the
 * {@link #evaluate(Context, Object)} method.
 *
 * <p>Implementing classes must implement both {@link #equals(Object)} and
 * {@link #hashCode()} methods as each instance of a {@code RouteMatcher}
 * will be used as the key for a route.</p>
 *
 * @see UriRouteMatcher
 *
 * @param <R> The type of the request.
 * @since 1.0.0
 */
public abstract class RouteMatcher<R> {

    /**
     * Evaluates the request and determines whether it matches the route.
     *
     * @param context The request context.
     * @param request The request.
     * @return A {@link RouteMatch}, if the request matches the route, or
     * {@code null}, if not.
     */
    public abstract RouteMatch evaluate(Context context, R request);

    /**
     * @return A string representation of the route matcher.
     */
    @Override
    public abstract String toString();

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object o);
}
