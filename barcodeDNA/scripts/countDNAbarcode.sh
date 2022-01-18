#!/bin/sh
#
usage_exit() {
    CMDNAME=`basename $0`
    echo "Usage: $CMDNAME [-t #remove-first-bases] [-b #remove-last-bases] [-f max-flank-mismatches] [-m max-mid-mismatches] [-l minimum-read-length] [-p number-of-thread] [-e e-value] [-s read1:f or read2:r] blast+DB output.csv ID_S??_L001_R1_001.fastq.gz ID_S??_L001_R2_001.fastq.gz" 1>&2
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
while getopts t:b:f:m:l:p:e:s:h OPT
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
OUTPUT=$2
READ1=$3
READ2=""
if [[ $# -gt 3 ]]; then
    READ2=$4
fi
# Check inputs
if [[ ! -f $BLASTDB ]]; then
    echo "Error. Blast+ DB FASTA file is required: $BLASTDB" 1>&2
    exit 1
fi
BLASTDB=$(cd $(dirname $BLASTDB) && pwd)/$(basename $BLASTDB)
if [[ ! -f $READ1 ]]; then
    echo "Error. FASTQ files are required: $READ1, $READ2" 1>&2
    exit 1
fi
#
GLDIR=$(cd $(dirname $0) && pwd)
EXE=$GLDIR/eval.sh
#EXE=echo
SUFFIX=png
OS=`uname`
if [[ $OS == "Linux" ]]; then
    SUFFIX=eps
fi
#
DBNAME=`basename $BLASTDB .fa`
HEAD1=`basename $READ1 .gz | sed -e 's/.fastq$//'`
FA1=${HEAD1}.fa
$EXE "$GLDIR/fastqToFastaEx.pl -t $RM_FIRST -b $RM_LAST -l $MIN_LENGTH $READ1 1> $FA1" || exit 1
echo -e "#qseqid\tsseqid\tpident\tlength\tmismatch\tgapopen\tqlen\tqstart\tqend\tslen\tsstart\tsend\tevalue\tbitscore" > ${HEAD1}.${DBNAME}.blast+
$EXE "blastn -task blastn-short -num_threads $NUM_THREAD -evalue $EVAL -db $BLASTDB -query $FA1 -outfmt \"6 qseqid sseqid pident length mismatch gapopen qlen qstart qend slen sstart send evalue bitscore\" >> ${HEAD1}.${DBNAME}.blast+" || exit 1
$EXE "$GLDIR/countBarcodeByBlast.pl -f $MAX_FLANK -m $MAX_MID $BLASTDB ${HEAD1}.${DBNAME}.blast+ 1> ${HEAD1}.${DBNAME}.count"
TITLE1=`echo $HEAD1 | sed -e 's/_[0-9][0-9][0-9]$//' | sed -e 's/_S[0-9]\+_L00[0-9]//'`
$EXE "$GLDIR/plotBarcodeCount.R ${HEAD1}.${DBNAME}.count \"${TITLE1} ${DBNAME}\" ${TITLE1} $SUFFIX"
if [[ -f $READ2 ]]; then
    HEAD2=`basename $READ2 .gz | sed -e 's/.fastq$//'`
    FA2=${HEAD2}.fa
    $EXE "$GLDIR/fastqToFastaEx.pl -t $RM_FIRST -b $RM_LAST -l $MIN_LENGTH $READ2 1> $FA2" || exit 1
    echo -e "#qseqid\tsseqid\tpident\tlength\tmismatch\tgapopen\tqlen\tqstart\tqend\tslen\tsstart\tsend\tevalue\tbitscore" > ${HEAD2}.${DBNAME}.blast+
    $EXE "blastn -task blastn-short -num_threads $NUM_THREAD -evalue $EVAL -db $BLASTDB -query $FA2 -outfmt \"6 qseqid sseqid pident length mismatch gapopen qlen qstart qend slen sstart send evalue bitscore\" >> ${HEAD2}.${DBNAME}.blast+" || exit 1
    $EXE "$GLDIR/countBarcodeByBlast.pl -f $MAX_FLANK -m $MAX_MID $BLASTDB ${HEAD2}.${DBNAME}.blast+ 1> ${HEAD2}.${DBNAME}.count"
    TITLE2=`echo $HEAD2 | sed -e 's/_[0-9][0-9][0-9]$//' | sed -e 's/_S[0-9]\+_L00[0-9]//'`
    $EXE "$GLDIR/plotBarcodeCount.R ${HEAD2}.${DBNAME}.count \"${TITLE2} ${DBNAME}\" ${TITLE2} $SUFFIX"
fi
#
if [[ $STRAND == "f" && -f ${HEAD1}.${DBNAME}.count ]]; then
    $EXE "$GLDIR/tsvToCsv.pl ${HEAD1}.${DBNAME}.count 1> $OUTPUT"
elif  [[ $STRAND == "r" && -f ${HEAD2}.${DBNAME}.count ]]; then
    $EXE "$GLDIR/tsvToCsv.pl ${HEAD2}.${DBNAME}.count 1> $OUTPUT"
else
    echo "Strand: -s f|r, your input: $STRAND, and r require R2 fastq file." 1>&2
    exit 1
fi
