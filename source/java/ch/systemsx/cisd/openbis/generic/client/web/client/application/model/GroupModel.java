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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.model;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.ModelData;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Group;

/**
 * {@link ModelData} for {@link Group}.
 * 
 * @author Izabela Adamczyk
 */
public class GroupModel extends BaseModelData
{
    private static final long serialVersionUID = 1L;

    public GroupModel()
    {
    }

    public GroupModel(final Group group)
    {
        set(ModelDataPropertyNames.CODE, group.getCode());
        set(ModelDataPropertyNames.DESCRIPTION, group.getDescription());
        set(ModelDataPropertyNames.REGISTRATOR, group.getRegistrator());
        set(ModelDataPropertyNames.REGISTRATION_DATE, group.getRegistrationDate());
        set(ModelDataPropertyNames.OBJECT, group);
    }

    public final static List<GroupModel> convert(final List<Group> groups)
    {
        final List<GroupModel> result = new ArrayList<GroupModel>();
        for (final Group g : groups)
        {
            result.add(new GroupModel(g));
        }
        return result;
    }

    public final Group getBaseObject()
    {
        return get(ModelDataPropertyNames.OBJECT);
    }
}
