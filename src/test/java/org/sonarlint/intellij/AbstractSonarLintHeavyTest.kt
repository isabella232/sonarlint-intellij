/*
 * SonarLint for IntelliJ IDEA
 * Copyright (C) 2015-2022 SonarSource
 * sonarlint@sonarsource.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonarlint.intellij

import com.intellij.openapi.module.Module
import com.intellij.serviceContainer.ComponentManagerImpl
import com.intellij.testFramework.HeavyPlatformTestCase
import org.sonarlint.intellij.common.util.SonarLintUtils
import org.sonarlint.intellij.common.util.SonarLintUtils.getService
import org.sonarlint.intellij.config.Settings
import org.sonarlint.intellij.config.global.ServerConnection
import org.sonarlint.intellij.core.EngineManager
import org.sonarlint.intellij.core.ProjectBindingManager
import org.sonarlint.intellij.core.TestEngineManager
import java.nio.file.Path
import java.nio.file.Paths

abstract class AbstractSonarLintHeavyTest : HeavyPlatformTestCase() {

    override fun setUp() {
        super.setUp()
        getEngineManager().clearAllEngines()
    }
    protected fun getTestDataPath(): Path =
        Paths.get("src/test/testData/${javaClass.simpleName}").toAbsolutePath()

    protected fun getEngineManager() =
        getService(EngineManager::class.java) as TestEngineManager

    protected fun connectModuleTo(projectKey: String) {
        connectModuleTo(module, projectKey)
    }

    protected fun connectModuleTo(module: Module, projectKey: String) {
        Settings.getSettingsFor(module).projectKey = projectKey
    }

    protected fun connectProjectTo(connection: ServerConnection, projectKey: String) {
        Settings.getGlobalSettings().addServerConnection(connection)
        Settings.getSettingsFor(project).bindTo(connection, projectKey)
    }

    protected fun unbindProject() {
        getService(project, ProjectBindingManager::class.java).unbind()
    }

    protected open fun <T : Any> replaceProjectService(clazz: Class<T>, newInstance: T) {
        (project as ComponentManagerImpl).replaceServiceInstance(clazz, newInstance, project)
    }
}
