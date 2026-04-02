Frontend Deployment to Nexus

File Name: 06-frontend-to-nexus-pipeline.groovy

This pipeline automates the "Tag & Push" process for your HTML/Frontend projects into your private Nexus registry.
Groovy

pipeline {
    agent any
    environment {
        IMAGE_NAME = "port"
        NEXUS_URL = "192.168.56.101:8082"
        // VERSION is passed as a Jenkins Build Parameter
    }
    stages {
        stage('Code Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/abhijeet-salunke-in/port1.git'
            }
        }
        stage('Build Docker Image') {
            steps {
                sh 'docker build -t $IMAGE_NAME:$VERSION .'
            }
        }
        stage('Login & Push') {
            steps {
                // Uses 'nexus_id' from Jenkins Credentials Provider
                withCredentials([usernamePassword(credentialsId: 'nexus_id', usernameVariable: 'USER', passwordVariable: 'PASS')]) {
                    sh "echo $PASS | docker login $NEXUS_URL -u $USER --password-stdin"
                    sh "docker tag $IMAGE_NAME:$VERSION $NEXUS_URL/$IMAGE_NAME:$VERSION"
                    sh "docker push $NEXUS_URL/$IMAGE_NAME:$VERSION"
                }
            }
        }
    }
    post {
        success { echo "✅ Image pushed to Nexus: $IMAGE_NAME:$VERSION" }
        failure { echo "❌ Pipeline failed" }
    }
}
