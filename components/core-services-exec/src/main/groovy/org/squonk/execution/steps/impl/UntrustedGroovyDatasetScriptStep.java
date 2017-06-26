/*
 * Copyright (c) 2017 Informatics Matters Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.squonk.execution.steps.impl;

import com.github.dockerjava.api.model.AccessMode;
import com.github.dockerjava.api.model.Volume;
import org.squonk.execution.docker.DockerRunner;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Created by timbo on 29/12/15.
 */
public class UntrustedGroovyDatasetScriptStep extends AbstractDockerScriptRunnerStep {

    private static final Logger LOG = Logger.getLogger(UntrustedGroovyDatasetScriptStep.class.getName());



    protected int executeRunner() {
        return runner.execute("run.groovy");
    }

    protected DockerRunner createRunner() throws IOException {
        statusMessage = MSG_PREPARING_CONTAINER;

        String image = getOption(OPTION_DOCKER_IMAGE, String.class);
        if (image == null) {
            image = "squonk/groovy";
        }

        String script = getOption(OPTION_SCRIPT, String.class);
        if (script == null) {
            statusMessage = "Error: Script to execute is not defined";
            throw new IllegalStateException("Script to execute is not defined. Should be present as option named " + OPTION_SCRIPT);
        }
        LOG.info("Docker image: " + image + ", Script: " + script);


        String localWorkDir = "/source";
        DockerRunner runner = createDockerRunner(image, localWorkDir)
                .withNetwork(ISOLATED_NETWORK_NAME);
        Volume maven = runner.addVolume("/var/maven_repo");
        runner.addBind("/var/maven_repo", maven, AccessMode.ro);

        LOG.info("Writing script file");
        runner.writeInput("run.groovy", script);

        return runner;
    }

}
