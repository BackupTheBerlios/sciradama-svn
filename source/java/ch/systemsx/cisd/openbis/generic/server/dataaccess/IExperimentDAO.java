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

package ch.systemsx.cisd.openbis.generic.server.dataaccess;

import java.util.List;

import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;

/**
 * <i>Data Access Object</i> for {@link ExperimentPE}.
 * 
 * @author    Franz-Josef Elmer
 */
public interface IExperimentDAO extends IGenericDAO<ExperimentPE>
{
    /**
     * Lists all belonging to given <code>project</code>. Fetches also properties.
     */
    public List<ExperimentPE> listExperimentsWithProperties(final ProjectPE project)
            throws DataAccessException;

    /**
     * Lists all of given <code>type</code> belonging to given <code>project</code>. Fetches also
     * properties.
     */
    public List<ExperimentPE> listExperimentsWithProperties(final ExperimentTypePE experimentType,
            final ProjectPE project) throws DataAccessException;

    /**
     * Lists all registered experiments. Doesn't fetch properties.
     */
    public List<ExperimentPE> listExperiments() throws DataAccessException;

    /**
     * Returns {@link ExperimentPE} defined by given project and experiment code.
     */
    public ExperimentPE tryFindByCodeAndProject(ProjectPE project, String experimentCode);

    /**
     * Lists experiments (with minimal additional information) belonging to the given
     * <code>project</code> and having a property with the specified value.
     */
    public List<ExperimentPE> listExperimentsByProjectAndProperty(final String propertyCode,
            final String propertyValue, final ProjectPE project) throws DataAccessException;

    /**
     * Inserts given {@link ExperimentPE} into the database.
     */
    public void createExperiment(ExperimentPE experiment);
}
