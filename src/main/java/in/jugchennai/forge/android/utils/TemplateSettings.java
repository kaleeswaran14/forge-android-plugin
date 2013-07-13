package in.jugchennai.forge.android.utils;

/**
 * Parameters to be passed to freemarker templates.
 *
 */
public class TemplateSettings {

    /** The activity name. */
    private String activityName = null;
    
    /** The top level pacakge. */
    private String topLevelPacakge = null;
    
    /** The import package. */
    private String importPackage = null;
    
    /** The activity label key . */
    private String activityLabelKey = null;
    
    /** The activity label value . */
    private String activityLabelValue = null;
    
    /**
     * Instantiates a new template settings.
     *
     * @param name of the template to be created
     * @param importPackage source package
     */
    public TemplateSettings(String activityName, String importPackage) {
        this.activityName = activityName;
        this.importPackage = importPackage;
    }


	/**
     * Gets the activity name.
     *
     * @return the name
     */
    public String getActivityName() {
		return activityName;
	}

    /**
     * Sets the activity name.
     *
     * @param name the new name
     */
	public void setActivityName(String activityName) {
		this.activityName = activityName;
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

    /**
     * Gets the activity label key.
     *
     * @return the activityLabelKey
     */
	public String getActivityLabelKey() {
		return activityLabelKey;
	}

    /**
     * Sets the activity label key.
     *
     * @param activity label key
     */
	public void setActivityLabelKey(String activityLabelKey) {
		this.activityLabelKey = activityLabelKey;
	}

    /**
     * Gets the activity label value.
     *
     * @return the activityLabelValue
     */
	public String getActivityLabelValue() {
		return activityLabelValue;
	}

    /**
     * Sets the activity label value.
     *
     * @param activity label value
     */
	public void setActivityLabelValue(String activityLabelValue) {
		this.activityLabelValue = activityLabelValue;
	}
}
