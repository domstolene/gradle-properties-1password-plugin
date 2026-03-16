package no.domstolene.gradle.properties.onepassword;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtraPropertiesExtension;

public final class OnePasswordGradlePropertiesPlugin implements Plugin<Project> {
    private static final String OP_PREFIX = "op://";

    @Override
    public void apply(Project project) {
        ProjectPropertyResolver resolver = new ProjectPropertyResolver(OpCliClient.fromProject(project));
        ExtraPropertiesExtension extraProperties = project.getExtensions().getExtraProperties();
        project.getProperties().forEach((key, value) -> {
            if (!(value instanceof String stringValue) || !stringValue.startsWith(OP_PREFIX)) {
                return;
            }
            String resolved = resolver.resolve(key, stringValue);
            extraProperties.set(key, resolved);
        });
    }
}
