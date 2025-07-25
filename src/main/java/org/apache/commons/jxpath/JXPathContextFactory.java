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

package org.apache.commons.jxpath;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.commons.jxpath.util.ClassLoaderUtil;
import org.apache.commons.lang3.SystemProperties;

/**
 * Defines a factory API that enables applications to obtain a {@link JXPathContext} instance. To acquire a JXPathContext, first call the static
 * {@link #newInstance} method of JXPathContextFactory. This method returns a concrete JXPathContextFactory. Then call {@link #newContext} on that instance. You
 * will rarely need to perform these steps explicitly: usually you can call one of the {@code JXPathContex.newContext} methods, which will perform these steps
 * for you.
 *
 * @see JXPathContext#newContext(Object)
 * @see JXPathContext#newContext(JXPathContext,Object)
 */
public abstract class JXPathContextFactory {

    /** The default property */
    public static final String FACTORY_NAME_PROPERTY = "org.apache.commons.jxpath.JXPathContextFactory";

    /** The default factory class */
    private static final String DEFAULT_FACTORY_CLASS = "org.apache.commons.jxpath.ri.JXPathContextFactoryReferenceImpl";

    /**
     * Avoid reading all the files when the findFactory method is called the second time ( cache the result of finding the default impl )
     */
    private static final String FACTORY_IMPL_NAME = findFactory(FACTORY_NAME_PROPERTY, DEFAULT_FACTORY_CLASS);

    /**
     * Temp debug code - this will be removed after we test everything
     */
    private static boolean debug;

    static {
        debug = SystemProperties.getProperty("jxpath.debug") != null;
    }

    /**
     * Private implementation method - will find the implementation class in the specified order.
     *
     * @param property       Property name
     * @param defaultFactory Default implementation, if nothing else is found
     * @return class name of the JXPathContextFactory
     */
    private static String findFactory(final String property, final String defaultFactory) {
        // Use the factory ID system property first
        final String systemProp = SystemProperties.getProperty(property);
        if (systemProp != null) {
            if (debug) {
                System.err.println("JXPath: found system property" + systemProp);
            }
            return systemProp;
        }
        // try to read from $java.home/lib/xml.properties
        try {
            final Path javaHome = Paths.get(SystemProperties.getJavaHome());
            final Path configFile = javaHome.resolve(Paths.get("lib", "jxpath.properties"));
            if (Files.exists(configFile)) {
                final Properties props = new Properties();
                try (InputStream fis = Files.newInputStream(configFile)) {
                    props.load(fis);
                }
                final String factory = props.getProperty(property);
                if (factory != null) {
                    if (debug) {
                        System.err.println("JXPath: found java.home property " + factory);
                    }
                    return factory;
                }
            }
        } catch (final IOException ex) {
            if (debug) {
                ex.printStackTrace();
            }
        }
        final String serviceId = "META-INF/services/" + property;
        // try to find services in CLASSPATH
        try {
            final ClassLoader cl = JXPathContextFactory.class.getClassLoader();
            try (InputStream is = cl == null ? ClassLoader.getSystemResourceAsStream(serviceId) : cl.getResourceAsStream(serviceId)) {
                if (is != null) {
                    if (debug) {
                        System.err.println("JXPath: found  " + serviceId);
                    }
                    final BufferedReader rd = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                    final String factory = rd.readLine();
                    if (factory != null && !"".equals(factory)) {
                        if (debug) {
                            System.err.println("JXPath: loaded from services: " + factory);
                        }
                        return factory;
                    }
                }
            }
        } catch (final Exception ex) {
            if (debug) {
                ex.printStackTrace();
            }
        }
        return defaultFactory;
    }
    // This code is duplicated in all factories.
    // Keep it in sync or move it to a common place
    // Because it's small probably it's easier to keep it here

    /**
     * Obtain a new instance of a {@code JXPathContextFactory}. This static method creates a new factory instance. This method uses the following ordered lookup
     * procedure to determine the {@code JXPathContextFactory} implementation class to load:
     * <ul>
     * <li>Use the {@code org.apache.commons.jxpath.JXPathContextFactory} system property.</li>
     * <li>Alternatively, use the JAVA_HOME (the parent directory where jdk is installed)/lib/jxpath.properties for a property file that contains the name of
     * the implementation class keyed on {@code org.apache.commons.jxpath.JXPathContextFactory}.</li>
     * <li>Use the Services API (as detailed in the JAR specification), if available, to determine the class name. The Services API will look for a class name
     * in the file {@code META- INF/services/<i>org.apache.commons.jxpath.
     * JXPathContextFactory</i>} in jars available to the runtime.</li>
     * <li>Platform default {@code JXPathContextFactory} instance.</li>
     * </ul>
     *
     * Once an application has obtained a reference to a {@code JXPathContextFactory} it can use the factory to obtain JXPathContext instances.
     *
     * @return JXPathContextFactory
     * @throws JXPathContextFactoryConfigurationError if the implementation is not available or cannot be instantiated.
     */
    public static JXPathContextFactory newInstance() {
        JXPathContextFactory factoryImpl;
        try {
            factoryImpl = ClassLoaderUtil.<JXPathContextFactory>getClass(FACTORY_IMPL_NAME, true).getConstructor().newInstance();
        } catch (final ReflectiveOperationException ie) {
            throw new JXPathContextFactoryConfigurationError(ie);
        }
        return factoryImpl;
    }

    /**
     * Constructs a new JXPathContextFactory.
     */
    protected JXPathContextFactory() {
    }

    /**
     * Creates a new instance of a JXPathContext using the currently configured parameters.
     *
     * @param parentContext parent context
     * @param contextBean   Object bean
     * @return JXPathContext
     * @throws JXPathContextFactoryConfigurationError if a JXPathContext cannot be created which satisfies the configuration requested
     */
    public abstract JXPathContext newContext(JXPathContext parentContext, Object contextBean);
}
