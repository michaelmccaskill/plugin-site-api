#!/usr/bin/env groovy

pipeline {
    agent { label 'maven' }

    options {
        buildDiscarder(logRotator(numToKeepStr: '7'))
        timestamps()
        timeout(time: 1, unit: 'HOURS')
    }
    triggers {
        cron('H/10 * * * *')
    }

    stages {
        stage('Checkout') {
            steps {
                deleteDir()
                git 'https://github.com/jenkins-infra/plugin-site-api.git'
            }
        }

        stage('Generate') {
            environment {
                PLUGIN_DOCUMENTATION_URL = 'https://updates.jenkins.io/current/plugin-documentation-urls.json'
            }
            steps {
                sh 'mvn -B -PgeneratePluginData'
            }

            post {
                success {
                    dir('target') {
                        archiveArtifacts artifacts: 'plugins.json.gzip', fingerprint: true
                    }
                }
            }
        }
    }
}
