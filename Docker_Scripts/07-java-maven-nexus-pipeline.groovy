Java Maven Project Deployment to Nexus

File Name: 07-java-maven-nexus-pipeline.groovy

This is a full CI/CD flow for Java applications, including the Maven build stage to create a .war or .jar file before Dockerization.
Manual Steps Summary:

    Maven Build: Run mvn clean package to generate the artifact.

    Dockerfile: Ensure the Dockerfile copies the generated .war file into a Tomcat/JDK base image.

    Nexus Tagging: Tag the image with the Nexus IP and custom port (e.g., 2007).

Jenkins Script:
Groovy

pipeline {
    agent any
    environment {
        NEXUS_URL = "192.168.56.101:2007"
    }
    stages {
        stage('Code') {
            steps {
                git branch: 'main', url: 'https://github.com/abhijeet-salunke-in/java1.git'
            }
        }
        stage('Maven Build') {
            steps {
                // Compiles code and creates the artifact
                sh 'mvn clean package'
            }
        }
        stage('Docker Build') {
            steps {
                sh 'docker build -t webapp:v$VERSION .'
            }
        }
        stage('Nexus Login & Push') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'nexus_id', usernameVariable: 'USER', passwordVariable: 'PASS')]) {
                    sh "echo $PASS | docker login $NEXUS_URL -u $USER --password-stdin"
                    sh "docker tag webapp:v$VERSION $NEXUS_URL/webapp:v$VERSION"
                    sh "docker push $NEXUS_URL/webapp:v$VERSION"
                }
            }
        }
    }
}
