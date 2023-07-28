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
		echo "WINDOW 500"
		echo "--- HRAPTr gct ---"
		numactl -C $core java -Xmx24G -cp lib/moa.jar:lib -javaagent:lib/sizeofag-1.0.4.jar moa.DoTask "EvaluateInterleavedTestThenTrain -l (trees.HRAPTr -g 50 -c 0.05 -t 0.1 -b -l ${method} -u -T 1 -W 500) -s ($stream) -e (BasicClassificationPerformanceEvaluator -o -p -r) -i 1000000 -f 100000 -d $RESDIR/HRAPTr-T1-W500-${method}-binSp-g50-c005-t01-${ds}-iter${iteration}.csv" > $RESDIR/term-HRAPTr-T1-W500-${method}-binSp-g50-c005-t01-${ds}-iter${iteration}
		echo "--- HRAPTr std ---"
		numactl -C $core java -Xmx24G -cp lib/moa.jar:lib -javaagent:lib/sizeofag-1.0.4.jar moa.DoTask "EvaluateInterleavedTestThenTrain -l (trees.HRAPTr -b -l ${method} -u -T 1 -W 500) -s ($stream) -e (BasicClassificationPerformanceEvaluator -o -p -r) -i 1000000 -f 100000 -d $RESDIR/HRAPTr-T1-W500-${method}-binSp-${ds}-iter${iteration}.csv" > $RESDIR/term-HRAPTr-T1-W500-${method}-binSp-${ds}-iter${iteration}
		echo "--- HRAPTr gct ---"
		numactl -C $core java -Xmx24G -cp lib/moa.jar:lib -javaagent:lib/sizeofag-1.0.4.jar moa.DoTask "EvaluateInterleavedTestThenTrain -l (trees.HRAPTr -g 50 -c 0.05 -t 0.1 -b -l ${method} -u -T 5 -W 500) -s ($stream) -e (BasicClassificationPerformanceEvaluator -o -p -r) -i 1000000 -f 100000 -d $RESDIR/HRAPTr-T5-W500-${method}-binSp-g50-c005-t01-${ds}-iter${iteration}.csv" > $RESDIR/term-HRAPTr-T5-W500-${method}-binSp-g50-c005-t01-${ds}-iter${iteration}
		echo "--- HRAPTr std ---"
		numactl -C $core java -Xmx24G -cp lib/moa.jar:lib -javaagent:lib/sizeofag-1.0.4.jar moa.DoTask "EvaluateInterleavedTestThenTrain -l (trees.HRAPTr -b -l ${method} -u -T 5 -W 500) -s ($stream) -e (BasicClassificationPerformanceEvaluator -o -p -r) -i 1000000 -f 100000 -d $RESDIR/HRAPTr-T5-W500-${method}-binSp-${ds}-iter${iteration}.csv" > $RESDIR/term-HRAPTr-T5-W500-${method}-binSp-${ds}-iter${iteration}
		echo "--- HRAPTr gct ---"
		numactl -C $core java -Xmx24G -cp lib/moa.jar:lib -javaagent:lib/sizeofag-1.0.4.jar moa.DoTask "EvaluateInterleavedTestThenTrain -l (trees.HRAPTr -g 50 -c 0.05 -t 0.1 -b -l ${method} -u -T 10 -W 500) -s ($stream) -e (BasicClassificationPerformanceEvaluator -o -p -r) -i 1000000 -f 100000 -d $RESDIR/HRAPTr-T10-W500-${method}-binSp-g50-c005-t01-${ds}-iter${iteration}.csv" > $RESDIR/term-HRAPTr-T10-W500-${method}-binSp-g50-c005-t01-${ds}-iter${iteration}
		echo "--- HRAPTr std ---"
		numactl -C $core java -Xmx24G -cp lib/moa.jar:lib -javaagent:lib/sizeofag-1.0.4.jar moa.DoTask "EvaluateInterleavedTestThenTrain -l (trees.HRAPTr -b -l ${method} -u -T 10 -W 500) -s ($stream) -e (BasicClassificationPerformanceEvaluator -o -p -r) -i 1000000 -f 100000 -d $RESDIR/HRAPTr-T10-W500-${method}-binSp-${ds}-iter${iteration}.csv" > $RESDIR/term-HRAPTr-T10-W500-${method}-binSp-${ds}-iter${iteration}

		echo "WINDOW 2000"
		echo "--- HRAPTr gct ---"
		numactl -C $core java -Xmx24G -cp lib/moa.jar:lib -javaagent:lib/sizeofag-1.0.4.jar moa.DoTask "EvaluateInterleavedTestThenTrain -l (trees.HRAPTr -g 50 -c 0.05 -t 0.1 -b -l ${method} -u -T 1 -W 2000) -s ($stream) -e (BasicClassificationPerformanceEvaluator -o -p -r) -i 1000000 -f 100000 -d $RESDIR/HRAPTr-T1-W2000-${method}-binSp-g50-c005-t01-${ds}-iter${iteration}.csv" > $RESDIR/term-HRAPTr-T1-W2000-${method}-binSp-g50-c005-t01-${ds}-iter${iteration}
		echo "--- HRAPTr std ---"
		numactl -C $core java -Xmx24G -cp lib/moa.jar:lib -javaagent:lib/sizeofag-1.0.4.jar moa.DoTask "EvaluateInterleavedTestThenTrain -l (trees.HRAPTr -b -l ${method} -u -T 1 -W 2000) -s ($stream) -e (BasicClassificationPerformanceEvaluator -o -p -r) -i 1000000 -f 100000 -d $RESDIR/HRAPTr-T1-W2000-${method}-binSp-${ds}-iter${iteration}.csv" > $RESDIR/term-HRAPTr-T1-W2000-${method}-binSp-${ds}-iter${iteration}
		echo "--- HRAPTr gct ---"
		numactl -C $core java -Xmx24G -cp lib/moa.jar:lib -javaagent:lib/sizeofag-1.0.4.jar moa.DoTask "EvaluateInterleavedTestThenTrain -l (trees.HRAPTr -g 50 -c 0.05 -t 0.1 -b -l ${method} -u -T 5 -W 2000) -s ($stream) -e (BasicClassificationPerformanceEvaluator -o -p -r) -i 1000000 -f 100000 -d $RESDIR/HRAPTr-T5-W2000-${method}-binSp-g50-c005-t01-${ds}-iter${iteration}.csv" > $RESDIR/term-HRAPTr-T5-W2000-${method}-binSp-g50-c005-t01-${ds}-iter${iteration}
		echo "--- HRAPTr std ---"
		numactl -C $core java -Xmx24G -cp lib/moa.jar:lib -javaagent:lib/sizeofag-1.0.4.jar moa.DoTask "EvaluateInterleavedTestThenTrain -l (trees.HRAPTr -b -l ${method} -u -T 5 -W 2000) -s ($stream) -e (BasicClassificationPerformanceEvaluator -o -p -r) -i 1000000 -f 100000 -d $RESDIR/HRAPTr-T5-W2000-${method}-binSp-${ds}-iter${iteration}.csv" > $RESDIR/term-HRAPTr-T5-W2000-${method}-binSp-${ds}-iter${iteration}
		echo "--- HRAPTr gct ---"
		numactl -C $core java -Xmx24G -cp lib/moa.jar:lib -javaagent:lib/sizeofag-1.0.4.jar moa.DoTask "EvaluateInterleavedTestThenTrain -l (trees.HRAPTr -g 50 -c 0.05 -t 0.1 -b -l ${method} -u -T 10 -W 2000) -s ($stream) -e (BasicClassificationPerformanceEvaluator -o -p -r) -i 1000000 -f 100000 -d $RESDIR/HRAPTr-T10-W2000-${method}-binSp-g50-c005-t01-${ds}-iter${iteration}.csv" > $RESDIR/term-HRAPTr-T10-W2000-${method}-binSp-g50-c005-t01-${ds}-iter${iteration}
		echo "--- HRAPTr std ---"
		numactl -C $core java -Xmx24G -cp lib/moa.jar:lib -javaagent:lib/sizeofag-1.0.4.jar moa.DoTask "EvaluateInterleavedTestThenTrain -l (trees.HRAPTr -b -l ${method} -u -T 10 -W 2000) -s ($stream) -e (BasicClassificationPerformanceEvaluator -o -p -r) -i 1000000 -f 100000 -d $RESDIR/HRAPTr-T10-W2000-${method}-binSp-${ds}-iter${iteration}.csv" > $RESDIR/term-HRAPTr-T10-W2000-${method}-binSp-${ds}-iter${iteration}

		echo "WINDOW 10000"	
		echo "--- HRAPTr gct ---"
		numactl -C $core java -Xmx24G -cp lib/moa.jar:lib -javaagent:lib/sizeofag-1.0.4.jar moa.DoTask "EvaluateInterleavedTestThenTrain -l (trees.HRAPTr -g 50 -c 0.05 -t 0.1 -b -l ${method} -u -T 1 -W 10000) -s ($stream) -e (BasicClassificationPerformanceEvaluator -o -p -r) -i 1000000 -f 100000 -d $RESDIR/HRAPTr-T1-W10000-${method}-binSp-g50-c005-t01-${ds}-iter${iteration}.csv" > $RESDIR/term-HRAPTr-T1-W10000-${method}-binSp-g50-c005-t01-${ds}-iter${iteration}
		echo "--- HRAPTr std ---"
		numactl -C $core java -Xmx24G -cp lib/moa.jar:lib -javaagent:lib/sizeofag-1.0.4.jar moa.DoTask "EvaluateInterleavedTestThenTrain -l (trees.HRAPTr -b -l ${method} -u -T 1 -W 10000) -s ($stream) -e (BasicClassificationPerformanceEvaluator -o -p -r) -i 1000000 -f 100000 -d $RESDIR/HRAPTr-T1-W10000-${method}-binSp-${ds}-iter${iteration}.csv" > $RESDIR/term-HRAPTr-T1-W10000-${method}-binSp-${ds}-iter${iteration}
		echo "--- HRAPTr gct ---"
		numactl -C $core java -Xmx24G -cp lib/moa.jar:lib -javaagent:lib/sizeofag-1.0.4.jar moa.DoTask "EvaluateInterleavedTestThenTrain -l (trees.HRAPTr -g 50 -c 0.05 -t 0.1 -b -l ${method} -u -T 5 -W 10000) -s ($stream) -e (BasicClassificationPerformanceEvaluator -o -p -r) -i 1000000 -f 100000 -d $RESDIR/HRAPTr-T5-W10000-${method}-binSp-g50-c005-t01-${ds}-iter${iteration}.csv" > $RESDIR/term-HRAPTr-T5-W10000-${method}-binSp-g50-c005-t01-${ds}-iter${iteration}
		echo "--- HRAPTr std ---"
		numactl -C $core java -Xmx24G -cp lib/moa.jar:lib -javaagent:lib/sizeofag-1.0.4.jar moa.DoTask "EvaluateInterleavedTestThenTrain -l (trees.HRAPTr -b -l ${method} -u -T 5 -W 10000) -s ($stream) -e (BasicClassificationPerformanceEvaluator -o -p -r) -i 1000000 -f 100000 -d $RESDIR/HRAPTr-T5-W10000-${method}-binSp-${ds}-iter${iteration}.csv" > $RESDIR/term-HRAPTr-T5-W10000-${method}-binSp-${ds}-iter${iteration}
		echo "--- HRAPTr gct ---"
		numactl -C $core java -Xmx24G -cp lib/moa.jar:lib -javaagent:lib/sizeofag-1.0.4.jar moa.DoTask "EvaluateInterleavedTestThenTrain -l (trees.HRAPTr -g 50 -c 0.05 -t 0.1 -b -l ${method} -u -T 10 -W 10000) -s ($stream) -e (BasicClassificationPerformanceEvaluator -o -p -r) -i 1000000 -f 100000 -d $RESDIR/HRAPTr-T10-W10000-${method}-binSp-g50-c005-t01-${ds}-iter${iteration}.csv" > $RESDIR/term-HRAPTr-T10-W10000-${method}-binSp-g50-c005-t01-${ds}-iter${iteration}
		echo "--- HRAPTr std ---"
		numactl -C $core java -Xmx24G -cp lib/moa.jar:lib -javaagent:lib/sizeofag-1.0.4.jar moa.DoTask "EvaluateInterleavedTestThenTrain -l (trees.HRAPTr -b -l ${method} -u -T 10 -W 10000) -s ($stream) -e (BasicClassificationPerformanceEvaluator -o -p -r) -i 1000000 -f 100000 -d $RESDIR/HRAPTr-T10-W10000-${method}-binSp-${ds}-iter${iteration}.csv" > $RESDIR/term-HRAPTr-T10-W10000-${method}-binSp-${ds}-iter${iteration}
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
