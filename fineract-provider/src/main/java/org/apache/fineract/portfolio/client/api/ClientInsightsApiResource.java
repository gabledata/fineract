/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.client.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.data.ClientInsightsData;
import org.apache.fineract.portfolio.client.service.ClientInsightsReadPlatformService;
import org.springframework.stereotype.Component;

@Path("/v1/clients/{clientId}/insights")
@Component
@Tag(name = "Client Insights", description = "AI-assisted summaries of a client's profile, generated on demand with the configured OpenAI model. "
        + "Requires fineract.ai.openai.enabled=true and an API key.")
@RequiredArgsConstructor
public class ClientInsightsApiResource {

    private final PlatformSecurityContext context;
    private final ClientInsightsReadPlatformService clientInsightsReadPlatformService;
    private final ToApiJsonSerializer<ClientInsightsData> toApiJsonSerializer;

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Generate AI insights for a client", operationId = "generateClientInsights", description = "Reads the client's profile and asks the configured AI model for a short summary a loan officer can use to prepare for a conversation.\n\n"
            + "Example Requests:\n" + "\n" + "clients/1/insights\n" + "\n" + "clients/1/insights?focus=repayment%20history")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ClientInsightsData.class)))
    public String generateInsights(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @QueryParam("focus") @Parameter(description = "Optional topic the summary should focus on") final String focus) {

        this.context.authenticatedUser().validateHasReadPermission(ClientApiConstants.CLIENT_RESOURCE_NAME);

        final ClientInsightsData insights = this.clientInsightsReadPlatformService.generateInsights(clientId, focus);

        return this.toApiJsonSerializer.serialize(insights);
    }
}
