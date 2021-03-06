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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample;

import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.sample.SampleTypeColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.entity_type.AbstractEntityTypeGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.entity_type.AddTypeDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CheckBoxField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.DescriptionField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.IntegerField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractRegistrationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;

/**
 * Grid displaying sample types.
 * 
 * @author Tomasz Pylak
 * @author Piotr Buczek
 */
public class SampleTypeGrid extends AbstractEntityTypeGrid<SampleType>
{
    public static final String BROWSER_ID = GenericConstants.ID_PREFIX + "sample-type-browser";

    public static final String GRID_ID = BROWSER_ID + "_grid";

    private static final Boolean DEFAULT_LISTABLE_VALUE = true;

    private static final Boolean DEFAULT_SHOW_CONTAINER_VALUE = false;

    private static final Number DEFAULT_GENERATED_FROM_HIERARCHY_DEPTH_VALUE = 2;

    public static IDisposableComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        final SampleTypeGrid grid = new SampleTypeGrid(viewContext);
        return grid.asDisposableWithoutToolbar();
    }

    private SampleTypeGrid(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(viewContext, BROWSER_ID, GRID_ID);
    }

    @Override
    protected void listEntities(DefaultResultSetConfig<String, SampleType> resultSetConfig,
            AbstractAsyncCallback<ResultSet<SampleType>> callback)
    {
        viewContext.getService().listSampleTypes(resultSetConfig, callback);
    }

    @Override
    protected void prepareExportEntities(TableExportCriteria<SampleType> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportSampleTypes(exportCriteria, callback);
    }

    @Override
    protected EntityKind getEntityKind()
    {
        return EntityKind.SAMPLE;
    }

    @Override
    protected Window createEditEntityTypeDialog(final EntityKind entityKind,
            final SampleType sampleType)
    {
        final String code = sampleType.getCode();
        String title =
                viewContext.getMessage(Dict.EDIT_TYPE_TITLE_TEMPLATE, entityKind.getDescription(),
                        code);
        return new AbstractRegistrationDialog(viewContext, title, postRegistrationCallback)
            {
                private final DescriptionField descriptionField;

                private final TextField<Number> generatedFromHierarchyDepthField;

                private final CheckBoxField showContainerField;

                private final CheckBoxField listableField;

                {
                    descriptionField = createDescriptionField(viewContext);
                    descriptionField.setValueAndUnescape(sampleType.getDescription());
                    addField(descriptionField);

                    listableField =
                            SampleTypeDialogFieldHelper.createListableField(viewContext, sampleType
                                    .isListable());
                    addField(listableField);

                    showContainerField =
                            SampleTypeDialogFieldHelper.createShowContainerField(viewContext,
                                    sampleType.isShowContainer());
                    addField(showContainerField);

                    generatedFromHierarchyDepthField =
                            SampleTypeDialogFieldHelper.createGeneratedFromHierarchyDepthField(
                                    viewContext, sampleType.getGeneratedFromHierarchyDepth());
                    addField(generatedFromHierarchyDepthField);
                }

                @Override
                protected void register(AsyncCallback<Void> registrationCallback)
                {
                    sampleType.setDescription(descriptionField.getValue());
                    sampleType.setListable(listableField.getValue());
                    sampleType.setGeneratedFromHierarchyDepth(generatedFromHierarchyDepthField
                            .getValue().intValue());
                    sampleType.setShowContainer(showContainerField.getValue());
                    viewContext.getService().updateEntityType(entityKind, sampleType,
                            registrationCallback);
                }
            };
    }

    @Override
    protected Window createRegisterEntityTypeDialog(String title, SampleType newEntityType)
    {
        return new AddTypeDialog<SampleType>(viewContext, title, postRegistrationCallback,
                newEntityType)
            {

                private TextField<Number> generatedFromHierarchyDepthField;

                private CheckBoxField showContainerField;

                private CheckBoxField listableField;

                @Override
                protected void onRender(Element parent, int pos)
                {
                    listableField =
                            SampleTypeDialogFieldHelper.createListableField(viewContext,
                                    DEFAULT_LISTABLE_VALUE);
                    addField(listableField);

                    showContainerField =
                            SampleTypeDialogFieldHelper.createShowContainerField(viewContext,
                                    DEFAULT_SHOW_CONTAINER_VALUE);
                    addField(showContainerField);

                    generatedFromHierarchyDepthField =
                            SampleTypeDialogFieldHelper.createGeneratedFromHierarchyDepthField(
                                    viewContext, DEFAULT_GENERATED_FROM_HIERARCHY_DEPTH_VALUE);
                    addField(generatedFromHierarchyDepthField);

                    super.onRender(parent, pos);
                }

                @Override
                protected void register(SampleType sampleType,
                        AsyncCallback<Void> registrationCallback)
                {
                    sampleType.setGeneratedFromHierarchyDepth(generatedFromHierarchyDepthField
                            .getValue().intValue());
                    sampleType.setShowContainer(showContainerField.getValue());
                    sampleType.setListable(listableField.getValue());
                    SampleTypeGrid.this.register(sampleType, registrationCallback);
                }
            };
    }

    @Override
    protected void register(SampleType sampleType, AsyncCallback<Void> registrationCallback)
    {
        viewContext.getService().registerSampleType(sampleType, registrationCallback);
    }

    @Override
    protected SampleType createNewEntityType()
    {
        return new SampleType();
    }

    @Override
    protected IColumnDefinitionKind<SampleType>[] getStaticColumnsDefinition()
    {
        return SampleTypeColDefKind.values();
    }

    // 
    // Helpers
    // 

    private static final class SampleTypeDialogFieldHelper
    {
        public static CheckBoxField createListableField(
                final IViewContext<ICommonClientServiceAsync> viewContext, Boolean value)
        {
            final String title = viewContext.getMessage(Dict.LISTABLE);
            final CheckBoxField field = new CheckBoxField(title, false);
            field.setValue(value);
            return field;
        }

        public static TextField<Number> createGeneratedFromHierarchyDepthField(
                final IViewContext<ICommonClientServiceAsync> viewContext, Number value)
        {
            final String title = viewContext.getMessage(Dict.GENERATED_FROM_HIERARCHY_DEPTH);
            final TextField<Number> field = new IntegerField(title, true);
            field.setValue(value);
            return field;
        }

        public static CheckBoxField createShowContainerField(
                final IViewContext<ICommonClientServiceAsync> viewContext, Boolean value)
        {
            final String title = viewContext.getMessage(Dict.SHOW_CONTAINER);
            final CheckBoxField field = new CheckBoxField(title, false);
            field.setValue(value);
            return field;
        }
    }

}
