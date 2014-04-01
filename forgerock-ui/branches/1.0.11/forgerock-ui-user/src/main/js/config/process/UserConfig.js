/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2013 ForgeRock AS. All rights reserved.
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://forgerock.org/license/CDDLv1.0.html
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at http://forgerock.org/license/CDDLv1.0.html
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 */

/*global define, require, window, _*/

/**
 * @author yaromin
 */
define("config/process/UserConfig", [
    "org/forgerock/commons/ui/common/util/Constants", 
    "org/forgerock/commons/ui/common/main/EventManager"
], function(constants, eventManager) {
    var obj = [
        {
            startEvent: constants.FORGOTTEN_PASSWORD_CHANGED_SUCCESSFULLY,
            description: "",
            dependencies: [
            ],
            processDescription: function(event) {
                eventManager.sendEvent(constants.EVENT_DISPLAY_MESSAGE_REQUEST, "changedPassword");
                eventManager.sendEvent(constants.EVENT_LOGIN_REQUEST, { userName: event.userName, password: event.password});
            }
        },
        {
            startEvent: constants.EVENT_USER_SUCCESSFULLY_REGISTERED,
            description: "User registered",
            dependencies: [
                "org/forgerock/commons/ui/common/main/Router"
            ],
            processDescription: function(event, router) {
                eventManager.sendEvent(constants.EVENT_DISPLAY_MESSAGE_REQUEST, "afterRegistration");

                if(event.selfRegistration) {
                    eventManager.sendEvent(constants.EVENT_LOGIN_REQUEST, { userName: event.user.userName, password: event.user.password});
                } else {
                    router.navigate(router.configuration.routes.adminUsers.url, {trigger: true});
                }
            }
        },
        {
            startEvent: constants.EVENT_HANDLE_DEFAULT_ROUTE,
            description: "",
            dependencies: [
                "org/forgerock/commons/ui/common/main/Router"
            ],
            processDescription: function(event, router) {
                router.routeTo(router.configuration.routes.profile, {trigger: true});
            }
        }
    ];
    return obj;
});
