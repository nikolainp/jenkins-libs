
String call(Map buildParams, String keyName){
   
    def defaultParams = getDefaultParams()
    if(env."${keyName}" != null ){
        println "ENV: для ключа ${keyName} найдено значение " +  env."${keyName}"
        return env."${keyName}"
    }else{
        if(buildParams."${keyName}" != null ){
            println "buildParams: для ключа ${keyName} найдено значение " +  buildParams."${keyName}"
            return buildParams."${keyName}"
        }
        if(defaultParams."${keyName}" != ''){
            println "defaultParams: для ключа ${keyName} найдено значение " +  defaultParams."${keyName}"
            return defaultParams."${keyName}"
        }
        println "Значение для ключа не найдено ${keyName} возвращаем пустую строку"
        return new String()
    }
}


def getDefaultParams(){
    return [
        // General
        'IS_FILE_CONTUR': 'true', //Выполнение задачи на файловой базе
        'FILE_BASE_PATH': './build/ib', // 'Путь к файловой базе'
        'USING_DOCKER': 'false', // Выполнять задачу в контейнере DOCKER
        'SEND_EMAIL': 'false', // Рассылка оповещения на почту
        'EMAILS_FOR_NOTIFICATION':"", // Почтовый ящик для уведомлений
        'AGENT': 'windows', // Агент
        'V8VERSION': "8.3", // Версия платформы
        'DB_USER_CREDENTIONALS_ID': "", // Идентификатор Аутентификации пользователя информационной базы

        // Gitsync
        'PATH_TO_GITSYNC_CONF': './tools/JSON/gitsync_conf.JSON', // Путь к конфигурационному файлу GITSYNC
        
        //Ci
        'PATH_TO_TAMPLATE_BASE' :'./examples/demo.dt',
        'SOURCE_PATH':'./src/cf', // Путь к шаблону базы.
        'UCCODE': 'locked', // Пароль блокировки информационной базы 
        'LOCK_MESSAGE': 'Регламентные работы Контуром CI',
        'VRUNNER_CONF': 'tools/JSON/vRunner.json', // Путь к конфигурационному файлу vrunner
        'PROCEDURE_SINTAX_CHECK': 'false', // Выполнять синтаксическую проверку средствами 1с
        'PROCEDURE_TDD_TEST': 'false', // Выполнять TDD тестирование
        'PROCEDURE_BDD_TEST': 'false' // Выполять BDD тестирование

    ]
}

def printDefaultParams(){
    println "${getDefaultParams()}" 
}





