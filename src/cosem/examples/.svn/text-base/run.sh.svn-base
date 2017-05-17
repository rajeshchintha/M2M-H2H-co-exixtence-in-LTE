#! /bin/bash
#
# Copyright (c) 2013 JMALK                                        
# 
# This script permit to run multiple times the same ns-3 script 
#

# Nota:
# i: index of number of independent runs 
# j: index ot number of simulations for each independent run

#_________________________________Script__________________________________________________#

declare -i a=50; # initial number of Smart Meters for each Data Concentrator

for ((i = 1; i<=2; i++))
do
	clear
	echo
        echo

	for ((j = 0; j<=4; j++))
	do	
                echo "Started simulation #"$i "with "$(($a + $j*$a))" SMs"; date;
		# Run ns-3 script
                ./waf --run "scratch/cosemDcWifiLte --nWifi=$(($a + $j*$a)) --rngRun=$i" > scratch/logDcWifiLte-$(($a + $j*$a))-$i.out 2>&1;
		echo "Finished simulation #"$i "with "$(($a + $j*$a))" SMs"; date;
                echo
                mv scratch/logDcWifiLte-$(($a + $j*$a))-$i.out metricsAMI/	
                mv metrics-$(($a + $j*$a))-$i.txt metricsAMI/	
	done
done

exit 0
