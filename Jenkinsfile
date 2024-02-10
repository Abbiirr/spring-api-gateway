pipeline {
    agent any

    stages {
        stage ('Build Image') {
            steps {
                checkout scmGit(branches: [[name: '*/dev']])

                script {
                    sh "java -version"
                    sh "./gradlew bootBuildImage --info"
                }
            }
        }

        stage ('Push Docker Image') {
            steps {
                script {
                    def username = sh(
                        script: './gradlew properties | grep -w "registryUsername" | awk -F ": " \'{print $2}\' | awk \'{$1=$1};1\' | tr -d "\\n"',
                        returnStdout: true,
                    )

                    def projectName = sh(
                        script: './gradlew properties | grep -w "name" | awk -F ": " \'{print $2}\' | awk \'{$1=$1};1\' | tr -d "\\n"',
                        returnStdout: true,
                    )

                    def version = sh(
                        script: './gradlew properties | grep -w "version" | awk -F ": " \'{print $2}\' | awk \'{$1=$1};1\' | tr -d "\\n"',
                        returnStdout: true,
                    )

                    // Push the Docker image to the registry
                    sh "docker push $username/$projectName:$version"
                }
            }
        }

        stage ('Rollout Deployment') {
            steps {
                script {
                    def username = sh(
                        script: './gradlew properties | grep -w "registryUsername" | awk -F ": " \'{print $2}\' | awk \'{$1=$1};1\' | tr -d "\\n"',
                        returnStdout: true,
                    )

                    def projectName = sh(
                        script: './gradlew properties | grep -w "name" | awk -F ": " \'{print $2}\' | awk \'{$1=$1};1\' | tr -d "\\n"',
                        returnStdout: true,
                    )

                    def version = sh(
                        script: './gradlew properties | grep -w "version" | awk -F ": " \'{print $2}\' | awk \'{$1=$1};1\' | tr -d "\\n"',
                        returnStdout: true,
                    )

                    // Deployment
                    sh "KUBECONFIG=/home/kubeconfig kubectl get pods --namespace dev"
                    sh "KUBECONFIG=/home/kubeconfig helm upgrade $projectName --namespace dev -f values-dev.yaml oci://registry-1.docker.io/5agitta/sagitta-chart"
                }
            }
        }
    }
}