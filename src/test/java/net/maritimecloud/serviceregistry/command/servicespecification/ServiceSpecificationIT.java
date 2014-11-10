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

import net.maritimecloud.serviceregistry.command.organization.*;
import net.maritimecloud.common.infrastructure.axon.AbstractAxonCqrsIT;
import java.util.UUID;
import javax.annotation.Resource;
import net.maritimecloud.portal.config.ApplicationTestConfig;
import net.maritimecloud.serviceregistry.query.ServiceSpecificationListener;
import net.maritimecloud.serviceregistry.query.ServiceSpecificationQueryRepository;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.*;

/**
 * Integration test for Organization commands
 * (run with 'mvn failsafe:integration-test')
 * @author Christoffer Børrild
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ApplicationTestConfig.class)
public class ServiceSpecificationIT extends AbstractAxonCqrsIT {
    
    @Resource
    protected CommandGateway commandGateway;

    @Resource
    protected ServiceSpecificationQueryRepository serviceSpecificationQueryRepository;
    
    private final String itemId = UUID.randomUUID().toString();
    private final OrganizationId organizationId = new OrganizationId(itemId);
    private final ServiceSpecificationId serviceSpecificationId1 = new ServiceSpecificationId(UUID.randomUUID().toString());
    private final ServiceSpecificationId serviceSpecificationId2 = new ServiceSpecificationId(UUID.randomUUID().toString());
    private final ServiceSpecificationId serviceSpecificationId3 = new ServiceSpecificationId(UUID.randomUUID().toString());
    private static final String A_NAME = "a name";
    private static final String A_SUMMARY_ = "a summary ...";
    private final CreateOrganizationCommand CREATE_ORGANIZATION_COMMAND = new CreateOrganizationCommand(organizationId, A_NAME, A_SUMMARY_);
    
    @Before
    public void setUp() {
        serviceSpecificationQueryRepository.deleteAll();
        subscribeListener(new ServiceSpecificationListener(serviceSpecificationQueryRepository));
    }

    @Test
    public void prepareServiceSpecification() {

        commandGateway.sendAndWait(CREATE_ORGANIZATION_COMMAND);
        commandGateway.sendAndWait(new PrepareServiceSpecificationCommand(organizationId, serviceSpecificationId1, A_NAME, A_SUMMARY_));
        assertEquals(1, serviceSpecificationQueryRepository.count());
        assertEquals("a name", serviceSpecificationQueryRepository.findOne(serviceSpecificationId1.identifier()).getName());
        
        commandGateway.sendAndWait(new PrepareServiceSpecificationCommand(organizationId, serviceSpecificationId2, A_NAME, A_SUMMARY_));
        commandGateway.sendAndWait(new PrepareServiceSpecificationCommand(organizationId, serviceSpecificationId3, A_NAME, A_SUMMARY_));

        assertEquals(3, serviceSpecificationQueryRepository.count());
        
        try {
            commandGateway.sendAndWait(new PrepareServiceSpecificationCommand(organizationId, serviceSpecificationId1, A_NAME, A_SUMMARY_));
            fail("Should fail as item already exist");
        } catch (Exception e) {
        }
        
        assertEquals(3, serviceSpecificationQueryRepository.count());
    }

    @Test
    public void changeNameAndSummary() {

        commandGateway.sendAndWait(CREATE_ORGANIZATION_COMMAND);
        commandGateway.sendAndWait(new PrepareServiceSpecificationCommand(organizationId, serviceSpecificationId1, A_NAME, A_SUMMARY_));
        assertEquals(1, serviceSpecificationQueryRepository.count());
        assertEquals("a name", serviceSpecificationQueryRepository.findOne(serviceSpecificationId1.identifier()).getName());
        
        commandGateway.sendAndWait(new ChangeServiceSpecificationNameAndSummaryCommand(serviceSpecificationId1, A_NAME+" version 2", A_SUMMARY_+" version 2"));
        assertEquals("a name version 2", serviceSpecificationQueryRepository.findOne(serviceSpecificationId1.identifier()).getName());
        assertEquals("a summary ... version 2", serviceSpecificationQueryRepository.findOne(serviceSpecificationId1.identifier()).getSummary());
        
    }
    
        // Next up: 
        // Wire up main (see https://github.com/MagnusSmith/axon-orders/tree/master/web-core/src/main/java/com/example/config )
        // Add REST interface
        // introduce ServiceInstances

}
