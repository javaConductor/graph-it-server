import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy
import ch.qos.logback.core.status.OnConsoleStatusListener

// For syntax, see http://logback.qos.ch/manual/groovy.html
// Logging detail levels: TRACK > DEBUG > INFO > WARN > ERROR

displayStatusOnConsole()
scan('5 minutes') // periodically scan for log configuration changes
setupAppenders()
setupLoggers()

def displayStatusOnConsole() {
    // According to the "logback" documentation, always a good idea to add an on console status listener
    statusListener OnConsoleStatusListener
}

def setupAppenders() {
    appender("FILE", RollingFileAppender) {
        // add a status message regarding the file property
        addInfo("Setting [file] property to [graph-it-server.log]")
        file = "/opt/graphOmatic/graph-it-server/graph-it-server.log"
        rollingPolicy(TimeBasedRollingPolicy) {
            fileNamePattern = "/opt/graphOmatic/graph-it-server/graph-it-server.log.%d{yyyy-MM-dd}.%i"
            timeBasedFileNamingAndTriggeringPolicy(SizeAndTimeBasedFNATP) {
                maxFileSize = "100MB"
            }
        }
        encoder(PatternLayoutEncoder) {
            pattern = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{66} - %msg%n"
        }
    }

    appender("CONSOLE", ConsoleAppender) {
        encoder(PatternLayoutEncoder) {
            pattern = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{66} - %msg%n"
        }
    }
}

def setupLoggers() {
    logger("com.bmc", WARN, ["CONSOLE"]) // Remedy
    logger("com.zaxxer.hikari", WARN, ["CONSOLE"]) // Hikari
    logger("org.apache.http", ERROR, ["CONSOLE"]) // Apache HTTP
    logger("groovyx.net.http", ERROR, ["CONSOLE"]) // Groovy HTTP
    logger("org.springframework", WARN, ["CONSOLE"]) // Spring
    logger("io.github.javaconductor.gserv", DEBUG, ["FILE"]) // gServ
    logger("com.graphomatic", DEBUG, ["CONSOLE", "FILE"]) // App
    //root(DEBUG, ["CONSOLE", "FILE"])
    root(DEBUG, ["FILE"])
}
