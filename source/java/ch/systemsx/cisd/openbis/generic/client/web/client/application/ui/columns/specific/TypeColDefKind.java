/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.AbstractColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractType;

/**
 * Generic columns for a grid of types.
 * 
 * @author   Franz-Josef Elmer
 */
public enum TypeColDefKind implements IColumnDefinitionKind<AbstractType>
{
    CODE(new AbstractColumnDefinitionKind<AbstractType>(Dict.CODE)
        {
            @Override
            public String tryGetValue(AbstractType entity)
            {
                return entity.getCode();
            }
        }),

    DESCRIPTION(new AbstractColumnDefinitionKind<AbstractType>(Dict.DESCRIPTION, 300)
        {
            @Override
            public String tryGetValue(AbstractType entity)
            {
                return entity.getDescription();
            }
        });

    private final AbstractColumnDefinitionKind<AbstractType> columnDefinitionKind;

    private TypeColDefKind(AbstractColumnDefinitionKind<AbstractType> columnDefinitionKind)
    {
        this.columnDefinitionKind = columnDefinitionKind;
    }

    public String id()
    {
        return name();
    }

    public AbstractColumnDefinitionKind<AbstractType> getDescriptor()
    {
        return columnDefinitionKind;
    }
}
