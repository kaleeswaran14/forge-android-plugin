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

import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.jboss.forge.maven.MavenCoreFacet;
import org.jboss.forge.project.dependencies.Dependency;
import org.jboss.forge.project.dependencies.DependencyBuilder;
import org.jboss.forge.project.facets.BaseFacet;
import org.jboss.forge.project.facets.DependencyFacet;
import org.jboss.forge.shell.ShellColor;
import org.jboss.forge.shell.ShellPrintWriter;
import org.jboss.forge.shell.ShellPrompt;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.RequiresFacet;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;


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
	private static final String PACKAGING_TYPE_APK = "apk";

	public static final String SUCCESS_MSG_FMT = "***SUCCESS*** %s %s has been installed.";

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
        String androidHome = System.getenv("ANDROID_HOME");
        if (StringUtils.isEmpty(androidHome)) {
        	this.writer.println(ShellColor.RED, "Android Home is not set in environment variable ");
        	return false;
        }
        installDependencies(getAndroidCoreDependency(), false);
        installplugin(androidBuildPlugin());
        setPackaging(PACKAGING_TYPE_APK);
        //android list targets
        //http://developer.android.com/tools/devices/managing-avds-cmdline.html
        setProperty("platform.version", "4.0.3"); // platform version need to get from user
		return true;
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
        removeplugin(androidBuildPlugin());
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
            final Dependency dep = this.shell.promptChoiceTyped("What version do you want to install ?", versions);
            dependency.setVersion(dep.getVersion());
        }
        this.dependencyFacet.addDirectDependency(dependency);

        this.writer.println(ShellColor.GREEN, dependency.getArtifactId() + ":" + dependency.getGroupId() + ":" + dependency.getVersion() + " is added to the dependency.");

    }
    
    /**
     * Method to set the packaging type into the project.
     * 
     * @param packagingType packagingType
     */
    private void setPackaging(final String packagingType) {
    	System.out.println("android packaging is set .... ");
    	MavenCoreFacet facet = this.project.getFacet(MavenCoreFacet.class);
    	Model pom = facet.getPOM();
    	pom.setPackaging(packagingType);
    	facet.setPOM(pom);
    }
    
    /**
     * Method to set the property values.
     * 
     * @param key key
     * @param value value
     */
    private void setProperty(final String key, final String value) {
    	MavenCoreFacet facet = this.project.getFacet(MavenCoreFacet.class);
    	Model pom = facet.getPOM();
    	pom.addProperty(key, value);
    	facet.setPOM(pom);
    }
    
    /**
     * Android core dependency.
     * 
     * @return the dependency builder
     */
    private static DependencyBuilder getAndroidCoreDependency() {
        return DependencyBuilder.create().setGroupId("com.google.android").setArtifactId("android").setVersion("4.0.3").setScopeType("provided");
    }
    
    /**
     * Method to add the specified plugin into build tag of the project.
     * 
     * @param Plugin the plugin
     */
    private void installplugin(Plugin plugin) {
    	MavenCoreFacet facet = this.project.getFacet(MavenCoreFacet.class);
    	Model pom = facet.getPOM();
    	Build build = pom.getBuild();
    	if (build == null) {
    		build = new Build();
    		pom.setBuild(build);
    	}
    	
    	// If the plugin is not available in pom.xml, add the dependency
    	List<Plugin> plugins = pom.getBuild().getPlugins();
    	if (CollectionUtils.isNotEmpty(plugins) && !plugins.contains(plugin)) {
        	pom.getBuild().addPlugin(plugin);
        	facet.setPOM(pom);
    	}
    }
    
    /**
     * Method to removes the specified plugin into build tag of the project.
     * 
     * @param Plugin the plugin
     */
    private void removeplugin(Plugin plugin) {
    	MavenCoreFacet facet = this.project.getFacet(MavenCoreFacet.class);
    	Model pom = facet.getPOM();
    	pom.getBuild().removePlugin(plugin);
    }
    
    /**
     * Android build plugins.
     * 
     * @return the dependency builder
     */
    private Plugin androidBuildPlugin() {
    	Plugin mavenAndroidPlugin = new Plugin();
    	mavenAndroidPlugin.setGroupId("com.jayway.maven.plugins.android.generation2");
    	mavenAndroidPlugin.setArtifactId("android-maven-plugin");
    	mavenAndroidPlugin.setConfiguration(pluginConfiguration("4.0.3"));
    	mavenAndroidPlugin.setVersion("3.6.0");
    	mavenAndroidPlugin.setExtensions(true);
		return mavenAndroidPlugin;
    }
    
    /**
     * Android plugin configuration.
     * 
     * @return the Object
     */
    private Object pluginConfiguration(String platformVersion) {
		try {
			return Xpp3DomBuilder.build(new StringReader(
			            "<configuration>\n" +
			            " <androidManifestFile>${project.basedir}/AndroidManifest.xml</androidManifestFile>\n" +
			            " <assetsDirectory>${project.basedir}/assets</assetsDirectory>\n" +
			            " <resourceDirectory>${project.basedir}/res</resourceDirectory>\n" +
			            " <nativeLibrariesDirectory>${project.basedir}/src/main/native</nativeLibrariesDirectory>\n" +
			            " <sdk>\n" +
			            " <platform>" + platformVersion + "</platform>\n" +
			            " </sdk>\n" +
			            " <deleteConflictingFiles>true</deleteConflictingFiles>\n" +
			            " <undeployBeforeDeploy>true</undeployBeforeDeploy>\n" +
			            "</configuration>"));
		 } catch (XmlPullParserException e) {
		     throw new IllegalStateException(e);
		 } catch (IOException e) {
		     throw new java.lang.IllegalStateException(e);
		 }
    }
    
}
