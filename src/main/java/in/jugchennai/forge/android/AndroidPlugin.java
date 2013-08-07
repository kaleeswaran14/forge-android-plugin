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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.jboss.forge.parser.xml.Node;
import org.jboss.forge.parser.xml.XMLParser;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.facets.DependencyFacet;
import org.jboss.forge.project.facets.JavaSourceFacet;
import org.jboss.forge.project.facets.events.InstallFacets;
import org.jboss.forge.resources.DirectoryResource;
import org.jboss.forge.resources.FileResource;
import org.jboss.forge.shell.Shell;
import org.jboss.forge.shell.ShellColor;
import org.jboss.forge.shell.ShellMessages;
import org.jboss.forge.shell.ShellPrintWriter;
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
@RequiresFacet({ DependencyFacet.class, JavaSourceFacet.class, AndroidFacet.class })
@RequiresProject
public class AndroidPlugin implements Plugin {

    /** The shell. */
    @Inject
    private Shell shell;

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
        // , @Option(name = "module", shortName = "m", required = true, help = "The Module name to be installed.") final String moduleName
        if (!this.project.hasFacet(AndroidFacet.class)) {
            this.install.fire(new InstallFacets(AndroidFacet.class));
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
            ShellMessages.info(writer, "Android is installed.");
        } else {
            ShellMessages.warn(writer, "Android is not installed. Use 'android setup' to install.");
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

        String manifestPackage = AndroidPluginUtils.getApplicationPackage(project, out);

        final DirectoryResource sourceFolder = project.getFacet(JavaSourceFacet.class).getSourceFolder();
        String javaFileSyntax = Packages.toFileSyntax(manifestPackage + ".");
        DirectoryResource packageDirectory = sourceFolder.getChildDirectory(javaFileSyntax);
        if (!packageDirectory.exists()) {
            out.println("Package does not exist creating it " + packageDirectory);
            packageDirectory.mkdir();
        }

        String activityName = AndroidPluginUtils.capitalize(name) + "Activity";
        FileResource<?> activityFile = (FileResource<?>) packageDirectory.getChild(activityName + ".java"); // name ends with activity by default
        if (activityFile.exists()) {
            out.println("Activity already exists ");

        } else {
            TemplateSettings settings = new TemplateSettings(activityName, manifestPackage);

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
            @Option(name = "sdk", shortName = "v", required = false, help = "SDK version to build.")
            final String sdk) {
        try {
            shell.execute("mvn clean install -X");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Deploy the application. mvn clean install -Dandroid.device="emulator" -Dandroid.emulator.avd=default -f pom.xml
     * 
     * @param out the out
     * @param device Android device type
     */
    @Command(value = "deploy", help = "Deploy the application")
    public void deploy(
            final PipeOut out,
            @Option(name = "device", shortName = "d", required = false, help = "Android device type.")
            final String device) {
        try {

            shell.execute("mvn android:deploy -X");
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
            @Option(name = "device", shortName = "d", required = false, help = "Android device type.")
            final String device) {
        try {
            shell.execute("mvn android:undeploy -X");
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
            @Option(name = "device", shortName = "d", required = false, help = "Android device type.")
            final String device) {
        try {
            shell.execute("mvn android:run -X");
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
            @Option(name = "device", shortName = "d", required = false, help = "Android device type.")
            final String device) {
        try {

            shell.execute("mvn android:devices");
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
            shell.execute("mvn android:help");
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
            @Option(name = "name", shortName = "n", required = true, help = "name of the device")
            final String name,
            @Option(name = "targetID", shortName = "t", required = true, help = "targetID")
            final String targetID,
            @Option(name = "options", shortName = "o", required = false, help = "extra options can be specified like [-<option> <value>] ... ")
            final String options) {
        try {
            String command = "android create avd -n " + name + " -t " + targetID + " ";
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
            @Option(name = "name", shortName = "n", required = true, help = "name of the device")
            final String name,
            @Option(name = "options", shortName = "o", required = false, help = "extra options can be specified like [-<option> <value>] ... ")
            final String options) {
        try {
            String command = "android move avd -n " + name + " -t ";
            if (StringUtils.isNotEmpty(options)) {
                command = command + options;
            }
            // shell.execute(command);
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
            @Option(name = "name", shortName = "n", required = true, help = "name of the device")
            final String name) {
        try {
            String command = "android delete avd -n " + name + " ";
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
            @Option(name = "name", shortName = "n", required = true, help = "name of the emulator")
            final String name) {
        try {
            shell.execute("mvn android:emulator-start");
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
            @Option(name = "name", shortName = "n", required = true, help = "name of the emulator")
            final String name) {
        try {
            shell.execute("mvn android:emulator-stop");
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
            @Option(name = "name", shortName = "n", required = true, help = "name of the emulator")
            final String name) {
        try {
            shell.execute("mvn android:emulator-stop-all");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Add string to string resource.
     * 
     * @param out the out
     * @param strname the strname
     * @param strvalue the strvalue
     */
    @Command(value = "string-add", help = "Add string to String resource")
    public void stringAdd(final PipeOut out,
            @Option(name = "name", shortName = "n", required = true, help = "Name of string object.")
            final String strname,
            @Option(name = "value", shortName = "v", required = true, help = "String value")
            final String strvalue) {

        try {
            valuesCommandHelper(out, "res/values/strings.xml", strname, strvalue, "string");
        } catch (Exception e) {

            e.printStackTrace();
        }

    }

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
            valuesCommandHelper(out, "res/values/color.xml", colrname, hexValue, "color");
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
            valuesCommandHelper(out, "res/values/dimens.xml", dmsname, dmsvalue, "dimen");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void valuesCommandHelper(final PipeOut out, String valueResourceURL, String valueResourceName, String valueResourceValue, String Type) throws Exception {
        FileResource<?> valueFile = project.getProjectRoot().getChildOfType(FileResource.class, valueResourceURL);
        if (valueFile.exists()) {
            Node node = XMLParser.parse(valueFile.getResourceInputStream());
            Node noderes = node.getOrCreate("resources");
            List<Node> tempres = noderes.getChildren();
            for (int i = 0; i < tempres.size(); i++) {
                String existingNameValue = tempres.get(i).getAttribute("name");
                if (existingNameValue.equalsIgnoreCase(valueResourceName))
                {
                    final int choiceIndex = shell.promptChoice(messages.getMessage("android.Value.update"), "Update", "Exit");
                    if (choiceIndex == 0) {
                        tempres.get(i).attribute("name", valueResourceName).text(valueResourceValue);
                        valueFile.setContents(XMLParser.toXMLInputStream(node));
                        ShellMessages.success(out, messages.getMessage("android.Value.successful"));
                        return;
                    }
                    else {
                        ShellMessages.info(out, messages.getMessage("android.Value.skip"));
                        return;
                    }
                }
            }
            noderes.createChild(Type).attribute("name", valueResourceName).text(valueResourceValue);
            valueFile.setContents(XMLParser.toXMLInputStream(node));
            ShellMessages.success(out, messages.getMessage("android.Value.successful"));
        }
    }

}