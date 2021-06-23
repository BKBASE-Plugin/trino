/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.prestosql.plugin.ignite;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.prestosql.Session;
import io.prestosql.plugin.tpch.TpchPlugin;
import io.prestosql.testing.DistributedQueryRunner;
import io.prestosql.testing.QueryRunner;
import io.prestosql.tpch.TpchTable;

import java.util.HashMap;
import java.util.Map;

import static io.airlift.testing.Closeables.closeAllSuppress;
import static io.prestosql.plugin.tpch.TpchMetadata.TINY_SCHEMA_NAME;
import static io.prestosql.testing.QueryAssertions.copyTpchTables;
import static io.prestosql.testing.TestingSession.testSessionBuilder;

public class IgniteQueryRunner
{
    private IgniteQueryRunner() {}

    private static final String TPCH_SCHEMA = "tpch";

    public static QueryRunner createMySqlQueryRunner(TestingIgniteServer server, TpchTable<?>... tables)
            throws Exception
    {
        return createIgniteQueryRunner(server, ImmutableMap.of(), ImmutableList.copyOf(tables));
    }

    public static QueryRunner createIgniteQueryRunner(TestingIgniteServer server, Map<String, String> connectorProperties, Iterable<TpchTable<?>> tables)
            throws Exception
    {
        DistributedQueryRunner queryRunner = DistributedQueryRunner.builder(createSession()).build();
        try {
            queryRunner.installPlugin(new TpchPlugin());
            queryRunner.createCatalog("tpch", "tpch");

            connectorProperties = new HashMap<>(ImmutableMap.copyOf(connectorProperties));
            connectorProperties.putIfAbsent("connection-url", TestingIgniteServer.JDBC_URL);
            connectorProperties.putIfAbsent("connection-user", TestingIgniteServer.USER);
            connectorProperties.putIfAbsent("connection-password", TestingIgniteServer.PASSWORD);
            connectorProperties.putIfAbsent("allow-drop-table", "true");

            queryRunner.installPlugin(new IgnitePlugin());
            queryRunner.createCatalog("ignite-test", "ignite", connectorProperties);

            copyTpchTables(queryRunner, "tpch", TINY_SCHEMA_NAME, createSession(), tables);

            return queryRunner;
        }
        catch (Throwable e) {
            closeAllSuppress(e, queryRunner);
            throw e;
        }
    }

    private static Session createSession()
    {
        return testSessionBuilder()
                .setCatalog("ignite-test")
                .setSchema(TPCH_SCHEMA)
                .build();
    }
}
