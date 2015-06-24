mvn clean install -DskipTests
java -Xmx1024m -Xms512m -jar target/torrenttunes-server.jar $@
