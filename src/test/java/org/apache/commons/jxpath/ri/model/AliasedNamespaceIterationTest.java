/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.jxpath.ri.model;

import java.util.Collection;

import org.apache.commons.jxpath.AbstractJXPathTest;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.xml.DocumentContainer;
import org.junit.jupiter.api.Test;

/**
 * Test aliased/doubled XML namespace iteration; JXPATH-125.
 */
class AliasedNamespaceIterationTest extends AbstractJXPathTest {

    protected JXPathContext context;

    protected JXPathContext createContext(final String model) {
        final JXPathContext context = JXPathContext.newContext(createDocumentContainer(model));
        context.registerNamespace("a", "ns");
        return context;
    }

    protected DocumentContainer createDocumentContainer(final String model) {
        final DocumentContainer result = new DocumentContainer(AbstractJXPathTest.class.getResource("IterateAliasedNS.xml"), model);
        return result;
    }

    protected void doTestIterate(final String model) {
        assertXPathPointerIterator(createContext(model), "/a:doc/a:elem", list("/a:doc[1]/a:elem[1]", "/a:doc[1]/a:elem[2]"));
    }

    protected void doTestIterate(final String xpath, final String model, final Collection expected) {
        assertXPathPointerIterator(createContext(model), xpath, expected);
    }

    @Test
    void testIterateDOM() {
        doTestIterate(DocumentContainer.MODEL_DOM);
    }

    @Test
    void testIterateJDOM() {
        doTestIterate(DocumentContainer.MODEL_JDOM);
    }
}
