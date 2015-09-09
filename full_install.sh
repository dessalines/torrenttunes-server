cd ../torrenttunes-client
git pull
mvn clean install -DskipTests
cd ../torrenttunes-server
git pull
mvn clean install -DskipTests
ps aux | grep -ie torrenttunes | awk '{print $2}' | xargs kill -9
sleep 2
nohup ./run.sh $@ &>log.out &
tail -f log.out
