enkins Pipeline: Docker-multi-container-creation

This allows you to create multiple versions of containers from the same project by using Jenkins parameters (IMAGE, MK_CONT, PORT_NO).
Groovy

pipeline {
    agent any 
    stages {
        stage('Code Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/abhijeet-salunke-in/port1.git'
            }
        }
        stage('Docker Build') {
            steps {
                // Uses the $IMAGE parameter to version the tag
                sh "docker build -t port:v${IMAGE} ."
            }
        }
        stage('Docker Run') {
            steps {
                // Dynamically names the container and assigns the port based on user input
                sh "docker run -itd --name portv${IMAGE}C${MK_CONT} -p ${PORT_NO}:80 port:v${IMAGE}"
            }
        }
    }
}
