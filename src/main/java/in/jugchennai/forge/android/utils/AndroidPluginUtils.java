package in.jugchennai.forge.android.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.forge.parser.JavaParser;
import org.jboss.forge.parser.java.JavaClass;
import org.jboss.forge.parser.xml.Node;
import org.jboss.forge.parser.xml.XMLParser;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.facets.JavaSourceFacet;
import org.jboss.forge.resources.DirectoryResource;
import org.jboss.forge.resources.FileResource;
import org.jboss.forge.shell.plugins.PipeOut;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public final class AndroidPluginUtils {
	
    /** The Constant TEMPLATE_KEY_PACKAGE. */
    public static final String TEMPLATE_KEY_PACKAGE = "package";

    /** The Constant TEMPLATE_KEY_NAME. */
    public static final String TEMPLATE_KEY_NAME = "name";

    /** The Constant TEMPLATE_KEY_PACKAGEIMPORT. */
    public static final String TEMPLATE_KEY_PACKAGEIMPORT = "packageImport";

    /** The Constant TEMPLATE_UNICODE. */
    public static final String TEMPLATE_UNICODE = "UTF-8";

    /** The Constant PACKAGE_DELIMITER. */
    public static final String PACKAGE_DELIMITER = ".";

    /** The resource bundle. */
    public static ResourceBundle resourceBundle = ResourceBundle.getBundle("ResourceBundle");

    private static Configuration cfg = new Configuration();
    static {
        cfg.setClassForTemplateLoading(AndroidPluginUtils.class, "../../../../../templates");
        cfg.setObjectWrapper(new DefaultObjectWrapper());
    }
    
    /**
     * Private constructor.
     */
    private AndroidPluginUtils() {
    	
    }
    
	public static void createResourceFileUsingTemplate(final Project project, final String templateFileName, final File fileObj, final Map<String, TemplateSettings> context) throws IOException, TemplateException {
		Template template = null;
		template = cfg.getTemplate(templateFileName);

		FileWriter fileWriter = new FileWriter(fileObj);
		template.process(context, fileWriter);
		fileWriter.flush();

	}

	public static void createJavaFileUsingTemplate(final Project project, final String templateFileName, final Map<String, TemplateSettings> context) throws IOException, TemplateException {
		final StringWriter writer = new StringWriter();
		Template template = null;
		template = cfg.getTemplate(templateFileName);

		final JavaSourceFacet java = project.getFacet(JavaSourceFacet.class);
		template.process(context, writer);
		writer.flush();

		final JavaClass javaClass = JavaParser.parse(JavaClass.class, writer.toString());
		java.saveJavaSource(javaClass);

	}
	
	public static String capitalize(String orgString) {
		return StringUtils.capitalize(orgString);
		
	}
	
	public static String uncapitalize(String orgString) {
		return StringUtils.uncapitalize(orgString);
	}
	
	public static String getApplicationPackage(final Project project, final PipeOut out) {
        FileResource<?> manifestFile = getManifestFile(project, out);
        if (!manifestFile.exists()) {
        	out.println("Manifest file is not found .");
        	return null;
        }
        Node TemplateManifestxml = XMLParser.parse(manifestFile.getResourceInputStream());
        Node root = TemplateManifestxml.getRoot();
        String manifestPackage = root.getAttribute("package");
		return manifestPackage;
	}

	public static FileResource<?> getManifestFile(final Project project, final PipeOut out) {
		// get acitivity package name
        DirectoryResource projectRoot = project.getProjectRoot();
        FileResource<?> manifestFile = (FileResource<?>) projectRoot.getChild("AndroidManifest.xml");
		return manifestFile;
	}
	
	public static boolean createActivityEntry(final Project project, final PipeOut out, final String activityName, final boolean isLaunchActivity) {
		FileResource<?> manifestFile = getManifestFile(project, out);
		// entry on manifest file
		Node TemplateManifestxml = XMLParser.parse(manifestFile.getResourceInputStream());
		Node root = TemplateManifestxml.getRoot();
		Node applicationNode = root.getSingle("application");
		if (applicationNode == null) {
			applicationNode = root.getOrCreate("application");
		}
        
        // if the activity name already exists in this file / or create it
        List<Node> activityNodes = applicationNode.getChildren();
        boolean activityFound = false;
        if (CollectionUtils.isNotEmpty(activityNodes)) {
        	for (Node activityNode : activityNodes) {
				if (activityName.equals(activityNode.getAttribute("android:name"))) {
					out.println("Activity already exists in manifest.xml file");
					activityFound = true;
					break;
				}
			}
        }
        
        if (!activityFound) { // when the activity is not found
        	Node newActivityNode = applicationNode.createChild("activity").attribute("android:name", activityName);
        	if (isLaunchActivity) {
        		System.out.println("launch activity data adding ... ");
        		Node intentFilter = newActivityNode.createChild("intent-filter");
				intentFilter.createChild("action").attribute("android:name", "android.intent.action.MAIN");
				intentFilter.createChild("action").attribute("android:name", "android.intent.category.LAUNCHER");
        	}
        }
        
        // writing modified manifest data
        InputStream xmlInputStream = XMLParser.toXMLInputStream(TemplateManifestxml);
        manifestFile.setContents(xmlInputStream);
		return true;
	}
	
	public static void copyImage(InputStream from, File targetFile) {
		try {
			BufferedInputStream bis = new BufferedInputStream(from, 4096);
			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(targetFile), 4096);
			int theChar;
			while ((theChar = bis.read()) != -1) {
				bos.write(theChar);
			}
			bos.close();
			bis.close();
			System.out.println("copy done!");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static String executeInShell(String command, String directory) {
		String result = "Success";
	    try {
	    	ProcessBuilder probuilder = new ProcessBuilder(createCommand(command) );
//	        probuilder.directory(new File(directory));
	        probuilder.redirectErrorStream(true);
	        Process process = probuilder.start();
	        InputStream is = process.getInputStream();
	        InputStreamReader isr = new InputStreamReader(is);
	        BufferedReader br = new BufferedReader(isr);
	        String line = "";
	        while ((line = br.readLine()) != null) {
	        	if(line.startsWith("[ERROR]")) {
					result = "Failure";
					break;
				}
	        	System.out.println(line);
	        }
        } catch (Exception e) {
            e.printStackTrace();
        }
		return result;
	}
	
	private static List<String> createCommand(String command) {
		List<String> commands = new ArrayList<String>();
		String property = System.getProperty("os.name");
		if(property.contains("Windows")) {
			commands.add("CMD");
			commands.add("/c");
		} else {
			commands.add("/bin/sh");
			commands.add("-c");
		}
		String[] splitedCommand = command.split(" ");
		if(splitedCommand != null) {
			for (String cmd : splitedCommand) {
				commands.add(cmd);
			}
		}
		return commands;
	}
}
