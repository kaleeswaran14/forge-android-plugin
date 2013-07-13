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

import in.jugchennai.forge.android.utils.TemplateSettings;
import in.jugchennai.forge.android.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.facets.DependencyFacet;
import org.jboss.forge.project.facets.JavaSourceFacet;
import org.jboss.forge.project.facets.MetadataFacet;
import org.jboss.forge.project.facets.events.InstallFacets;
import org.jboss.forge.resources.DirectoryResource;
import org.jboss.forge.resources.FileResource;
import org.jboss.forge.resources.Resource;
import org.jboss.forge.shell.ShellColor;
import org.jboss.forge.shell.ShellPrintWriter;
import org.jboss.forge.shell.ShellPrompt;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.Command;
import org.jboss.forge.shell.plugins.DefaultCommand;
import org.jboss.forge.shell.plugins.Help;
import org.jboss.forge.shell.plugins.Option;
import org.jboss.forge.shell.plugins.PipeOut;
import org.jboss.forge.shell.plugins.Plugin;
import org.jboss.forge.shell.plugins.RequiresFacet;
import org.jboss.forge.shell.plugins.RequiresProject;
import org.jboss.forge.shell.plugins.SetupCommand;

import freemarker.template.TemplateException;

/**
 * 
 * <b>Contributors</b>
 * 
 * @author Rajmahendra Hegde <rajmahendra@gmail.com>
 *
 */

@Alias("android")
@Help("A Forge plugin to enable and work on Android development.")
@RequiresFacet({DependencyFacet.class, JavaSourceFacet.class})
@RequiresProject
public class AndroidPlugin implements Plugin  {
	
    /** The shell. */
    @Inject
    private ShellPrompt shell;

    /** The project. */
    @Inject
    private Project project;

    /** The install. */
    @Inject
    private Event<InstallFacets> install;

    /** The writer. */
    @Inject
    private ShellPrintWriter writer;
	
    /**
     * The setup command for Android. This adds dependency to the current project
     * 
     * @param out the out
     * @param moduleName the module name
     * @throws TemplateException 
     * @throws IOException 
     */
	@SetupCommand(help = "Installs basic setup to work with Android application.")
	 public void setup(final PipeOut out) throws IOException, TemplateException {
		//, @Option(name = "module", shortName = "m", required = true, help = "The Module name to be installed.") final String moduleName
		if (!this.project.hasFacet(AndroidFacet.class)) {
		    this.install.fire(new InstallFacets(AndroidFacet.class));
		}
		DirectoryResource projectRoot = this.project.getProjectRoot();
		projectRoot.getOrCreateChildDirectory("assets");
		DirectoryResource resDirectory = projectRoot.getOrCreateChildDirectory("res");
		
		// res inner directories
		DirectoryResource layoutDirectory = resDirectory.getOrCreateChildDirectory("layout");
		DirectoryResource drawableHDPIDirectory = resDirectory.getOrCreateChildDirectory("drawable-hdpi");
		DirectoryResource drawableLDPIDirectory = resDirectory.getOrCreateChildDirectory("drawable-ldpi");
		DirectoryResource drawableMDPIDirectory = resDirectory.getOrCreateChildDirectory("drawable-mdpi");
		DirectoryResource valuesDirectory = resDirectory.getOrCreateChildDirectory("values");
		
		// create icons
		List<FileResource<?>> iconResources = new ArrayList<FileResource<?>>(3);
		FileResource<?> hdpiIconFile =  (FileResource<?>) drawableHDPIDirectory.getChild("icon.png");
		FileResource<?> ldpiIconFile =  (FileResource<?>) drawableLDPIDirectory.getChild("icon.png");
		FileResource<?> mdpiIconFile =  (FileResource<?>) drawableMDPIDirectory.getChild("icon.png");
		iconResources.add(hdpiIconFile);
		iconResources.add(ldpiIconFile);
		iconResources.add(mdpiIconFile);
		
		InputStream stream = null;
//		if (CollectionUtils.isNotEmpty(iconResources)) {
//			for (FileResource<?> iconResource : iconResources) {
//				if (!iconResource.exists()) {
//					stream = AndroidPlugin.class.getResourceAsStream("/templates/icons.png");
//					iconResource.setContents(stream);
//					out.println(ShellColor.YELLOW, String.format(AndroidFacet.SUCCESS_MSG_FMT, "icons.png", "icon"));
//				}
//			}
//		}
		
		// activity creation
        final MetadataFacet metadata = this.project.getFacet(MetadataFacet.class);
        String projectName = metadata.getProjectName();
		TemplateSettings settings = new TemplateSettings(Utils.capitalize(projectName) + "Activity", metadata.getTopLevelPackage());
		// app name value which will be inserted in manifest and strings.xml
		settings.setActivityLabelKey("app_name");
		settings.setActivityLabelValue(projectName);
		
        final Map<String, TemplateSettings> context = new HashMap<String, TemplateSettings>();
        settings.setTopLevelPacakge(metadata.getTopLevelPackage());
        context.put("settings", settings);
        Utils.createJavaFileUsingTemplate(this.project, "TemplateActivity.ftl", context);
        out.println(ShellColor.YELLOW, String.format(AndroidFacet.SUCCESS_MSG_FMT, projectName, "class"));
        
		// manifest and default.properties file with activity name
		FileResource<?> manifestFile = (FileResource<?>) projectRoot.getChild("AndroidManifest.xml");
		if (!manifestFile.exists()) {
	        File jnlpTemplate = new File(projectRoot.getUnderlyingResourceObject().getPath() + System.getProperty("file.separator") + "AndroidManifest.xml");
			Utils.createFileUsingTemplate(project, "TemplateManifest.ftl", jnlpTemplate, context);
			out.println(ShellColor.YELLOW, String.format(AndroidFacet.SUCCESS_MSG_FMT, "AndroidManifest.xml", "file"));
		}
		
		FileResource<?> valuesStringsFile = (FileResource<?>) valuesDirectory.getChild("strings.xml");
		if (!valuesStringsFile.exists()) {
			File stringsFileObj = new File(valuesDirectory.getUnderlyingResourceObject().getPath() + System.getProperty("file.separator") + "strings.xml");
			Utils.createResourceFileUsingTemplate(project, "TemplateStrings.ftl", stringsFileObj, context);
			out.println(ShellColor.YELLOW, String.format(AndroidFacet.SUCCESS_MSG_FMT, "strings.xml", "file"));
		}
		
		// create files alone
		FileResource<?> defaultPropFile = (FileResource<?>) projectRoot.getChild("default.properties");
		if (!defaultPropFile.exists()) {
			stream = AndroidPlugin.class.getResourceAsStream("/templates/TemplateProperties.ftl");
			defaultPropFile.setContents(stream);
			out.println(ShellColor.YELLOW, String.format(AndroidFacet.SUCCESS_MSG_FMT, "default.properties", "file"));
		}
		
		FileResource<?> layoutMainFile = (FileResource<?>) layoutDirectory.getChild("main.xml");
		if (!layoutMainFile.exists()) {
			stream = AndroidPlugin.class.getResourceAsStream("/templates/TemplateLayoutMain.ftl");
			layoutMainFile.setContents(stream);
			out.println(ShellColor.YELLOW, String.format(AndroidFacet.SUCCESS_MSG_FMT, "main.xml", "file"));
		}
        
		if (this.project.hasFacet(AndroidFacet.class)) {
		    this.writer.println(ShellColor.GREEN, "Android is configured.");
		}
	}
	
    /**
     * If android command is not executed with any argument this method will be called.
     * 
     * @param out the out
     */
	@DefaultCommand
	private void defaultCommand(final PipeOut out) {
        if (this.project.hasFacet(AndroidFacet.class)) {
            out.println("Android is installed.");
        } else {
            out.println("Android is not installed. Use 'android setup' to install.");
        }
	}
	
	
    /**
     * Creates the activity.
     * 
     * @param out the out
     * @param name the activity name
     */
    @Command(value = "activity-create", help = "Create a activity for the given name")
    public void createActivity(
            final PipeOut out,
            @Option(name = "name", shortName = "n", required = true, help = "Name of the activity to be created.")
            final String name) {
    }
    
    /**
     * Creates the android project.
     * 
     * @param out the out
     * @param name the project name
     */
    @Command(value = "project-create", help = "Create a android application with the given name")
    public void createProject(
            final PipeOut out,
            @Option(name = "name", shortName = "n", required = true, help = "Name of the application to be created.")
            final String name) {
    }
    
    /**
     * Change the emulator.
     * 
     * @param out the out
     * @param number the emulator number
     */
    @Command(value = "change-emulator", help = "Change the emulator")
    public void changeEmulator(
            final PipeOut out,
            @Option(name = "number", shortName = "n", required = true, help = "Emulator number.")
            final String number) {
    }
}
