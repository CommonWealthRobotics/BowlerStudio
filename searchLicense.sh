#!/bin/bash
for VARIABLE in $(cat DEPENDENCIES_unknown.md)
do
	LOCATION=$(locate -l 1 $VARIABLE)
	echo $VARIABLE
	unzip -l $LOCATION | grep -i "license"
done