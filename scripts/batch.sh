#!/bin/bash
BATCHNAME=multi_vdn12_345ms
#BATCHNAME=multi_vmn12_345ds
#BATCHNAME=multi_vmns1_2345d
#BATCHNAME=multi_vdms1_2345n
#BATCHNAME=multi_vdnsm1_2345
#BATCHNAME=multi_vdns1_2345m
#BATCHNAME=multi_vdns12_345m
#BATCHNAME=multi_vdns_12345m
#BATCHNAME=multi_app12_sn345
#BATCHNAME=multi_app12_sm345
#BATCHNAME=multi_app12_dm345

#BATCHNAME=multi_app123_ds45
#BATCHNAME=multi_app123_vs45
#BATCHNAME=multi_app_12345
#BATCHNAME=multi_app1_2345
#BATCHNAME=multi_app12_solr345

for i in {1..5}
do
	RUN_ID=${BATCHNAME}_run_${i}
	echo "Executing ${RUN_ID}"
	./dorun.sh
	mv dump result/${RUN_ID}
	mongo 10.1.3.1/collector_b --eval 'db.responseTime.renameCollection("'${RUN_ID}'")'
	echo "Finished ${RUN_ID}"
done
