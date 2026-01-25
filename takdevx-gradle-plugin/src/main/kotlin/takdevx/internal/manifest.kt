package takdevx.internal

import com.android.build.gradle.tasks.ProcessApplicationManifest
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.provider.Property
import org.w3c.dom.Element
import takdevx.TakdevxProjectExtension
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

internal fun Project.registerManifestModification(extension: TakdevxProjectExtension) {
  pluginManager.withPlugin("com.android.application") {
    val metadataMap = extension.pluginId
      .map { id -> mapOf("plugin-id" to id) }
      .orElse(emptyMap())

    // e.g. processCivDebugMainManifest
    tasks.withType(ProcessApplicationManifest::class.java).configureEach { t ->
      t.inputs.property("metadataMap", metadataMap)
      t.inputs.property("verbose", extension.verbose)
      t.doLast {
        val manifest = t.mergedManifest.get().asFile
        t.modifyManifest(manifest, metadataMap.get(), extension.verbose)
      }
    }
  }
}

@Suppress("HttpUrlsUsage")
private fun Task.modifyManifest(manifestFile: File, metadata: Map<String, String>, verbose: Property<Boolean>) {
  val docFactory = DocumentBuilderFactory.newInstance()
  docFactory.isNamespaceAware = true
  val docBuilder = docFactory.newDocumentBuilder()
  val doc = docBuilder.parse(manifestFile)

  val applicationElement = doc
    .getElementsByTagName("application")
    .item(0) as? Element
    ?: throw GradleException("No <application> element found in $manifestFile")

  val androidNs = "http://schemas.android.com/apk/res/android"
  var changed = false

  metadata.forEach { (name, value) ->
    var existingElement: Element? = null

    // Find existing meta-data element with matching android:name
    val metadataElements = applicationElement.getElementsByTagName("meta-data")
    for (i in 0 until metadataElements.length) {
      val element = metadataElements.item(i) as Element
      if (element.getAttributeNS(androidNs, "name") == name) {
        existingElement = element
        break
      }
    }

    if (existingElement != null) {
      log(verbose, "Updating the meta-data plugin id $value")
      existingElement.setAttributeNS(androidNs, "android:value", value)
      changed = true
    } else {
      log(verbose, "Inserting new meta-data plugin id $value")
      val newElement = doc.createElement("meta-data")
      newElement.setAttributeNS(androidNs, "android:name", name)
      newElement.setAttributeNS(androidNs, "android:value", value)
      applicationElement.appendChild(newElement)
      changed = true
    }
  }

  if (changed) {
    val transformerFactory = TransformerFactory.newInstance()
    val transformer = transformerFactory.newTransformer()
    transformer.setOutputProperty(OutputKeys.INDENT, "yes")
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4")

    val source = DOMSource(doc)
    val result = StreamResult(manifestFile)
    transformer.transform(source, result)
    log(verbose, "Successfully added metadata to $manifestFile: $metadata")
  } else {
    log(verbose, "Didn't add any metadata to $manifestFile")
  }
}
