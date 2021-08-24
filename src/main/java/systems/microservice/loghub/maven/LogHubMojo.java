/*
 * Copyright (C) 2020 Microservice Systems, Inc.
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package systems.microservice.loghub.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import systems.microservice.loghub.sdk.util.Argument;
import systems.microservice.loghub.sdk.util.FileUtil;
import systems.microservice.loghub.sdk.util.StringUtil;
import systems.microservice.loghub.sdk.util.TimeUtil;

/**
 * @author Dmitry Kotlyarov
 * @since 1.0
 */
@Mojo(name = "properties", defaultPhase = LifecyclePhase.GENERATE_RESOURCES, threadSafe = true)
public class LogHubMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project.build.directory}", required = true, readonly = true)
    private String buildDirectory;

    @Parameter(defaultValue = "${project.artifactId}", required = true, readonly = true)
    private String application;

    @Parameter(defaultValue = "${project.version}", required = true, readonly = true)
    private String version;

    @Parameter(defaultValue = "", required = true, readonly = true)
    private String revision;

    public LogHubMojo() {
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Log log = getLog();
        log.info("Generating application meta information for LogHub...");
        String metaDirectory = buildDirectory + "/classes/META-INF";
        String buildTime = TimeUtil.format(System.currentTimeMillis());
        String application = getApplication();
        String version = getVersion();
        String revision = getRevision(null, null, buildTime);
        log.info(String.format("Meta information directory: %s", metaDirectory));
        log.info(String.format("loghub.application: %s", application));
        log.info(String.format("loghub.version: %s", version));
        log.info(String.format("loghub.revision: %s", revision));
        log.info(String.format("loghub.build.time: %s", buildTime));
        FileUtil.storeString(metaDirectory + "/loghub", "application", application);
        FileUtil.storeString(metaDirectory + "/loghub", "version", version);
        FileUtil.storeString(metaDirectory + "/loghub", "revision", revision);
    }

    private String getApplication() {
        String a = System.getenv("LOGHUB_APPLICATION");
        if (a == null) {
            a = System.getProperty("loghub.application");
            if (a == null) {
                a = application;
            }
        }
        return Argument.application("application", a);
    }

    private String getVersion() {
        String v = System.getenv("LOGHUB_VERSION");
        if (v == null) {
            v = System.getProperty("loghub.version");
            if (v == null) {
                v = version;
            }
        }
        return Argument.version("version", v);
    }

    private String getRevision(String branch, String commit, String time) {
        String r = System.getenv("LOGHUB_REVISION");
        if (r == null) {
            r = System.getProperty("loghub.revision");
            if (r == null) {
                r = revision;
                if (r.isEmpty()) {
                    if ((branch != null) && (commit != null)) {
                        r = String.format("%s-%s", StringUtil.slug(branch), commit);
                    } else {
                        r = StringUtil.slug(time);
                    }
                }
            }
        }
        return r;
    }
}
