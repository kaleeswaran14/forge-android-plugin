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

import java.util.List;

import javax.inject.Inject;

import org.jboss.forge.project.dependencies.Dependency;
import org.jboss.forge.project.dependencies.DependencyBuilder;
import org.jboss.forge.project.facets.BaseFacet;
import org.jboss.forge.project.facets.DependencyFacet;
import org.jboss.forge.shell.ShellColor;
import org.jboss.forge.shell.ShellPrintWriter;
import org.jboss.forge.shell.ShellPrompt;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.RequiresFacet;

/**
 * 
 * <b>Contributors</b>
 * 
 * @author Rajmahendra Hegde <rajmahendra@gmail.com>
 *
 */

@Alias("org.android")
@RequiresFacet({DependencyFacet.class})
public class AndroidFacet extends BaseFacet {

    /** The shell. */
    @Inject
    private ShellPrompt shell;

    /** The dependency facet. */
    private DependencyFacet dependencyFacet;

    /** The writer. */
    @Inject
    private ShellPrintWriter writer;
    
	@Override
	public boolean install() {
        this.dependencyFacet = this.project.getFacet(DependencyFacet.class);
        this.dependencyFacet.addRepository("JBoss Maven repository", "https://repository.jboss.org/nexus/content/groups/public-jboss/");
        installDependencies(getAndroidCoreDependency(), true);
		return false;
	}

	@Override
	public boolean isInstalled() {
		if (this.project.hasFacet(AndroidFacet.class)) {
			return true;
		}
		return false;
	}
	
	@Override
	public boolean uninstall() {
        this.dependencyFacet = this.project.getFacet(DependencyFacet.class);
        this.dependencyFacet.removeDependency(getAndroidCoreDependency());
        this.dependencyFacet.removeRepository("JBoss Maven repository");
		return true;
	}
	
    /**
     * Method to install the specified Dependencies into the project.
     * 
     * @param dependency the dependency
     * @param askVersion Version of the dependency
     */
    private void installDependencies(final DependencyBuilder dependency, final boolean askVersion) {

        final List<Dependency> versions = this.dependencyFacet.resolveAvailableVersions(dependency);
        if (askVersion) {
            final Dependency dep = this.shell.promptChoiceTyped("What version do you want to install?", versions);
            dependency.setVersion(dep.getVersion());
        }
        this.dependencyFacet.addDirectDependency(dependency);

        this.writer.println(ShellColor.GREEN, dependency.getArtifactId() + ":" + dependency.getGroupId() + ":" + dependency.getVersion() + " is added to the dependency.");

    }

    /**
     * Android core dependency.
     * 
     * @return the dependency builder
     */
    private static DependencyBuilder getAndroidCoreDependency() {
        return DependencyBuilder.create().setGroupId("com.google.android").setArtifactId("android").setVersion("2.1.2").setScopeType("provided");
    }
    
    /**
     * Android build plugins.
     * 
     * @return the dependency builder
     */
    // TODO : here we need to add a plugin inside the build -> plugins tag
    private static DependencyBuilder androidBuildPlugin() {
		return null;
    }
}
