#!/usr/bin/env groovy
def call(Map buildEnv){
    
    pipeline {
       
        agent {
            label getParameterValue(buildEnv, 'AGENT')
        }

        environment{
            def PATH_TO_TAMPLATE_BASE       = getParameterValue(buildEnv, 'PATH_TO_TAMPLATE_BASE')
            def SOURCE_PATH                 = getParameterValue(buildEnv, 'SOURCE_PATH')
            def V8VERSION                   = getParameterValue(buildEnv, 'V8VERSION')
            def VRUNNER_CONF                = getParameterValue(buildEnv, 'VRUNNER_CONF')
            def _DB_USER_CREDENTIONALS_ID   = getParameterValue(buildEnv, 'DB_USER_CREDENTIONALS_ID')
            def PROCEDURE_SINTAX_CHECK      = getParameterValue(buildEnv, 'PROCEDURE_SINTAX_CHECK')
            def PROCEDURE_TDD_TEST          = getParameterValue(buildEnv, 'PROCEDURE_TDD_TEST')
            def PROCEDURE_BDD_TEST          = getParameterValue(buildEnv, 'PROCEDURE_BDD_TEST')
            def EMAILS_FOR_NOTIFICATION     = getParameterValue(buildEnv, 'EMAILS_FOR_NOTIFICATION')
        }

        post {  //Выполняется после сборки
            always {
                junit allowEmptyResults: true, testResults: '**/out/junit/*.xml'
                allure includeProperties: false, jdk: '', results: [[path: 'out/allure'], [path: 'out/addallure.xml']]
            }
            failure {
                cmdRun("echo Сообщение выводится при ошибке")      
                // cmdRun("vrunner session unlock --ras ${rasString} --db ${env.baseName} --db-user ${Base1C_Usr}  ${lockParams} --v8version ${V8VERSION}")
                sendEmailMessage("failed", EMAILS_FOR_NOTIFICATION)
            }
            success {
               sendEmailMessage("success", EMAILS_FOR_NOTIFICATION)
            } 
        }
    
        stages {
            
            stage("Обновление тестового контура") {
                steps {                      
                    timestamps {
                        script{
                            timeout(20) {
                                prepareBase(buildEnv) 
                            }
                        }
                            
                    }
                }
            }

            stage('Синтаксическая проверка'){
                steps {
                    timestamps {
                        script{
                            timeout(20) {
                                try{
                                    println "LOG: PROCEDURE_SINTAX_CHECK -  ${PROCEDURE_SINTAX_CHECK}"
                                    if(PROCEDURE_SINTAX_CHECK.trim().equals("true")){
                                        syntaxCheck(buildEnv, connectionString) 
                                    } 
                                } catch (err) {
                                    currentBuild.result = 'FAILURE'
                                }
                            }                                                 
                        }
                    }
                }
            }
            
            stage('Дымовое тестирование'){
                steps {
                    timestamps {
                        script{
                            timeout(20) {
                                try{
                                    println "LOG: PROCEDURE_SINTAX_CHECK -  ${PROCEDURE_TDD_TEST}"
                                    if(PROCEDURE_TDD_TEST.trim().equals("true")){
                                        tddTesting(buildEnv)
                                    }
                                } catch (err) {
                                    currentBuild.result = 'FAILURE'
                                }
                            }                             
                        }
                        
                    }
                }
            }

            stage('Функциональное тестирование'){
                steps {
                    timestamps {
                        script{
                            timeout(20) {
                                try{
                                    if(PROCEDURE_BDD_TEST.trim().equals("true")){
                                        bddTesting(buildEnv)
                                    }
                                } catch (err) {
                                    currentBuild.result = 'FAILURE'
                                }
                            }
                        }                   
                    }
                }
            }
            
            stage('Сборка поставки'){
                steps {
                    timestamps {
                        script{
                            timeout(20) {
                                try{
                                    if(currentBuild.result != 'FAILURE'){
                                        buildRelise(buildEnv, connectionString)
                                    }
                                } catch (err) {
                                    currentBuild.result = 'FAILURE'
                                }
                            }
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

// Подготавливаем тестовую базу к работе
def prepareBase(Map buildEnv){
    def connectionString = getConnectionString(buildEnv)
    if (fileExists("${PATH_TO_TAMPLATE_BASE}")) { 
        println "LOG: tamplate DB file exist"
        cmdRun("vrunner init-dev --ibconnection ${connectionString} --dt ${PATH_TO_TAMPLATE_BASE} %userCredentionalID% --v8version ${V8VERSION}", getDBUserCredentialsId())
        cmdRun("vrunner compile --ibconnection ${connectionString} --src=${SOURCE_PATH} %userCredentionalID% -c --noupdate --ibconnection ${connectionString}  --v8version ${V8VERSION}" , getDBUserCredentialsId())
        cmdRun("vrunner updatedb --ibconnection ${connectionString} %userCredentionalID%  --v8version ${V8VERSION}" , getDBUserCredentialsId())
    } else {
        cmdRun("vrunner init-dev --src=${SOURCE_PATH} %userCredentionalID% --v8version ${V8VERSION}" , getDBUserCredentialsId())
    }  
                
    cmdRun("vrunner run --ibconnection ${connectionString} %userCredentionalID% --command 'ЗапуститьОбновлениеИнформационнойБазы;ЗавершитьРаботуСистемы;' --execute \$runnerRoot/epf/ЗакрытьПредприятие.epf" , getDBUserCredentialsId())

    if (fileExists('compile.log')) {
        archiveArtifacts 'compile.log'
    }
      
}

def syntaxCheck(Map buildEnv, String connectionString) {   
    cmdRun("vrunner syntax-check %userCredentionalID%  --junitpath ./out/junit/syntaxCheck.xml --ibconnection ${connectionString}  --v8version ${V8VERSION}" , getDBUserCredentialsId())
}

// Дымовое тестирование (BDD)
def tddTesting(Map buildEnv){
    def connectionString = getConnectionString(buildEnv)   
    cmdRun("vrunner xunit %userCredentionalID% --settings ${VRUNNER_CONF} --ibconnection ${connectionString} --v8version ${V8VERSION}  --testclient ::1538" , getDBUserCredentialsId())
    if (fileExists('log-xunit.txt')) {
        archiveArtifacts 'log-xunit.txt'
    }
}

// Vanessa-Add 
def bddTesting(Map buildEnv){
    def connectionString = getConnectionString(buildEnv)
    cmdRun("runner vanessa %userCredentionalID% --settings ${VRUNNER_CONF}  --ibconnection ${connectionString} --v8version ${V8VERSION}", getDBUserCredentialsId())  

    if (fileExists('vbOnline.log')) {
        archiveArtifacts 'vbOnline.log'
    }
// vbOnline.log
}



def buildRelise(Map buildEnv){
    def connectionString = getConnectionString(buildEnv)
    cmdRun("packman set-database ${connectionString} %userCredentionalID%", getDBUserCredentialsId())
    cmdRun("packman make-cf -v8version ${V8VERSION}")
    
    if (fileExists('.packman/1cv8.cf')) {
        archiveArtifacts '.packman/1cv8.cf'
    }
}

def getDBUserCredentialsId(Map buildEnv) {
    try{
        DB_USER_CREDENTIONALS_ID = _DB_USER_CREDENTIONALS_ID
        return "${DB_USER_CREDENTIONALS_ID}"
    } catch (err) {
        return ""
    }
}

