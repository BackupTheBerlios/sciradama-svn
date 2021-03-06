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

package ch.systemsx.cisd.openbis.generic.server.business.bo.common.entity;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.sql.Connection;

import net.lemnik.eodsql.QueryTool;

import org.springframework.dao.EmptyResultDataAccessException;

import ch.rinn.restrictions.Friend;
import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.DatabaseContextUtils;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.QueryStrategyChooser;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.QueryStrategyChooser.IEntitiesCountProvider;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.PersistencyResources;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Group;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Invalidation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.translator.DatabaseInstanceTranslator;

/**
 * @author Tomasz Pylak
 */
@Friend(toClasses =
    { SampleReferenceRecord.class, ExperimentProjectGroupCodeRecord.class,
            ISecondaryEntityListingQuery.class })
public class SecondaryEntityDAO
{
    interface ISecondaryEntitySetListingQuery
    {
        Iterable<SampleReferenceRecord> getSamples(LongSet sampleIds);
    }

    /**
     * Creates a new instance based on {@link PersistencyResources} and home
     * {@link DatabaseInstancePE} of specified DAO factory.
     */
    public static SecondaryEntityDAO create(IDAOFactory daoFactory)
    {
        Connection connection = DatabaseContextUtils.getConnection(daoFactory);
        ISecondaryEntityListingQuery query =
                QueryTool.getQuery(connection, ISecondaryEntityListingQuery.class);
        return create(daoFactory, query);
    }

    @Private
    public static SecondaryEntityDAO create(IDAOFactory daoFactory,
            ISecondaryEntityListingQuery query)
    {
        final boolean supportsSetQuery = DatabaseContextUtils.isSupportingSetQueries(daoFactory);
        return new SecondaryEntityDAO(supportsSetQuery, query, daoFactory.getHomeDatabaseInstance());
    }

    private final ISecondaryEntitySetListingQuery setQuery;

    private final ISecondaryEntityListingQuery query;

    private final DatabaseInstance databaseInstance;

    private SecondaryEntityDAO(boolean supportsSetQuery, final ISecondaryEntityListingQuery query,
            final DatabaseInstancePE databaseInstancePE)
    {
        this.query = query;
        this.setQuery = createSetQuery(supportsSetQuery, query, databaseInstancePE.getId());
        this.databaseInstance = DatabaseInstanceTranslator.translate(databaseInstancePE);
    }

    private static ISecondaryEntitySetListingQuery createSetQuery(boolean supportsSetQuery,
            ISecondaryEntityListingQuery query, long databaseInstanceId)
    {
        if (supportsSetQuery)
        {
            return asDatasetSetListingQuery(query);
        } else
        {
            QueryStrategyChooser strategyChooser = createStrategyChooser(query, databaseInstanceId);
            return new SecondaryEntitySetListingQueryFallback(query, strategyChooser,
                    databaseInstanceId);
        }
    }

    private static ISecondaryEntitySetListingQuery asDatasetSetListingQuery(
            final ISecondaryEntityListingQuery query)
    {
        return new ISecondaryEntitySetListingQuery()
            {
                public Iterable<SampleReferenceRecord> getSamples(LongSet sampleIds)
                {
                    return query.getSamples(sampleIds);
                }
            };
    }

    private static QueryStrategyChooser createStrategyChooser(
            final ISecondaryEntityListingQuery query, final long databaseInstanceId)
    {
        return new QueryStrategyChooser(new IEntitiesCountProvider()
            {
                public long count()
                {
                    return query.getSampleCount(databaseInstanceId);
                }
            });
    }

    public Experiment getExperiment(final long experimentId)
    {
        final ExperimentProjectGroupCodeRecord record =
                query.getExperimentAndProjectAndGroupCodeForId(experimentId);
        if (record == null)
        {
            throw new EmptyResultDataAccessException(1);
        }
        return createExperiment(experimentId, record);
    }

    private Experiment createExperiment(final long experimentId,
            final ExperimentProjectGroupCodeRecord record)
    {
        final Group group = new Group();
        group.setCode(escapeHtml(record.g_code));
        group.setInstance(databaseInstance);

        final Experiment experiment = new Experiment();
        experiment.setId(experimentId);
        experiment.setCode(escapeHtml(record.e_code));
        experiment.setIdentifier(new ExperimentIdentifier(null, group.getCode(), record.p_code,
                record.e_code).toString());
        final Project project = new Project();
        project.setCode(escapeHtml(record.p_code));
        project.setGroup(group);
        experiment.setProject(project);
        final ExperimentType experimentType = new ExperimentType();
        experimentType.setCode(escapeHtml(record.et_code));
        experiment.setExperimentType(experimentType);
        return experiment;
    }

    public Person getPerson(long personId)
    {
        Person registrator = query.getPersonById(personId);
        if (registrator == null)
        {
            throw new EmptyResultDataAccessException(1);
        }
        registrator.setUserId(escapeHtml(registrator.getUserId()));
        registrator.setEmail(escapeHtml(registrator.getEmail()));
        registrator.setFirstName(escapeHtml(registrator.getFirstName()));
        registrator.setLastName(escapeHtml(registrator.getLastName()));
        return registrator;
    }

    public Group[] getAllGroups(long databaseInstanceId)
    {
        return query.getAllGroups(databaseInstanceId);
    }

    public long getGroupIdForCode(String groupCode)
    {
        return query.getGroupIdForCode(groupCode);
    }

    public Long2ObjectMap<Sample> getSamples(LongSet sampleIds)
    {
        final Iterable<SampleReferenceRecord> sampleRecords = setQuery.getSamples(sampleIds);
        Long2ObjectMap<Sample> result = new Long2ObjectOpenHashMap<Sample>();
        for (SampleReferenceRecord record : sampleRecords)
        {
            result.put(record.id, createSample(record, databaseInstance));
        }
        return result;
    }

    private static Sample createSample(SampleReferenceRecord record,
            DatabaseInstance databaseInstance)
    {
        Sample sample = new Sample();
        sample.setId(record.id);
        sample.setCode(escapeHtml(record.s_code));
        sample.setSampleType(createSampleType(record.st_code, databaseInstance));
        sample.setInvalidation(createInvalidation(record.inva_id));
        sample.setGroup(tryCreateGroup(record.g_code, databaseInstance));
        sample.setDatabaseInstance(tryGetDatabaseInstance(record.g_code, databaseInstance));
        sample.setPermId(escapeHtml(record.perm_id));
        sample.setIdentifier(escapeHtml(createIdentifier(sample).toString()));
        return sample;
    }

    private static SampleIdentifier createIdentifier(Sample sample)
    {
        Group group = sample.getGroup();
        if (group != null)
        {
            GroupIdentifier groupIdentifier =
                    new GroupIdentifier(group.getInstance().getCode(), group.getCode());
            return new SampleIdentifier(groupIdentifier, sample.getCode());
        } else
        {
            DatabaseInstanceIdentifier instanceIdentifier =
                    new DatabaseInstanceIdentifier(sample.getDatabaseInstance().getCode());
            return new SampleIdentifier(instanceIdentifier, sample.getCode());
        }
    }

    private static DatabaseInstance tryGetDatabaseInstance(String groupCodeOrNull,
            DatabaseInstance databaseInstance)
    {
        if (groupCodeOrNull == null)
        {
            return databaseInstance;
        } else
        {
            return null;
        }
    }

    private static Group tryCreateGroup(String codeOrNull, DatabaseInstance databaseInstance)
    {
        if (codeOrNull == null)
        {
            return null;
        } else
        {
            Group group = new Group();
            group.setCode(escapeHtml(codeOrNull));
            group.setInstance(databaseInstance);
            return group;
        }
    }

    private static Invalidation createInvalidation(Long invalidationIdOrNull)
    {
        if (invalidationIdOrNull == null)
        {
            return null;
        } else
        {
            return new Invalidation();
        }
    }

    private static SampleType createSampleType(String code, DatabaseInstance databaseInstance)
    {
        SampleType sampleType = new SampleType();
        sampleType.setCode(escapeHtml(code));
        sampleType.setDatabaseInstance(databaseInstance);
        return sampleType;
    }
}
