def call(result, MailList){
    if (params.SEND_EMAIL) {
        emailext (
        mimeType: 'text/html',
        subject: "Сборка ${result}: Задача '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
        body: """<p>Сборка ${result}: Задача '${env.JOB_NAME} [${env.BUILD_NUMBER}]':</p>
            <p>Подробная информация по ссылке: "<a href="${env.BUILD_URL}">${env.JOB_NAME} [${env.BUILD_NUMBER}]</a>"</p>""",
        recipientProviders: [[$class: 'DevelopersRecipientProvider']],
        to: "${MailList}"
    )
    } 
}