package takdevx.internal

import java.io.File

internal data class PathTuple(
  val apiJar: File,
  val keystore: File,
  val mapping: File,
  val coreRules: File,
)
