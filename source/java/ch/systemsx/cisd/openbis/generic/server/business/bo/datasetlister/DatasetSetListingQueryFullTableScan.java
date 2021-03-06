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

package ch.systemsx.cisd.openbis.generic.server.business.bo.datasetlister;

import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.Iterator;

import net.lemnik.eodsql.DataIterator;

import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.iterators.FilterIterator;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.exceptions.NotImplementedException;

/**
 * An implementation of {@link IDatasetSetListingQuery} which gets all all rows and then filters
 * them down by sample id. This will be a faster way of getting the datasets then getting them one
 * by one (as {@link DatasetSetListingQueryOneByOne} does) when a the requested datasets are a
 * considerable part of all datasets.
 * 
 * @author Tomasz Pylak
 */
@Friend(toClasses =
    { DatasetRecord.class, IDatasetListingQuery.class })
class DatasetSetListingQueryFullTableScan implements IDatasetSetListingQuery
{
    private final long databaseInstanceId;

    private final IDatasetListingQuery query;

    public DatasetSetListingQueryFullTableScan(final IDatasetListingQuery query,
            final long databaseInstanceId)
    {
        this.query = query;
        this.databaseInstanceId = databaseInstanceId;
    }

    public Iterable<DatasetRecord> getDatasets(final LongSet datasetIds)
    {
        return new Iterable<DatasetRecord>()
            {
                public Iterator<DatasetRecord> iterator()
                {
                    return new FilterIterator<DatasetRecord>(query.getDatasets(databaseInstanceId),
                            new Predicate<DatasetRecord>()
                                {
                                    public boolean evaluate(DatasetRecord dataset)
                                    {
                                        return datasetIds.contains(dataset.id);
                                    }
                                });
                }
            };
    }

    // TODO 2009-09-01, Tomasz Pylak: implement me! (h2)
    public DataIterator<Long> getDatasetChildrenIds(LongSet entityIds)
    {
        throw new NotImplementedException();
    }

}
