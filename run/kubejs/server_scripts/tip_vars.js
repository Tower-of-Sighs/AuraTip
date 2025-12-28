const $SimpleDateFormat = Java.loadClass('java.text.SimpleDateFormat')
const $Date = Java.loadClass('java.util.Date')

TipVars.registerDynamic('date', () => {
    return new $SimpleDateFormat("yyyy-MM-dd").format(new $Date())
})
