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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui;

import java.util.List;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.GroupModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.DropDownList;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SessionContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.User;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Group;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;

/**
 * {@link ComboBox} containing list of groups loaded from the server.
 * 
 * @author Izabela Adamczyk
 */
public class GroupSelectionWidget extends DropDownList<GroupModel, Group>
{
    private static final String EMPTY_RESULT_SUFFIX = "groups";

    private static final String CHOOSE_SUFFIX = "group";

    public static final String SUFFIX = "group-select";

    private final IViewContext<?> viewContext;

    private String initialGroupOrNull;

    public static final boolean isSharedGroup(Group g)
    {
        return SHARED_GROUP_CODE.equals(g.getCode());
    }

    public static final String SHARED_GROUP_CODE = "(Shared)";

    private final boolean addShared;

    public boolean dataLoaded = false;

    public GroupSelectionWidget(final IViewContext<?> viewContext, final String idSuffix,
            boolean addShared)
    {
        this(viewContext, idSuffix, addShared, null);
    }

    public GroupSelectionWidget(final IViewContext<?> viewContext, final String idSuffix,
            boolean addShared, final String initialGroupCodeOrNull)
    {
        super(viewContext, SUFFIX + idSuffix, Dict.GROUP, ModelDataPropertyNames.CODE,
                CHOOSE_SUFFIX, EMPTY_RESULT_SUFFIX);
        this.viewContext = viewContext;
        this.addShared = addShared;
        this.initialGroupOrNull = initialGroupCodeOrNull;

    }

    /**
     * Returns the {@link Group} currently selected.
     * 
     * @return <code>null</code> if nothing is selected yet.
     */
    public final Group tryGetSelectedGroup()
    {
        return super.tryGetSelected();
    }

    private Group createSharedGroup()
    {
        final Group group = new Group();
        group.setCode(SHARED_GROUP_CODE);
        group.setIdentifier("/");
        return group;
    }

    public final class ListGroupsCallback extends AbstractAsyncCallback<ResultSet<Group>>
    {
        ListGroupsCallback(final IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected final void process(final ResultSet<Group> result)
        {
            final ListStore<GroupModel> groupStore = getStore();
            groupStore.removeAll();
            if (addShared)
            {
                groupStore.add(new GroupModel(createSharedGroup()));
            }
            groupStore.add(convertItems(result.getList()));
            if (groupStore.getCount() > 0)
            {
                setEmptyText(viewContext.getMessage(Dict.COMBO_BOX_CHOOSE, CHOOSE_SUFFIX));
                setReadOnly(false);
                final int homeGroupIndex = getHomeGroupIndex(groupStore);
                if (homeGroupIndex > -1)
                {
                    setValue(groupStore.getAt(homeGroupIndex));
                    setOriginalValue(getValue());
                }
            } else
            {
                setEmptyText(viewContext.getMessage(Dict.COMBO_BOX_EMPTY, EMPTY_RESULT_SUFFIX));
                setReadOnly(true);
            }
            dataLoaded = true;
            selectGroupAndUpdateOriginal(initialGroupOrNull);
        }

        int getHomeGroupIndex(ListStore<GroupModel> groupStore)
        {
            final SessionContext sessionContext = viewContext.getModel().getSessionContext();
            final User user = sessionContext.getUser();
            final String homeGroup = user.getHomeGroupCode();
            if (homeGroup != null)
            {
                for (int i = 0; i < groupStore.getCount(); i++)
                {
                    if (groupStore.getAt(i).get(ModelDataPropertyNames.CODE).equals(homeGroup))
                    {
                        return i;
                    }
                }
            }
            return -1;
        }
    }

    public void selectGroupAndUpdateOriginal(String group)
    {
        initialGroupOrNull = group;
        if (dataLoaded && initialGroupOrNull != null)
        {
            GWTUtils.setSelectedItem(GroupSelectionWidget.this, ModelDataPropertyNames.CODE,
                    initialGroupOrNull);
            setOriginalValue(getValue());
        }
    }

    @Override
    protected List<GroupModel> convertItems(List<Group> result)
    {
        return GroupModel.convert(result);
    }

    @Override
    protected void loadData(AbstractAsyncCallback<List<Group>> callback)
    {
        DefaultResultSetConfig<String, Group> config = DefaultResultSetConfig.createFetchAll();
        viewContext.getCommonService().listGroups(config, new ListGroupsCallback(viewContext));
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return DatabaseModificationKind.any(ObjectKind.GROUP);
    }
}
