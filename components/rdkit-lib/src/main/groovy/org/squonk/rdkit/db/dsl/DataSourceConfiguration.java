/*
 * Copyright (c) 2017 Informatics Matters Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.squonk.rdkit.db.dsl;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * Created by timbo on 16/12/2015.
 */
public class DataSourceConfiguration implements IConfiguration {

    final DataSource dataSource;
    final Map props;

    public DataSourceConfiguration(DataSource dataSource, Map props) {
        this.dataSource = dataSource;
        this.props = props;

    }

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public <T> T getProperty(String name) {
        return (T) props.get(name);
    }

    @Override
    public <T> T getProperty(String name, T defaultValue) {
        return (T) props.getOrDefault(name, defaultValue);
    }

}
