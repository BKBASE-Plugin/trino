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
package io.trino.plugin.ignite;

import com.google.common.collect.ImmutableMap;
import io.trino.plugin.jdbc.JdbcConnectorFactory;
import io.trino.spi.connector.Connector;
import io.trino.spi.connector.ConnectorFactory;
import io.trino.testing.TestingConnectorContext;
import org.testng.annotations.Test;

import static com.google.common.collect.Iterables.getOnlyElement;
import static io.airlift.testing.Assertions.assertInstanceOf;
import static org.testng.Assert.assertNotNull;

public class TestIgnitePlugin
{
    @Test
    public void testStartup()
    {
        IgnitePlugin plugin = new IgnitePlugin();

        ConnectorFactory factory = getOnlyElement(plugin.getConnectorFactories());
        assertInstanceOf(factory, JdbcConnectorFactory.class);

        Connector c = factory.create(
                "test-connector",
                ImmutableMap.of("connection-url", "jdbc:ignite:thin://localhost:10080", "connection-user", "root", "connection-password", "test|test"),
                new TestingConnectorContext());
        assertNotNull(c);
    }
}
