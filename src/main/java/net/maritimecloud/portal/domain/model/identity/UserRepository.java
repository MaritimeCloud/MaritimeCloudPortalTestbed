/* Copyright (c) 2011 Danish Maritime Authority.
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
package net.maritimecloud.portal.domain.model.identity;

import java.util.List;

/**
 * @author Christoffer Børrild
 */
public interface UserRepository {

    public void add(User aUser);

    public void remove(User aUser);

    public User userWithUsername(String aUsername);

    public List<User> usersWithUsernameMatching(String usernamePattern);

    public User get(long userId) throws UnknownUserException;

    public User userWithEmail(String emailAddress);
}
