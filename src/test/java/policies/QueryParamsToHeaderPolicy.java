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
package policies;/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import io.gravitee.gateway.jupiter.api.context.HttpExecutionContext;
import io.gravitee.gateway.jupiter.api.context.HttpRequest;
import io.gravitee.gateway.jupiter.api.context.HttpResponse;
import io.gravitee.gateway.jupiter.api.policy.Policy;
import io.reactivex.rxjava3.core.Completable;

/**
 * @author Yann TAVERNIER (yann.tavernier at graviteesource.com)
 * @author GraviteeSource Team
 */
public class QueryParamsToHeaderPolicy extends QueryParamsToHeaderPolicyV3 implements Policy {

    @Override
    public String id() {
        return "query-params-to-header";
    }

    @Override
    public Completable onResponse(HttpExecutionContext ctx) {
        final HttpRequest request = ctx.request();
        final HttpResponse response = ctx.response();
        if (request.parameters() != null) {
            request.parameters().forEach((key, value) -> response.headers().set(key, value.toString()));
        }
        return Completable.complete();
    }
}
