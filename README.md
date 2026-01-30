# imp_scripts

## 📌 About This Repository

This repository contains **important DevOps and CentOS/Linux scripts** that are commonly used while learning and practicing **real-world DevOps scenarios**.

The purpose of this repository is to:
- Store reusable DevOps scripts
- Automate common tasks
- Understand CI/CD concepts practically
- Maintain scripts for future reference

---

## 🛠️ What This Repository Contains

This repository includes scripts related to:

- Linux / CentOS administration
- Jenkins automation
- Nexus artifact management
- Tomcat deployment and rollback
- CI/CD pipeline support scripts
- DevOps learning and practice scripts

Each script is written to be **simple, readable, and well-commented**.

---

## 🔄 Jenkins + Nexus + Tomcat Rollback Script

One of the key scripts in this repository is a **rollback automation script**.

### What the rollback script does:
- Takes a version as input from Jenkins
- Downloads the selected artifact from Nexus
- Removes the currently deployed application
- Deploys the selected older version to Tomcat
- Restarts Tomcat

### Why rollback is needed:
- If a new deployment fails
- To quickly restore the last working version
- To avoid rebuilding the application
- To reduce downtime

This follows the DevOps principle:

> **Build once, deploy many, rollback safely**

---

## 🧠 DevOps Concepts Covered

By using these scripts, you will understand:

- CI/CD basics
- Jenkins job automation
- Artifact management using Nexus
- Application versioning
- Rollback strategies
- Linux permissions and ownership
- Tomcat application lifecycle

---

## 🚀 How to Use

1. Clone the repository:
   ```bash
   git clone https://github.com/abhijeet-salunke-in/imp_scripts.git
