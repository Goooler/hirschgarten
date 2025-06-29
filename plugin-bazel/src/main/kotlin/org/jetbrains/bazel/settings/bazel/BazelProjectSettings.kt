package org.jetbrains.bazel.settings.bazel

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StoragePathMacros
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import org.jetbrains.bazel.buildifier.BuildifierUtil
import java.net.URI
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists

data class BazelProjectSettings(
  val projectViewPath: Path? = null,
  val buildifierExecutablePath: Path? = null,
  val runBuildifierOnSave: Boolean = true,
  val showExcludedDirectoriesAsSeparateNode: Boolean = true,
  // experimental settings
) {
  internal fun withNewProjectViewPath(newProjectViewFilePath: Path): BazelProjectSettings = copy(projectViewPath = newProjectViewFilePath)

  fun withNewBuildifierExecutablePath(newBuildifierExecutablePath: Path): BazelProjectSettings =
    copy(buildifierExecutablePath = newBuildifierExecutablePath)

  fun getBuildifierPathString(): String? =
    buildifierExecutablePath?.takeIf { it.exists() }?.toAbsolutePath()?.toString()
      ?: BuildifierUtil.detectBuildifierExecutable()?.absolutePath
}

internal data class BazelProjectSettingsState(
  var projectViewPathUri: String? = null,
  var buildifierExecutablePathUri: String? = null,
  var runBuildifierOnSave: Boolean = true,
  var showExcludedDirectoriesAsSeparateNode: Boolean = true,
) {
  fun isEmptyState(): Boolean = this == BazelProjectSettingsState()
}

@State(
  name = "BazelProjectSettingsService",
  storages = [Storage(StoragePathMacros.WORKSPACE_FILE)],
  reportStatistic = true,
)
@Service(Service.Level.PROJECT)
internal class BazelProjectSettingsService :
  DumbAware,
  PersistentStateComponent<BazelProjectSettingsState> {
  var settings: BazelProjectSettings = BazelProjectSettings()

  override fun getState(): BazelProjectSettingsState =
    BazelProjectSettingsState(
      projectViewPathUri = settings.projectViewPath?.toUri()?.toString(),
      buildifierExecutablePathUri = settings.buildifierExecutablePath?.toUri()?.toString(),
      runBuildifierOnSave = settings.runBuildifierOnSave,
      showExcludedDirectoriesAsSeparateNode = settings.showExcludedDirectoriesAsSeparateNode,
    )

  override fun loadState(settingsState: BazelProjectSettingsState) {
    if (!settingsState.isEmptyState()) {
      this.settings =
        BazelProjectSettings(
          projectViewPath = settingsState.projectViewPathUri?.takeIf { it.isNotBlank() }?.let { Paths.get(URI(it)) },
          buildifierExecutablePath = settingsState.buildifierExecutablePathUri?.takeIf { it.isNotBlank() }?.let { Paths.get(URI(it)) },
          runBuildifierOnSave = settingsState.runBuildifierOnSave,
          showExcludedDirectoriesAsSeparateNode = settingsState.showExcludedDirectoriesAsSeparateNode,
        )
    }
  }

  companion object {
    @JvmStatic
    fun getInstance(project: Project): BazelProjectSettingsService = project.getService(BazelProjectSettingsService::class.java)
  }
}

var Project.bazelProjectSettings: BazelProjectSettings
  get() = BazelProjectSettingsService.getInstance(this).settings.copy()
  set(value) {
    BazelProjectSettingsService.getInstance(this).settings = value.copy()
  }
