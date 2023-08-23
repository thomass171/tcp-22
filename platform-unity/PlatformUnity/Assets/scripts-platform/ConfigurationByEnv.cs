using UnityEngine;
using System.Collections;
using System;
using java.lang;
using de.yard.threed.core.platform;
using de.yard.threed.engine.platform;

using de.yard.threed.engine;
using java.util;
using de.yard.threed.engine.platform.common;
using de.yard.threed.outofbrowser;
using de.yard.threed.core.configuration;

namespace de.yard.threed.platform.unity
{

    /**
     * Derived from java-commons ConfigurationByEnv.
     */
    public class ConfigurationByEnv : Configuration
    {

        public ConfigurationByEnv()
        {
        }

        override
        public String getPropertyString(String property)
        {
            return Environment.GetEnvironmentVariable(property);
        }

        /**
         * A ConfigurationByArgs should be added in related main classes with top prio.
         * ConfigurationByEnv is only available in Java envs!
         * /
        public static Configuration buildDefaultConfigurationWithArgsAndEnv(String[] args, Map<String, String> properties) {
            return new ConfigurationByArgs(args).addConfiguration(new ConfigurationByEnv(), true).addConfiguration(
                    new ConfigurationByProperties(properties), true);
        }*/

        /**
         * The default configuration is always a command line configuration initially.
         * 5.2.23: "byargs" needs args!! So better have a separate explicit init.
         * 6.2.23: Now build a typical default configuration with
         * 1) ByEnv (top prio), eg. for "HOSTDIR", "ADDITIONALBUNDLE". ConfigurationByEnv is only available in Java envs!
         * 2) by properties
         *
         */
        public static Configuration buildDefaultConfigurationWithEnv(Map<String, String> properties)
        {
            return new ConfigurationByEnv().addConfiguration(
                    new ConfigurationByProperties(properties), true);
        }
    }
}
