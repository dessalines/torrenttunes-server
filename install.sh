git pull
mvn clean install -DskipTests
nohup ./run.sh -loglevel debug &>log.out &
tail -f log.out