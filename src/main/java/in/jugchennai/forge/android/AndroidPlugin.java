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
import in.jugchennai.forge.android.utils.AndroidPluginUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.jboss.forge.parser.xml.Node;
import org.jboss.forge.parser.xml.XMLParser;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.facets.DependencyFacet;
import org.jboss.forge.project.facets.JavaSourceFacet;
import org.jboss.forge.project.facets.MetadataFacet;
import org.jboss.forge.project.facets.events.InstallFacets;
import org.jboss.forge.resources.DirectoryResource;
import org.jboss.forge.resources.FileResource;
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
import org.jboss.forge.shell.util.Packages;
import org.jboss.forge.shell.Shell;

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
	
    /** The shell prompt. */
    @Inject
    private ShellPrompt shellPrompt;

    /** The shell. */
	@Inject
	Shell shell;
	
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
     * @throws TemplateException 
     * @throws IOException 
     */
	@SetupCommand(help = "Installs basic setup to work with Android application.")
	 public void setup(final PipeOut out) throws IOException, TemplateException {
		if (!this.project.hasFacet(AndroidFacet.class)) {
		    this.install.fire(new InstallFacets(AndroidFacet.class));
		}
		// create assets folder
		DirectoryResource projectRoot = this.project.getProjectRoot();
		projectRoot.getOrCreateChildDirectory("assets");
		// Res folder
		DirectoryResource resDirectory = projectRoot.getOrCreateChildDirectory("res");
		
		// Create drawables and icon directories
		List<FileResource<?>> iconResources = getIconResources(resDirectory);
		
		// Copy template icon files to all iconResources folder
		copyIconFromTemplate(iconResources);

		// Project name
        final MetadataFacet metadata = this.project.getFacet(MetadataFacet.class);
        String projectName = metadata.getProjectName();
        String topLevelPackage = metadata.getTopLevelPackage();
        String activityPackage = ".activity";
        
		// activity creation
		final Map<String, TemplateSettings> context = getSettingsContext(projectName, topLevelPackage, activityPackage);
        createDefaultActivity(out, projectName, context);
        
        // Create default manifest file
		createDefaultManifest(out, projectRoot, context);
		
		// strings file creation
		createStringFile(out, resDirectory, context);
		
		// create properties file
		createPropertiesFile(out, projectRoot);
		
		// create layout file
		createLayoutFile(out, resDirectory);
        
		if (this.project.hasFacet(AndroidFacet.class)) {
		    this.writer.println(ShellColor.GREEN, "Android is configured.");
		}
	}

	/**
	 * @param out
	 * @param resDirectory
	 */
	private void createLayoutFile(final PipeOut out,
			DirectoryResource resDirectory) {
		InputStream stream = null;
		DirectoryResource layoutDirectory = resDirectory.getOrCreateChildDirectory("layout");
		FileResource<?> layoutMainFile = (FileResource<?>) layoutDirectory.getChild("main.xml");
		if (!layoutMainFile.exists()) {
			stream = AndroidPlugin.class.getResourceAsStream("/templates/TemplateLayoutMain.ftl");
			layoutMainFile.setContents(stream);
			out.println(ShellColor.YELLOW, String.format(AndroidFacet.SUCCESS_MSG_FMT, "main.xml", "file"));
		}
	}

	/**
	 * @param out
	 * @param projectRoot
	 */
	private void createPropertiesFile(final PipeOut out,
			DirectoryResource projectRoot) {
		InputStream stream = null;
		FileResource<?> defaultPropFile = (FileResource<?>) projectRoot.getChild("default.properties");
		if (!defaultPropFile.exists()) {
			stream = AndroidPlugin.class.getResourceAsStream("/templates/TemplateProperties.ftl");
			defaultPropFile.setContents(stream);
			out.println(ShellColor.YELLOW, String.format(AndroidFacet.SUCCESS_MSG_FMT, "default.properties", "file"));
		}
	}

	/**
	 * @param out
	 * @param resDirectory
	 * @param context
	 * @throws IOException
	 * @throws TemplateException
	 */
	private void createStringFile(final PipeOut out, DirectoryResource resDirectory, final Map<String, TemplateSettings> context) throws IOException, TemplateException {
		DirectoryResource valuesDirectory = resDirectory.getOrCreateChildDirectory("values");
		FileResource<?> valuesStringsFile = (FileResource<?>) valuesDirectory.getChild("strings.xml");
		if (!valuesStringsFile.exists()) {
			File stringsFileObj = new File(valuesDirectory.getUnderlyingResourceObject().getPath() + System.getProperty("file.separator") + "strings.xml");
			AndroidPluginUtils.createResourceFileUsingTemplate(project, "TemplateStrings.ftl", stringsFileObj, context);
			out.println(ShellColor.YELLOW, String.format(AndroidFacet.SUCCESS_MSG_FMT, "strings.xml", "file"));
		}
	}

	/**
	 * @param out
	 * @param projectRoot
	 * @param context
	 * @throws IOException
	 * @throws TemplateException
	 */
	private void createDefaultManifest(final PipeOut out,
			DirectoryResource projectRoot,
			final Map<String, TemplateSettings> context) throws IOException,
			TemplateException {
		// manifest and default.properties file with activity name
		FileResource<?> manifestFile = (FileResource<?>) projectRoot.getChild("AndroidManifest.xml");
		if (!manifestFile.exists()) {
	        File jnlpTemplate = new File(projectRoot.getUnderlyingResourceObject().getPath() + System.getProperty("file.separator") + "AndroidManifest.xml");
			AndroidPluginUtils.createResourceFileUsingTemplate(project, "TemplateManifest.ftl", jnlpTemplate, context);
			out.println(ShellColor.YELLOW, String.format(AndroidFacet.SUCCESS_MSG_FMT, "AndroidManifest.xml", "file"));
		}
	}

	/**
	 * @param out
	 * @param projectName
	 * @param context
	 * @throws IOException
	 * @throws TemplateException
	 */
	private void createDefaultActivity(final PipeOut out, String projectName,
			final Map<String, TemplateSettings> context) throws IOException,
			TemplateException {
		AndroidPluginUtils.createJavaFileUsingTemplate(this.project, "TemplateActivity.ftl", context);
        out.println(ShellColor.YELLOW, String.format(AndroidFacet.SUCCESS_MSG_FMT, projectName, "class"));
	}

	/**
	 * @param projectName
	 * @param topLevelPackage
	 * @param activityPackage
	 * @return
	 */
	private Map<String, TemplateSettings> getSettingsContext(
			String projectName, String topLevelPackage, String activityPackage) {
		TemplateSettings settings = new TemplateSettings(AndroidPluginUtils.capitalize(projectName) + "Activity", "");
		// app name value which will be inserted in manifest and strings.xml
		settings.setActivityLabelKey("app_name");
		settings.setActivityLabelValue(projectName);
		settings.setTopLevelPacakge(topLevelPackage + activityPackage);
		// without considering existing java file it is updating it
        final Map<String, TemplateSettings> context = new HashMap<String, TemplateSettings>();
        context.put("settings", settings);
		return context;
	}

	/**
	 * @param iconResources
	 */
	private void copyIconFromTemplate(List<FileResource<?>> iconResources) {
		InputStream stream = null;
		if (CollectionUtils.isNotEmpty(iconResources)) {
			/*for (FileResource<?> iconResource : iconResources) {
				if (!iconResource.exists()) {
					stream = AndroidPlugin.class.getResourceAsStream("/templates/icons.png");
					iconResource.setContents(stream);
//					File iconFolder = iconResource.getUnderlyingResourceObject().getAbsoluteFile();
//					System.out.println("iconFolder > " + iconFolder);
//					Utils.copyImage(stream, iconFolder);
					out.println(ShellColor.YELLOW, String.format(AndroidFacet.SUCCESS_MSG_FMT, "icons.png", "icon"));
				}
			}*/
		}
	}

	/**
	 * @param resDirectory
	 * @return
	 */
	private List<FileResource<?>> getIconResources(DirectoryResource resDirectory) {
		// res inner directories
		DirectoryResource drawableHDPIDirectory = resDirectory.getOrCreateChildDirectory("drawable-hdpi");
		DirectoryResource drawableLDPIDirectory = resDirectory.getOrCreateChildDirectory("drawable-ldpi");
		DirectoryResource drawableMDPIDirectory = resDirectory.getOrCreateChildDirectory("drawable-mdpi");
		
		// create icons
		List<FileResource<?>> iconResources = new ArrayList<FileResource<?>>(3);
		FileResource<?> hdpiIconFile =  (FileResource<?>) drawableHDPIDirectory.getChild("icon.png");
		FileResource<?> ldpiIconFile =  (FileResource<?>) drawableLDPIDirectory.getChild("icon.png");
		FileResource<?> mdpiIconFile =  (FileResource<?>) drawableMDPIDirectory.getChild("icon.png");
		iconResources.add(hdpiIconFile);
		iconResources.add(ldpiIconFile);
		iconResources.add(mdpiIconFile);
		return iconResources;
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
    		final String name,
    		@Option(name = "isLaunchActivity", shortName = "i", help = "Is launch activity", defaultValue = "false", flagOnly = false)
            final boolean isLaunchActivity) {
//    	projectName > androidForgeTestApp
//    	topLevelPackage > com.example.androidForgeTestApp
    	
        final MetadataFacet metadata = project.getFacet(MetadataFacet.class);
        String projectName = metadata.getProjectName();
        String topLevelPackage = metadata.getTopLevelPackage();
        System.out.println("projectName > " + projectName);
        System.out.println("topLevelPackage > " + topLevelPackage);
        
        
        // get acitivity package name
        String manifestPackage = AndroidPluginUtils.getApplicationPackage(project, out);
        System.out.println("manifestPackage > " + manifestPackage);
        
        // folder already exists
        final DirectoryResource sourceFolder = project.getFacet(JavaSourceFacet.class).getSourceFolder();
        String javaFileSyntax = Packages.toFileSyntax(manifestPackage + ".");
		DirectoryResource packageDirectory = sourceFolder.getChildDirectory(javaFileSyntax);
        if (!packageDirectory.exists()) {
        	out.println("Package does not exist creating it " + packageDirectory);
        	packageDirectory.mkdir();
        }
        
        // creating activity file
        String activityName = AndroidPluginUtils.capitalize(name) + "Activity";
        FileResource<?> activityFile = (FileResource<?>)packageDirectory.getChild(activityName + ".java"); // name ends with activity by default
        if (activityFile.exists()) {
        	out.println("Activity already exists ");
//        	boolean doOverwrite = shell.promptBoolean("File already exists ");
        	// do u want to overwrite it
        } else {
			TemplateSettings settings = new TemplateSettings(activityName, manifestPackage);
    		// without considering existing java file it is updating it
            final Map<String, TemplateSettings> context = new HashMap<String, TemplateSettings>();
            settings.setTopLevelPacakge(manifestPackage);
            context.put("settings", settings);
            try {
				AndroidPluginUtils.createJavaFileUsingTemplate(this.project, "TemplateActivity.ftl", context);
			} catch (Exception e) {
				out.println(ShellColor.RED, "Not able to create the activity");
				return;
			}
            out.println(name + "Activity created");
        }
        
        // entry on manifest file
        AndroidPluginUtils.createActivityEntry(project, out, activityName, isLaunchActivity);
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
    // min and max verison and build environment versions(2 places)
    
    /**
     * Build the application.
     * 
     * @param out the out
     * @param sdk SDK version to build
     */
    @Command(value = "build", help = "Build the application")
    public void build(
    		final PipeOut out, 
    		@Option(name = "sdk", shortName = "v", required = false, help = "SDK version to build.") final String sdk) {
    	try {
//    		shell.execute("mvn clean compile android:generate-sources android:dex android:apk");
    		String cmd = "mvn clean install -X";
			shell.execute(cmd);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    
    /**
     * Deploy the application.
     * mvn clean install -Dandroid.device="emulator" -Dandroid.emulator.avd=default -f pom.xml
     * 
     * @param out the out
     * @param device Android device type
     */
    @Command(value = "deploy", help = "Deploy the application")
    public void deploy(
    		final PipeOut out, 
    		@Option(name = "device", shortName = "d", required = false, help = "Android device type.") final String device) {
    	try {
    		String cmd = "mvn android:deploy -X";
			shell.execute(cmd);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    /**
     * Undeploy the application.
     * 
     * @param out the out
     * @param device Android device type
     */
    @Command(value = "undeploy", help = "undeploy the application")
    public void undeploy(
    		final PipeOut out, 
    		@Option(name = "device", shortName = "d", required = false, help = "Android device type.") final String device) {
    	try {
    		String cmd = "mvn android:undeploy -X";
			shell.execute(cmd);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    /**
     * Undeploy the application.
     * 
     * @param out the out
     * @param device Android device type
     */
    @Command(value = "run", help = "run the application")
    public void run(
    		final PipeOut out, 
    		@Option(name = "device", shortName = "d", required = false, help = "Android device type.") final String device) {
    	try {
    		String cmd = "mvn android:run -X";
			shell.execute(cmd);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    /**
     * list android devices.
     * 
     * @param out the out
     */
    @Command(value = "devices", help = "list android devices")
    public void listDevices(
    		final PipeOut out, 
    		@Option(name = "device", shortName = "d", required = false, help = "Android device type.") final String device) {
    	try {
    		String cmd = "mvn android:devices";
			shell.execute(cmd);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    /**
     * help.
     * 
     * @param out the out
     */
    @Command(value = "help", help = "help")
    public void help(final PipeOut out) {
    	try {
    		String cmd = "mvn android:help";
			shell.execute(cmd);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    /**
     * list sdks.
     * 
     * @param out the out
     * @param device Android device type
     */
    @Command(value = "sdks", help = "list sdks on this machine")
    public void sdks(final PipeOut out) {
    	try {
    		String cmd = "android list targets";
			shell.execute(cmd);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    /**
     * Create AVD.
     * 
     * @param out the out
     * @param device Android device type
     */
    @Command(value = "avd-create", help = "create avd")
    public void createAvd(
    		final PipeOut out, 
    		@Option(name = "name", shortName = "n", required = true, help = "name of the device") final String name,
    		@Option(name = "targetID", shortName = "t", required = true, help = "targetID") final String targetID,
    		@Option(name = "options", shortName = "o", required = false, help = "extra options can be specified like [-<option> <value>] ... ") final String options) {
    	try {
    		System.out.println("android create ");
    		String command = "android create avd -n "+ name +" -t "+ targetID +" ";
    		if (StringUtils.isNotEmpty(options)) {
    			command = command + options;
    		}
    		AndroidPluginUtils.executeInShell(command, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    /**
     * Move AVD.
     * 
     * @param out the out
     * @param device Android device type
     */
    @Command(value = "avd-move", help = "move avd")
    public void moveAvd(
    		final PipeOut out, 
    		@Option(name = "name", shortName = "n", required = true, help = "name of the device") final String name,
    		@Option(name = "options", shortName = "o", required = false, help = "extra options can be specified like [-<option> <value>] ... ") final String options) {
    	try {
    		String command = "android move avd -n "+ name +" -t ";
    		if (StringUtils.isNotEmpty(options)) {
    			command = command + options;
    		}
//			shell.execute(command);
    		AndroidPluginUtils.executeInShell(command, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    /**
     * Update AVD.
     * 
     * @param out the out
     * @param device Android device type
     */
    @Command(value = "avd-update", help = "update avd")
    public void updateAvd(final PipeOut out) {
    	try {
    		String command = "avd-update";
//			shell.execute(cmd);
    		AndroidPluginUtils.executeInShell(command, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    /**
     * Delete AVD.
     * 
     * @param out the out
     * @param device Android device type
     */
    @Command(value = "avd-delete", help = "delete avd")
    public void deleteAvd(
    		final PipeOut out, 
    		@Option(name = "name", shortName = "n", required = true, help = "name of the device") final String name) {
    	try {
    		String command = "android delete avd -n "+ name +" ";
//			shell.execute(cmd);
    		AndroidPluginUtils.executeInShell(command, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    /**
     * Start the emulator.
     * 
     * @param out the out
     * @param name name of the emulator
     */
    @Command(value = "emulator-start", help = "start emulator")
    public void startEmulator(
    		final PipeOut out, 
    		@Option(name = "name", shortName = "n", required = true, help = "name of the emulator") final String name) {
    	try {
    		String command = "mvn android:emulator-start";
			shell.execute(command);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    /**
     * Stop the emulator.
     * 
     * @param out the out
     * @param name name of the emulator
     */
    @Command(value = "emulator-stop", help = "stop emulator")
    public void stopEmulator(
    		final PipeOut out, 
    		@Option(name = "name", shortName = "n", required = true, help = "name of the emulator") final String name) {
    	try {
    		String command = "mvn android:emulator-stop";
			shell.execute(command);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    /**
     * Stop all the emulator.
     * 
     * @param out the out
     * @param name name of the emulator
     */
    @Command(value = "emulator-stop-all", help = "stop all emulator")
    public void stopAllEmulator(
    		final PipeOut out, 
    		@Option(name = "name", shortName = "n", required = true, help = "name of the emulator") final String name) {
    	try {
    		String command = "mvn android:emulator-stop-all";
			shell.execute(command);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}
