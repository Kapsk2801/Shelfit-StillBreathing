pipeline {
    agent any

    environment {
        DOCKER_HUB = "kapsk"
        K8S_DIR = "k8s" // folder containing YAML manifests
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/Kapsk2801/Shelfit-StillBreathing.git'
            }
        }

        stage('Build JARs') {
            steps {
                script {
                    def services = [
                        "book-service",
                        "config-server",
                        "eureka-server",
                        "api-gateway",
                        "order-service",
                        "user-service",
                        "inventory-service"
                    ]
                    for (svc in services) {
                        dir(svc) {
                            echo "🏗 Building ${svc}..."
                            bat "mvn clean package -DskipTests"
                        }
                    }
                }
            }
        }

        stage('Build & Push Docker Images') {
            steps {
                script {
                    def services = [
                        "book-service",
                        "config-server",
                        "eureka-server",
                        "api-gateway",
                        "order-service",
                        "user-service",
                        "inventory-service"
                    ]

                    withCredentials([usernamePassword(credentialsId: 'dockerhub', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                        bat 'echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin'

                        for (svc in services) {
                            dir(svc) {
                                echo "🐳 Building Docker image for ${svc}..."
                                bat "docker build -t ${DOCKER_HUB}/shelfit-${svc}:latest ."
                                bat "docker push ${DOCKER_HUB}/shelfit-${svc}:latest"
                            }
                        }
                    }
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                script {
                    echo "🚀 Deploying to Kubernetes..."

                    // You will provide kubeconfig as Jenkins secret file
                    withCredentials([file(credentialsId: 'kubeconfig', variable: 'KUBECONFIG_FILE')]) {
                        bat '''
                            mkdir -p ~/.kube
                            cp $KUBECONFIG_FILE ~/.kube/config

                            echo "✅ Using provided kubeconfig"
                            kubectl config current-context

                            echo "🔄 Applying all manifests..."
                            kubectl apply -f ${K8S_DIR}/

                            echo "✅ Checking deployment status..."
                            kubectl get pods -o wide
                            kubectl get svc -o wide
                        '''
                    }
                }
            }
        }

        stage('Cleanup') {
            steps {
                bat 'docker system prune -f'
            }
        }
    }

    post {
        success {
            echo '✅ CI/CD pipeline executed successfully — images pushed & deployed!'
        }
        failure {
            echo '❌ Build or deployment failed!'
        }
    }
}
