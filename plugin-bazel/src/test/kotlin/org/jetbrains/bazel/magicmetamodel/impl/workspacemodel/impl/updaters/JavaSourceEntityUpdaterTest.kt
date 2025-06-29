package org.jetbrains.bazel.magicmetamodel.impl.workspacemodel.impl.updaters

import com.intellij.java.workspace.entities.JavaSourceRootPropertiesEntity
import com.intellij.java.workspace.entities.javaSourceRoots
import com.intellij.platform.workspace.jps.entities.ContentRootEntity
import com.intellij.platform.workspace.jps.entities.SourceRootEntity
import com.intellij.platform.workspace.jps.entities.SourceRootTypeId
import com.intellij.platform.workspace.storage.impl.url.toVirtualFileUrl
import org.jetbrains.bazel.sdkcompat.workspacemodel.entities.JavaSourceRoot
import org.jetbrains.bazel.workspace.model.matchers.entries.ExpectedSourceRootEntity
import org.jetbrains.bazel.workspace.model.matchers.entries.shouldBeEqual
import org.jetbrains.bazel.workspace.model.matchers.entries.shouldContainExactlyInAnyOrder
import org.jetbrains.bazel.workspace.model.test.framework.WorkspaceModelWithParentJavaModuleBaseTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.io.path.Path

@DisplayName("javaSourceEntityUpdater.addEntity(entityToAdd, parentModuleEntity) tests")
class JavaSourceEntityUpdaterTest : WorkspaceModelWithParentJavaModuleBaseTest() {
  private lateinit var javaSourceEntityUpdater: JavaSourceEntityUpdater

  @BeforeEach
  override fun beforeEach() {
    // given
    super.beforeEach()

    val workspaceModelEntityUpdaterConfig =
      WorkspaceModelEntityUpdaterConfig(workspaceEntityStorageBuilder, virtualFileUrlManager, projectBasePath, project)
    javaSourceEntityUpdater = JavaSourceEntityUpdater(workspaceModelEntityUpdaterConfig)
  }

  @Test
  fun `should add one java source root to the workspace model`() {
    // given
    val sourceDir = Path("/root/dir/example/package/")
    val generated = false
    val packagePrefix = "example.package"

    val javaSourceRoot =
      JavaSourceRoot(
        sourcePath = sourceDir,
        generated = generated,
        rootType = SourceRootTypeId("java-source"),
        packagePrefix = packagePrefix,
      )

    // when
    val returnedJavaSourceRootEntity =
      runTestWriteAction {
        javaSourceEntityUpdater.addEntity(javaSourceRoot, parentModuleEntity)
      }

    // then
    val virtualSourceDir = sourceDir.toVirtualFileUrl(virtualFileUrlManager)
    val expectedJavaSourceRootEntity =
      ExpectedSourceRootEntity(
        contentRootEntity =
          ContentRootEntity(
            entitySource = parentModuleEntity.entitySource,
            url = virtualSourceDir,
            excludedPatterns = emptyList(),
          ),
        sourceRootEntity =
          SourceRootEntity(
            entitySource = parentModuleEntity.entitySource,
            url = virtualSourceDir,
            rootTypeId = SourceRootTypeId("java-source"),
          ) {
            javaSourceRoots =
              listOf(
                JavaSourceRootPropertiesEntity(
                  entitySource = parentModuleEntity.entitySource,
                  generated = generated,
                  packagePrefix = packagePrefix,
                ),
              )
          },
        parentModuleEntity = parentModuleEntity,
      )

    returnedJavaSourceRootEntity.sourceRoot shouldBeEqual expectedJavaSourceRootEntity
    loadedEntries(SourceRootEntity::class.java) shouldContainExactlyInAnyOrder listOf(expectedJavaSourceRootEntity)
  }

  @Test
  fun `should add multiple java source roots to the workspace model`() {
    // given
    val sourceDir1 = Path("/root/dir/example/package/")
    val generated1 = false
    val packagePrefix1 = "example.package"

    val javaSourceRoot1 =
      JavaSourceRoot(
        sourcePath = sourceDir1,
        generated = generated1,
        rootType = SourceRootTypeId("java-source"),
        packagePrefix = packagePrefix1,
      )

    val sourceDir2 = Path("/another/root/dir/another/example/package/")
    val generated2 = true
    val packagePrefix2 = "another.example.package"

    val javaSourceRoot2 =
      JavaSourceRoot(
        sourcePath = sourceDir2,
        generated = generated2,
        rootType = SourceRootTypeId("java-test"),
        packagePrefix = packagePrefix2,
      )

    val javaSourceRoots = listOf(javaSourceRoot1, javaSourceRoot2)

    // when
    val returnedJavaSourceRootEntries =
      runTestWriteAction {
        javaSourceEntityUpdater.addEntities(javaSourceRoots, parentModuleEntity)
      }

    // then
    val virtualSourceDir1 = sourceDir1.toVirtualFileUrl(virtualFileUrlManager)
    val expectedJavaSourceRootEntity1 =
      ExpectedSourceRootEntity(
        contentRootEntity =
          ContentRootEntity(
            entitySource = parentModuleEntity.entitySource,
            url = virtualSourceDir1,
            excludedPatterns = emptyList(),
          ),
        sourceRootEntity =
          SourceRootEntity(
            entitySource = parentModuleEntity.entitySource,
            url = virtualSourceDir1,
            rootTypeId = SourceRootTypeId("java-source"),
          ) {
            this.javaSourceRoots =
              listOf(
                JavaSourceRootPropertiesEntity(
                  entitySource = parentModuleEntity.entitySource,
                  generated = generated1,
                  packagePrefix = packagePrefix1,
                ),
              )
          },
        parentModuleEntity = parentModuleEntity,
      )

    val virtualSourceDir2 = sourceDir2.toVirtualFileUrl(virtualFileUrlManager)
    val expectedJavaSourceRootEntity2 =
      ExpectedSourceRootEntity(
        contentRootEntity =
          ContentRootEntity(
            entitySource = parentModuleEntity.entitySource,
            url = virtualSourceDir2,
            excludedPatterns = emptyList(),
          ),
        sourceRootEntity =
          SourceRootEntity(
            entitySource = parentModuleEntity.entitySource,
            url = virtualSourceDir2,
            rootTypeId = SourceRootTypeId("java-test"),
          ) {
            this.javaSourceRoots =
              listOf(
                JavaSourceRootPropertiesEntity(
                  entitySource = parentModuleEntity.entitySource,
                  generated = generated2,
                  packagePrefix = packagePrefix2,
                ),
              )
          },
        parentModuleEntity = parentModuleEntity,
      )

    val expectedJavaSourceRootEntries = listOf(expectedJavaSourceRootEntity1, expectedJavaSourceRootEntity2)

    returnedJavaSourceRootEntries.map { it.sourceRoot } shouldContainExactlyInAnyOrder expectedJavaSourceRootEntries
    loadedEntries(SourceRootEntity::class.java) shouldContainExactlyInAnyOrder expectedJavaSourceRootEntries
  }
}
