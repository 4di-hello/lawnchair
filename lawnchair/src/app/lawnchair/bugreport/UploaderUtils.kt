package app.lawnchair.bugreport

object UploaderUtils {

    private val katbinService = KatbinService.create()

    // Fork: no network access except for updates; crash logs can still be copied or shared.
    const val IS_ALIVE_AVAILABLE = false

    suspend fun upload(report: BugReport): String {
        val body = KatbinUploadBody(KatbinPaste(content = report.contents))
        val result = katbinService.upload(body)
        return "https://katb.in/${result.id}"
    }
}
