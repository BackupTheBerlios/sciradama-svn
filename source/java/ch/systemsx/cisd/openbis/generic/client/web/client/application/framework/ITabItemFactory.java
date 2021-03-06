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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.framework;

/**
 * Creates {@link ITabItem} which consist of a description and a content of the tab.<br>
 * Allows to create a tab in a moment when it is needed.
 * 
 * @author Tomasz Pylak
 */
public interface ITabItemFactory
{
    /** Creates and initializes a new tab. */
    ITabItem create();

    /**
     * Returns the unique identifier of this tab item. Note that the id should be unique among all
     * tabs, not widgets.
     * <p>
     * The framework ensures that no two components with the same id will be displayed. Instead the
     * one already created will get the focus.
     * </p>
     */
    public String getId();
}
