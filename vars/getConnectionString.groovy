def call (Map buildEnv) {
    def IS_FILE_CONTUR  = getParameterValue(buildEnv, 'IS_FILE_CONTUR')
    def FILE_BASE_PATH  = getParameterValue(buildEnv, 'FILE_BASE_PATH')
    def SERVER_1C       = getParameterValue(buildEnv, 'SERVER_1C')
    def BASE_NAME       = getParameterValue(buildEnv, 'BASE_NAME')
    
    if (IS_FILE_CONTUR.trim().equals("true")) {
       connectionString = "/F${FILE_BASE_PATH}"
    }else {
        connectionString = "/S${SERVER_1C}\\${BASE_NAME}"
    }
    return connectionString
}