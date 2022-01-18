#!/bin/sh
#
usage_exit() {
    CMDNAME=`basename $0`
    echo "Usage: $CMDNAME [-t #remove-first-bases] [-b #remove-last-bases] [-f max-flank-mismatches] [-m max-mid-mismatches] [-l minimum-read-length] [-p number-of-thread] [-e e-value] [-s read1:f or read2:r] [-x collect-flank-mismatches] [-y collect-mid-mismatches] blast+DB output-prefix *_R1_*.fastq.gz" 1>&2
    exit 1
}
#
echo "$0 $* at $HOST"
#
RM_FIRST=3
RM_LAST=0
MAX_FLANK=2
MAX_MID=1
MIN_LENGTH=20
NUM_THREAD=8
EVAL=1e-5
STRAND="f"
COLLECT_FLANK=2
COLLECT_MID=1
while getopts t:b:f:m:l:p:e:x:y:s:h OPT
do
    case $OPT in
	t) RM_FIRST=$OPTARG
	    ;;
	b) RM_LAST=$OPTARG
	    ;;
	f) MAX_FLANK=$OPTARG
	    ;;
	m) MAX_MID=$OPTARG
	    ;;
	l) MIN_LENGTH=$OPTARG
	    ;;
	p) NUM_THREAD=$OPTARG
	    ;;
	e) EVAL=$OPTARG
	    ;;
	x) COLLECT_FLANK=$OPTARG
	    ;;
	y) COLLECT_MID=$OPTARG
	    ;;
	s) STRAND=$OPTARG
	    ;;
	h) usage_exit
	    ;;
	\?) usage_exit
	    ;;
    esac
done
shift $((OPTIND - 1))
if [ $# -lt 3 ]; then
    usage_exit
fi
BLASTDB=$1
PREFIX=$2
shift 2
R1LIST=$*
#
GLDIR=$(cd $(dirname $0) && pwd)
EXE=$GLDIR/eval.sh
#EXE=echo
CDIR=$PWD
DBNAME=`basename $BLASTDB .fa`
BLASTDB=$(cd $(dirname $BLASTDB) && pwd)/$(basename $BLASTDB)
#
MLIST=""
for f in $R1LIST
do
    if [[ $f =~ Undetermined ]]; then
	continue
    fi
    NAME=`basename $f .gz | sed -e 's/.fastq$//' | sed -e 's/_S[0-9]\+_L[0-9][0-9][0-9]_R.*//'`
    READ1=$(cd $(dirname $f) && pwd)/$(basename $f)
    READ2=`echo $READ1 | sed -e 's/_R1_/_R2_/'`
    $EXE "mkdir -p $NAME"
    cd $NAME
    $EXE "$GLDIR/countDNAbarcode.sh -t $RM_FIRST -b $RM_LAST -f $MAX_FLANK -m $MAX_MID -l $MIN_LENGTH -p $NUM_THREAD -e $EVAL -s $STRAND $BLASTDB ${NAME}.${DBNAME}.csv $READ1 $READ2 > ${NAME}.${DBNAME}.log 2>&1"
    MLIST="$MLIST ${NAME}/${NAME}.${DBNAME}.csv"
    cd $CDIR
done
#OUTPUT_PROP=${PREFIX}.proportions.csv
OUTPUT_PROP=${PREFIX}.percent.csv
OUTPUT_COUN=${PREFIX}.counts.csv
$EXE "$GLDIR/collectMFcsv.pl -t Prop -x $COLLECT_FLANK -y $COLLECT_MID -s $STRAND -fa $BLASTDB $MLIST > $OUTPUT_PROP"
$EXE "$GLDIR/collectMFcsv.pl -t Count -x $COLLECT_FLANK -y $COLLECT_MID -s $STRAND -fa $BLASTDB $MLIST > $OUTPUT_COUN"
