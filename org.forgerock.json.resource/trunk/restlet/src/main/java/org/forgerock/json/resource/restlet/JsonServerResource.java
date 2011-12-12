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
 * information: "Portions Copyrighted [year] [name of copyright owner]".
 *
 * Copyright © 2011 ForgeRock AS. All rights reserved.
 */

package org.forgerock.json.resource.restlet;

// Java SE
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// Restlet
import org.restlet.data.Conditions;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.data.Tag;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

// Jackson
import org.restlet.ext.jackson.JacksonRepresentation;

// Restlet Utilities
import org.forgerock.restlet.ExtendedServerResource;

// JSON Fluent
import org.forgerock.json.fluent.JsonValue;

// JSON Resource
import org.forgerock.json.resource.JsonResource;
import org.forgerock.json.resource.JsonResourceAccessor;
import org.forgerock.json.resource.JsonResourceContext;
import org.forgerock.json.resource.JsonResourceException;

/**
 * TODO: Description.
 *
 * @author Paul C. Bryan
 */
public class JsonServerResource extends ExtendedServerResource {

    /** TODO: Description. */
    private JsonResourceAccessor accessor;

    /** TODO: Description. */
    private Conditions conditions;

    /** TODO: Description. */
    private String id;

    /** TODO: Description. */
    private String rev;

    /** Current value of the resource (if it has been already read). */
    private JsonValue value;

    /**
     * TODO: Description.
     *
     * @param value TODO.
     * @return TODO.
     */
    private Tag getTag(JsonValue value) {
        Object _rev = (value != null ? value.get("_rev").getObject() : null);
        return (_rev != null && _rev instanceof String ? new Tag((String)_rev, false) : null);
    }

    /**
     * TODO: Description.
     *
     * @param value TODO.
     * @return TODO.
     */
    private Representation toRepresentation(JsonValue value) {
        JacksonRepresentation<Object> result = null;
        if (value != null) {
            result = new JacksonRepresentation<Object>(value.getObject());
            result.setObjectClass(Object.class); // probably superfluous
            result.setTag(getTag(value)); // set ETag, if _rev is in value
        }
        return result;
    }

    /**
     * TODO: Description.
     *
     * @param parent TODO.
     * @return TODO.
     */
    private JsonValue newHttpContext(JsonValue parent) {
        return RestletRequestHttpContext.newContext(getRequest(), parent);
    }

    /**
     * Returns the request's query parameters as a JSON value.
     */
    private JsonValue getQueryParams() {
        Map<String, Object> map = new HashMap<String, Object>();
        Form query = getQuery();
        if (query != null) {
            map.putAll(query.getValuesMap()); // copy for isolation and mutability
        }
        return new JsonValue(map);
    }

    /**
     * Reads the JSON resource value. Enforces any preconditions provided in the incoming
     * request. Caches the read value in {@code value} to prevent multiple reads when
     * precondition(s) must be tested against current version of the resource.
     *
     * @return the value of the JSON resource.
     * @throws JsonResourceException if the JSON resource could not be read or a precondition failed.
     */
    private JsonValue read() throws JsonResourceException {
        if (this.value == null) {
            JsonValue value = accessor.read(this.id);
            Status status = conditions.getStatus(getMethod(), true, getTag(value), null);
            if (status != null && status.isError()) {
                throw JsonResourceException.VERSION_MISMATCH;
            }
            this.value = value; // cache to prevent multiple reads
        }
        return this.value;
    }

    /**
     * Returns the entity as a JSON value, or {@code null} if there is no entity or if it
     * cannot be represented as a JSON value.
     *
     * @param entity the entity to be mapped to the JSON value.
     * @return the entity as a JSON value, or {@code null} if empty or does not exist.
     */
    private JsonValue entityValue(Representation entity) {
        JsonValue result = null;
        if (entity != null && !(entity instanceof EmptyRepresentation)) {
            JacksonRepresentation jr = (entity instanceof JacksonRepresentation ?
             (JacksonRepresentation)entity : new JacksonRepresentation<Object>(entity, Object.class));
            result = new JsonValue(jr.getObject());
        }
        return result;
    }

    /**
     * Initializes the state of the resource.
     */
    @Override
    public void doInit() {
        setAnnotated(false); // using method names, not annotations
        setNegotiated(false); // we shall speak all-JSON for now
        setConditional(false); // conditional requests handled in implementation
        conditions = getConditions();
        String remaining = getReference().getRemainingPart(false, false);
        if (remaining != null && remaining.length() > 0) {
            id = remaining; // default: null (resource itself is being operated on)
        }
        accessor = new JsonResourceAccessor(
         (JsonResource)(getRequestAttributes().get(JsonResource.class.getName())),
         new JsonValue(newHttpContext(JsonResourceContext.newRootContext()))
        );
    }

    /**
     * Handles a call without content negotiation of the response entity. Performs
     * operations common across all methods, prior to the call to the method-specific
     * handler.
     */
    @Override
    protected Representation doHandle() throws ResourceException {
        try {
            if (conditions.getModifiedSince() != null || conditions.getUnmodifiedSince() != null) {
                throw JsonResourceException.VERSION_MISMATCH; // unsupported
            }
            if (conditions.getMatch().size() > 1 || conditions.getNoneMatch().size() > 0) {
                rev = getTag(read()).getName(); // derive from fetched resource
            } else if (conditions.getMatch().size() == 1) {
                rev = conditions.getMatch().get(0).getName(); // derive from request
            }
        } catch (JsonResourceException jre) {
            throw new ResourceException(jre);
        }
        return super.doHandle();
    }

    /**
     * Processes a GET request. The GET method can be used to read a resource, or perform a
     * query against the resource within.
     * <p>
     * If no query is included in the request, then the request is dispatched to the resource
     * with a {@code "read"} method. Otherwise, the request is dispatched to the resource
     * with a {@code "query"} method.
     *
     * @return TODO.
     * @throws ResourceException TODO.
     */
    @Override
    public Representation get() throws ResourceException {
        Representation response = null;
        try {
            Form query = getQuery();
            if (query == null || query.size() == 0) { // read
                response = toRepresentation(read());
            } else if (conditions.hasSome()) { // query w. precondition: automatic mismatch
                    throw JsonResourceException.VERSION_MISMATCH;
            } else { // query                
                response = toRepresentation(accessor.query(this.id, getQueryParams()));
            }
            if (response == null) { // expect a response from the read or query
                throw JsonResourceException.INTERNAL_ERROR;
            }
        } catch (JsonResourceException jre) {
            throw new ResourceException(jre);
        }
        return response;
    }

    /**
     * Processes a PUT request. The PUT method can be used to create an object or update an
     * existing object.
     * <p>
     * The request is unambigously interpreted as a create if the {@code If-None-Match}
     * header has the single value that is: {@code *}. The request is unambiguously interpeted
     * as an update if the {@code If-Match} header has a single value that is not: {@code *}.
     * If the request is ambiguous, then this implementation first attempts to update the
     * object, and the update fails with a {@code NotFoundException}, then attempts to create
     * the object.
     *
     * @param entity TODO.
     * @return TODO.
     * @throws ResourceException TODO.
     */
    @Override
    public Representation put(Representation entity) throws ResourceException {
        Representation response = null;
        List<Tag> match = conditions.getMatch();
        List<Tag> noneMatch = conditions.getNoneMatch();
        try {
            JsonValue value = entityValue(entity);
            if (match.size() == 0 && noneMatch.size() == 1
            && noneMatch.get(0) != null && noneMatch.get(0).equals(Tag.ALL)) { // unambiguous create
                response = toRepresentation(accessor.create(this.id, value));
                setStatus(Status.SUCCESS_CREATED);
            } else if (noneMatch.size() == 0 && match.size() == 1
             && match.get(0) != null && !match.get(0).equals(Tag.ALL)) { // unambiguous update
                response = toRepresentation(accessor.update(this.id, this.rev, value));
// TODO: Should a successful update to the _id property result in a redirect to the new resource?
            } else { // ambiguous whether object is being created or updated
                try { // try update first
                    response = toRepresentation(accessor.update(this.id, this.rev, value));
                } catch (JsonResourceException jre) {
                    if (jre.hasCode(JsonResourceException.NOT_FOUND)) { // nothing to update; fallback to create
                        response = toRepresentation(accessor.create(this.id, value));
                        setStatus(Status.SUCCESS_CREATED);
                    } else {
                        throw jre;
                    }
                }
            }
            if (response == null) { // expect a response from the create or update
                throw JsonResourceException.INTERNAL_ERROR;
            }
        } catch (JsonResourceException jre) {
            throw new ResourceException(jre);
        }
        return response;
    }

    /**
     * Processes a POST request. The POST method is used to perform a number of different
     * functions, including: object creation, HTTP method override and initiation of object set
     * actions.
     * <p>
     * By default, the Restlet tunnel service enables support for the
     * {@code X-HTTP-Method-Override} header. If this header is set in an incoming request,
     * a different method will be automatically invoked.
     * <p>
     * If a query parameter named "{@code _action}" is included, and contains the value
     * "{@code create}", then the request is dispatched to the resource with a "{@code create}"
     * method. Otherwise, the request is dispatched to the resource with an "{@code action}"
     * method.
     *
     * @param entity TODO.
     * @return TODO.
     * @throws ResourceException TODO.
     */
    @Override 
    public Representation post(Representation entity) throws ResourceException {
        Representation response = null;
        Form query = getQuery();
        String _action = query.getFirstValue("_action");
        try {
            if ("create".equals(_action)) {
                String _id = query.getFirstValue("_id");
                if (_id != null) { // allow optional specification of identifier in query parameter
                    this.id = _id;
                }
                response = toRepresentation(accessor.create(this.id, entityValue(entity)));
                if (response == null) { // expect a response to the create
                    throw JsonResourceException.INTERNAL_ERROR;
                }
                setStatus(Status.SUCCESS_CREATED);
            } else { // action
                response = toRepresentation(accessor.action(this.id, getQueryParams(), entityValue(entity)));
                if (response == null) {
                    setStatus(Status.SUCCESS_NO_CONTENT);
                }
            }
        } catch (JsonResourceException jre) {
            throw new ResourceException(jre);
        }
        return response;
    }

    /**
     * Processes a DELETE request. The request is dispatched to the JSON resource with a
     * {@code "delete"} method.
     */
    @Override
    public Representation delete() throws ResourceException {
        try {
            accessor.delete(this.id, this.rev);
        } catch (JsonResourceException jre) {
            throw new ResourceException(jre);
        }
        setStatus(Status.SUCCESS_NO_CONTENT);
        return null; // no content
    }

    /**
     * Processes a PATCH request. The request is dispatched to the JSON resource with a
     * {@code "patch"} method.
     *
     * @return TODO.
     * @throws ResourceException TODO.
     */
    @Override
    public Representation patch(Representation entity) throws ResourceException {
        Representation response = null;
        try {
            response = toRepresentation(accessor.patch(this.id, this.rev, entityValue(entity)));
            if (response == null) { // expect a response to the patch
                throw JsonResourceException.INTERNAL_ERROR;
            }
        } catch (JsonResourceException jre) {
            throw new ResourceException(jre);
        }
        return response;
    }

    /**
     * Overrides the response to provide a JSON error structure in the entity if a
     * {@code ResourceException} is being thrown.
     */
    @Override
    protected void doCatch(Throwable throwable) {
        JsonResourceException jre = null;
        if (throwable instanceof ResourceException) {
            Throwable cause = throwable.getCause();
            if (cause != null && cause instanceof JsonResourceException) {
                jre = (JsonResourceException)cause;
            }
        }
        if (jre == null) {
            jre = new JsonResourceException(JsonResourceException.INTERNAL_ERROR, throwable);
        }
        int code = jre.getCode();
        if (code < 400 || code > 599) { // not an HTTP error status code
            code = 500; // force internal server error
        }
        String reason = jre.getReason();
        if (reason == null) {
            reason = "Internal Server Error";
        }
        setStatus(new Status(code, throwable, reason, jre.getMessage(), null));
        getResponse().setEntity(toRepresentation(jre.toJsonValue()));
    }
}
