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

import net.lemnik.eodsql.DataIterator;
import net.lemnik.eodsql.Select;
import net.lemnik.eodsql.TransactionQuery;

import ch.rinn.restrictions.Friend;
import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.CodeRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.GenericEntityPropertyRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.IPropertyListingQuery;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.MaterialEntityPropertyRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.VocabularyTermRecord;

/**
 * A {@link TransactionQuery} interface for obtaining large sets of dataset-related entities from
 * the database.
 * <p>
 * This interface is intended to be used only in this package. The <code>public</code> modifier is
 * needed for creating a dynamic proxy by the EOD SQL library.
 * 
 * @author Tomasz Pylak
 */
@Private
@Friend(toClasses =
    { DataStoreRecord.class })
public interface IDatasetListingQuery extends TransactionQuery, IPropertyListingQuery
{
    public static final int FETCH_SIZE = 1000;

    /**
     * Returns the datasets for the given experiment id.
     */
    @Select(sql = "select * from data join external_data on data.id = external_data.data_id where data.expe_id=?{1}", fetchSize = FETCH_SIZE)
    public DataIterator<DatasetRecord> getDatasetsForExperiment(long experimentId);

    /**
     * Returns the directly connected datasets for the given sample id.
     */
    @Select(sql = "select * from data join external_data on data.id = external_data.data_id where data.samp_id=?{1}", fetchSize = FETCH_SIZE)
    public DataIterator<DatasetRecord> getDatasetsForSample(long sampleId);

    /**
     * Returns the directly connected dataset ids for the given sample id.
     */
    @Select(sql = "select id from data where data.samp_id=?{1}", fetchSize = FETCH_SIZE)
    public DataIterator<Long> getDatasetIdsForSample(long sampleId);

    /**
     * Returns the datasets that are children of a dataset with given id.
     */
    @Select(sql = "SELECT * FROM data JOIN external_data ON data.id = external_data.data_id"
            + "    WHERE data.id IN (SELECT data_id_child FROM data_set_relationships r WHERE r.data_id_parent=?{1})", fetchSize = FETCH_SIZE)
    public DataIterator<DatasetRecord> getChildDatasetsForParent(long parentDatasetId);

    /**
     * Returns the datasets that are parents of a dataset with given id.
     */
    @Select(sql = "SELECT * FROM data JOIN external_data ON data.id = external_data.data_id"
            + "    WHERE data.id IN (SELECT data_id_parent FROM data_set_relationships r WHERE r.data_id_child=?{1})", fetchSize = FETCH_SIZE)
    public DataIterator<DatasetRecord> getParentDatasetsForChild(long childDatasetId);

    /**
     * Returns the datasets for the given <var>datasetId</var>.
     */
    @Select("select * from data d join external_data e on d.id = e.data_id" + " where d.id=?{1}")
    public DatasetRecord getDataset(long datasetId);

    /**
     * Returns all datasets in the database.
     */
    @Select(sql = "select * from data d join external_data e on d.id = e.data_id"
            + "     where (select dbin_id from data_set_types t where t.id = d.dsty_id) = ?{1}", fetchSize = FETCH_SIZE)
    public DataIterator<DatasetRecord> getDatasets(long dbInstanceId);

    @Select(sql = "select id, code from data_set_types where dbin_id=?{1}")
    public CodeRecord[] getDatasetTypes(long databaseInstanceId);

    @Select(sql = "select id, code, download_url from data_stores where dbin_id=?{1}")
    public DataStoreRecord[] getDataStores(long databaseInstanceId);

    @Select(sql = "select id, code from file_format_types where dbin_id=?{1}")
    public CodeRecord[] getFileFormatTypes(long databaseInstanceId);

    @Select(sql = "select id, code from locator_types")
    public CodeRecord[] getLocatorTypes();

    // ------------- Properties

    /**
     * Returns all generic property values of the sample with <var>entityId</var>.
     */
    @Select("select pr.ds_id as entity_id, etpt.prty_id, pr.value from data_set_properties pr"
            + "      join data_set_type_property_types etpt on pr.dstpt_id=etpt.id"
            + "   where pr.value is not null and pr.ds_id=?{1}")
    public DataIterator<GenericEntityPropertyRecord> getEntityPropertyGenericValues(long entityId);

    /**
     * Returns all generic property values of all samples.
     */
    @Select(sql = "select pr.ds_id as entity_id, etpt.prty_id, pr.value from data_set_properties pr"
            + "      join data_set_type_property_types etpt on pr.dstpt_id=etpt.id"
            + "      join property_types pt on etpt.prty_id=pt.id"
            + "   where pr.value is not null and pt.dbin_id=?{1}", fetchSize = FETCH_SIZE)
    public DataIterator<GenericEntityPropertyRecord> getAllEntityPropertyGenericValues(
            long dbInstanceId);

    /**
     * Returns all controlled vocabulary property values of the sample with <var>sampleId</var>.
     */
    @Select("select pr.ds_id as entity_id, etpt.prty_id, cvte.id, cvte.covo_id, cvte.code, cvte.label"
            + "      from data_set_properties pr"
            + "      join data_set_type_property_types etpt on pr.dstpt_id=etpt.id"
            + "      join controlled_vocabulary_terms cvte on pr.cvte_id=cvte.id"
            + "   where pr.ds_id=?{1}")
    public DataIterator<VocabularyTermRecord> getEntityPropertyVocabularyTermValues(long entityId);

    /**
     * Returns all controlled vocabulary property values of all samples.
     */
    @Select(sql = "select pr.ds_id as entity_id, etpt.prty_id, cvte.id, cvte.covo_id, cvte.code, cvte.label"
            + "      from data_set_properties pr"
            + "      join data_set_type_property_types etpt on pr.dstpt_id=etpt.id"
            + "      join controlled_vocabulary_terms cvte on pr.cvte_id=cvte.id"
            + "      join property_types pt on etpt.prty_id=pt.id"
            + "    where pt.dbin_id=?{1}        ", fetchSize = FETCH_SIZE)
    public DataIterator<VocabularyTermRecord> getAllEntityPropertyVocabularyTermValues(
            long dbInstanceId);

    /**
     * Returns all material-type property values of the sample with <var>sampleId</var>
     */
    @Select("select pr.ds_id as entity_id, etpt.prty_id, m.id, m.code, m.maty_id"
            + "      from data_set_properties pr"
            + "      join data_set_type_property_types etpt on pr.dstpt_id=etpt.id"
            + "      join materials m on pr.mate_prop_id=m.id where pr.ds_id=?{1}")
    public DataIterator<MaterialEntityPropertyRecord> getEntityPropertyMaterialValues(long entityId);

    /**
     * Returns all material-type property values of all samples.
     */
    @Select(sql = "select pr.ds_id as entity_id, etpt.prty_id, m.id, m.code, m.maty_id"
            + "      from data_set_properties pr"
            + "      join data_set_type_property_types etpt on pr.dstpt_id=etpt.id"
            + "      join materials m on pr.mate_prop_id=m.id"
            + "      join property_types pt on etpt.prty_id=pt.id"
            + "    where pt.dbin_id=?{1}    ", fetchSize = FETCH_SIZE)
    public DataIterator<MaterialEntityPropertyRecord> getAllEntityPropertyMaterialValues(
            long dbInstanceId);
}
