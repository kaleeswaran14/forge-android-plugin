package in.jugchennai.forge.android.utils;

public final class Constants {
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
}
