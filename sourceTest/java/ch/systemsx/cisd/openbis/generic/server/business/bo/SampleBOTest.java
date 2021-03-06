/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import static ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool.EXAMPLE_DATABASE_INSTANCE;
import static ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool.EXAMPLE_GROUP;
import static ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool.EXAMPLE_GROUP2;
import static ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool.EXAMPLE_PERSON;
import static ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool.EXAMPLE_SESSION;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jmock.Expectations;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.utilities.BeanUtils;
import ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool;
import ch.systemsx.cisd.openbis.generic.shared.CommonTestUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.IdentifierHelper;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.types.SampleTypeCode;

/**
 * Test cases for corresponding {@link SampleBO} class.
 * 
 * @author    Franz-Josef Elmer
 */
public final class SampleBOTest extends AbstractBOTest
{
    private static final TechId SAMPLE_TECH_ID = CommonTestUtils.TECH_ID;

    private static final String DB = "DB";

    private static final String DILUTION_PLATE = SampleTypeCode.DILUTION_PLATE.getCode();

    private static final String MASTER_PLATE = SampleTypeCode.MASTER_PLATE.getCode();

    private static final String DEFAULT_SAMPLE_CODE = "xx";

    static SamplePE createSample(final String sampleCode)
    {
        return createSample(sampleCode, "sample-type-code");
    }

    private static SamplePE createAnySample()
    {
        final SamplePE sample = new SamplePE();
        sample.setCode(DEFAULT_SAMPLE_CODE);
        sample.setModificationDate(new Date());
        return sample;
    }

    private static SamplePE createSample(final SampleIdentifier sampleIdentifier,
            final String sampleTypeCode)
    {
        final SamplePE sample = new SamplePE();
        sample.setCode(sampleIdentifier.getSampleCode());
        sample.setRegistrator(EXAMPLE_PERSON);
        final SampleTypePE sampleTypeDTO = new SampleTypePE();
        sampleTypeDTO.setCode(sampleTypeCode);
        sample.setSampleType(sampleTypeDTO);
        return sample;
    }

    private static SamplePE createSample(final String sampleCode, final String sampleTypeCode)
    {
        return createSample(SampleIdentifier.createHomeGroup(sampleCode), sampleTypeCode);
    }

    private final static IEntityProperty createSampleProperty()
    {
        final IEntityProperty sampleProperty = new EntityProperty();
        sampleProperty.setValue("blue");
        final PropertyType propertyType = new PropertyType();
        propertyType.setLabel("color");
        propertyType.setCode("color");
        final DataType dataType = new DataType();
        dataType.setCode(DataTypeCode.VARCHAR);
        propertyType.setDataType(dataType);
        sampleProperty.setPropertyType(propertyType);
        return sampleProperty;
    }

    private final static SampleIdentifier getGroupSampleIdentifier(final String code)
    {
        return new SampleIdentifier(IdentifierHelper.createGroupIdentifier(EXAMPLE_GROUP), code);
    }

    private final static SampleIdentifier getSharedSampleIdentifier(final String code)
    {
        return new SampleIdentifier(new DatabaseInstanceIdentifier(DB), code);
    }

    @Test
    public void testDefineAndSaveSampleWithProperties()
    {
        final NewSample newSample = new NewSample();

        newSample.setIdentifier(getGroupSampleIdentifier(DEFAULT_SAMPLE_CODE).toString());
        newSample.setSampleType(createSampleType(SampleTypeCode.MASTER_PLATE));

        final SampleTypePE sampleType = new SampleTypePE();
        sampleType.setCode(MASTER_PLATE);
        sampleType.setId(new Long(21L));
        sampleType.setDatabaseInstance(new DatabaseInstancePE());
        final IEntityProperty sampleProperty = createSampleProperty();
        newSample.setProperties(new IEntityProperty[]
            { sampleProperty });
        final SamplePropertyPE samplePropertyPE = new SamplePropertyPE();
        samplePropertyPE.setRegistrator(EXAMPLE_SESSION.tryGetPerson());
        final SampleTypePropertyTypePE sampleTypePropertyType = new SampleTypePropertyTypePE();
        samplePropertyPE.setEntityTypePropertyType(sampleTypePropertyType);

        context.checking(new Expectations()
            {
                {
                    ManagerTestTool.prepareFindGroup(this, daoFactory, groupDAO,
                            databaseInstanceDAO);

                    one(sampleTypeDAO).tryFindSampleTypeByCode(MASTER_PLATE);
                    will(returnValue(sampleType));

                    one(propertiesConverter).convertProperties(newSample.getProperties(),
                            MASTER_PLATE, EXAMPLE_PERSON);
                    final List<SamplePropertyPE> set = new ArrayList<SamplePropertyPE>();
                    set.add(samplePropertyPE);
                    will(returnValue(set));

                    one(daoFactory).getPermIdDAO();
                    will(returnValue(permIdDAO));

                    one(permIdDAO).createPermId();
                    will(returnValue("2009010112341234-1"));

                    BaseMatcher<SamplePE> matcher = new BaseMatcher<SamplePE>()
                        {
                            public void describeTo(final Description description)
                            {
                            }

                            public boolean matches(final Object item)
                            {
                                if (item instanceof SamplePE == false)
                                {
                                    return false;
                                }
                                final SamplePE sample = (SamplePE) item;
                                assertEquals(EXAMPLE_SESSION.tryGetHomeGroup(), sample.getGroup());
                                assertNull(sample.getDatabaseInstance());
                                assertEquals(newSample.getIdentifier(), sample
                                        .getSampleIdentifier().toString());
                                assertEquals(EXAMPLE_PERSON, sample.getRegistrator());
                                return true;
                            }
                        };
                    one(sampleDAO).createSample(with(matcher));

                    allowing(externalDataDAO).listExternalData(with(matcher));
                    will(returnValue(new ArrayList<ExternalDataPE>()));

                    extracted(sampleType);

                }

                @SuppressWarnings("unchecked")
                private void extracted(final SampleTypePE type)
                {
                    one(propertiesConverter).checkMandatoryProperties(
                            with(aNonNull(Collection.class)), with(type));
                }
            });

        final SampleBO sampleBO = createSampleBO();
        sampleBO.define(newSample);

        final Set<SamplePropertyPE> properties = sampleBO.getSample().getProperties();
        assertEquals(1, properties.size());
        assertSame(samplePropertyPE, properties.iterator().next());

        sampleBO.save();

        context.assertIsSatisfied();
    }

    @Test
    public final void testDefineSampleHappyCase()
    {
        final SampleIdentifier sampleIdentifier =
                getGroupSampleIdentifier("SAMPLE_CONTAINER:" + DEFAULT_SAMPLE_CODE);
        final NewSample newSample = new NewSample();
        newSample.setIdentifier(sampleIdentifier.toString());
        newSample.setSampleType(createSampleType(SampleTypeCode.DILUTION_PLATE));

        final SampleIdentifier generatedFromIdentifier =
                getGroupSampleIdentifier("SAMPLE_GENERATOR");
        newSample.setParentIdentifier(generatedFromIdentifier.toString());

        final SampleIdentifier containerIdentifier = getGroupSampleIdentifier("SAMPLE_CONTAINER");
        newSample.setContainerIdentifier(containerIdentifier.toString());

        newSample.setProperties(IEntityProperty.EMPTY_ARRAY);

        final SamplePE generatedFrom = new SamplePE();
        generatedFrom.setRegistrator(EXAMPLE_PERSON);
        generatedFrom.setGroup(EXAMPLE_GROUP);
        generatedFrom.setCode("SAMPLE_GENERATOR");

        final SamplePE container = new SamplePE();
        container.setRegistrator(EXAMPLE_PERSON);
        container.setGroup(EXAMPLE_GROUP);
        container.setCode("SAMPLE_CONTAINER");

        final SampleTypePE sampleType = new SampleTypePE();
        sampleType.setCode(DILUTION_PLATE);

        final SamplePE samplePE = new SamplePE();
        samplePE.setRegistrator(EXAMPLE_PERSON);
        samplePE.setGeneratedFrom(generatedFrom);
        samplePE.setContainer(container);
        samplePE.setSampleType(sampleType);

        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getSampleDAO();
                    will(returnValue(sampleDAO));

                    ManagerTestTool.prepareFindGroup(this, daoFactory, groupDAO,
                            databaseInstanceDAO);

                    one(sampleDAO).tryFindByCodeAndGroup(generatedFromIdentifier.getSampleCode(),
                            EXAMPLE_GROUP);
                    will(returnValue(generatedFrom));

                    one(sampleDAO).tryFindByCodeAndGroup(containerIdentifier.getSampleCode(),
                            EXAMPLE_GROUP);
                    will(returnValue(container));

                    one(sampleTypeDAO).tryFindSampleTypeByCode(DILUTION_PLATE);
                    will(returnValue(sampleType));

                    one(propertiesConverter).convertProperties(newSample.getProperties(),
                            DILUTION_PLATE, EXAMPLE_PERSON);
                    will(returnValue(new ArrayList<SamplePropertyPE>()));

                    one(daoFactory).getPermIdDAO();
                    will(returnValue(permIdDAO));

                    one(permIdDAO).createPermId();
                    will(returnValue("2009010112341234-1"));
                }
            });

        final SampleBO sampleBO = createSampleBO();
        sampleBO.define(newSample);

        final SamplePE sample = sampleBO.getSample();
        assertEquals(sampleIdentifier.toString(), sample.getSampleIdentifier().toString());
        assertEquals(EXAMPLE_PERSON, sample.getRegistrator());
        assertSame(sampleType, sample.getSampleType());
        assertEquals(container, sample.getContainer());
        assertEquals(generatedFrom, sample.getGeneratedFrom());

        context.assertIsSatisfied();
    }

    @Test
    public void testDelete()
    {
        final TechId sampleId = SAMPLE_TECH_ID;
        final SamplePE sample = createSample(DEFAULT_SAMPLE_CODE);
        sample.setId(sampleId.getId());
        final String reason = "reason";

        prepareTryToLoadOfSampleWithId(sample);
        context.checking(new Expectations()
            {
                {
                    PersonPE person = EXAMPLE_SESSION.tryGetPerson();
                    EventPE event = SampleBO.createDeletionEvent(sample, person, reason);
                    one(eventDAO).persist(event);
                    one(sampleDAO).delete(sample);
                }
            });
        final SampleBO sampleBO = createSampleBO();
        sampleBO.deleteByTechId(sampleId, reason);
        context.assertIsSatisfied();
    }

    @Test
    public final void testDetachFromExperiment()
    {
        final SamplePE sample = createAnySample();
        sample.setExperiment(new ExperimentPE());
        sample.setId(SAMPLE_TECH_ID.getId());

        prepareExperimentUpdateOnly(sample);
        ExperimentIdentifier experimentIdentifier = null;

        updateSampleExperiment(SAMPLE_TECH_ID, sample, experimentIdentifier);
        assertNull(sample.getExperiment());
        context.assertIsSatisfied();
    }

    @Test
    public final void testDetachFromExperimentWithDatasetsFails()
    {
        final SamplePE sample = createAnySample();
        sample.setExperiment(new ExperimentPE());
        sample.setId(SAMPLE_TECH_ID.getId());

        prepareTryToLoadOfSampleWithId(sample);
        prepareNoPropertiesToUpdate(sample);
        context.checking(new Expectations()
            {
                {
                    allowing(externalDataDAO).hasExternalData(with(sample));
                    will(returnValue(true));
                }
            });
        ExperimentIdentifier experimentIdentifier = null;
        String errorMsg =
                "Cannot detach the sample 'xx' from the experiment because there are already datasets attached to the sample.";
        try
        {
            updateSampleExperiment(SAMPLE_TECH_ID, sample, experimentIdentifier);
        } catch (UserFailureException e)
        {
            assertEquals(errorMsg, e.getMessage());
            return;
        }
        fail("Following exception expected: " + errorMsg);
    }

    @Test
    public final void testEditExperiment()
    {
        final ProjectPE project = createProject();
        // create experiment which we will attach the sample
        final ExperimentPE experimentToAttach = new ExperimentPE();
        experimentToAttach.setCode("exp1");
        experimentToAttach.setProject(project);
        final ExperimentIdentifier experimentIdentifier = new ExperimentIdentifier();
        experimentIdentifier.setExperimentCode("exp1");
        experimentIdentifier.setProjectCode(project.getCode());
        experimentIdentifier.setGroupCode(project.getGroup().getCode());
        experimentIdentifier.setDatabaseInstanceCode(project.getGroup().getDatabaseInstance()
                .getCode());

        // create a sample already attached to an experiment
        final ExperimentPE sampleExperiment = new ExperimentPE();
        sampleExperiment.setCode("exp2");
        sampleExperiment.setProject(project);
        final SamplePE sample = new SamplePE();
        sample.setId(SAMPLE_TECH_ID.getId());
        sample.setCode("sampleCode");
        sample.setExperiment(sampleExperiment);
        sample.setGroup(EXAMPLE_GROUP);

        Date now = new Date();
        sample.setModificationDate(now);

        prepareTryToLoadOfSampleWithId(sample);
        prepareNoPropertiesToUpdate(sample);
        context.checking(new Expectations()
            {
                {
                    one(daoFactory).getProjectDAO();
                    will(returnValue(projectDAO));
                    one(projectDAO).tryFindProject(experimentIdentifier.getDatabaseInstanceCode(),
                            experimentIdentifier.getGroupCode(),
                            experimentIdentifier.getProjectCode());
                    will(returnValue(project));

                    one(daoFactory).getExperimentDAO();
                    will(returnValue(experimentDAO));
                    one(experimentDAO).tryFindByCodeAndProject(project,
                            experimentIdentifier.getExperimentCode());
                    will(returnValue(experimentToAttach));

                    // no datasets attached
                    allowing(externalDataDAO).listExternalData(with(sample));
                    will(returnValue(new ArrayList<ExternalDataPE>()));
                }
            });
        createSampleBO().update(
                new SampleUpdatesDTO(SAMPLE_TECH_ID, null, experimentIdentifier, Collections
                        .<NewAttachment> emptyList(), now, null, null, null));

        assertEquals(experimentToAttach, sample.getExperiment());
        context.assertIsSatisfied();
    }

    @Test
    public final void testEditSampleChangeGroupToShared()
    {
        final SamplePE sample = new SamplePE();
        sample.setId(SAMPLE_TECH_ID.getId());
        String code = "sampleCode";
        sample.setCode(code);
        sample.setGroup(EXAMPLE_GROUP);

        Date now = new Date();
        sample.setModificationDate(now);

        prepareTryToLoadOfSampleWithId(sample);
        prepareNoPropertiesToUpdate(sample);
        context.checking(new Expectations()
            {
                {

                    allowing(daoFactory).getHomeDatabaseInstance();
                    will(returnValue(EXAMPLE_DATABASE_INSTANCE));

                    allowing(externalDataDAO).hasExternalData(with(sample));
                    will(returnValue(false));
                }
            });
        String newSampleIdentifierWithoutDb = "/" + code;
        assertNotNull(sample.getGroup());
        createSampleBO().update(
                new SampleUpdatesDTO(SAMPLE_TECH_ID, null, null, Collections
                        .<NewAttachment> emptyList(), now, SampleIdentifierFactory
                        .parse(newSampleIdentifierWithoutDb), null, null));
        String newSampleIdentWithDb =
                EXAMPLE_DATABASE_INSTANCE.getCode() + ":" + newSampleIdentifierWithoutDb;
        assertEquals(newSampleIdentWithDb, sample.getSampleIdentifier().toString());
        assertNull(sample.getGroup());
        context.assertIsSatisfied();

    }

    @Test
    public final void testEditSamplesParent()
    {

        final SamplePE parent = new SamplePE();
        parent.setId(SAMPLE_TECH_ID.getId());
        String parentCode = "sampleParent";
        parent.setCode(parentCode);
        parent.setGroup(EXAMPLE_GROUP);

        final SamplePE sample = new SamplePE();
        sample.setId(SAMPLE_TECH_ID.getId());
        String code = "sampleCode";
        sample.setCode(code);
        sample.setGroup(EXAMPLE_GROUP);

        Date now = new Date();
        sample.setModificationDate(now);

        prepareTryToLoadOfSampleWithId(sample);
        prepareNoPropertiesToUpdate(sample);
        context.checking(new Expectations()
            {
                {

                    allowing(databaseInstanceDAO).tryFindDatabaseInstanceByCode(
                            EXAMPLE_DATABASE_INSTANCE.getCode());
                    will(returnValue(EXAMPLE_DATABASE_INSTANCE));

                    allowing(groupDAO).tryFindGroupByCodeAndDatabaseInstance(
                            parent.getGroup().getCode(), EXAMPLE_DATABASE_INSTANCE);
                    will(returnValue(EXAMPLE_GROUP));

                    allowing(sampleDAO).tryFindByCodeAndGroup(parent.getCode(), EXAMPLE_GROUP);
                    will(returnValue(parent));

                    allowing(externalDataDAO).hasExternalData(with(sample));
                    will(returnValue(false));
                }
            });
        assertNull(sample.getGeneratedFrom());
        createSampleBO().update(
                new SampleUpdatesDTO(SAMPLE_TECH_ID, null, null, Collections
                        .<NewAttachment> emptyList(), now, null, parent.getSampleIdentifier()
                        .toString(), null));
        assertNotNull(sample.getGeneratedFrom());
        assertEquals(parent, sample.getGeneratedFrom());
        context.assertIsSatisfied();

    }

    @Test
    public final void testEditSamplesContainer()
    {

        final SamplePE container = new SamplePE();
        container.setId(SAMPLE_TECH_ID.getId());
        String parentCode = "sampleContainer";
        container.setCode(parentCode);
        container.setGroup(EXAMPLE_GROUP);

        final SamplePE sample = new SamplePE();
        sample.setId(SAMPLE_TECH_ID.getId());
        String code = "sampleCode";
        sample.setCode(code);
        sample.setGroup(EXAMPLE_GROUP);

        Date now = new Date();
        sample.setModificationDate(now);

        prepareTryToLoadOfSampleWithId(sample);
        prepareNoPropertiesToUpdate(sample);
        context.checking(new Expectations()
            {
                {

                    allowing(databaseInstanceDAO).tryFindDatabaseInstanceByCode(
                            EXAMPLE_DATABASE_INSTANCE.getCode());
                    will(returnValue(EXAMPLE_DATABASE_INSTANCE));

                    allowing(groupDAO).tryFindGroupByCodeAndDatabaseInstance(
                            container.getGroup().getCode(), EXAMPLE_DATABASE_INSTANCE);
                    will(returnValue(EXAMPLE_GROUP));

                    allowing(sampleDAO).tryFindByCodeAndGroup(container.getCode(), EXAMPLE_GROUP);
                    will(returnValue(container));

                    allowing(externalDataDAO).hasExternalData(with(sample));
                    will(returnValue(false));
                }
            });
        assertNull(sample.getContainer());
        createSampleBO().update(
                new SampleUpdatesDTO(SAMPLE_TECH_ID, null, null, Collections
                        .<NewAttachment> emptyList(), now, null, null, container
                        .getSampleIdentifier().toString()));
        assertNotNull(sample.getContainer());
        assertEquals(container, sample.getContainer());
        context.assertIsSatisfied();
    }

    @Test
    public final void testEditAndSaveSamplesContainerFromDifferentGroup()
    {

        final SamplePE container = new SamplePE();
        container.setId(SAMPLE_TECH_ID.getId());
        String parentCode = "sampleContainer";
        container.setCode(parentCode);
        container.setGroup(EXAMPLE_GROUP2);

        final SamplePE sample = new SamplePE();
        sample.setId(SAMPLE_TECH_ID.getId());
        String code = "sampleCode";
        sample.setCode(code);
        sample.setGroup(EXAMPLE_GROUP);

        Date now = new Date();
        sample.setModificationDate(now);

        prepareTryToLoadOfSampleWithId(sample);
        prepareNoPropertiesToUpdate(sample);
        context.checking(new Expectations()
            {
                {

                    allowing(databaseInstanceDAO).tryFindDatabaseInstanceByCode(
                            EXAMPLE_DATABASE_INSTANCE.getCode());
                    will(returnValue(EXAMPLE_DATABASE_INSTANCE));

                    allowing(groupDAO).tryFindGroupByCodeAndDatabaseInstance(
                            container.getGroup().getCode(), EXAMPLE_DATABASE_INSTANCE);
                    will(returnValue(EXAMPLE_GROUP2));

                    allowing(sampleDAO).tryFindByCodeAndGroup(container.getCode(), EXAMPLE_GROUP2);
                    will(returnValue(container));

                    allowing(externalDataDAO).hasExternalData(with(sample));
                    will(returnValue(false));

                    one(sampleDAO).createSample(sample);
                    one(propertiesConverter).checkMandatoryProperties(sample.getProperties(),
                            sample.getSampleType());
                }
            });
        assertNull(sample.getContainer());
        SampleBO bo = createSampleBO();
        bo.update(new SampleUpdatesDTO(SAMPLE_TECH_ID, null, null, Collections
                .<NewAttachment> emptyList(), now, null, null, container.getSampleIdentifier()
                .toString()));
        boolean exceptionThrown = false;
        try
        {
            bo.save();
        } catch (UserFailureException ex)
        {
            exceptionThrown = true;
            assertTrue(ex.getMessage().contains("has to be in the same group"));
        }
        assertTrue(exceptionThrown);
        context.assertIsSatisfied();
    }

    @Test
    public final void testEditSampleNoExperimentForSampleWithDatasets()
    {
        final SamplePE sample = new SamplePE();
        sample.setId(SAMPLE_TECH_ID.getId());
        sample.setCode("sampleCode");
        sample.setGroup(EXAMPLE_GROUP);

        Date now = new Date();
        sample.setModificationDate(now);

        prepareTryToLoadOfSampleWithId(sample);
        prepareNoPropertiesToUpdate(sample);
        context.checking(new Expectations()
            {
                {

                    allowing(externalDataDAO).hasExternalData(with(sample));
                    will(returnValue(true));
                }
            });
        boolean exceptionThrown = false;
        try
        {
            createSampleBO().update(
                    new SampleUpdatesDTO(SAMPLE_TECH_ID, null, null, Collections
                            .<NewAttachment> emptyList(), now, null, null, null));
        } catch (UserFailureException ex)
        {
            exceptionThrown = true;
            assertTrue(ex
                    .getMessage()
                    .contains(
                            "from the experiment because there are already datasets attached to the sample"));
        }
        assertTrue(exceptionThrown);
        context.assertIsSatisfied();
    }

    @Test
    public final void testEditStaleSampleFails()
    {
        Date then = new Date(0);
        Date now = new Date();

        SamplePE sample = new SamplePE();
        sample.setModificationDate(then);
        sample.setId(SAMPLE_TECH_ID.getId());

        prepareTryToLoadOfSampleWithId(sample);
        try
        {
            SampleUpdatesDTO updates =
                    new SampleUpdatesDTO(SAMPLE_TECH_ID, null, null, Collections
                            .<NewAttachment> emptyList(), now, null, null, null);
            createSampleBO().update(updates);
        } catch (UserFailureException e)
        {
            return;
        }
        fail("The edition of stale sample should throw an exception");
    }

    @Test(expectedExceptions = UserFailureException.class)
    public final void testFailToDefineSharedSampleWithParentInAGroup()
    {
        final SampleIdentifier sharedSampleIdentifier =
                getSharedSampleIdentifier(DEFAULT_SAMPLE_CODE);
        final NewSample newSharedSample = new NewSample();
        newSharedSample.setIdentifier(sharedSampleIdentifier.toString());
        final SampleType sampleType = createSampleType(SampleTypeCode.DILUTION_PLATE);
        newSharedSample.setSampleType(sampleType);

        final SampleIdentifier parentGroupIdentifier = getGroupSampleIdentifier("SAMPLE_GENERATOR");
        newSharedSample.setParentIdentifier(parentGroupIdentifier.toString());

        newSharedSample.setProperties(IEntityProperty.EMPTY_ARRAY);

        context.checking(new Expectations()
            {
                {
                    one(databaseInstanceDAO).tryFindDatabaseInstanceByCode(DB);
                    will(returnValue(ManagerTestTool.EXAMPLE_DATABASE_INSTANCE));

                    allowing(daoFactory).getSampleDAO();
                    will(returnValue(sampleDAO));

                    ManagerTestTool.prepareFindGroup(this, daoFactory, groupDAO,
                            databaseInstanceDAO);

                    final SamplePE groupParent = new SamplePE();
                    groupParent.setRegistrator(EXAMPLE_PERSON);
                    groupParent.setGroup(EXAMPLE_GROUP);
                    groupParent.setCode("SAMPLE_GENERATOR");
                    one(sampleDAO).tryFindByCodeAndGroup(parentGroupIdentifier.getSampleCode(),
                            EXAMPLE_GROUP);
                    will(returnValue(groupParent));

                    one(sampleTypeDAO).tryFindSampleTypeByCode(DILUTION_PLATE);
                    will(returnValue(BeanUtils.createBean(SampleTypePE.class, sampleType)));

                    one(propertiesConverter).convertProperties(newSharedSample.getProperties(),
                            DILUTION_PLATE, EXAMPLE_PERSON);
                    will(returnValue(new ArrayList<SamplePropertyPE>()));

                    one(daoFactory).getPermIdDAO();
                    will(returnValue(permIdDAO));

                    one(permIdDAO).createPermId();
                    will(returnValue("2009010112341234-1"));
                }
            });

        final SampleBO sampleBO = createSampleBO();
        sampleBO.define(newSharedSample);
        context.assertIsSatisfied();
    }

    @Test
    public void testGetUndefinedSample()
    {
        try
        {
            createSampleBO().getSample();
            fail("UserFailureException expected");
        } catch (final IllegalStateException e)
        {
            assertEquals("Unloaded sample.", e.getMessage());
        }
    }

    @Test
    public void testLoadByGroupSampleIdentifier()
    {
        SampleIdentifier sampleIdentifier = getGroupSampleIdentifier("s1");
        SamplePE sample = new SamplePE();
        prepareTryToLoadOfGroupSample(sampleIdentifier, sample);

        SampleBO sampleBO = createSampleBO();
        sampleBO.loadBySampleIdentifier(sampleIdentifier);

        assertSame(sample, sampleBO.tryToGetSample());
        context.assertIsSatisfied();
    }

    @Test
    public final void testRegisterSampleWithUnknownContainer()
    {
        final NewSample sample = new NewSample();
        sample.setIdentifier(getGroupSampleIdentifier(DEFAULT_SAMPLE_CODE).toString());
        sample.setSampleType(createSampleType(SampleTypeCode.DILUTION_PLATE));
        sample.setContainerIdentifier(getGroupSampleIdentifier("DOES_NOT_EXIST").toString());

        context.checking(new Expectations()
            {
                {
                    ManagerTestTool.prepareFindGroup(this, daoFactory, groupDAO,
                            databaseInstanceDAO);

                    one(sampleTypeDAO).tryFindSampleTypeByCode(
                            SampleTypeCode.DILUTION_PLATE.getCode());
                    will(returnValue(new SampleTypePE()));

                    one(propertiesConverter).convertProperties(IEntityProperty.EMPTY_ARRAY, null,
                            EXAMPLE_PERSON);

                    one(sampleDAO).tryFindByCodeAndGroup("DOES_NOT_EXIST",
                            EXAMPLE_SESSION.tryGetHomeGroup());
                    will(returnValue(null));
                }
            });
        try
        {
            createSampleBO().define(sample);
        } catch (final UserFailureException ex)
        {
            assertEquals("No sample could be found for identifier "
                    + "'MY_DATABASE_INSTANCE:/MY_GROUP/DOES_NOT_EXIST'.", ex.getMessage());
        }
        context.assertIsSatisfied();
    }

    @Test
    public final void testRegisterSampleWithUnknownParent()
    {
        final NewSample sample = new NewSample();
        sample.setIdentifier(getGroupSampleIdentifier(DEFAULT_SAMPLE_CODE).toString());
        sample.setSampleType(createSampleType(SampleTypeCode.DILUTION_PLATE));
        sample.setParentIdentifier(getGroupSampleIdentifier("DOES_NOT_EXIST").toString());

        context.checking(new Expectations()
            {
                {
                    ManagerTestTool.prepareFindGroup(this, daoFactory, groupDAO,
                            databaseInstanceDAO);

                    one(sampleTypeDAO).tryFindSampleTypeByCode(
                            SampleTypeCode.DILUTION_PLATE.getCode());
                    will(returnValue(new SampleTypePE()));

                    one(propertiesConverter).convertProperties(IEntityProperty.EMPTY_ARRAY, null,
                            EXAMPLE_PERSON);

                    one(sampleDAO).tryFindByCodeAndGroup("DOES_NOT_EXIST",
                            EXAMPLE_SESSION.tryGetHomeGroup());
                    will(returnValue(null));
                }
            });
        try
        {
            createSampleBO().define(sample);
        } catch (final UserFailureException ex)
        {
            assertEquals("No sample could be found for identifier "
                    + "'MY_DATABASE_INSTANCE:/MY_GROUP/DOES_NOT_EXIST'.", ex.getMessage());
        }
        context.assertIsSatisfied();
    }

    @Test
    public void testTryToLoadBySampleIdentifier()
    {
        SampleIdentifier sampleIdentifier = getSharedSampleIdentifier("s1");
        SamplePE sample = new SamplePE();
        prepareTryToLoadOfInstanceSample(sampleIdentifier, sample);

        SampleBO sampleBO = createSampleBO();
        sampleBO.tryToLoadBySampleIdentifier(sampleIdentifier);

        assertSame(sample, sampleBO.tryToGetSample());
        context.assertIsSatisfied();
    }

    @Test
    public void testTryToLoadByUnkownSampleIdentifier()
    {
        SampleIdentifier sampleIdentifier = getSharedSampleIdentifier("s1");
        prepareTryToLoadOfInstanceSample(sampleIdentifier, null);

        SampleBO sampleBO = createSampleBO();
        sampleBO.tryToLoadBySampleIdentifier(sampleIdentifier);
        try
        {
            sampleBO.getSample();
            fail("IllegalStateException expected.");
        } catch (IllegalStateException e)
        {
            assertEquals("Unloaded sample.", e.getMessage());
        }

        assertSame(null, sampleBO.tryToGetSample());
        context.assertIsSatisfied();
    }

    private ProjectPE createProject()
    {
        ProjectPE project = new ProjectPE();
        project.setCode("code");
        project.setGroup(EXAMPLE_GROUP);
        return project;
    }

    private final SampleBO createSampleBO()
    {
        return new SampleBO(daoFactory, EXAMPLE_SESSION, propertiesConverter);
    }

    private SampleType createSampleType(final SampleTypeCode sampleTypeCode)
    {
        final SampleType sampleType = new SampleType();
        sampleType.setCode(sampleTypeCode.getCode());
        return sampleType;
    }

    private void prepareExperimentUpdateOnly(final SamplePE sample)
    {
        prepareTryToLoadOfSampleWithId(sample);
        prepareNoPropertiesToUpdate(sample);
        prepareNoDatasetsFound(sample);
    }

    private void prepareNoDatasetsFound(final SamplePE sample)
    {
        context.checking(new Expectations()
            {
                {
                    allowing(externalDataDAO).hasExternalData(with(sample));
                    will(returnValue(false));
                }
            });
    }

    private void prepareNoPropertiesToUpdate(final SamplePE sample)
    {
        context.checking(new Expectations()
            {
                {
                    one(propertiesConverter).updateProperties(sample.getProperties(), null, null,
                            EXAMPLE_PERSON);
                }
            });
    }

    private void prepareTryToLoadOfGroupSample(final SampleIdentifier sampleIdentifier,
            final SamplePE sample)
    {
        context.checking(new Expectations()
            {
                {
                    one(databaseInstanceDAO).tryFindDatabaseInstanceByCode("MY_DATABASE_INSTANCE");
                    DatabaseInstancePE databaseInstance = new DatabaseInstancePE();
                    databaseInstance.setCode("MY_DATABASE_INSTANCE");
                    will(returnValue(databaseInstance));

                    one(groupDAO).tryFindGroupByCodeAndDatabaseInstance(
                            sampleIdentifier.getGroupLevel().getGroupCode(), databaseInstance);
                    GroupPE group = new GroupPE();
                    will(returnValue(group));

                    String sampleCode = sampleIdentifier.getSampleCode();
                    one(sampleDAO).tryFindByCodeAndGroup(sampleCode, group);
                    will(returnValue(sample));
                }
            });
    }

    private void prepareTryToLoadOfInstanceSample(final SampleIdentifier sampleIdentifier,
            final SamplePE sample)
    {
        context.checking(new Expectations()
            {
                {
                    one(databaseInstanceDAO).tryFindDatabaseInstanceByCode(DB);
                    DatabaseInstancePE databaseInstance = new DatabaseInstancePE();
                    databaseInstance.setCode(DB);
                    will(returnValue(databaseInstance));

                    String sampleCode = sampleIdentifier.getSampleCode();
                    one(sampleDAO).tryFindByCodeAndDatabaseInstance(sampleCode, databaseInstance);
                    will(returnValue(sample));
                }
            });
    }

    private void prepareTryToLoadOfSampleWithId(final SamplePE sample)
    {
        context.checking(new Expectations()
            {
                {
                    one(sampleDAO).tryGetByTechId(with(new TechId(sample.getId())),
                            with(any(String[].class)));
                    will(returnValue(sample));
                }
            });
    }

    private void updateSampleExperiment(final TechId sampleId, final SamplePE sample,
            ExperimentIdentifier experimentIdentifier)
    {
        createSampleBO().update(
                new SampleUpdatesDTO(sampleId, null, experimentIdentifier, Collections
                        .<NewAttachment> emptyList(), sample.getModificationDate(), null, null,
                        null));
    }
}
