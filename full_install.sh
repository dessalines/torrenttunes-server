killit torrenttunes
cd ../torrenttunes-client
git pull
mvn clean install -DskipTests
cd ../torrenttunes-server
git pull
mvn clean install -DskipTests
nohup ./run.sh -loglevel debug &>log.out &
tail -f log.out