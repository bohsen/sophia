import org.apache.logging.log4j.kotlin.logger
import java.nio.file.Path
import java.util.*


/**
 * Client for interacting with SOPHiA CLI tool
 */
class SophiaClient(
    private val processHandler: ProcessHandler = ProcessHandler(),
    private val settings: KeyValueStore = KeyValueStore()
) {

    private val logger = logger()

    /**
     * Check that dependencies like Python is installed
     */
    fun dependencyCheck(): CommandOutput {
        logger.info("Dependency check started")
        logger.info("Checking that Python is installed")
        return processHandler.execute(getPythonExe(), "--version")
    }

    fun login(): CommandOutput {
        logger.info {
            "Login started..."
        }
        val tokenCard = checkNotNull(settings.tokenCard)
        val parser = LoginProcessParser(tokenCard)

        return processHandler.execute(parser)
    }

    fun logout(): CommandOutput {
        logger.info {
            "logout() started..."
        }
        return processHandler.execute(
            getPythonExe(),
            resolvePythonScriptPath(),
            "logout"
        )
    }

    fun getUserInfo(): CommandOutput {
        logger.info {
            "getUserInfo() started..."
        }
        return processHandler.execute(
            getPythonExe(),
            resolvePythonScriptPath(),
            "userInfo"
        )
    }

    fun getPipelines(): CommandOutput {
        logger.info {
            "getPipelines() started..."
        }
        return processHandler.execute(
            getPythonExe(),
            resolvePythonScriptPath(),
            "pipeline",
            "--list"
        )
    }

    fun prepareRun(path: Path, pipeline: Int): CommandOutput {
        logger.info("Preparing run")
        TODO("PS C:\\SOPHiA> python.exe .\\sg-upload-v2-wrapper.py new -j .\\ade.json")
        """
            PS C:\SOPHiA> python.exe .\adegen.py .\STS1_010120\ -o C:\SOPHiA\ade.json
            Prerequisites
             - be logged in with sg-upload-v2-latest.jar
             - all files in target folder should use the same pipeline
            Please do not upload any files containing nominative information or any other direct identifier related to a patient (e.g. patient’s first and/or last names in file name)
            Do you wish to continue? (y/n) y
            ['java', '-jar', './sg-upload-v2-latest.jar']
            Reading FastQ folder
            Found 2 files
            Creating patients
            Already existing patients: 23651549
            All patients already exist
            []
            Listing patients
            [{"medicalInformationId":555099,"personalInformationId":554554,"userRef":"23651549"}]
            Parsing IDs
            Fetching userInfo
            {"userId":83329,"loginUsername":"d.lundsted@rn.dk","clientId":23697,"jwt":"eyJhbGciOiJIUzUxMiJ9.eyJqdGkiOiJTR0FfVVBMT0FERVIiLCJpc3MiOiJERE0iLCJpYXQiOjE2OTIyNzg0MzksImFwcGxpY2F0aW9uVG9rZW4iOiJhcHBUb2tlbiIsImFwcGxpY2F0aW9uU291cmNlIjoiU0dBX1VQTE9BREVSIiwiYXBwbGljYXRpb25UYXJnZXQiOiJERE1fU0dBX0NIQSIsInJvbGVzIjpbIlRLX1JFVk9LRSIsIkRETSIsIlNHQV9VUExPQUQiXSwiY2xpZW50SWQiOjIzNjk3LCJ1c2VySWQiOjgzMzI5fQ.WFGZbwTfNIKX3X87rzRuBehWmngbNxzi5JMocNM59vDv0NSrkF2Qr_5VIIlW__rgDLkM2eVut7c3c84bTV1LwA","baseUrl":"https://api-cha.sophiagenetics.com/"}
            Fetching available pipelines
            Multiple pipelines available
             5959: Custom Solid Tumor Solution
               31: NO_PIPELINE
             2328: Custom Solid Tumor Solution
             1718: SOPHiA DDM Myeloid Solution (MYS_v1)
             2276: SOPHiA DDM Solid Tumor Plus Solution
              159: NO_PIPELINE
               44: NO_PIPELINE
             4388: SOPHiA DDM RNAtarget Oncology Solution
             5290: SOPHiA DDM RNAtarget Oncology Solution
             4387: SOPHiA DDM RNAtarget Oncology Solution
            Enter pipeline ID: 2328
            Json written to C:\SOPHiA\ade.json
        """.trimIndent()
    }

    fun createRun(): CommandOutput {
        TODO("PS C:\\SOPHiA> python.exe .\\sg-upload-v2-wrapper.py new -j .\\ade.json")
    }
    fun uploadRun(): CommandOutput {
        TODO()
    }

    @OptIn(ExperimentalStdlibApi::class)
    private val isWindows = System.getProperty("os.name")
        .lowercase(Locale.getDefault()).startsWith("windows")

    private fun getPythonExe() = if (isWindows) {
        "python.exe"
    } else {
        "python3"
    }

    private fun resolvePythonScriptPath() = getFilePath("sg-upload-v2-wrapper.py")

    private fun getPwFile(): String = getFilePath("pw.txt")

    private fun getFilePath(filename: String): String {
        val classLoader = this.javaClass.classLoader
        val path = classLoader.getResource(filename)?.path
        return path.orEmpty()
    }
}