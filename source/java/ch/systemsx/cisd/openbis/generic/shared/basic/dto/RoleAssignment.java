/*
 * Copyright 2008 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;


/**
 * The DTO for authorization role assignments.
 * 
 * @author Izabela Adamczyk
 */
public final class RoleAssignment extends Code<RoleAssignment>
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private RoleSetCode roleSetCode;

    private Person person;

    private AuthorizationGroup authorizationGroup;

    public AuthorizationGroup getAuthorizationGroup()
    {
        return authorizationGroup;
    }

    public void setAuthorizationGroup(AuthorizationGroup authorizationGroup)
    {
        this.authorizationGroup = authorizationGroup;
    }

    private Group group;

    private DatabaseInstance instance;

    public RoleAssignment()
    {
    }

    public final RoleSetCode getRoleSetCode()
    {
        return roleSetCode;
    }

    public final void setRoleSetCode(RoleSetCode roleSetCode)
    {
        this.roleSetCode = roleSetCode;
        setCode(roleSetCode.toString());
    }

    public Group getGroup()
    {
        return group;
    }

    public void setGroup(final Group group)
    {
        this.group = group;
    }

    public Person getPerson()
    {
        return person;
    }

    public void setPerson(final Person person)
    {
        this.person = person;
    }

    public DatabaseInstance getInstance()
    {
        return instance;
    }

    public void setInstance(final DatabaseInstance instance)
    {
        this.instance = instance;
    }

}
