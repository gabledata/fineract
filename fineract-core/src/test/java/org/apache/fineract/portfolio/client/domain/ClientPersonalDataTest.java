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
package org.apache.fineract.portfolio.client.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.fineract.infrastructure.core.annotation.PersonalData;
import org.apache.fineract.infrastructure.core.annotation.PersonalData.LoggingPolicy;
import org.junit.jupiter.api.Test;

class ClientPersonalDataTest {

    private static final Set<String> DIRECT_PERSONAL_DATA_FIELDS = Set.of("accountNumber", "firstname", "middlename", "lastname",
            "fullname", "displayName", "mobileNo", "emailAddress", "externalId", "dateOfBirth", "gender");

    @Test
    void directlyIdentifyingFieldsDeclareGdprMetadata() {
        Set<Field> fields = Arrays.stream(Client.class.getDeclaredFields())
                .filter(field -> DIRECT_PERSONAL_DATA_FIELDS.contains(field.getName())).collect(Collectors.toSet());

        assertThat(fields).extracting(Field::getName).containsExactlyInAnyOrderElementsOf(DIRECT_PERSONAL_DATA_FIELDS);
        assertThat(fields).allSatisfy(field -> {
            PersonalData metadata = field.getAnnotation(PersonalData.class);
            assertThat(metadata).as("GDPR metadata on Client.%s", field.getName()).isNotNull();
            assertThat(metadata.purposes()).as("processing purposes on Client.%s", field.getName()).isNotEmpty()
                    .allSatisfy(purpose -> assertThat(purpose).isNotBlank());
            assertThat(metadata.lawfulBasisPolicy()).as("lawful-basis policy on Client.%s", field.getName()).isNotBlank();
            assertThat(metadata.retentionPolicy()).as("retention policy on Client.%s", field.getName()).isNotBlank();
            assertThat(metadata.logging()).as("logging policy on Client.%s", field.getName()).isEqualTo(LoggingPolicy.PROHIBITED);
        });
    }
}
