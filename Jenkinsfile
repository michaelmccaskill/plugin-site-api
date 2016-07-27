#!groovy

node {
  stage 'Checkout'
  echo "Checking out branch ${env.BRANCH_NAME}"
  checkout scm

  stage 'Build & Test'
  withEnv(["PATH+MAVEN=${tool 'M3'}/bin"]) {
    sh "mvn -B -Dmaven.test.failure.ignore clean verify"
    step([$class: 'ArtifactArchiver', artifacts: '**/target/*.war', fingerprint: true])
    step([$class: 'JUnitResultArchiver', testResults: '**/target/surefire-reports/*.xml'])
  }

}
