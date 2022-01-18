#!/bin/sh
#
if [ $# -lt 2 ]; then
  echo "exeGlycomQC.sh NAME name_S??_L001_R1_001.fastq.gz name_S??_L001_R2_001.fastq.gz" 1>&2
  exit 1
fi
#
NAME=$1
READ1=$2
READ2=$3
#
NUM_THREAD=8
ADAPTER1=AGATCGGAAGAGC # AGATCGGAAGAGCACACGTCTGAACTCCAGTCAC
ADAPTER2=AGATCGGAAGAGC # AGATCGGAAGAGCGTCGTGTAGGGAAAGAGTGTA
#
EXE=eval.sh
#EXE=echo
#
SEQ_DIR=sequence
mkdir -p $SEQ_DIR; cd $SEQ_DIR
# -rmHead 0 -rmTail 1
$EXE "countTileFastq.pl -id $NAME -auto-m31Q $READ1 > ${NAME}_R1_identSummary.txt" || exit 1
$EXE "countTileFastq.pl -id $NAME -auto-m31Q $READ2 > ${NAME}_R2_identSummary.txt" || exit 1
cut -f 7 ${NAME}_R2_identSummary.txt | paste ${NAME}_R1_identSummary.txt - > ${NAME}_identSummary.txt
#MAXL=`tail -n 1 ${NAME}_identSummary.txt | cut -f 7`
#
mkdir org; cd org; mv -f ../${NAME}*.fq .
$EXE "skewerFastq.pl -id $NAME -t $NUM_THREAD -a1 $ADAPTER1 -a2 $ADAPTER2 *_R1_*.fq 1> ${NAME}_skewer.txt 2> skewerFastq.err" || exit 1
mv *_skewer.fq ../; mv *.txt ../; cd ..; rename _skewer.fq .fq *_skewer.fq
#$EXE rm -rf org
MAXL=`tail -n 1 ${NAME}_readLengthHist.txt | cut -f 1`
if [[ ! $MAXL =~ ^[0-9]+$ ]]; then
    echo -n "Error exit. MAXL is not defined for $NAME in $0 $* at $HOST " 1>&2
    date 1>&2
    exit 1
fi
$EXE "countIlluminaCalls.pl -id $NAME -t $NUM_THREAD -mL $MAXL ${NAME}*.fq 1> ${NAME}_contentsBaseQ.txt 2> ${NAME}.countIlluminaCalls.log" || exit 1
LANE=`getFastqLane.pl $READ1`
RHEAD=`dirname $READ1`
RHEAD=`dirname $RHEAD | sed -e 's/\/.*\///'`
echo ${RHEAD}${LANE} > ${NAME}.RG-ID.txt
cd ..
