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
package org.apache.fineract.portfolio.client.service;

import com.openai.client.OpenAIClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.exception.PlatformServiceUnavailableException;
import org.apache.fineract.portfolio.client.data.ClientContextData;
import org.apache.fineract.portfolio.client.data.ClientInsightsData;
import org.apache.fineract.portfolio.client.exception.ClientNotFoundException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Generates client insights by reading the client's profile and asking the configured OpenAI model to summarize it.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClientInsightsReadPlatformServiceImpl implements ClientInsightsReadPlatformService {

    private static final String SYSTEM_PROMPT = "You are an assistant for loan officers at a microfinance institution. "
            + "Given a client profile, write a short, factual summary that helps the officer prepare for a conversation "
            + "with the client. Do not invent information that is not in the profile.";

    private final JdbcTemplate jdbcTemplate;
    private final OpenAIClient openAIClient;
    private final FineractProperties fineractProperties;
    private final ClientContextMapper clientContextMapper = new ClientContextMapper();

    @Override
    public ClientInsightsData generateInsights(final Long clientId, final String focus) {
        final FineractProperties.FineractOpenAiProperties openAi = this.fineractProperties.getAi().getOpenai();
        if (!Boolean.TRUE.equals(openAi.getEnabled())) {
            throw new PlatformServiceUnavailableException("error.msg.client.insights.disabled",
                    "AI client insights are not enabled on this instance (fineract.ai.openai.enabled=false)");
        }

        final ClientContextData client = retrieveClientContext(clientId);
        final String prompt = buildPrompt(client, focus);

        final ChatCompletionCreateParams request = ChatCompletionCreateParams.builder() //
                .model(openAi.getModel()) //
                .addSystemMessage(SYSTEM_PROMPT) //
                .addUserMessage(prompt) //
                .build();

        final ChatCompletion completion = this.openAIClient.chat().completions().create(request);

        final String summary = completion.choices().stream() //
                .flatMap(choice -> choice.message().content().stream()) //
                .collect(Collectors.joining("\n"));

        return new ClientInsightsData(client.getId(), client.getAccountNo(), client.getDisplayName(), openAi.getModel(), summary);
    }

    private ClientContextData retrieveClientContext(final Long clientId) {
        final String sql = "select " + this.clientContextMapper.schema() + " where c.id = ?";
        try {
            return this.jdbcTemplate.queryForObject(sql, this.clientContextMapper, clientId); // NOSONAR
        } catch (final EmptyResultDataAccessException e) {
            throw new ClientNotFoundException(clientId, e);
        }
    }

    private String buildPrompt(final ClientContextData client, final String focus) {
        final String profile = String.format("Client profile:%n" //
                + "- Name: %s%n" //
                + "- Account number: %s%n" //
                + "- External id: %s%n" //
                + "- Mobile number: %s%n" //
                + "- Email address: %s%n" //
                + "- Date of birth: %s%n" //
                + "- Client since: %s%n" //
                + "- Office: %s%n", //
                client.getDisplayName(), client.getAccountNo(), client.getExternalId(), client.getMobileNo(), client.getEmailAddress(),
                client.getDateOfBirth(), client.getActivationDate(), client.getOfficeName());
        if (StringUtils.isBlank(focus)) {
            return profile + "Summarize this client for a loan officer in at most three sentences.";
        }
        return profile + "Summarize this client for a loan officer in at most three sentences, focusing on: " + focus;
    }

    private static final class ClientContextMapper implements RowMapper<ClientContextData> {

        private static final String SCHEMA = "c.id as id, c.account_no as accountNo, c.external_id as externalId, "
                + "c.display_name as displayName, c.mobile_no as mobileNo, c.email_address as emailAddress, "
                + "c.date_of_birth as dateOfBirth, c.activation_date as activationDate, o.name as officeName "
                + "from m_client c join m_office o on o.id = c.office_id";

        public String schema() {
            return SCHEMA;
        }

        @Override
        public ClientContextData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final Long id = JdbcSupport.getLong(rs, "id");
            final String accountNo = rs.getString("accountNo");
            final String externalId = rs.getString("externalId");
            final String displayName = rs.getString("displayName");
            final String mobileNo = rs.getString("mobileNo");
            final String emailAddress = rs.getString("emailAddress");
            final LocalDate dateOfBirth = JdbcSupport.getLocalDate(rs, "dateOfBirth");
            final LocalDate activationDate = JdbcSupport.getLocalDate(rs, "activationDate");
            final String officeName = rs.getString("officeName");
            return new ClientContextData(id, accountNo, externalId, displayName, mobileNo, emailAddress, dateOfBirth, activationDate,
                    officeName);
        }
    }
}
