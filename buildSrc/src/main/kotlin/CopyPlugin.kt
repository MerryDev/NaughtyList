import org.gradle.api.provider.Property
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

abstract class CopyPlugin : Copy() {

    @get:Optional
    @get:Input
    abstract val runDir: Property<String>

}