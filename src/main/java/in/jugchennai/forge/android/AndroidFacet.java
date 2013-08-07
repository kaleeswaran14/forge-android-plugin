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

import static in.jugchennai.forge.android.utils.AndroidPluginUtils.capitalize;
import static in.jugchennai.forge.android.utils.AndroidPluginUtils.createResourceFileUsingTemplate;
import static in.jugchennai.forge.android.utils.AndroidPluginUtils.createJavaFileUsingTemplate;
import freemarker.template.TemplateException;
import in.jugchennai.forge.android.utils.MessageUtil;
import in.jugchennai.forge.android.utils.TemplateSettings;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.jboss.forge.maven.MavenCoreFacet;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.dependencies.Dependency;
import org.jboss.forge.project.dependencies.DependencyBuilder;
import org.jboss.forge.project.facets.BaseFacet;
import org.jboss.forge.project.facets.DependencyFacet;
import org.jboss.forge.project.facets.MetadataFacet;
import org.jboss.forge.project.facets.PackagingFacet;
import org.jboss.forge.resources.DirectoryResource;
import org.jboss.forge.resources.FileResource;
import org.jboss.forge.shell.Shell;
import org.jboss.forge.shell.ShellColor;
import org.jboss.forge.shell.ShellMessages;
import org.jboss.forge.shell.ShellPrintWriter;
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
@RequiresFacet({ DependencyFacet.class })
public class AndroidFacet extends BaseFacet {
    public static final String SUCCESS_MSG_FMT = "***SUCCESS*** %s %s has been installed.";

    /** The shell. */
    @Inject
    private Shell shell;

    /** The writer. */
    @Inject
    private ShellPrintWriter writer;

    /** The String resource bundle. */
    public static MessageUtil messages = MessageUtil.INSTANCE;

    @Override
    public boolean install() {
        InputStream stream = null;
        final String androidHome = System.getenv("ANDROID_HOME");
        if (StringUtils.isEmpty(androidHome)) {
            this.writer.println(ShellColor.RED, "Android Home is not set in environment variable ");
            return false;
        }
        installDependencies(getAndroidCoreDependency(), true);
        installplugin(androidBuildPlugin());

        final DirectoryResource projectRoot = this.project.getProjectRoot();

        projectRoot.getChildDirectory("assets").mkdir();

        final DirectoryResource resDirectory = projectRoot.getOrCreateChildDirectory("res");

        resDirectory.getChildDirectory("layout").mkdir();
        resDirectory.getChildDirectory("drawables").mkdir();
        resDirectory.getChildDirectory("drawable-hdpi").mkdir();
        resDirectory.getChildDirectory("drawable-ldpi").mkdir();
        resDirectory.getChildDirectory("drawable-mdpi").mkdir();
        resDirectory.getChildDirectory("values").mkdir();

        final MetadataFacet metadata = this.project.getFacet(MetadataFacet.class);
        final PackagingFacet packagingFacet = this.project.getFacet(PackagingFacet.class);
        
        final String projectName = metadata.getProjectName();
        final TemplateSettings settings = new TemplateSettings(capitalize(projectName) + "Activity", metadata.getTopLevelPackage());
        // app name value which will be inserted in manifest and strings.xml
        settings.setActivityLabelKey("app_name");
        settings.setActivityLabelValue(projectName);

        final Map<String, TemplateSettings> context = new HashMap<String, TemplateSettings>();
        settings.setTopLevelPacakge(metadata.getTopLevelPackage());
        context.put("settings", settings);
        try {
            createJavaFileUsingTemplate(this.project, "TemplateActivity.ftl", context);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TemplateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        ShellMessages.info(this.writer, String.format(AndroidFacet.SUCCESS_MSG_FMT, projectName, "class"));

        // manifest and default.properties file with activity name
        final FileResource<?> manifestFile = (FileResource<?>) projectRoot.getChild("AndroidManifest.xml");
        if (!manifestFile.exists()) {
            final File jnlpTemplate = new File(projectRoot.getUnderlyingResourceObject().getPath() + System.getProperty("file.separator") + "AndroidManifest.xml");
            try {
                createResourceFileUsingTemplate(this.project, "TemplateManifest.ftl", jnlpTemplate, context);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (TemplateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            ShellMessages.info(this.writer, String.format(AndroidFacet.SUCCESS_MSG_FMT, "AndroidManifest.xml", "file"));
        }

        try {
            createAndroidResourceFiles(this.project, "strings.xml", "TemplateStrings.ftl", context, "file");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TemplateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            createAndroidResourceFiles(this.project, "color.xml", "TemplateColors.ftl", context, "file");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TemplateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            createAndroidResourceFiles(this.project, "dimens.xml", "TemplateDimens.ftl", context, "file");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TemplateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            createAndroidResourceFiles(this.project, "main.xml", "/templates/TemplateLayoutMain.ftl", context, "stream");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TemplateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        final FileResource<?> defaultPropFile = (FileResource<?>) projectRoot.getChild("default.properties");
        if (!defaultPropFile.exists()) {
            stream = AndroidPlugin.class.getResourceAsStream("/templates/TemplateProperties.ftl");
            defaultPropFile.setContents(stream);
            ShellMessages.warn(this.writer, String.format(AndroidFacet.SUCCESS_MSG_FMT, "default.properties", "file"));
        }

        return true;
    }

    @Override
    public boolean isInstalled() {

        final DependencyFacet dFacet = this.project.getFacet(DependencyFacet.class);
        if (dFacet.hasDirectDependency(getAndroidCoreDependency())) {
            return true;
        }
        return false;
    }

    /**
     * Method to install the specified Dependencies into the project.
     * 
     * @param dependency the dependency
     * @param askVersion Version of the dependency
     */
    private void installDependencies(final DependencyBuilder dependency, final boolean askVersion) {
        final DependencyFacet dFacet = this.project.getFacet(DependencyFacet.class);
        final List<Dependency> versions = dFacet.resolveAvailableVersions(dependency);
        if (askVersion) {
            final Dependency dep = this.shell.promptChoiceTyped("What version do you want to install ?", versions);
            dependency.setVersion(dep.getVersion());
        }
        dFacet.addDirectDependency(dependency);

        this.writer.println(ShellColor.GREEN, dependency.getArtifactId() + ":" + dependency.getGroupId() + ":" + dependency.getVersion() + " is added to the dependency.");

    }

    /**
     * Android core dependency.
     * 
     * @return the dependency builder
     */
    private static DependencyBuilder getAndroidCoreDependency() {
        return DependencyBuilder.create().setGroupId("com.google.android").setArtifactId("android").setVersion("4.1.1.4").setScopeType("provided");
    }

    /**
     * Method to add the specified plugin into build tag of the project.
     * 
     * @param Plugin the plugin
     */
    private void installplugin(final Plugin plugin) {
        final MavenCoreFacet facet = this.project.getFacet(MavenCoreFacet.class);
        final Model pom = facet.getPOM();
        Build build = pom.getBuild();
        if (build == null) {
            build = new Build();
            pom.setBuild(build);
        }
        pom.getBuild().addPlugin(plugin);
        facet.setPOM(pom);
    }

  

    /**
     * Android build plugins.
     * 
     * @return the dependency builder
     */
    private Plugin androidBuildPlugin() {
        final Plugin mavenAndroidPlugin = new Plugin();
        mavenAndroidPlugin.setGroupId("com.jayway.maven.plugins.android.generation2");
        mavenAndroidPlugin.setArtifactId("maven-android-plugin");
        mavenAndroidPlugin.setConfiguration(pluginConfiguration("7"));
        mavenAndroidPlugin.setVersion("2.8.4");
        mavenAndroidPlugin.setExtensions(true);
        return mavenAndroidPlugin;
    }

    /**
     * Android plugin configuration.
     * 
     * @return the Object
     */
    private Object pluginConfiguration(final String platformVersion) {
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
        } catch (final XmlPullParserException e) {
            throw new IllegalStateException(e);
        } catch (final IOException e) {
            throw new java.lang.IllegalStateException(e);
        }
    }

    public void createAndroidResourceFiles(final Project project, final String fileName, final String templateName, final Map<String, TemplateSettings> Context, final String type) throws IOException,
            TemplateException {
        final DirectoryResource projectRoot = project.getProjectRoot();
        final DirectoryResource resDirectory = projectRoot.getOrCreateChildDirectory("res");
        final DirectoryResource valuesDirectory = resDirectory.getOrCreateChildDirectory("values");
        if (type == "file") {
            final FileResource<?> File = (FileResource<?>) valuesDirectory.getChild(fileName);
            if (!File.exists()) {
                final File FileObj = new File(valuesDirectory.getUnderlyingResourceObject().getPath() + System.getProperty("file.separator") + fileName);
                createResourceFileUsingTemplate(project, templateName, FileObj, Context);
                ShellMessages.info(this.writer, String.format(messages.getKeyValue("SUCCESS_MSG_FMT"), fileName, "file"));
            }
        }
        else {
            InputStream stream = null;
            final DirectoryResource layoutDirectory = resDirectory.getOrCreateChildDirectory("layout");
            final FileResource<?> defaultPropFile = (FileResource<?>) layoutDirectory.getChild(fileName);
            if (!defaultPropFile.exists()) {
                stream = AndroidPlugin.class.getResourceAsStream(templateName);
                defaultPropFile.setContents(stream);
                ShellMessages.info(this.writer, String.format(messages.getKeyValue("SUCCESS_MSG_FMT"), fileName, "stream"));
            }
        }
    }

}