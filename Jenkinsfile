#!/usr/bin/env groovy

/* Only keep the 10 most recent builds. */
properties([[$class: 'jenkins.model.BuildDiscarderProperty',
                strategy: [$class: 'LogRotator', numToKeepStr: '10']]])

def isPullRequest = !!(env.CHANGE_ID)
def isMultibranch = !!(env.BRANCH_NAME)
String shortCommit = ''

node('docker') {
    stage('Checkout') {
        checkout scm
        sh 'git rev-parse HEAD > GIT_COMMIT'
        shortCommit = readFile('GIT_COMMIT').take(6)

        dir('deploy') {
            echo 'Cloning the latest front-end site for baking our container'
            git 'https://github.com/jenkins-infra/plugin-site.git'
        }
    }

    timestamps {
        stage('Generate Plugin Data') {
            docker.image('maven').inside {
                sh 'mvn -PgeneratePluginData'
            }
        }

        /*
         * Running everything within an nginx container to provide the
         * DATA_FILE_URL necessary for the build and execution of the docker
         * container
         */
        docker.image('nginx:alpine').withRun('-v $PWD/target:/usr/share/nginx/html') { c ->

            /*
             * Building our war file inside a Maven container which links to
             * the nginx container for accessing the DATA_FILE_URL
             */
            stage('Build') {
                docker.image('maven').inside("--link ${c.id}:nginx") {
                    withEnv([
                        'DATA_FILE_URL=http://nginx/plugins.json.gzip',
                    ]) {
                        sh 'mvn -B -Dmaven.test.failure.ignore verify'
                        /* Copy our war file into the deploy directory for easy
                         * COPYing into our container
                         */
                        sh 'cp target/*.war deploy'
                    }
                }

                /** archive all our artifacts for reporting later */
                junit 'target/surefire-reports/**/*.xml'
                archiveArtifacts archives: 'target/*.war', fingerprint: true
            }

            /*
             * Build our application container with some extra parameters to
             * make sure it doesn't leave temporary containers behind on the
             * agent
             */
            def container
            stage('Containerize') {
                container = docker.build("jenkinsciinfra/plugin-site:${env.BUILD_ID}-${shortCommit}",
                                        '--no-cache --rm deploy')
                if (!(isPullRequest || isMultibranch)) {
                    container.push()
                }
            }

            /*
             * Spin up our built container and make sure we can execute API
             * calls against it before calling it successful
             */
            stage('Verify Container') {
                container.withRun("--link ${c.id}:nginx -e DATA_FILE_URL=http://nginx/plugins.json.gzip") { api ->
                    docker.image('maven').inside("--link ${api.id}:api") {
                        sh 'curl -v http://api:8080/versions'
                    }
                }
            }

            stage('Tag container as latest') {
                if (!(isPullRequest || isMultibranch)) {
                    container.push('latest')
                }
            }
        }
    }
}
