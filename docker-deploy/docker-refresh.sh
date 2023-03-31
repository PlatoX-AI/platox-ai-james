# sudo /usr/local/bin/docker-compose -f $( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )/docker-compose.yml down
git -C $( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )/../ pull origin master
# sudo /usr/local/bin/docker-compose -d -f $( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )/docker-compose.yml up
sudo /usr/local/bin/docker-compose -f $( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )/docker-compose.yml restart platox-ai-james