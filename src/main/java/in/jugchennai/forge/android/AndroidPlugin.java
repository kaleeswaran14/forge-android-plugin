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

import freemarker.template.TemplateException;
import in.jugchennai.forge.android.utils.AndroidPluginUtils;
import in.jugchennai.forge.android.utils.MessageUtil;
import in.jugchennai.forge.android.utils.TemplateSettings;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.event.Event;
import javax.inject.Inject;

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
import org.jboss.forge.shell.ShellMessages;
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

/**
 * 
 * <b>Contributors</b>
 * 
 * @author Rajmahendra Hegde <rajmahendra@gmail.com>
 *
 */

@Alias("android")
@Help("A Forge plugin to enable and work on Android development.")
@RequiresFacet({DependencyFacet.class, AndroidFacet.class})
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
    
    /** The String resource bundle. */
    public static MessageUtil messages = MessageUtil.INSTANCE;
	
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
		TemplateSettings settings = new TemplateSettings(AndroidPluginUtils.capitalize(projectName) + "Activity", metadata.getTopLevelPackage());
		// app name value which will be inserted in manifest and strings.xml
		settings.setActivityLabelKey("app_name");
		settings.setActivityLabelValue(projectName);
		
        final Map<String, TemplateSettings> context = new HashMap<String, TemplateSettings>();
        settings.setTopLevelPacakge(metadata.getTopLevelPackage());
        context.put("settings", settings);
        AndroidPluginUtils.createJavaFileUsingTemplate(this.project, "TemplateActivity.ftl", context);
        out.println(ShellColor.YELLOW, String.format(AndroidFacet.SUCCESS_MSG_FMT, projectName, "class"));
        
		// manifest and default.properties file with activity name
		FileResource<?> manifestFile = (FileResource<?>) projectRoot.getChild("AndroidManifest.xml");
		if (!manifestFile.exists()) {
	        File jnlpTemplate = new File(projectRoot.getUnderlyingResourceObject().getPath() + System.getProperty("file.separator") + "AndroidManifest.xml");
	        AndroidPluginUtils.createResourceFileUsingTemplate(project, "TemplateManifest.ftl", jnlpTemplate, context);
			out.println(ShellColor.YELLOW, String.format(AndroidFacet.SUCCESS_MSG_FMT, "AndroidManifest.xml", "file"));
		}
		
		//res Files creation		
		fileCreatHelper (out,this.project,"strings.xml" ,"TemplateStrings.ftl",context,"file");
		fileCreatHelper (out,this.project,"color.xml" ,"TemplateColors.ftl",context,"file");
		fileCreatHelper (out,this.project,"dimens.xml" ,"TemplateDimens.ftl",context,"file");
		fileCreatHelper (out,this.project,"main.xml" ,"/templates/TemplateLayoutMain.ftl",context,"stream");
		
		FileResource<?> defaultPropFile = (FileResource<?>) projectRoot.getChild("default.properties");
		if (!defaultPropFile.exists()) {
			stream = AndroidPlugin.class.getResourceAsStream("/templates/TemplateProperties.ftl");
			defaultPropFile.setContents(stream);
			out.println(ShellColor.YELLOW, String.format(AndroidFacet.SUCCESS_MSG_FMT, "default.properties", "file"));
		}
		
		// create files alone
		/*FileResource<?> defaultPropFile = (FileResource<?>) projectRoot.getChild("default.properties");
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
		}*/
        
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
     *//*
    @Command(value = "change-emulator", help = "Change the emulator")
    public void changeEmulator(
            final PipeOut out,
            @Option(name = "number", shortName = "n", required = true, help = "Emulator number.")
            final String number) {
    	
    }
    // min and max verison and build environment versions(2 places)
    
    *//**
     * Build the application.
     * 
     * @param out the out
     * @param sdk SDK version to build
     *//*
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
    
    
    *//**
     * Deploy the application.
     * mvn clean install -Dandroid.device="emulator" -Dandroid.emulator.avd=default -f pom.xml
     * 
     * @param out the out
     * @param device Android device type
     *//*
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
    
    *//**
     * Undeploy the application.
     * 
     * @param out the out
     * @param device Android device type
     *//*
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
    
    *//**
     * Undeploy the application.
     * 
     * @param out the out
     * @param device Android device type
     *//*
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
    
    *//**
     * list android devices.
     * 
     * @param out the out
     *//*
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
    
    *//**
     * help.
     * 
     * @param out the out
     *//*
    @Command(value = "help", help = "help")
    public void help(final PipeOut out) {
    	try {
    		String cmd = "mvn android:help";
			shell.execute(cmd);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    *//**
     * list sdks.
     * 
     * @param out the out
     * @param device Android device type
     *//*
    @Command(value = "sdks", help = "list sdks on this machine")
    public void sdks(final PipeOut out) {
    	try {
    		String cmd = "android list targets";
			shell.execute(cmd);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    *//**
     * Create AVD.
     * 
     * @param out the out
     * @param device Android device type
     *//*
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
    
    *//**
     * Move AVD.
     * 
     * @param out the out
     * @param device Android device type
     *//*
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
    
    *//**
     * Update AVD.
     * 
     * @param out the out
     * @param device Android device type
     *//*
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
    
    *//**
     * Delete AVD.
     * 
     * @param out the out
     * @param device Android device type
     *//*
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
    
    *//**
     * Start the emulator.
     * 
     * @param out the out
     * @param name name of the emulator
     *//*
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
    
    *//**
     * Stop the emulator.
     * 
     * @param out the out
     * @param name name of the emulator
     *//*
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
    
    *//**
     * Stop all the emulator.
     * 
     * @param out the out
     * @param name name of the emulator
     *//*
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
    }*/
    
    /**
     * Add string to string resource.
     * 
     * @param out the out
     * @param stringName the string name
     * @param stringValue the string value
     */
    @Command(value = "string-add", help = "Add string to String resource")
    public void stringAdd(final PipeOut out,
            @Option(name = "name", shortName = "n", required = true, help = "Name of string object.")
            final String strname,
            @Option(name = "value", shortName = "v", required = true, help = "String value")
            final String strvalue) {
    
	try {
		valuesCommandHelper(out, "res/values/strings.xml",strname,strvalue,"string");
	} catch (Exception e) {

		e.printStackTrace();
	}
    	
    }
    
    /**
     * Add string to string resource.
     * 
     * @param out the out
     * @param stringName the string name
     * @param stringValue the string value
     
    @Command(value = "string-remove", help = "Add string to String resource")
    public void stringRemove(final PipeOut out,
            @Option(name = "name", shortName = "n", required = true, help = "Name of string object.")
            final String strname) {
    	
FileResource<?> stringFile = project.getProjectRoot().getChildOfType(FileResource.class, "res/values/strings.xml");
    
    	if(stringFile.exists()) {
    		System.out.println("File Exist");
    		Node node = XMLParser.parse(stringFile.getResourceInputStream());
    		Node stringres = node.getOrCreate("resources");
    		
    		System.out.println(node);
    		System.out.println("string Name:"+stringres.getChildren());
    		
    		for(Node tempstringres : stringres.getChildren()){
    			
    			System.out.println("Name Attribute: "+tempstringres.getAttribute("name"));
    			if (tempstringres.getAttribute("name").equals(strname))
                {
    				
    				stringres.removeChild(tempstringres);
                   out.println(strname+" is deleted from string.xml!");
                   stringFile.setContents(XMLParser.toXMLInputStream(node));
                  break;
                }
    		}
    	}
    	
    }*/
    
    
    /**
     * Add color to color resource.
     * 
     * @param out the out
     * @param colorname the string colrname
     * @param hexValue the hex value
     */
    @Command(value = "color-add", help = "Add color to Color resource")
    public void colorAdd(final PipeOut out,
            @Option(name = "name", shortName = "n", required = true, help = "Name of color object.")
            final String colrname,
            @Option(name = "hexvalue", shortName = "v", required = true, help = "Color hexValue")
            final String hexValue) {
	try {
		valuesCommandHelper(out, "res/values/color.xml",colrname,hexValue,"color");
	} catch (Exception e) {
		e.printStackTrace();
	}
    	
    }
    /**
     * Add dimens to dimens resource.
     * 
     * @param out the out
     * @param dimensName the dimens name
     * @param pixel Value the pixel value
     */
    @Command(value = "dimens-add", help = "Add dimens to dimens resource")
    public void dimensAdd(final PipeOut out,
            @Option(name = "name", shortName = "n", required = true, help = "Name of dimens object.")
            final String dmsname,
            @Option(name = "pixel", shortName = "p", required = true, help = "pixel value")
            final String dmsvalue) {
    
		try {
			valuesCommandHelper(out, "res/values/color.xml",dmsname,dmsvalue,"dimen");
		} catch (Exception e) {
			e.printStackTrace();
		}	
    }
    
    public void valuesCommandHelper (final PipeOut out,String valueResourceURL,String valueResourceName,String valueResourceValue,String Type) throws Exception{
    	FileResource<?> valueFile = project.getProjectRoot().getChildOfType(FileResource.class, valueResourceURL);
        if(valueFile.exists()) {
	        Node node = XMLParser.parse(valueFile.getResourceInputStream());
    		Node noderes = node.getOrCreate("resources");
    		List<Node> tempres = noderes.getChildren();
    		for(int i=0;i< tempres.size();i++){
				String existingNameValue = tempres.get(i).getAttribute("name");
				if(existingNameValue.equalsIgnoreCase(valueResourceName))
				{
					final int choiceIndex =shell.promptChoice(messages.getMessage("android.Value.update"),"Update","Exit");
					   if(choiceIndex == 0){
						   tempres.get(i).attribute("name",valueResourceName).text(valueResourceValue);
						   valueFile.setContents(XMLParser.toXMLInputStream(node));
						   ShellMessages.success(out,messages.getMessage("android.Value.successful"));
						   return;}
					   else{
						   ShellMessages.info(out, messages.getMessage("android.Value.skip"));
						   return;}
				}    				
		}
			noderes.createChild(Type).attribute("name", valueResourceName).text(valueResourceValue);
			valueFile.setContents(XMLParser.toXMLInputStream(node));
			ShellMessages.success(out, messages.getMessage("android.Value.successful"));	
	    }
    }
    
    public void fileCreatHelper (final PipeOut outln,final Project project,String fileName ,String templateName,Map<String, TemplateSettings> Context,String type) throws IOException, TemplateException{
    	DirectoryResource projectRoot = project.getProjectRoot();
    	DirectoryResource resDirectory = projectRoot.getOrCreateChildDirectory("res");
    	DirectoryResource valuesDirectory = resDirectory.getOrCreateChildDirectory("values");
    	if(type == "file"){
	    	FileResource<?> File = (FileResource<?>) valuesDirectory.getChild(fileName);
			if (!File.exists()) {
				File FileObj = new File(valuesDirectory.getUnderlyingResourceObject().getPath() + System.getProperty("file.separator") + fileName);
				AndroidPluginUtils.createResourceFileUsingTemplate(project, templateName, FileObj, Context);
				ShellMessages.info(outln, String.format(messages.getKeyValue("SUCCESS_MSG_FMT"), fileName, "file"));
			}
    	}
    	else{
    		InputStream stream = null;
    		DirectoryResource layoutDirectory = resDirectory.getOrCreateChildDirectory("layout");
    		FileResource<?> defaultPropFile = (FileResource<?>) layoutDirectory.getChild(fileName);
    		if (!defaultPropFile.exists()) {
    			stream = AndroidPlugin.class.getResourceAsStream(templateName);
    			defaultPropFile.setContents(stream);
    			ShellMessages.info(outln,String.format(messages.getKeyValue("SUCCESS_MSG_FMT"), fileName, "stream"));
    		}
    	}
    }
    
    
    
  
}