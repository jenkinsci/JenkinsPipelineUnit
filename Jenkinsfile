#!groovy

def deployBranches = [ "master", "develop" ]
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
            file(credentialsId: 'gpg-settings', variable: 'MVN_SETTINGS')]) {
                try {
                    sh "mvn clean $phase -P release --settings=$MVN_SETTINGS -Dgpg.publicKeyring=$GPG_PUB_RING -Dgpg.secretKeyring=$GPG_SEC_RING"
                } finally {
                    archiveArtifacts 'target/*.jar'
                    archiveArtifacts 'target/*.asc'
                    archiveArtifacts 'target/*.pom'
                    junit 'target/surefire-reports/*.xml'
                }
        }
        step([$class: 'WsCleanup'])
    }
}