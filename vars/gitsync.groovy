def call(Map buildEnv){

    
    def connectionString = getConnectionString(buildEnv)

    pipeline {
        agent {
            label 'windows'
        } 

        post {  //Выполняется после сборки
            failure {
                // отправляем письмо
            }
 
        }

        environment{
            def PATH_TO_GITSYNC_CONF = getParameterValue(buildEnv, 'PATH_TO_GITSYNC_CONF')
            
        }

        stages {
            stage("gitsync") {
                steps { 
                    script{
                        // gitsync
                    }
                }
            }
        }
    }
}

