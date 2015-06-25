mvn clean install -DskipTests
java -Xmx512m -Xms256m -jar target/torrenttunes-server.jar $@
