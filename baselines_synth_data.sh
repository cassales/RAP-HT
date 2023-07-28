#!/bin/bash
ds=$1
stream=$2
core=$3
begin=$4
end=$5
RESDIR=ICDE-results-synthethic
mkdir -p $RESDIR

function X {
	ds=$1
	stream=$2
	iteration=$3
	core=$4
	echo "$ds $stream $iteration $core"
	echo "============================== $ds =============================="
	for method in "MC" "NBAdaptive";
	do
		echo " ++++++++++++ standalone tree $method ++++++++++++ "
		#echo "--- HT gct ---"
#		numactl -C $core java -Xmx24G -cp lib/moa.jar:lib -javaagent:lib/sizeofag-1.0.4.jar moa.DoTask "EvaluateInterleavedTestThenTrain -l (trees.HoeffdingTree -g 50 -c 0.05 -t 0.1 -b -l ${method}) -s ($stream) -e (BasicClassificationPerformanceEvaluator -o -p -r) -i 1000000 -f 100000 -d $RESDIR/HT-${method}-binSp-g50-c005-t01-${ds}-iter${iteration}.csv" > $RESDIR/term-HT-${method}-binSp-g50-c005-t01-${ds}-iter${iteration}
		#echo "--- HT confidence ---"
		#numactl -C $core java -Xmx24G -cp lib/moa.jar:lib -javaagent:lib/sizeofag-1.0.4.jar moa.DoTask "EvaluateInterleavedTestThenTrain -l (trees.HRAPTr -b -l ${method} -c 0.000000001) -s ($stream) -e (BasicClassificationPerformanceEvaluator -o -p -r) -i 1000000 -f 100000 -d $RESDIR/HTconf-${method}-binSp-${ds}-iter${iteration}.csv" > $RESDIR/term-HTconf-${method}-binSp-${ds}-iter${iteration}
		echo "--- HT tieThreshold ---"
		numactl -C $core java -Xmx24G -cp lib/moa.jar:lib -javaagent:lib/sizeofag-1.0.4.jar moa.DoTask "EvaluateInterleavedTestThenTrain -l (trees.HRAPTr -b -l ${method} -t 0.025) -s ($stream) -e (BasicClassificationPerformanceEvaluator -o -p -r) -i 1000000 -f 100000 -d $RESDIR/HTtie-${method}-binSp-${ds}-iter${iteration}.csv" > $RESDIR/term-HTtie-${method}-binSp-${ds}-iter${iteration}
		#echo "--- HT confidence AND tie ---"
		numactl -C $core java -Xmx24G -cp lib/moa.jar:lib -javaagent:lib/sizeofag-1.0.4.jar moa.DoTask "EvaluateInterleavedTestThenTrain -l (trees.HRAPTr -b -l ${method} -c 0.000000001 -t 0.025) -s ($stream) -e (BasicClassificationPerformanceEvaluator -o -p -r) -i 1000000 -f 100000 -d $RESDIR/HTct-${method}-binSp-${ds}-iter${iteration}.csv" > $RESDIR/term-HTct-${method}-binSp-${ds}-iter${iteration}
#		echo "--- EFDT gct ---"
#		numactl -C $core java -Xmx24G -cp lib/moa.jar:lib -javaagent:lib/sizeofag-1.0.4.jar moa.DoTask "EvaluateInterleavedTestThenTrain -l (trees.EFDT -g 50 -c 0.05 -t 0.1 -b -l ${method}) -s ($stream) -e (BasicClassificationPerformanceEvaluator -o -p -r) -i 1000000 -f 100000 -d $RESDIR/EFDT-${method}-binSp-g50-c005-t01-${ds}-iter${iteration}.csv" > $RESDIR/term-EFDT-${method}-binSp-g50-c005-t01-${ds}-iter${iteration}
#		echo "--- EFDT std ---"
#		numactl -C $core java -Xmx24G -cp lib/moa.jar:lib -javaagent:lib/sizeofag-1.0.4.jar moa.DoTask "EvaluateInterleavedTestThenTrain -l (trees.EFDT -b -l ${method}) -s ($stream) -e (BasicClassificationPerformanceEvaluator -o -p -r) -i 1000000 -f 100000 -d $RESDIR/EFDT-${method}-binSp-${ds}-iter${iteration}.csv" > $RESDIR/term-EFDT-${method}-binSp-${ds}-iter${iteration}
#		echo "--- HAT ---"
#		numactl -C $core java -Xmx24G -cp lib/moa.jar:lib -javaagent:lib/sizeofag-1.0.4.jar moa.DoTask "EvaluateInterleavedTestThenTrain -l (trees.HoeffdingAdaptiveTree -b -l ${method}) -s ($stream) -e (BasicClassificationPerformanceEvaluator -o -p -r) -i 1000000 -f 100000 -d $RESDIR/HAT-${method}-binSp-${ds}-iter${iteration}.csv" > $RESDIR/term-HAT-${method}-binSp-${ds}-iter${iteration}
	done

}

# core=0
aux="$stream"

for ((i=$begin; i<=$end; ++i));
do
	echo $i
	X $ds "${aux//JJ/$i}" $i $core
	# X $ds "generators.WaveformGenerator -i $i" $core

done
