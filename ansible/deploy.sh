#ssh -i ansible/key.pem -o StrictHostKeyChecking=no ubuntu@35.194.75.244 'ls -la'
#ssh -i ansible/key.pem -o StrictHostKeyChecking=no ubuntu@35.186.190.72 'ls -la'
#ssh -i ansible/key.pem -o StrictHostKeyChecking=no ubuntu@35.188.230.154 'ls -la'
#ssh -i ansible/key.pem -o StrictHostKeyChecking=no ubuntu@35.199.58.74 'ls -la'
#ssh -i ansible/key.pem -o StrictHostKeyChecking=no ubuntu@35.230.166.77 'ls -la'
#ssh -i ansible/key.pem -o StrictHostKeyChecking=no ubuntu@35.230.168.111 'ls -la'
#ssh -i ansible/key.pem -o StrictHostKeyChecking=no ubuntu@35.194.88.166 'ls -la'
#ssh -i ansible/key.pem -o StrictHostKeyChecking=no ubuntu@35.221.53.164 'ls -la'
#ssh -i ansible/key.pem -o StrictHostKeyChecking=no ubuntu@35.236.216.126 'ls -la'
#ssh -i ansible/key.pem -o StrictHostKeyChecking=no ubuntu@35.199.11.229 'ls -la'
#ssh -i ansible/key.pem -o StrictHostKeyChecking=no ubuntu@35.194.84.89 'ls -la'
ssh -i ansible/key.pem -o StrictHostKeyChecking=no ubuntu@34.145.185.53 'ls -la'

mkdir ansible/host_vars
#for i in `seq 1 11`; do eval echo "key: \${{ secrets.SERVER${i} }}" >> ansible/host_vars/server${i}; done
#for i in `seq 1 11`; do eval echo "key: \$SERVER${i}" >> ansible/host_vars/server${i}; done
# For 2 containers on one host.
#for i in `seq 12 22`; do j=$(($i - 11)); eval echo "key2: \$SERVER${i}" >> ansible/host_vars/server${j}; done
ansible-playbook --private-key=ansible/key.pem -i ansible/inventory -f 11 -e "del_volume=${DELETE_VOLUME}" -e "image_tag=${IMAGE_TAG}" -e "count=11" -e "user_login=ubuntu" ansible/deploy.yml
