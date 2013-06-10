/*
* Copyright 2013 JUGChennai
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
package in.jugchennai.forge.android;

/**
 * 
 * <b>Contributors</b>
 * 
 * @author Rajmahendra Hegde <rajmahendra@gmail.com>
 *
 */

import static junit.framework.Assert.assertNotNull;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.dependencies.DependencyResolver;
import org.jboss.forge.test.AbstractShellTest;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;

public class AndroidPluginTest extends AbstractShellTest {
	  @Inject
	    private DependencyResolver resolver;

	    @Deployment
	    public static JavaArchive getDeployment() {
	        return AbstractShellTest.getDeployment().addPackages(true, AndroidPlugin.class.getPackage());
	    }

	    private Project initializeJRebirthFacesProject() throws Exception {
	        Project p = initializeJavaProject();

	        
	        getShell().execute("android setup");
	        return p;
	    }

	    @Test
	    public void testSetup() throws Exception {

	        initializeJRebirthFacesProject();
	        final Project project = initializeJRebirthFacesProject();

	        assertNotNull(resolver);

	        assertNotNull(project.hasFacet(AndroidFacet.class));

	    }
}
