/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 * @authors Andrew Dinn
 */

/**
 * Copy of the test for TestNG functionality using BMUnitConfig
 * annotations to define configuration properties
 */
package test;

import org.jboss.byteman.contrib.bmunit.BMNGListener;
import org.jboss.byteman.contrib.bmunit.BMRule;
import org.jboss.byteman.contrib.bmunit.BMRules;
import org.jboss.byteman.contrib.bmunit.BMScript;
import org.jboss.byteman.contrib.bmunit.BMScripts;
import org.jboss.byteman.contrib.bmunit.BMUnitConfig;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

// this runner extends the BMNGRunner which allows it to be executed via TestNG
@BMScript(dir="test/scripts")
// You can also specify rules directly using annotations
// either a one off rule using @BMRule(...)
// or a set of rules using @BMRules( @BMRUle(...), ... @BMRule(...))
// but not both
// note that rules defined using @BMRule get loaded and, hence, injected after rules defined using
// @BMScript
// clearly this is a little clumsy, especially when embedding literal strings in the rule text
// a macro facility as per any bog standard Lisp would be a big help here
// but this does ensure that the rule is right beside the test it applies
@BMRule(name="NGUnitTest tryAlways trace rule",
        targetClass = "NGUnitConfigTest",
        targetMethod = "tryAlways",
        condition = "TRUE",
        action="traceln(\"Byteman: NG intercepted at entry in tryAlways from class @BMRules rule\");"
)
// annottaing the class with BMNGListener mixes in the behaviour which processes
// @BMRule and @BMScript annotations. ANother option is to have the test class
// inherit from class BMNGRunner
@Listeners(BMNGListener.class)
// we use a config annoation to set the load dir,
// switch on bmunit verbosity and use port 9191
// for the agent listener
@BMUnitConfig(loadDirectory="test/scripts", bmunitVerbose=true, agentPort="9191")
public class NGUnitConfigTest
{
    @Test()
    // A method annotation identifies a rule script which is loaded before calling the test method and
    // then unloaded after the test has run
    @BMScript()
    // a method config annotation overrides the bmunit
    // verbosity and enables the Byteman verbose setting
    @BMUnitConfig(bmunitVerbose=false)
    public void testOne()
    {
        tryOne();
        tryTwo();
        tryThree();
        tryAlways();
    }

    @Test()
    // If you supply a value then this is used when looking for the script otherwise the method name is used
    @BMScript(value="two", dir="test/scripts")
    // another method config annotation overrides the script
    // lookup directory -- it should not change anything because
    // the script annotation specifies a dir
    @BMUnitConfig(loadDirectory="test", debug=true)
    public void testTwo()
    {
        tryOne();
        tryTwo();
        tryThree();
        tryAlways();
    }

    @Test()
    // you can load several scripts using the BMScripts annotation. this is useful if you want
    // several test to share some rules but also have their own specific rules
    @BMScripts(
            scripts = { @BMScript(value="three"),  @BMScript(value="three-extra") }
    )
    // BMRule and BMRules annotations can also be used at the method level
    // to configure rules specific to a given test method
    // note that rules defined using @BMRule get loaded and, hence, injected after rules defined using
    // @BMScript
    @BMRules( rules = {
            @BMRule(name="NGUnitTest.testThree tryThree trace rule",
                    targetClass = "NGUnitConfigTest",
                    targetMethod = "tryThree",
                    condition = "TRUE",
                    action="traceln(\"Byteman: NG intercepted at entry in tryThree from method @BMRule rule\");"
            ),
            @BMRule(name="NGUnitTest.testThree tryAlways trace rule",
                    targetClass = "NGUnitConfigTest",
                    targetMethod = "tryAlways",
                    binding = "test = $0;",
                    condition = "TRUE",
                    action="traceln(\"Byteman: NG intercepted at entry in tryAlways from method @BMRule rule in test class \" + test.getClass().getName());"
            )
    })
    public void testThree()
    {
        tryOne();
        tryTwo();
        tryThree();
        tryAlways();
    }

    // The remaining methods have code injected into by the scripts for the class and test methods

    public void tryOne()
    {
        // message injected by Byteman in testOne
    }

    public void tryTwo()
    {
        // message injected by Byteman in testTwo
    }

    public void tryThree()
    {
        // eventually message will be injected by Byteman in testThree
    }

    public void tryAlways()
    {
        // message injected by Byteman in all tests
    }

}
