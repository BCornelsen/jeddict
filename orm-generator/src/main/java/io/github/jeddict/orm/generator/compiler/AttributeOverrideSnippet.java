/**
 * Copyright 2013-2018 the original author or authors from the Jeddict project (https://jeddict.github.io/).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package io.github.jeddict.orm.generator.compiler;

import static io.github.jeddict.jcode.JPAConstants.ATTRIBUTE_OVERRIDE;
import static io.github.jeddict.jcode.JPAConstants.ATTRIBUTE_OVERRIDE_FQN;
import static io.github.jeddict.jcode.JPAConstants.COLUMN_FQN;
import static io.github.jeddict.orm.generator.util.ORMConverterUtil.AT;
import static io.github.jeddict.orm.generator.util.ORMConverterUtil.CLOSE_PARANTHESES;
import static io.github.jeddict.orm.generator.util.ORMConverterUtil.OPEN_PARANTHESES;
import static java.util.Arrays.asList;
import java.util.Collection;
import static org.apache.commons.lang.StringUtils.isBlank;

public class AttributeOverrideSnippet implements Snippet {

    private String name = null;

    private ColumnDefSnippet columnDef = null;

    public ColumnDefSnippet getColumnDef() {
        return columnDef;
    }

    public void setColumnDef(ColumnDefSnippet columnDef) {
        this.columnDef = columnDef;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getSnippet() throws InvalidDataException {
        if (isBlank(name) || columnDef == null) {
            throw new InvalidDataException("Name and ColumnDef required");
        }

        StringBuilder builder = new StringBuilder(AT);
        builder.append(ATTRIBUTE_OVERRIDE)
                .append(OPEN_PARANTHESES)
                .append(buildString("name", name))
                .append(buildSnippet("column", columnDef));

        return builder.substring(0, builder.length() - 1) + CLOSE_PARANTHESES;
    }

    @Override
    public Collection<String> getImportSnippets() throws InvalidDataException {
        return asList(
                ATTRIBUTE_OVERRIDE_FQN,
                COLUMN_FQN
        );
    }
}
