@echo off
java -javaagent:target/lib/spring-instrument-5.0.8.RELEASE.jar -Xmx160M -Djava.awt.headless=true -XX:+HeapDumpOnOutOfMemoryError -jar target/service-1.0.jar