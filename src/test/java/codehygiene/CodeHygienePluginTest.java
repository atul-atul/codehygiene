package codehygiene;

import org.gradle.testfixtures.ProjectBuilder;
import org.gradle.api.Project;
import org.junit.Test;
import static org.junit.Assert.*;

public class CodeHygienePluginTest {
    @Test public void pluginRegistersATask() {
        Project project = ProjectBuilder.builder().build();
        project.getPlugins().apply("codehygiene.secrets");

        assertTrue(project.getPluginManager().hasPlugin("codehygiene.secrets"));
        assertNotNull(project.getTasks().findByName("exposed_secrets"));
    }
}
