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
package net.maritimecloud.portal.config;

import net.maritimecloud.identityregistry.query.UserQueryRepository;
import net.maritimecloud.serviceregistry.query.OrganizationQueryRepository;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 *
 * @author Christoffer Børrild
 */
@Configuration
@EnableJpaRepositories(basePackageClasses = {OrganizationQueryRepository.class, UserQueryRepository.class})
@EntityScan(basePackageClasses = {OrganizationQueryRepository.class, UserQueryRepository.class}) //todo: replace with marker interfaces
@EnableAutoConfiguration
public class JpaConfig {

}
