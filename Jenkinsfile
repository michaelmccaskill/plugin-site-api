#!/usr/bin/env groovy


node('docker') {
    stage('Build') {
        timestamps {
            checkout scm
            docker.image('maven').inside {
                sh 'mvn -B -Dmaven.test.failure.ignore clean verify'
            }

            junit 'target/surefire-reports/**/*.xml'
            archiveArtifacts archives: 'target/**/*.war', fingerprint: true
        }
    }
}
