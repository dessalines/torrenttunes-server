ps aux | grep -ie torrenttunes | awk '{print $2}' | xargs kill -9
git pull
mvn clean install -DskipTests
nohup ./run.sh $@ &>log.out &
tail -f log.out