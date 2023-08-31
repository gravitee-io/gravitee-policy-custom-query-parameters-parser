/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.policy.v3.customqueryparametersparser;

import io.gravitee.common.util.MultiValueMap;
import io.gravitee.common.util.URIUtils;
import io.gravitee.gateway.api.ExecutionContext;
import io.gravitee.policy.api.PolicyChain;
import io.gravitee.policy.api.annotations.OnRequest;

/**
 * @author Yann TAVERNIER (yann.tavernier at graviteesource.com)
 * @author GraviteeSource Team
 */
public class CustomQueryParametersParserPolicyV3 {

    @OnRequest
    public void onRequest(ExecutionContext executionContext, PolicyChain policyChain) {
        parseParameters(executionContext.request().uri(), executionContext.request().parameters());
        policyChain.doNext(executionContext.request(), executionContext.response());
    }

    public static void parseParameters(String uri, MultiValueMap<String, String> parameters) {
        if (uri.contains(";")) {
            parameters.clear();
            parameters.putAll(URIUtils.parameters(uri, true));
        }
    }
}
