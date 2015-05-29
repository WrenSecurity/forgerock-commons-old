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
 * Copyright 2013-2015 ForgeRock AS.
 */

package org.forgerock.json.resource.descriptor;

import static java.util.Collections.unmodifiableSet;
import static org.forgerock.json.fluent.JsonValue.field;
import static org.forgerock.json.fluent.JsonValue.json;
import static org.forgerock.json.fluent.JsonValue.object;
import static org.forgerock.json.resource.Requests.copyOfActionRequest;
import static org.forgerock.json.resource.Requests.copyOfCreateRequest;
import static org.forgerock.json.resource.Requests.copyOfDeleteRequest;
import static org.forgerock.json.resource.Requests.copyOfPatchRequest;
import static org.forgerock.json.resource.Requests.copyOfQueryRequest;
import static org.forgerock.json.resource.Requests.copyOfReadRequest;
import static org.forgerock.json.resource.Requests.copyOfUpdateRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.forgerock.http.ServerContext;
import org.forgerock.i18n.LocalizableMessage;
import org.forgerock.json.fluent.JsonValue;
import org.forgerock.json.resource.AbstractRequestHandler;
import org.forgerock.json.resource.ActionRequest;
import org.forgerock.json.resource.CreateRequest;
import org.forgerock.json.resource.DeleteRequest;
import org.forgerock.json.resource.NotFoundException;
import org.forgerock.json.resource.NotSupportedException;
import org.forgerock.json.resource.PatchRequest;
import org.forgerock.json.resource.QueryRequest;
import org.forgerock.json.resource.QueryResultHandler;
import org.forgerock.json.resource.ReadRequest;
import org.forgerock.json.resource.Request;
import org.forgerock.json.resource.RequestHandler;
import org.forgerock.json.resource.Resource;
import org.forgerock.json.resource.ResourceException;
import org.forgerock.http.ResourcePath;
import org.forgerock.json.resource.ResultHandler;
import org.forgerock.json.resource.UpdateRequest;
import org.forgerock.json.resource.descriptor.RelationDescriptor.Multiplicity;

import com.fasterxml.jackson.databind.ObjectMapper;

@SuppressWarnings("javadoc")
public final class Api {
    static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    private static abstract class AbstractResolverHandler implements ResultHandler<RequestHandler> {
        private final ResultHandler<?> handler;
        private final Resolver resolver;

        private AbstractResolverHandler(final ResultHandler<?> handler, final Resolver resolver) {
            this.handler = handler;
            this.resolver = resolver;
        }

        @Override
        public final void handleException(final ResourceException error) {
            resolver.close();
            handler.handleException(error);
        }

        @Override
        public final void handleResult(final RequestHandler requestHandler) {
            resolver.close();
            dispatch(requestHandler);
        }

        protected abstract void dispatch(RequestHandler requestHandler);
    }

    public static RequestHandler newApiDescriptorRequestHandler(final ApiDescriptor api) {
        return new AbstractRequestHandler() {
            @Override
            public void handleRead(final ServerContext context, final ReadRequest request,
                    final ResultHandler<Resource> handler) {
                if (request.getResourcePathObject().isEmpty()) {
                    handler.handleResult(new Resource(null, null, json(apiToJson(api))));
                } else {
                    handler.handleException(new NotSupportedException());
                }
            }
        };
    }

    public static RequestHandler newApiDescriptorRequestHandler(final Collection<ApiDescriptor> apis) {
        return new AbstractRequestHandler() {
            @Override
            public void handleRead(final ServerContext context, final ReadRequest request,
                    final ResultHandler<Resource> handler) {
                if (request.getResourcePathObject().isEmpty()) {
                    final List<Object> values = new ArrayList<Object>(apis.size());
                    for (final ApiDescriptor api : apis) {
                        values.add(apiToJson(api));
                    }
                    handler.handleResult(new Resource(null, null, json(values)));
                } else {
                    handler.handleException(new NotSupportedException());
                }
            }
        };
    }

    public static RequestHandler newApiDispatcher(final ApiDescriptor api,
            final ResolverFactory factory) {
        return new RequestHandler() {

            @Override
            public void handleAction(final ServerContext context, final ActionRequest request,
                    final ResultHandler<JsonValue> handler) {
                final ActionRequest mutableCopy = copyOfActionRequest(request);
                final Resolver resolver = factory.createResolver(context, request);
                resolveAndInvoke(api.getRelations(), mutableCopy, resolver,
                        new AbstractResolverHandler(handler, resolver) {
                            @Override
                            protected void dispatch(final RequestHandler resolvedRequestHandler) {
                                resolvedRequestHandler.handleAction(context, mutableCopy, handler);
                            }
                        });
            }

            @Override
            public void handleCreate(final ServerContext context, final CreateRequest request,
                    final ResultHandler<Resource> handler) {
                final CreateRequest mutableCopy = copyOfCreateRequest(request);
                final Resolver resolver = factory.createResolver(context, request);
                resolveAndInvoke(api.getRelations(), mutableCopy, resolver,
                        new AbstractResolverHandler(handler, resolver) {
                            @Override
                            protected void dispatch(final RequestHandler resolvedRequestHandler) {
                                resolvedRequestHandler.handleCreate(context, mutableCopy, handler);
                            }
                        });
            }

            @Override
            public void handleDelete(final ServerContext context, final DeleteRequest request,
                    final ResultHandler<Resource> handler) {
                final DeleteRequest mutableCopy = copyOfDeleteRequest(request);
                final Resolver resolver = factory.createResolver(context, request);
                resolveAndInvoke(api.getRelations(), mutableCopy, resolver,
                        new AbstractResolverHandler(handler, resolver) {
                            @Override
                            protected void dispatch(final RequestHandler resolvedRequestHandler) {
                                resolvedRequestHandler.handleDelete(context, mutableCopy, handler);
                            }
                        });
            }

            @Override
            public void handlePatch(final ServerContext context, final PatchRequest request,
                    final ResultHandler<Resource> handler) {
                final PatchRequest mutableCopy = copyOfPatchRequest(request);
                final Resolver resolver = factory.createResolver(context, request);
                resolveAndInvoke(api.getRelations(), mutableCopy, resolver,
                        new AbstractResolverHandler(handler, resolver) {
                            @Override
                            protected void dispatch(final RequestHandler resolvedRequestHandler) {
                                resolvedRequestHandler.handlePatch(context, mutableCopy, handler);
                            }
                        });
            }

            @Override
            public void handleQuery(final ServerContext context, final QueryRequest request,
                    final QueryResultHandler handler) {
                final QueryRequest mutableCopy = copyOfQueryRequest(request);
                final Resolver resolver = factory.createResolver(context, request);
                resolveAndInvoke(api.getRelations(), mutableCopy, resolver,
                        new AbstractResolverHandler(handler, resolver) {
                            @Override
                            protected void dispatch(final RequestHandler resolvedRequestHandler) {
                                resolvedRequestHandler.handleQuery(context, mutableCopy, handler);
                            }
                        });
            }

            @Override
            public void handleRead(final ServerContext context, final ReadRequest request,
                    final ResultHandler<Resource> handler) {
                final ReadRequest mutableCopy = copyOfReadRequest(request);
                final Resolver resolver = factory.createResolver(context, request);
                resolveAndInvoke(api.getRelations(), mutableCopy, resolver,
                        new AbstractResolverHandler(handler, resolver) {
                            @Override
                            protected void dispatch(final RequestHandler resolvedRequestHandler) {
                                resolvedRequestHandler.handleRead(context, mutableCopy, handler);
                            }
                        });
            }

            @Override
            public void handleUpdate(final ServerContext context, final UpdateRequest request,
                    final ResultHandler<Resource> handler) {
                final UpdateRequest mutableCopy = copyOfUpdateRequest(request);
                final Resolver resolver = factory.createResolver(context, request);
                resolveAndInvoke(api.getRelations(), mutableCopy, resolver,
                        new AbstractResolverHandler(handler, resolver) {
                            @Override
                            protected void dispatch(final RequestHandler resolvedRequestHandler) {
                                resolvedRequestHandler.handleUpdate(context, mutableCopy, handler);
                            }
                        });
            }

            private boolean isBetterMatch(final RelationDescriptor oldMatch,
                    final RelationDescriptor newMatch) {
                return oldMatch == null
                        || oldMatch.getResourcePathObject().size() < newMatch
                                .getResourcePathObject().size();
            }

            private boolean isChildRequest(final ResourcePath relationPath,
                    final ResourcePath target) {
                return target.size() == relationPath.size() + 1;
            }

            private boolean isOneToMany(final RelationDescriptor relation) {
                return relation.getMultiplicity() == Multiplicity.ONE_TO_MANY;
            }

            private void resolveAndInvoke(final Collection<RelationDescriptor> relations,
                    final Request mutableRequest, final Resolver resolver,
                    final ResultHandler<RequestHandler> handler) {
                // @formatter:off
                /*
                 * We need to find the best match so first try all
                 * relations to see if there is an exact match, then try
                 * all one-to-many relations to see if there is a child
                 * match, then try all relations to see if there is a
                 * starts with match for sub-resources. In other words:
                 *
                 * singleton
                 * collection
                 * collection/{id}
                 * collection/{id}/*
                 * singleton/*
                 */
                // @formatter:on
                final ResourcePath path = mutableRequest.getResourcePathObject();
                RelationDescriptor exactMatch = null;
                RelationDescriptor childMatch = null;
                RelationDescriptor subMatch = null;
                for (final RelationDescriptor relation : relations) {
                    final ResourcePath relationPath = relation.getResourcePathObject();
                    if (path.equals(relationPath)) {
                        /*
                         * Got an exact match - this wins outright so no point
                         * in continuing.
                         */
                        exactMatch = relation;
                        break;
                    } else if (path.startsWith(relationPath)) {
                        if (isOneToMany(relation) && isChildRequest(relationPath, path)) {
                            // Child match.
                            childMatch = relation;
                        } else if (isBetterMatch(subMatch, relation)) {
                            /*
                             * Sub-resource match: the new relation is more
                             * specific than the old one.
                             */
                            subMatch = relation;
                        }
                    }
                }
                if (exactMatch != null || childMatch != null) {
                    try {
                        RequestHandler resolvedRequestHandler;
                        if (exactMatch != null) {
                            resolvedRequestHandler = resolver.getRequestHandler(exactMatch);
                            mutableRequest.setResourcePath(ResourcePath.empty());
                        } else {
                            resolvedRequestHandler = resolver.getRequestHandler(childMatch);
                            mutableRequest.setResourcePath(path.tail(path.size() - 1));
                        }
                        handler.handleResult(resolvedRequestHandler);
                    } catch (final ResourceException e) {
                        handler.handleException(e);
                    }
                } else if (subMatch != null) {
                    final String childId;
                    final int relationNameSize = subMatch.getResourcePathObject().size();
                    if (isOneToMany(subMatch)) {
                        // Strip off collection name and resource ID.
                        mutableRequest.setResourcePath(path.tail(relationNameSize + 1));
                        childId = path.get(relationNameSize);
                    } else {
                        // Strip off resource name.
                        mutableRequest.setResourcePath(path.tail(relationNameSize));
                        childId = null;
                    }
                    resolver.getRelationsForResource(subMatch, childId,
                            new ResultHandler<Collection<RelationDescriptor>>() {
                                @Override
                                public void handleException(final ResourceException error) {
                                    handler.handleException(error);
                                }

                                @Override
                                public void handleResult(final Collection<RelationDescriptor> result) {
                                    resolveAndInvoke(result, mutableRequest, resolver, handler);
                                }
                            });
                } else {
                    handler.handleException(new NotFoundException(String.format(
                            "Resource '%s' not found", path)));
                }
            }
        };
    }

    static LocalizableMessage defaultToEmptyMessageIfNull(final LocalizableMessage description) {
        return description != null ? description : LocalizableMessage.EMPTY;
    }

    static <T> Set<T> unmodifiableCopyOf(final Collection<T> set) {
        return unmodifiableSet(new LinkedHashSet<T>(set));
    }

    private static Entry<String, Object> actionsToJson(final Set<ActionDescriptor> actions) {
        if (actions.isEmpty()) {
            return null;
        }
        final List<Object> json = new ArrayList<Object>(actions.size());
        for (final ActionDescriptor action : actions) {
            // @formatter:off
            json.add(object(
                    field("name", action.getName()),
                    field("description", action.getDescription()),
                    parametersToJson(action.getParameters()),
                    profilesToJson(action.getProfiles())
            ));
            // @formatter:on
        }
        return field("actions", json);
    }

    private static Object apiToJson(final ApiDescriptor api) {
        // @formatter:off
        return object(
                field("urn", String.valueOf(api.getUrn())),
                field("name", String.valueOf(api.getUrn().getName())),
                field("version", String.valueOf(api.getUrn().getVersion())),
                field("description", api.getDescription()),
                relationsToJson(api.getRelations()),
                resourcesToJson(api.getResources()),
                profilesToJson(api.getProfiles())
        );
        // @formatter:on
    }

    private static Entry<String, Object> parametersToJson(final Set<ActionParameter> parameters) {
        if (parameters.isEmpty()) {
            return null;
        }
        final List<Object> json = new ArrayList<Object>(parameters.size());
        for (final ActionParameter parameter : parameters) {
            // @formatter:off
            json.add(object(
                    field("name", parameter.getName()),
                    field("description", parameter.getDescription())
            ));
            // @formatter:on
        }
        return field("parameters", json);
    }

    private static Entry<String, Object> parentToJson(final Urn parent) {
        return parent == null ? null : field("parent", String.valueOf(parent));
    }

    private static Entry<String, Object> profilesToJson(final Set<Profile> profiles) {
        if (profiles.isEmpty()) {
            return null;
        }
        final List<Object> json = new ArrayList<Object>(profiles.size());
        for (final Profile profile : profiles) {
            // @formatter:off
            json.add(object(
                    field("urn", String.valueOf(profile.getUrn())),
                    field("name", String.valueOf(profile.getUrn().getName())),
                    field("version", String.valueOf(profile.getUrn().getVersion())),
                    field("content", profile.getContent().getObject())
            ));
            // @formatter:on
        }
        return field("profiles", json);
    }

    private static Entry<String, Object> relationsToJson(final Set<RelationDescriptor> relations) {
        if (relations.isEmpty()) {
            return null;
        }
        final List<Object> json = new ArrayList<Object>(relations.size());
        for (final RelationDescriptor relation : relations) {
            // @formatter:off
            json.add(object(
                    field("path", relation.getResourcePath()),
                    field("description", relation.getDescription()),
                    field("multiplicity", relation.getMultiplicity()),
                    actionsToJson(relation.getActions()),
                    field("resource", String.valueOf(relation.getResource().getUrn())),
                    profilesToJson(relation.getProfiles())
            ));
            // @formatter:on
        }
        return field("relations", json);
    }

    private static Entry<String, Object> resourcesToJson(final Set<ResourceDescriptor> resources) {
        if (resources.isEmpty()) {
            return null;
        }
        final List<Object> json = new ArrayList<Object>(resources.size());
        for (final ResourceDescriptor resource : resources) {
            // @formatter:off
            json.add(object(
                    field("urn", String.valueOf(resource.getUrn())),
                    field("name", String.valueOf(resource.getUrn().getName())),
                    field("version", String.valueOf(resource.getUrn().getVersion())),
                    field("description", resource.getDescription()),
                    field("schema", JSON_MAPPER.convertValue(resource.getSchema(), Map.class)),
                    parentToJson(resource.getParentUrn()),
                    actionsToJson(resource.getActions()),
                    relationsToJson(resource.getRelations()),
                    profilesToJson(resource.getProfiles())
            ));
            // @formatter:on
        }
        return field("resources", json);
    }

    private Api() {
        // Nothing to do.
    }
}
