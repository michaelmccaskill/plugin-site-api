#!/usr/bin/env groovy


node('docker') {
    checkout scm

    stage('Generate Plugin Data') {
        timestamps {
            docker.iamge('maven').inside {
                sh 'mvn -PgeneratePluginData'
            }
        }
    }

    stage('Build') {
        timestamps {
            docker.image('nginx:alpine').withRun('-p 8080:80 -v $PWD/target:/usr/share/nginx/html') {
                docker.image('maven').inside {
                    withEnv([
                        'DATA_FILE_URL=http://localhost:8080/target/plugins.json.gzip',
                    ]) {
                        sh 'mvn -B -Dmaven.test.failure.ignore clean verify'
                    {
                }
            }'

            junit 'target/surefire-reports/**/*.xml'
            archiveArtifacts archives: 'target/**/*.war', fingerprint: true
        }
    }
}
