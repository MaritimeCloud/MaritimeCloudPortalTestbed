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
package net.maritimecloud.serviceregistry.command.organization;

import net.maritimecloud.serviceregistry.query.AliasRegistryEntry;
import java.util.List;
import net.maritimecloud.serviceregistry.command.api.CreateOrganization;
import net.maritimecloud.serviceregistry.command.api.ChangeOrganizationNameAndSummary;
import net.maritimecloud.common.infrastructure.axon.AbstractAxonCqrsIT;
import static net.maritimecloud.common.infrastructure.axon.CommonFixture.A_COVERAGE;
import static net.maritimecloud.common.infrastructure.axon.CommonFixture.A_NAME;
import static net.maritimecloud.common.infrastructure.axon.CommonFixture.A_SUMMARY;
import static net.maritimecloud.common.infrastructure.axon.CommonFixture.aPrepareServiceSpecificationCommand;
import static net.maritimecloud.common.infrastructure.axon.CommonFixture.generateServiceInstanceId;
import static net.maritimecloud.common.infrastructure.axon.CommonFixture.generateServiceSpecificationId;
import net.maritimecloud.serviceregistry.command.api.AddOrganizationAlias;
import net.maritimecloud.serviceregistry.command.api.AddServiceInstanceAlias;
import net.maritimecloud.serviceregistry.command.api.ChangeOrganizationWebsiteUrl;
import net.maritimecloud.serviceregistry.command.api.PrepareServiceSpecification;
import net.maritimecloud.serviceregistry.command.api.ProvideServiceInstance;
import net.maritimecloud.serviceregistry.command.api.RemoveOrganizationAlias;
import net.maritimecloud.serviceregistry.command.serviceinstance.ServiceInstanceId;
import net.maritimecloud.serviceregistry.command.servicespecification.ServiceSpecificationId;
import net.maritimecloud.serviceregistry.domain.service.AliasGroups;
import net.maritimecloud.serviceregistry.query.OrganizationEntry;
import org.axonframework.commandhandling.CommandExecutionException;
import org.axonframework.repository.AggregateNotFoundException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

/**
 * Integration test for Organization commands (run with 'mvn failsafe:integration-test')
 * <p>
 * @author Christoffer Børrild
 */
public class OrganizationIT extends AbstractAxonCqrsIT {

    private CreateOrganization createOrganizationCommand;
    private OrganizationId organizationId;
    private OrganizationId organizationId2;
    private ServiceSpecificationId serviceSpecificationId;
    private PrepareServiceSpecification prepareServiceSpecificationCommand;
    private ProvideServiceInstance provideServiceInstanceCommand;
    private ServiceInstanceId serviceInstanceId;

    @Before
    public void setUp() {
        // prepare an organization 
        createOrganizationCommand = generateCreateOrganizationCommand(generateIdentity());
        organizationId = createOrganizationCommand.getOrganizationId();
        organizationId2 = generateOrganizationId();

        // prepare a service specification
        serviceSpecificationId = generateServiceSpecificationId();
        prepareServiceSpecificationCommand = aPrepareServiceSpecificationCommand(organizationId, serviceSpecificationId);
        // Prepare a service instance 
        serviceInstanceId = generateServiceInstanceId();
        provideServiceInstanceCommand = new ProvideServiceInstance(
                organizationId,
                serviceSpecificationId,
                serviceInstanceId,
                A_NAME,
                A_SUMMARY,
                A_COVERAGE);
    }

    @Test
    public void testOrganization() {

        commandGateway().sendAndWait(createOrganizationCommand);
        commandGateway().sendAndWait(new ChangeOrganizationNameAndSummary(organizationId, ANOTHER_NAME, ANOTHER_SUMMARY));

        try {
            commandGateway().sendAndWait(createOrganizationCommand);
            fail("Should fail as item already exist");
        } catch (Exception e) {
        }

        assertEquals(1, organizationQueryRepository.count());
        OrganizationEntry entry = organizationQueryRepository.findOne(organizationId.identifier());
        assertEquals(ANOTHER_NAME, entry.getName());
        assertEquals(ANOTHER_SUMMARY, entry.getSummary());

        commandGateway().send(new CreateOrganization(organizationId2, AN_ALIAS, A_NAME, A_SUMMARY, A_URL));
        assertEquals(2, organizationQueryRepository.count());
    }

    @Test(expected = AggregateNotFoundException.class)
    public void cannotChangeNonExistingOrganization() throws Throwable {
        try {
            commandGateway().sendAndWait(new ChangeOrganizationNameAndSummary(new OrganizationId("notCreated"), A_NAME, A_SUMMARY));
        } catch (CommandExecutionException e) {
            throw e.getCause();
        }
    }

    @Test
    public void changeOrganization() throws Throwable {
        commandGateway().sendAndWait(createOrganizationCommand);
        commandGateway().sendAndWait(new ChangeOrganizationNameAndSummary(organizationId, ANOTHER_NAME, ANOTHER_SUMMARY));
        commandGateway().sendAndWait(new ChangeOrganizationWebsiteUrl(organizationId, ANOTHER_URL));
        OrganizationEntry entry = organizationQueryRepository.findOne(organizationId.identifier());
        assertEquals(ANOTHER_NAME, entry.getName());
        assertEquals(ANOTHER_SUMMARY, entry.getSummary());
        assertEquals(ANOTHER_URL, entry.getUrl());
    }

    @Test
    public void addOrganizationAlias() {

        // Given an organization (with a Service Specification and a provided Service Instance)
        commandGateway().sendAndWait(createOrganizationCommand);
        
        // NOTE: we expect the primary alias to have been denied, since it is used in another test!!! (FIXME)

        // When
        commandGateway().sendAndWait(new AddOrganizationAlias(organizationId, AN_ALIAS+organizationId.identifier()));

        // Then
        AliasRegistryEntry instance = aliasRegistryQueryRepository.findByGroupIdAndTypeNameAndAlias(
                AliasGroups.USERS_AND_ORGANIZATIONS.name(),
                OrganizationId.class.getName(),
                AN_ALIAS+organizationId.identifier());
        assertNotNull(instance);
        assertEquals(organizationId.identifier(), instance.getTargetId());
        assertEquals(AN_ALIAS+organizationId.identifier(), instance.getAlias());

        // When add another
        commandGateway().sendAndWait(new AddOrganizationAlias(organizationId, ANOTHER_ALIAS+organizationId.identifier()));

        // then
        List<AliasRegistryEntry> instances = aliasRegistryQueryRepository.findByGroupIdAndTypeNameAndTargetId(
                AliasGroups.USERS_AND_ORGANIZATIONS.name(),
                OrganizationId.class.getName(),
                organizationId.identifier());
        assertEquals(2, instances.size());

        // when add again
        commandGateway().sendAndWait(new AddOrganizationAlias(organizationId, ANOTHER_ALIAS+organizationId.identifier()));

        // then still
        instances = aliasRegistryQueryRepository.findByGroupIdAndTypeNameAndTargetId(
                AliasGroups.USERS_AND_ORGANIZATIONS.name(),
                OrganizationId.class.getName(),
                organizationId.identifier());
        assertEquals(2, instances.size());

        // given another organization 
        commandGateway().sendAndWait(generateCreateOrganizationCommand(organizationId2.identifier()));

        // when add same alias to another organization (it should be denied) 
        commandGateway().sendAndWait(new AddOrganizationAlias(organizationId2, AN_ALIAS+organizationId.identifier()));

        // then still (...and an denied-event is emitted)
        instances = aliasRegistryQueryRepository.findByGroupIdAndTypeNameAndTargetId(
                AliasGroups.USERS_AND_ORGANIZATIONS.name(),
                OrganizationId.class.getName(),
                organizationId.identifier());
        assertEquals(2, instances.size());

        // when remove alias
        commandGateway().sendAndWait(new RemoveOrganizationAlias(organizationId, AN_ALIAS+organizationId.identifier()));

        // then
        instances = aliasRegistryQueryRepository.findByGroupIdAndTypeNameAndTargetId(
                AliasGroups.USERS_AND_ORGANIZATIONS.name(),
                OrganizationId.class.getName(),
                organizationId.identifier());
        assertEquals(1, instances.size());

        // Clean up - because otherwise we leave inconsistency in the eventstore 
        // (this test did not replay all existing events, hence we might leave it 
        // inconsistent)
        
        // when remove alias
        commandGateway().sendAndWait(new RemoveOrganizationAlias(organizationId, ANOTHER_ALIAS+organizationId.identifier()));

        // then
        instances = aliasRegistryQueryRepository.findByGroupIdAndTypeNameAndTargetId(
                AliasGroups.USERS_AND_ORGANIZATIONS.name(),
                OrganizationId.class.getName(),
                organizationId.identifier());
        assertEquals(0, instances.size());

    }

    @Test
    public void addServiceInstanceAlias() {

        // Given an organization (with a Service Specification and a provided Service Instance)
        commandGateway().sendAndWait(createOrganizationCommand);
        commandGateway().sendAndWait(prepareServiceSpecificationCommand);
        commandGateway().sendAndWait(provideServiceInstanceCommand);

        // When
        commandGateway().sendAndWait(new AddServiceInstanceAlias(organizationId, serviceInstanceId, AN_ALIAS));

        // Then
        AliasRegistryEntry instance = aliasRegistryQueryRepository.findByGroupIdAndTypeNameAndAlias(
                organizationId.identifier(),
                ServiceInstanceId.class.getName(),
                AN_ALIAS);
        assertNotNull(instance);
        assertEquals(serviceInstanceId.identifier(), instance.getTargetId());
        assertEquals(AN_ALIAS, instance.getAlias());

        commandGateway().sendAndWait(new AddServiceInstanceAlias(organizationId, serviceInstanceId, ANOTHER_ALIAS));
        List<AliasRegistryEntry> instances = aliasRegistryQueryRepository.findByGroupIdAndTypeNameAndTargetId(
                organizationId.identifier(),
                ServiceInstanceId.class.getName(),
                serviceInstanceId.identifier());

        assertEquals(2, instances.size());
    }

}
