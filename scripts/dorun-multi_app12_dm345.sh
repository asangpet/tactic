#!/bin/bash
echo "Reset DB..."
ssh root@bench-drupal-db shutdown -r now
sleep 10
echo "Reset Image server..."
ssh root@bench-nfs shutdown -r now
sleep 10
echo "Reset Memcache..."
ssh root@bench-memcache shutdown -r now
sleep 10
echo "Reset Solr..."
ssh root@bench-solr shutdown -r now
sleep 10
echo "Reset Varnish..."
ssh root@varnish shutdown -r now
sleep 10
echo "Reset Appserver 1..."
ssh root@bench-drupal shutdown -r now
sleep 10
echo "Reset Appserver 2..."
ssh root@bench-drupal-2 shutdown -r now
sleep 10
echo "Reset Appserver 3..."
ssh root@bench-drupal-3 shutdown -r now
sleep 10
echo "Reset Appserver 4..."
ssh root@bench-drupal-4 shutdown -r now
sleep 10
echo "Reset Appserver 5..."
ssh root@bench-drupal-5 shutdown -r now
echo "Waiting for system to get up..."
sleep 60
curl http://varnish | grep Drupal
curl http://varnish | grep Drupal
curl http://varnish | grep Drupal
curl http://varnish | grep Drupal
curl http://varnish | grep Drupal
echo "Prep Response DB"
mongo 10.1.3.1/collector_b dropdb.js
echo "Prep Utilization DB"
mongo bench-monitor/collectd dropcollectd.js
sleep 15
curl http://bench-monitor:5100/makeindex
echo "Initiating packet capture"
# For multi_app12_solr345
#ssh root@amdw6 /opt/ccapper -s 10.0.70.1 10.0.50.1 10.0.91.1 10.0.90.1 10.0.60.1 10.0.60.2 &
#ssh root@amdw7 /opt/ccapper -s 10.0.60.3 10.0.60.4 10.0.60.5 10.0.80.1 &

# For multi_app_12345
#ssh root@amdw6 /opt/ccapper -s 10.0.70.1 10.0.50.1 10.0.91.1 10.0.90.1 10.0.80.1 &
#ssh root@amdw7 /opt/ccapper -s 10.0.60.1 10.0.60.2 10.0.60.3 10.0.60.4 10.0.60.5 &

# For multi_app123_vs45
#ssh root@amdw6 /opt/ccapper -s 10.0.70.1 10.0.60.1 10.0.60.2 10.0.60.3 10.0.91.1 10.0.90.1 &
#ssh root@amdw7 /opt/ccapper -s 10.0.50.1 10.0.80.1 10.0.60.4 10.0.60.5 &

# For multi_app123_ds45
#ssh root@amdw6 /opt/ccapper -s 10.0.50.1 10.0.60.1 10.0.60.2 10.0.60.3 10.0.91.1 10.0.90.1 &
#ssh root@amdw7 /opt/ccapper -s 10.0.70.1 10.0.80.1 10.0.60.4 10.0.60.5 &

# For multi_app123_dm45
#ssh root@amdw6 /opt/ccapper -s 10.0.50.1 10.0.60.1 10.0.60.2 10.0.60.3 10.0.91.1 10.0.80.1 &
#ssh root@amdw7 /opt/ccapper -s 10.0.70.1 10.0.60.4 10.0.60.5 10.0.90.1 &

# For multi_app12_dm345
ssh root@amdw6 /opt/ccapper -s 10.0.50.1 10.0.60.1 10.0.60.2 10.0.91.1 10.0.80.1 &
ssh root@amdw7 /opt/ccapper -s 10.0.70.1 10.0.60.3 10.0.60.4 10.0.60.5 10.0.90.1 &

sleep 10
echo "Running test"
bin/tactic drupal_progressive.trace
echo "Gathering stats"
mongodump -h bench-monitor -d collectd
mongodump -h 10.1.3.1 -d collector_b -c responseTime
mv tactic.log dump/
curl -s -H "Content-Type: application/json" http://amdw6:9111/vcontrol/virtualMachine/list | python -msimplejson.tool > dump/guests-amdw6.json
curl -s -H "Content-Type: application/json" http://amdw7:9111/vcontrol/virtualMachine/list | python -msimplejson.tool > dump/guests-amdw7.json
echo "Terminating..."
ssh root@amdw6 pkill ccapper
ssh root@amdw6 pkill tshark
ssh root@amdw7 pkill ccapper
ssh root@amdw7 pkill tshark
