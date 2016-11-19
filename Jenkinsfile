#!/usr/bin/env groovy

properties([
    /* Only keep the most recent builds. */
    buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '', numToKeepStr: '20')),
    /* build regularly */
    pipelineTriggers([cron('H/10 * * * *')])
])


node('docker') {

    stage('Checkout') {
        /* Make sure we're always starting with a fresh workspace */
        deleteDir()
        git 'https://github.com/jenkins-infra/plugin-site-api.git'
    }

    stage ('Generate') {
        timestamps {
            docker.image('maven').inside {
                sh 'mvn -PgeneratePluginData'
            }
        }
    }

    stage ('Archive') {
        archiveArtifacts artifacts: 'target/*.json.gzip', fingerprint: true
    }
}
