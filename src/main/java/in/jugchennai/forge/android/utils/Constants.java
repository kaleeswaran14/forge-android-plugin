package in.jugchennai.forge.android.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import java.util.ResourceBundle;

import org.jboss.forge.parser.JavaParser;
import org.jboss.forge.parser.java.JavaClass;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.facets.JavaSourceFacet;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public final class Constants {
	
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
        cfg.setClassForTemplateLoading(Constants.class, "../../../../template");
        cfg.setObjectWrapper(new DefaultObjectWrapper());
    }
    
    /**
     * The Enum Android CreationType.
     */
    public enum CreationType {

        /** The mv. */
        A("ui"), /** The activity. */
        AV("ui.v"); /** The activity and view. */

        String packageName;

        CreationType(String thePackageName) {
            packageName = thePackageName;
        }

        String getPackageName() {
            return packageName;
        }
    }
    
    /**
     * Private constructor.
     */
    private Constants() {
    	
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
}
