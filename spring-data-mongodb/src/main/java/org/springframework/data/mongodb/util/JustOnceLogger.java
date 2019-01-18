/*
 * Copyright 2019 the original author or authors.
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
package org.springframework.data.mongodb.util;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Strobl
 * @since 2.2
 */
public class JustOnceLogger {

	private static final Map<Class<?>, Set<String>> KNOWN_LOGS = new ConcurrentHashMap<>();
	private static final String AUTO_INDEX_CREATION_DEPRECATED;

	static {
		AUTO_INDEX_CREATION_DEPRECATED = "Automatic index creation has been deprecated as of Spring Data MongoDB 2.2 and is scheduled for removal.\n"
				+ "\tTo turn OFF this feature please set 'MongoMappingContext#setAutoIndexCreation(false)' or override 'MongoConfigurationSupport#autoIndexCreation()'.\n"
				+ "\tHowever, derivation of index definitions from annotations remains in the codebase. So you may still use this feature eg. in an application ready block.\n"
				+ "\n" //
				+ "\t> -----------------------------------------------------------------------------------------\n"
				+ "\t> @EventListener(ApplicationReadyEvent.class)\n" //
				+ "\t> public void initIndicesAfterStartup() {\n" //
				+ "\t>\n" //
				+ "\t>     IndexOperations indexOps = mongoTemplate.indexOps(DomainType.class);\n" //
				+ "\t>\n" //
				+ "\t>     IndexResolver resolver = new MongoPersistentEntityIndexResolver(mongoMappingContext);\n"
				+ "\t>     resolver.resolveIndexFor(DomainType.class).forEach(indexOps::ensureIndex);\n" //
				+ "\t> }\n" //
				+ "\t> -----------------------------------------------------------------------------------------\n";
	}

	public static void logWarnIndexCreationDeprecated(Class<?> logSource) {
		warnOnce(logSource, AUTO_INDEX_CREATION_DEPRECATED);
	}

	public static void warnOnce(Class<?> logSource, String message) {

		Logger logger = LoggerFactory.getLogger(logSource);
		if (!logger.isWarnEnabled()) {
			return;
		}

		if (!KNOWN_LOGS.containsKey(logSource)) {

			KNOWN_LOGS.put(logSource, new ConcurrentSkipListSet<>(Collections.singleton(message)));
			logger.warn(message);
		} else {

			Set<String> messages = KNOWN_LOGS.get(logSource);
			if (messages.contains(message)) {
				return;
			}

			messages.add(message);
			logger.warn(message);
		}
	}
}
