const $SimpleDateFormat = Java.loadClass('java.text.SimpleDateFormat')

TipVars.registerDynamic('date', () => {
    return new $SimpleDateFormat("yyyy-MM-dd").format(Date.now())
})
