/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.oodt.grid;

import org.apache.oodt.product.QueryHandler;

/**
 * A product server.
 *
 */
public class ProductServer extends Server {
	/**
	 * Creates a new <code>ProductServer</code> instance.
	 *
	 * @param configuration System configuration.
	 * @param className Class name of query handler.
	 */
	public ProductServer(Configuration configuration, String className) {
		super (configuration, className);
	}

	/** {@inheritDoc} */
	protected String getType() {
		return "product";
	}

	public int hashCode() {
		return super.hashCode() ^ 0xaaaaaaaa;
	}

	public boolean equals(Object obj) {
		return super.equals(obj) && obj instanceof ProductServer;
	}

	public String toString() {
		return "ProductServer[" + super.toString() + "]";
	}

	/**
	 * Create a query handler from this server.
	 *
	 * @return a <code>QueryHandler</code> value.
	 * @throws ClassNotFoundException if the class can't be found.
	 * @throws InstantiationException if the handler can't be instantiated.
	 * @throws IllegalAccessException if the handler has no public constructor.
	 */
	public QueryHandler createQueryHandler() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		return (QueryHandler) createHandler();
	}
}
