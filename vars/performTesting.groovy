#!/usr/bin/env groovy
def call(Map buildEnv){
  
    pipeline {
       
        agent {
            label getParameterValue(buildEnv, 'AGENT')
        }

        environment{
            def PATH_TO_TAMPLATE_BASE       = getParameterValue(buildEnv, 'PATH_TO_TAMPLATE_BASE')
        }

        post {  //Выполняется после сборки
            always {
                // junit. подсказка: testResults: '**/out/junit/*.xml'
                // allure Подсказка results: [[path: 'out/allure'], [path: 'out/addallure.xml']]
            }
            failure {
                cmdRun("echo Сообщение выводится при ошибке")      
            }
            success {
               // Все хорошо)
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
                                        syntaxCheck(buildEnv) 
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
                                        buildRelise(buildEnv)
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
        // init-dev 
        // compile 
        // updatedb 
    } else {
         // init-dev 
    }  
                
     // run 

    if (fileExists('compile.log')) {
        archiveArtifacts 'compile.log'
    }
      
}

def syntaxCheck(Map buildEnv) {   
    
}

// Дымовое тестирование (BDD)
def tddTesting(Map buildEnv){   
    
}

// Vanessa-Add 
def bddTesting(Map buildEnv){
  
}



def buildRelise(Map buildEnv, String connectionString){

}

def getDBUserCredentialsId() {
   
}

