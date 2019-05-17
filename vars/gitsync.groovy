def call(Map buildEnv){

    
    def connectionString = getConnectionString(buildEnv)

    pipeline {
        agent {
            label getParameterValue(buildEnv, 'AGENT')
        } 

        post {  //Выполняется после сборки
            failure {
                sendEmailMessage("failed", buildEnv.EMAILS_FOR_NOTIFICATION)
            }
 
        }

        environment{
            def PATH_TO_GITSYNC_CONF        = getParameterValue(buildEnv, 'PATH_TO_GITSYNC_CONF')
            def USING_DOCKER                = getParameterValue(buildEnv, 'USING_DOCKER')
            def _DB_USER_CREDENTIONALS_ID    = getParameterValue(buildEnv, 'DB_USER_CREDENTIONALS_ID')
            def V8VERSION                   = getParameterValue(buildEnv, 'V8VERSION')
        }

        stages {
            stage("gitsync") {
                steps { 
                    script{
                        if (fileExists("${PATH_TO_GITSYNC_CONF}")) {
                            comandGitsync = "gitsync %userCredentionalID% -v --v8version ${V8VERSION} --ibconnection ${connectionString}  all ${PATH_TO_GITSYNC_CONF}"                
                            println USING_DOCKER.getClass().toString()
                            if (USING_DOCKER.trim().equals("true")){                             
                                println 'Лог: Запуск синхронизации в контейнере docker'
                                // execGitsyncDocker(buildEnv, comandGitsync)
                            }else{ 
                                try{
                                    DB_USER_CREDENTIONALS_ID = _DB_USER_CREDENTIONALS_ID
                                    cmdRun(comandGitsync, "${DB_USER_CREDENTIONALS_ID}")
                                } catch (err) {
                                    cmdRun(comandGitsync)
                                }
                                    
                            }
                        
                        }else{
                            println "Конфигурационный файл gitsync по пути ${PATH_TO_GITSYNC_CONF} не найден "
                            currentBuild.result = 'FAILURE'
                        }
                    }
                }
            }
        }
    }
}

def call(){
    call([:])  
}

def Выполнить(Map buildEnv){
    call(buildEnv)
}
