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

package ch.systemsx.cisd.openbis.generic.shared.dto.types;

/**
 * The current <code>SampleType</code> codes.
 * <p>
 * This enumeration should reflect the values in the database and is <i>Unit</i> tested to ensure
 * this point.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public enum SampleTypeCode
{
    CELL_PLATE("CELL_PLATE", true, "cell plate"), 
    CONTROL_LAYOUT("CONTROL_LAYOUT", false, "control layout"), 
    DILUTION_PLATE("DILUTION_PLATE", true, "dilution plate"),
    MASTER_PLATE("MASTER_PLATE", false, "master plate"), 
    REINFECT_PLATE("REINFECT_PLATE", true, "reinfection plate"),
    WELL("WELL", false, "well");

    private final String code;

    private final boolean parentRequired;

    private final String description;

    private SampleTypeCode(final String code, final boolean parentRequired, final String description)
    {
        this.code = code;
        this.parentRequired = parentRequired;
        this.description = description;
    }

    public final String getCode()
    {
        return code;
    }

    public final boolean isParentRequired()
    {
        return parentRequired;
    }

    public final String getDescription()
    {
        return description;
    }

    /** For given <var>typeCode</var> returns the corresponding <code>SampleTypeCode</code>. */
    public final static SampleTypeCode getSampleTypeCode(final String typeCode)
    {
        assert typeCode != null : "Unspecified sample type code.";
        for (final SampleTypeCode sampleTypeCode : values())
        {
            if (sampleTypeCode.code.equalsIgnoreCase(typeCode))
            {
                return sampleTypeCode;
            }
        }
        throw new IllegalArgumentException(String.format("No sample type for given code '%s'.",
                typeCode));
    }
}
