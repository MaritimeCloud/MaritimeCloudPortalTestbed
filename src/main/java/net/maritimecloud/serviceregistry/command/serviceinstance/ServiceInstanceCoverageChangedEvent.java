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
package net.maritimecloud.serviceregistry.command.serviceinstance;

import org.axonframework.commandhandling.annotation.TargetAggregateIdentifier;

/**
 * @author Christoffer Børrild
 */
public class ServiceInstanceCoverageChangedEvent {

    @TargetAggregateIdentifier
    private final ServiceInstanceId serviceInstanceId;
    private final Coverage coverage;

    public ServiceInstanceCoverageChangedEvent(ServiceInstanceId serviceInstanceId, Coverage coverage) {
        this.serviceInstanceId = serviceInstanceId;
        this.coverage = coverage;
    }

    public ServiceInstanceId getServiceInstanceId() {
        return serviceInstanceId;
    }

    public Coverage getCoverage() {
        return coverage;
    }

}
