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

package ch.systemsx.cisd.openbis.generic.client.web.server.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.systemsx.cisd.common.evaluator.Evaluator;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.CustomFilterInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ParameterWithValue;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;

/**
 * Utility class containing functions helpful with dealing with filters.
 * 
 * @author Izabela Adamczyk
 */
public class FilterUtils
{
    public static final class Row
    {
        private final Map<String, String> map = new HashMap<String, String>();
        
        void put(String columnID, String value)
        {
            map.put(columnID, value);
        }
        
        public String get(String columnID)
        {
            return map.get(columnID);
        }

        @Override
        public String toString()
        {
            return map.toString();
        }
        
    }
    
    /**
     * Applies the filter described by <code>customFilterInfo</code> to
     * <code>allRows<code> and adds the result to the
     * <code>filterdRows<code>.
     */
    public static <T> void applyCustomFilter(final List<T> allRows,
            CustomFilterInfo<T> customFilterInfo, List<T> filterdRows)
    {
        String expression = customFilterInfo.getExpression();
        for (ParameterWithValue pw : customFilterInfo.getParameters())
        {
            expression = substituteParameter(expression, pw.getParameter(), pw.getValue());
        }
        Map<String, String> columnVariables = new HashMap<String, String>();
//        expression = substituteColumnsWithVariables(customFilterInfo, expression, columnVariables);
        Evaluator e = new Evaluator(expression, Math.class, null);
        Row row = new Row();
        e.set("row", row);
        for (T rowData : allRows)
        {
            for (IColumnDefinition<T> col : customFilterInfo.getColumns())
            {
                String value = col.getValue(rowData);
                row.put(col.getIdentifier(), value);
            }
            System.out.println(row);
            if (e.evalToBoolean())
            {
                filterdRows.add(rowData);
            }
        }
    }

    private static <T> String substituteColumnsWithVariables(CustomFilterInfo<T> customFilterInfo,
            String expression, Map<String, String> columnVariables)
    {
        String result = expression;
        int varCount = 0;
        for (IColumnDefinition<T> col : customFilterInfo.getColumns())
        {
            String var = "var" + varCount;
            varCount++;
            columnVariables.put(col.getIdentifier(), var);
            result =
                    expression.replaceAll(Pattern.quote("col(" + col.getIdentifier() + ")"),
                            Matcher.quoteReplacement(var));
        }
        return result;
    }

    private static String substituteParameter(String expression, String p, String value)
    {
        String substParameter = "${" + p + "}";
        String quotedParameter = Pattern.quote(substParameter);
        String quotedReplacement = Matcher.quoteReplacement(value);
        return expression.replaceAll(quotedParameter, quotedReplacement);
    }
}
