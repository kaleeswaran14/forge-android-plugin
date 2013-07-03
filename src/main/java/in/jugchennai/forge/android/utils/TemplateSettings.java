package in.jugchennai.forge.android.utils;

/**
 * Parameters to be passed to freemarker templates.
 *
 */
public class TemplateSettings {

    /** The name. */
    private String name = null;
    
    /** The top level pacakge. */
    private String topLevelPacakge = null;
    
    /** The import package. */
    private String importPackage = null;
    
    /**
     * Instantiates a new template settings.
     *
     * @param name of the template to be created
     * @param importPackage source package
     */
    public TemplateSettings(String name, String importPackage) {
        this.name = name;
        this.importPackage = importPackage;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the top level pacakge.
     *
     * @return the top level pacakge
     */
    public String getTopLevelPacakge() {
        return topLevelPacakge;
    }

    /**
     * Sets the top level pacakge.
     *
     * @param topLevelPacakge the new top level pacakge
     */
    public void setTopLevelPacakge(String topLevelPacakge) {
        this.topLevelPacakge = topLevelPacakge;
    }

    /**
     * Gets the import package.
     *
     * @return the import package
     */
    public String getImportPackage() {
        return importPackage;
    }

    /**
     * Sets the import package.
     *
     * @param importPackage the new import package
     */
    public void setImportPackage(String importPackage) {
        this.importPackage = importPackage;
    }
}
