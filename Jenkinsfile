#!groovy

def deployBranches = [ "master" ]
def phase = "verify"

stage ('Build') {
    node {
        checkout scm
        def branch = scm.branches[0].name
        if (deployBranches.contains(branch)) {
            phase = "deploy"
        }
        echo "Running mvn $phase on branch $branch"
        sh 'mkdir -p ~/.gnupg'
        withCredentials([
            file(credentialsId: 'gpg-pubring', variable: 'GPG_PUB_RING'),
            file(credentialsId: 'gpg-secring', variable: 'GPG_SEC_RING'),
            file(credentialsId: 'gradle-settings', variable: 'GRADLE_SETTINGS')]) {
                try {
                    sh "./gradlew $phase -P signing.secretKeyRingFile=$GPG_SEC_RING -P extProps=$GRADLE_SETTINGS"
                } finally {
                    archiveArtifacts 'build/libs/*.jar'
                    archiveArtifacts 'build/libs/*.asc'
                    if (phase == 'deploy') archiveArtifacts 'build/poms/*.xml'
                    if (phase == 'deploy') archiveArtifacts 'build/poms/*.asc'
                    junit allowEmptyResults: true, testResults: 'build/test-results/test/*.xml'
                }
        }
        step([$class: 'WsCleanup'])
    }
}