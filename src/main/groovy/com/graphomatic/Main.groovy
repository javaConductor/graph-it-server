package com.graphomatic

import com.graphomatic.service.RestService
import groovy.util.logging.Slf4j
import org.springframework.context.support.GenericGroovyApplicationContext
import sun.misc.Signal
import sun.misc.SignalHandler

/**
 * Created by lcollins on 6/27/2015.
 */
@Slf4j
class Main {

    static void main(String[] args){

        GenericGroovyApplicationContext context =
                new GenericGroovyApplicationContext("classpath:AppConfig.groovy");
        RestService restService = context.getBean("restService") as RestService
        restService.start();

        Signal.handle(new Signal("TERM"), new SignalHandler() {
            @Override
            void handle(Signal signal) {
                log.trace("Stopping TERM")
                restService.stop()
                System.exit(-1)
            }
        });
        Signal.handle(new Signal("INT"), new SignalHandler() {
            @Override
            void handle(Signal signal) {
                log.trace("Stopping INT")
                restService.stop()
                System.exit(-1)
            }
        });



    }

}
