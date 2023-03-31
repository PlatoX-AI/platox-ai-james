## down
sudo /usr/local/bin/docker-compose -f $( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )/docker-compose.yml down

git -C $( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )/../ pull origin master
# cp  /usr/syno/etc/certificate/_archive/Urop5B/privkey.pem /volume1/docker/linanw/platox-ai-james/docker-deploy/root/conf/
# cp  /usr/syno/etc/certificate/_archive/Urop5B/cert.pem /volume1/docker/linanw/platox-ai-james/docker-deploy/root/conf/

## up
sudo /usr/local/bin/docker-compose -f $( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )/docker-compose.yml up -d

## restart
# sudo /usr/local/bin/docker-compose -f $( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )/docker-compose.yml restart platox-ai-james