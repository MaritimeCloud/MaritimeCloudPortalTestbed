/* Copyright 2014 Danish Maritime Authority.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package net.maritimecloud.serviceregistry.command.servicespecification;

import net.maritimecloud.common.infrastructure.axon.CommonFixture;
import net.maritimecloud.serviceregistry.command.organization.OrganizationId;
import org.axonframework.test.FixtureConfiguration;
import org.axonframework.test.Fixtures;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Christoffer Børrild
 */
public class ServiceSpecificationTest extends CommonFixture {

    private FixtureConfiguration<ServiceSpecification> fixture;

    @Before
    public void setUp() {
        fixture = Fixtures.newGivenWhenThenFixture(ServiceSpecification.class);
        fixture.registerInjectableResource(fixture.getRepository());
    }

    @Test
    public void changeServiceSpecificationNameAndSummary() {
        OrganizationId organizationId = new OrganizationId(AN_ORG_ID);

        fixture.given(new ServiceSpecificationCreatedEvent(organizationId, new ServiceSpecificationId(A_SPEC_ID), ServiceType.AISASM, A_NAME, A_SUMMARY))
                .when(new ChangeServiceSpecificationNameAndSummaryCommand(new ServiceSpecificationId(A_SPEC_ID), ANOTHER_NAME, ANOTHER_SUMMARY))
                .expectEvents(new ServiceSpecificationNameAndSummaryChangedEvent(new ServiceSpecificationId(A_SPEC_ID), ANOTHER_NAME, ANOTHER_SUMMARY));
    }

}
