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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;

/**
 * Utility routines for DAOs.
 * 
 * @author Bernd Rinn
 */
final class DAOUtils
{

    private DAOUtils()
    {
        // Cannot be instantiated
    }

    /**
     * Don't try to get properties for more than 10000 entities.
     */
    final static int MAX_COUNT_FOR_PROPERTIES = 10000;

    /**
     * Returns the number of entities that the given <var>critera</var> will return.
     */
    static int getCount(final Criteria criteria)
    {
        int count = (Integer) criteria.setProjection(Projections.rowCount()).uniqueResult();
        // Undo the rowCount projection
        criteria.setProjection(null);
        criteria.setResultTransformer(Criteria.ROOT_ENTITY);
        return count;
    }

}
