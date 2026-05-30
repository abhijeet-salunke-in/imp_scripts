/*
------------------------------------------------------------------
Pipeline Name : Java Web Application Deployment on Tomcat using jenkins SSH Credentials.

Description:
    This Jenkins pipeline automates the deployment of a Java
    web application to an Apache Tomcat server.

Workflow:
    1. Clone Java application source code from GitHub
    2. Build the application using Maven
    3. Generate WAR file
    4. Deploy WAR file to Apache Tomcat using Jenkins

Tools & Technologies:
    - Jenkins
    - GitHub
    - Maven
    - Apache Tomcat 9
    - SSH Credentials
    - Deploy to Container Plugin

Author : Abhijeet Salunke
------------------------------------------------------------------
*/

pipeline{
    
    // Run pipeline on any available Jenkins agent
    agent any
    
    stages{
        
        /*
        ----------------------------------------------------------
        Stage: Source Code Checkout
        Purpose:
            Pull Java web application source code from GitHub.
        ----------------------------------------------------------
        */
        stage('code'){
            steps{
                git branch: 'main',
                url: 'https://github.com/abhijeet-salunke-in/java3.git'
            }
        }
        
        /*
        ----------------------------------------------------------
        Stage: Maven Build
        Purpose:
            Compile the application and generate WAR artifact.
        ----------------------------------------------------------
        */
        stage('maven build'){
            steps{
                sh "mvn clean package"
            }
        }
        
        /*
        ----------------------------------------------------------
        Stage: Deploy to Tomcat
        Purpose:
            Deploy generated WAR file to Apache Tomcat
            using Jenkins deployment configuration.
        ----------------------------------------------------------
        */
        stage('deploy to tomcat'){
            steps{
                withCredentials([
                    sshUserPrivateKey(
                        credentialsId: 'ssh-id',
                        keyFileVariable: 'SSH_KEY',
                        usernameVariable: 'jenkins_sshID'
                    )
                ]) {

                    deploy(
                        adapters: [
                            tomcat9(
                                alternativeDeploymentContext: '',
                                credentialsId: 'tom_id',
                                path: '',
                                url: 'http://10.0.2.15:8082/'
                            )
                        ],
                        contextPath: 'java1',
                        war: 'target/*.war'
                    )
                }
            }
        }
    }
}
