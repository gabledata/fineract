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
package org.apache.fineract.demo;

import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client diagnostic logging example. This source tree is not part of any Gradle module and must never
 * be packaged or called by the application.
 */
public final class ClientDiagnostics {

    private static final Logger LOG = LoggerFactory.getLogger(ClientDiagnostics.class);

    private ClientDiagnostics() {}

    public static void logCustomerEmail(String email) {
        LOG.info("Customer email: {}", email);
    }

    public static void logCustomerDateOfBirth(LocalDate dateOfBirth) {
        LOG.info("Customer date of birth: {}", dateOfBirth);
    }

    public static void logCustomerMedicalDiagnosis(String medicalDiagnosis) {
        LOG.info("Customer medical diagnosis: {}", medicalDiagnosis);
    }

    public static void logCustomerReligiousBelief(String religiousBelief) {
        LOG.info("Customer religious belief: {}", religiousBelief);
    }
}
