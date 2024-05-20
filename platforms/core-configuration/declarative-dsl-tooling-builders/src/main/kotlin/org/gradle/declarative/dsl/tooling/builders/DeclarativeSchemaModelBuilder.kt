/*
 * Copyright 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.declarative.dsl.tooling.builders

import org.gradle.api.Project
import org.gradle.declarative.dsl.schema.AnalysisSchema
import org.gradle.declarative.dsl.tooling.models.DeclarativeSchemaModel
import org.gradle.internal.build.BuildState
import org.gradle.internal.declarativedsl.project.projectEvaluationSchema
import org.gradle.plugin.software.internal.SoftwareTypeRegistry
import org.gradle.tooling.provider.model.ToolingModelBuilder
import org.gradle.tooling.provider.model.internal.BuildScopeModelBuilder
import java.io.Serializable


class DeclarativeSchemaModelBuilder(private val softwareTypeRegistry: SoftwareTypeRegistry) : ToolingModelBuilder, BuildScopeModelBuilder {

    override fun create(target: BuildState?): Any {
        // Make sure the project tree has been loaded and can be queried (but not necessarily configured)
        target?.ensureProjectsLoaded()

        val schema = projectEvaluationSchema(softwareTypeRegistry).analysisSchema
        return DefaultDeclarativeSchemaModel(schema)
    }

    override fun canBuild(modelName: String): Boolean =
        modelName == "org.gradle.declarative.dsl.tooling.models.DeclarativeSchemaModel"

    override fun buildAll(modelName: String, project: Project): Any {
        error("Model should be built before the configuration phase")
    }
}


private
class DefaultDeclarativeSchemaModel(private val projectSchema: AnalysisSchema) : DeclarativeSchemaModel, Serializable {

    override fun getProjectSchema(): AnalysisSchema {
        return projectSchema
    }
}
