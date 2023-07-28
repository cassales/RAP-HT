#!/bin/bash
ds=$1
stream=$2
core=$3
begin=$4
end=$5
RESDIR=ICDE-results-ensembles-synthethic
mkdir -p $RESDIR

function X {
	ds=$1
	stream=$2
	iteration=$3
	core=$4
	echo "$ds $stream $iteration $core"
	echo "============================== $ds =============================="
	#for method in "MC" "NBAdaptive";
	for method in "NBAdaptive";
	do
		for ens in "meta.OzaBag";
		do 
			echo " ++++++++++++ standalone tree $method ++++++++++++ "
			echo "--- HT vanilla ---"
			numactl -C $core java -Xmx24G -cp lib/moa.jar:lib -javaagent:lib/sizeofag-1.0.4.jar moa.DoTask "EvaluateInterleavedTestThenTrain -l (meta.OzaBag -l (trees.HRAPTr -b -l ${method}) -s 30) -s ($stream) -e (BasicClassificationPerformanceEvaluator -o -p -r) -i 1000000 -f 100000 -d $RESDIR/HT-${method}-binSp-${ds}-iter${iteration}.csv" > $RESDIR/term-HT-${method}-binSp-${ds}-iter${iteration}
#			echo "--- HT confidence ---"
#			numactl -C $core java -Xmx24G -cp lib/moa.jar:lib -javaagent:lib/sizeofag-1.0.4.jar moa.DoTask "EvaluateInterleavedTestThenTrain -l (meta.OzaBag -l (trees.HRAPTr -b -l ${method} -c 0.000000001) -s 30) -s ($stream) -e (BasicClassificationPerformanceEvaluator -o -p -r) -i 1000000 -f 100000 -d $RESDIR/HTconf-${method}-binSp-${ds}-iter${iteration}.csv" > $RESDIR/term-HTconf-${method}-binSp-${ds}-iter${iteration}
#			echo "--- HT tieThreshold ---"
#			numactl -C $core java -Xmx24G -cp lib/moa.jar:lib -javaagent:lib/sizeofag-1.0.4.jar moa.DoTask "EvaluateInterleavedTestThenTrain -l (meta.OzaBag -l (trees.HRAPTr -b -l ${method} -t 0.025) -s 30) -s ($stream) -e (BasicClassificationPerformanceEvaluator -o -p -r) -i 1000000 -f 100000 -d $RESDIR/HTtie-${method}-binSp-${ds}-iter${iteration}.csv" > $RESDIR/term-HTtie-${method}-binSp-${ds}-iter${iteration}
#			echo "--- HT confidence AND tie ---"
#			numactl -C $core java -Xmx24G -cp lib/moa.jar:lib -javaagent:lib/sizeofag-1.0.4.jar moa.DoTask "EvaluateInterleavedTestThenTrain -l (meta.OzaBag -l (trees.HRAPTr -b -l ${method} -c 0.000000001 -t 0.025) -s 30) -s ($stream) -e (BasicClassificationPerformanceEvaluator -o -p -r) -i 1000000 -f 100000 -d $RESDIR/HTct-${method}-binSp-${ds}-iter${iteration}.csv" > $RESDIR/term-HTct-${method}-binSp-${ds}-iter${iteration}
#			echo "--- EFDT std ---"
#			numactl -C $core java -Xmx24G -cp lib/moa.jar:lib -javaagent:lib/sizeofag-1.0.4.jar moa.DoTask "EvaluateInterleavedTestThenTrain -l (meta.OzaBag -l (trees.EFDT -b -l ${method}) -s 30) -s ($stream) -e (BasicClassificationPerformanceEvaluator -o -p -r) -i 1000000 -f 100000 -d $RESDIR/EFDT-${method}-binSp-${ds}-iter${iteration}.csv" > $RESDIR/term-EFDT-${method}-binSp-${ds}-iter${iteration}
#			echo "--- H-RAPTr-5-2000 ---"
#			numactl -C $core java -Xmx24G -cp lib/moa.jar:lib -javaagent:lib/sizeofag-1.0.4.jar moa.DoTask "EvaluateInterleavedTestThenTrain -l (meta.OzaBag -l (trees.HRAPTr -b -l ${method} -u -T 5 -W 2000) -s 30) -s ($stream) -e (BasicClassificationPerformanceEvaluator -o -p -r) -i 1000000 -f 100000 -d $RESDIR/HRAPTr-T5-W2000-${method}-binSp-${ds}-iter${iteration}.csv" > $RESDIR/term-HRAPTr-T5-W2000-${method}-binSp-${ds}-iter${iteration}
		done
	done

}

# core=0
aux="$stream"

for ((i=$begin; i<=$end; ++i));
do
	echo 
	X $ds "${aux//JJ/$i}" $i $core
	# X $ds "generators.WaveformGenerator -i $i" $core

done
