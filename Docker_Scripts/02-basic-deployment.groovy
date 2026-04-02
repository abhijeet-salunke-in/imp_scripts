Jenkins Pipeline: Docker-cont-creation

This script automates the process of pulling code, building an image named portfolio, and running it on port 5001.
Groovy

pipeline {
    agent any 
    stages {
        stage('Checkout Code') {
            steps {
                // Fetch code from GitHub
                git branch: 'main', url: 'https://github.com/abhijeet-salunke-in/port1.git'
            }
        }
        stage('Docker Build') {
            steps {
                // Build the image from the Dockerfile in the repo
                sh 'docker build -t portfolio .'
            }   
        }
        stage('Docker Run') {
            steps {
                // Run the container in detached mode (-d) on port 5001
                sh 'docker run -itd --name portfoliocont -p 5001:80 portfolio:latest'
            }
        }
    }
}
