#!/bin/bash

#./gradlew showAll>alllibs.txt

sort alllibs.txt |grep .jar|uniq >DEPENDENCIES_shallow.md

echo "" > DEPENDENCIES.md
echo "" > DEPENDENCIES_unknown.md
for VARIABLE in $(cat DEPENDENCIES_shallow.md)
do
    LOCATION=$(locate -l 1 $VARIABLE)
    FILE=$(unzip -l $LOCATION | grep LICENSE|grep -v "LICENSE.")
    stringarray=($FILE)
    LOCENSELOC=$(echo ${stringarray[3]})
    if [ -z "$LOCENSELOC" ]
    then
	FILE=$(unzip -l $LOCATION | grep LICENSE|grep ".txt"|grep -v "documentation")
	stringarray=($FILE)
	LOCENSELOC=$(echo ${stringarray[3]})
	
    fi
    
    if [ -z "$LOCENSELOC" ]
    then
    	echo "$VARIABLE No license file"
	TYPE="No License $LOCATION"
	echo "$VARIABLE , $TYPE">> DEPENDENCIES_unknown.md
    else
        echo "Licance file to be used: $VARIABLE $LOCENSELOC"	   
    	#echo "Searching $VARIABLE for $LOCENSELOC"
	    
	LICENSE=$(unzip -p $LOCATION $LOCENSELOC)

	TYPE=$LICENSE
	if [ -z "$TYPE" ]
	then
	echo "$VARIABLE No license file"
	TYPE="No License $LOCATION"
	else
	    shopt -s nocasematch;
	    if [[ "$LICENSE" =~ "apache" ]]; then
		  TYPE="Apache"
		  #echo "Apache license found"
	    elif   [[ "$LICENSE" =~ "MIT License" ]]; then
	    	TYPE="MIT License"
	    elif   [[ "$LICENSE" =~ "BSD" ]]; then
	    	TYPE="BSD"
	    elif   [[ "$LICENSE" =~ "W3C" ]]; then
	    	TYPE="W3C"
	    elif   [[ "$LICENSE" =~ "CDDL" ]]; then
	    	TYPE="CDDL"	
	    else
	    	echo "  $LOCATION Unknown $LICENSE" 
	    	TYPE="Unknown"
	    	#exit 1	  
	    fi
	fi
	echo "$VARIABLE , $TYPE">> DEPENDENCIES.md
    fi
    
    
done
