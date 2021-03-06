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
package net.maritimecloud.serviceregistry.command.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import net.maritimecloud.serviceregistry.infrastructure.jackson.CoverageDeserializer;
import net.maritimecloud.serviceregistry.infrastructure.jackson.CoverageSerializer;
import net.maritimecloud.common.cqrs.Command;
import net.maritimecloud.serviceregistry.command.organization.OrganizationId;
import net.maritimecloud.serviceregistry.command.serviceinstance.Coverage;
import net.maritimecloud.serviceregistry.command.serviceinstance.ServiceInstanceId;
import net.maritimecloud.serviceregistry.command.servicespecification.ServiceSpecificationId;
import org.axonframework.commandhandling.annotation.TargetAggregateIdentifier;
import org.axonframework.common.Assert;

/**
 *
 * @author Christoffer Børrild
 */
public class ProvideServiceInstance implements Command {

    @TargetAggregateIdentifier
    private final ServiceInstanceId serviceInstanceId;
    private final OrganizationId providerId;
    private final ServiceSpecificationId specificationId;
    private final String name;
    private final String summary;
    private final Coverage coverage;

    @JsonCreator
    public ProvideServiceInstance(
            @JsonProperty("providerId") OrganizationId providerId,
            @JsonProperty("specificationId") ServiceSpecificationId specificationId,
            @JsonProperty("serviceInstanceId") ServiceInstanceId serviceInstanceId,
            @JsonProperty("name") String name,
            @JsonProperty("summary") String summary,
            @JsonProperty("coverage") @JsonSerialize(using = CoverageSerializer.class) @JsonDeserialize(using = CoverageDeserializer.class) Coverage coverage
    ) {
        Assert.notNull(providerId, "The organizationId of the providing organization must be supplied");
        Assert.notNull(specificationId, "The serviceSpecificationId must be provided");
        Assert.notNull(name, "The provided name cannot be null");
        Assert.notNull(summary, "The provided summary cannot be null");
        this.providerId = providerId;
        this.specificationId = specificationId;
        this.serviceInstanceId = serviceInstanceId;
        this.name = name;
        this.summary = summary;
        this.coverage = coverage;
    }

    public OrganizationId getProviderId() {
        return providerId;
    }

    public ServiceSpecificationId getSpecificationId() {
        return specificationId;
    }

    public ServiceInstanceId getServiceInstanceId() {
        return serviceInstanceId;
    }

    public String getName() {
        return name;
    }

    public String getSummary() {
        return summary;
    }

    public Coverage getCoverage() {
        return coverage;
    }

}
