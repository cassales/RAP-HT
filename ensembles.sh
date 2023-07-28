#!/bin/bash
core_counter=$1
begin=$2
end=$3

ds="LED_a"
./ensembles_synth_data.sh $ds "ConceptDriftStream -s (generators.LEDGeneratorDrift -d 1 -i JJ) -d (ConceptDriftStream -s (generators.LEDGeneratorDrift -d 3 -i JJ) -d (ConceptDriftStream -s (generators.LEDGeneratorDrift -d 5 -i JJ)  -d (generators.LEDGeneratorDrift -d 7 -i JJ) -w 50 -p 250000) -w 50 -p 250000) -w 50 -p 250000" ${core_counter} $begin $end &
((core_counter=core_counter+1))

ds="LED_g"
./ensembles_synth_data.sh $ds "ConceptDriftStream -s (generators.LEDGeneratorDrift -d 1 -i JJ) -d (ConceptDriftStream -s (generators.LEDGeneratorDrift -d 3 -i JJ) -d (ConceptDriftStream -s (generators.LEDGeneratorDrift -d 5 -i JJ)  -d (generators.LEDGeneratorDrift -d 7 -i JJ) -w 50000 -p 250000) -w 50000 -p 250000) -w 50000 -p 250000" ${core_counter} $begin $end &
((core_counter=core_counter+1))

ds="LED"
./ensembles_synth_data.sh $ds "generators.LEDGenerator -i JJ" ${core_counter} $begin $end &
((core_counter=core_counter+1))

ds="AGR_a"
./ensembles_synth_data.sh $ds "ConceptDriftStream -s (generators.AgrawalGenerator -f 1 -i JJ) -d (ConceptDriftStream -s (generators.AgrawalGenerator -f 2 -i JJ) -d (ConceptDriftStream -s (generators.AgrawalGenerator -f 3 -i JJ) -d (generators.AgrawalGenerator -f 4 -i JJ) -w 50 -p 250000) -w 50 -p 250000) -w 50 -p 250000" ${core_counter} $begin $end &
((core_counter=core_counter+1))

ds="AGR_g"
./ensembles_synth_data.sh $ds "ConceptDriftStream -s (generators.AgrawalGenerator -f 1 -i JJ) -d (ConceptDriftStream -s (generators.AgrawalGenerator -f 2 -i JJ) -d (ConceptDriftStream -s (generators.AgrawalGenerator -f 3 -i JJ) -d (generators.AgrawalGenerator -f 4 -i JJ) -w 50000 -p 250000) -w 50000 -p 250000) -w 50000 -p 250000" ${core_counter} $begin $end &
((core_counter=core_counter+1))

ds="AGR"
./ensembles_synth_data.sh $ds "generators.AgrawalGenerator -i JJ" ${core_counter} $begin $end &
((core_counter=core_counter+1))

ds="RBF"
./ensembles_synth_data.sh $ds "generators.RandomRBFGenerator -r JJ" ${core_counter} $begin $end &
((core_counter=core_counter+1))

ds="RBF_s"
./ensembles_synth_data.sh $ds "generators.RandomRBFGeneratorDrift -s .00001 -i JJ" ${core_counter} $begin $end &
((core_counter=core_counter+1))

ds="RBF_m"
./ensembles_synth_data.sh $ds "generators.RandomRBFGeneratorDrift -s .0001 -i JJ" ${core_counter} $begin $end &
((core_counter=core_counter+1))

ds="RBF_f" 
./ensembles_synth_data.sh $ds "generators.RandomRBFGeneratorDrift -s .001 -i JJ" ${core_counter} $begin $end &
((core_counter=core_counter+1))

ds="HPlane"
./ensembles_synth_data.sh $ds "generators.HyperplaneGenerator -i JJ -k 0" ${core_counter} $begin $end &
((core_counter=core_counter+1))

ds="HPlane_s"
./ensembles_synth_data.sh $ds "generators.HyperplaneGenerator -i JJ -t .00001" ${core_counter} $begin $end &
((core_counter=core_counter+1))

ds="HPlane_m"
./ensembles_synth_data.sh $ds "generators.HyperplaneGenerator -i JJ -t .0001" ${core_counter} $begin $end &
((core_counter=core_counter+1))

ds="HPlane_f"
./ensembles_synth_data.sh $ds "generators.HyperplaneGenerator -i JJ -t .001" ${core_counter} $begin $end &
((core_counter=core_counter+1))

ds="Wave"
./ensembles_synth_data.sh $ds "generators.WaveformGenerator -i JJ" ${core_counter} $begin $end &
((core_counter=core_counter+1))

ds="Wform_d5n"
./ensembles_synth_data.sh $ds "generators.WaveformGeneratorDrift -d 5 -i JJ -n" ${core_counter} $begin $end &
((core_counter=core_counter+1))

ds="RTG"
./ensembles_synth_data.sh $ds "generators.RandomTreeGenerator -i JJ" ${core_counter} $begin $end &
((core_counter=core_counter+1))
