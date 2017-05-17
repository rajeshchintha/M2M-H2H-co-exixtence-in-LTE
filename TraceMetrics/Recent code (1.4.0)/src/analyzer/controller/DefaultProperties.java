package analyzer.controller;

import java.util.Properties;

/**
 * for future implementation only
 */
public final class DefaultProperties extends Properties {

    private static Properties alerts = new Properties();
    private static Properties general = new Properties();

    private DefaultProperties(){
        alerts.put("a_fieldWithoutLenght", true);
        alerts.put("a_recovetableWindowAlert", true);

        general.put("g_splashScreen",true);
    }

    public static Properties getDefaultValues(){
        Properties defaults = new DefaultProperties();
        defaults.putAll(alerts);
        defaults.putAll(general);
        return defaults;
    }

}
