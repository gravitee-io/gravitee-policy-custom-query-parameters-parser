package io.gravitee.policy.customqueryparametersparser;/**
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

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static io.gravitee.policy.customqueryparametersparser.CustomQueryParametersParserPolicy.POLICY_ID;
import static org.assertj.core.api.Assertions.assertThat;

import io.gravitee.apim.gateway.tests.sdk.AbstractGatewayTest;
import io.gravitee.apim.gateway.tests.sdk.annotations.DeployApi;
import io.gravitee.apim.gateway.tests.sdk.annotations.GatewayTest;
import io.gravitee.apim.gateway.tests.sdk.configuration.GatewayMode;
import io.gravitee.apim.gateway.tests.sdk.policy.PolicyBuilder;
import io.gravitee.common.util.LinkedMultiValueMap;
import io.gravitee.common.util.MultiValueMap;
import io.gravitee.plugin.policy.PolicyPlugin;
import io.vertx.core.http.HttpMethod;
import io.vertx.rxjava3.core.http.HttpClient;
import io.vertx.rxjava3.core.http.HttpClientRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import policies.QueryParamsToHeaderPolicy;

/**
 * @author Yann TAVERNIER (yann.tavernier at graviteesource.com)
 * @author GraviteeSource Team
 */
@GatewayTest(mode = GatewayMode.JUPITER)
@DeployApi("/apis/api.json")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class CustomQueryParametersParserPolicyIntegrationTest extends AbstractGatewayTest {

    @Override
    public void configurePolicies(Map<String, PolicyPlugin> policies) {
        policies.put(POLICY_ID, PolicyBuilder.build(POLICY_ID, CustomQueryParametersParserPolicy.class));
        policies.put("query-params-to-header", PolicyBuilder.build("query-params-to-header", QueryParamsToHeaderPolicy.class));
    }

    @ParameterizedTest
    @MethodSource("provideArguments")
    public void should_preserve_query_params_and_extract_them_properly(
        String query,
        MultiValueMap<String, String> expectedQueryParameters,
        HttpClient client
    ) throws Exception {
        wiremock.stubFor(get("/team" + query).willReturn(ok()));

        client
            .rxRequest(HttpMethod.GET, "/test" + query)
            .flatMap(HttpClientRequest::rxSend)
            .test()
            .await()
            .assertComplete()
            .assertValue(response -> {
                assertThat(response.statusCode()).isEqualTo(200);
                expectedQueryParameters.forEach((expectedKey, expectedValue) -> {
                    assertThat(response.headers().contains(expectedKey)).isTrue();
                    // The policy is doing a toString to fill the headers, so we need to handle the null case
                    assertThat(response.headers().get(expectedKey))
                        .isEqualTo(expectedValue.isEmpty() ? "[null]" : expectedValue.toString());
                });
                return true;
            })
            .assertNoErrors();

        wiremock.verify(1, getRequestedFor(urlEqualTo("/team" + query)));
    }

    public Stream<Arguments> provideArguments() {
        return Stream.of(
            Arguments.of("", new LinkedMultiValueMap<>()),
            Arguments.of("?filter=field1", new LinkedMultiValueMap<>(Map.of("filter", List.of("field1")))),
            Arguments.of("?filter=field1&filter=field2", new LinkedMultiValueMap<>(Map.of("filter", List.of("field1", "field2")))),
            Arguments.of("?filter=field1&asc", new LinkedMultiValueMap<>(Map.of("filter", List.of("field1"), "asc", List.of()))),
            Arguments.of(
                "?filter=200-299;300-399;500-599",
                new LinkedMultiValueMap<>(Map.of("filter", List.of("200-299;300-399;500-599")))
            ),
            Arguments.of(
                "?tagNames=GIO%20APIM%20GW*;GIO%20%20APIM%20REST*",
                new LinkedMultiValueMap<>(Map.of("tagNames", List.of("GIO%20APIM%20GW*;GIO%20A%20PIM%20REST*")))
            ),
            Arguments.of(
                "?$expand=Person($select=SaleChannel,Id;$expand=age)",
                new LinkedMultiValueMap<>(Map.of("$expand", List.of("Person($select=SaleChannel,Id;$expand=age)")))
            )
        );
    }
}
