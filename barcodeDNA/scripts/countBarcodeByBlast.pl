#!/usr/bin/env perl
#
# Count exact reference match query
#
use strict;
use warnings;
use File::Basename;
#require("utilities.pl");
#require("sequenceUtil.pl");

&usage() if ( @ARGV < 2 );
{
    my $maxFlank = 2;
    my $maxMid = 1;
    my $sep = "\t";
    my @argsList = ();
    while ( scalar(@ARGV) ) {
	my $arg = shift @ARGV;
	if ( $arg eq "-h" ) {
	    usage();
	} elsif ( $arg eq "-f" ) {
	    $maxFlank = shift @ARGV;
	} elsif ( $arg eq "-m" ) {
	    $maxMid = shift @ARGV;
	} elsif ( $arg eq "-char" ) {
	    $sep = shift @ARGV;
	} else {
	    push( @argsList, $arg );
	}
    }
    &usage() if ( @argsList < 2 );
    my $fastaFile = shift @argsList;
    my $outfmt6File = shift @argsList;
    countBarcodeByBlast( $fastaFile, $maxFlank, $maxMid, $sep, $outfmt6File );
}
exit(0);

#
# Subroutines
#
# usage
sub usage {
    print STDERR "Usage:\n";
    print STDERR "../scripts/countBarcodeByBlast.pl [-f 2] [-m 1] [-char separator-char] barcodes.fa ID.barcodes.blast+ >! ID.barcodes.count.txt\n";
    exit(1);
}

sub countBarcodeByBlast {
    my ( $fastaFile, $maxFlank, $maxMid, $sep, $outfmt6File ) = @_;

    # $idInfo{sseqid}[0] = sequence
    # $idInfo{sseqid}[1] = reverse complementary sequence
    my %idInfo;
    # $idHit{sseqid}[$m][$f][0] = count same strand
    # $idHit{sseqid}[$m][$f][1] = count opposite strand
    # $idHit{sseqid}[$m][$f][2] = count sum
    my %idHit;
    print STDERR "Reading $fastaFile...\n";
    readFastaIdHash( $fastaFile, \%idInfo, $maxFlank, $maxMid, \%idHit );

    print STDERR "Reading $outfmt6File...\n";
    readOutfmt6Hit( $outfmt6File, $maxFlank, $maxMid, \%idHit );

    print STDERR "Outputing...\n";
    outputIdHit( \*STDOUT, $sep, \%idInfo, $maxFlank, $maxMid, \%idHit );
    #outputIdHitCSV( \*STDOUT, \%idInfo, $maxFlank, $maxMid, \%idHit );
}

sub readFastaIdHash {
    my ( $fastaFile, $idInfo, $maxFlank, $maxMid, $idHit ) = @_;

    my $in;
    openFile( $fastaFile, \$in );
    while ( my $line = readline($in) ) {
	$line =~ s/[\r\n]+\z//;
	if ( $line eq "" ) {
	    next;
	} elsif ( $line !~ /^>/ ) {
	    print STDERR "Error. $line\n";
	    exit(1);
	}
	$line =~ s/^>//; # ID
	my $seq = readline($in);
	chomp( $seq );
	$seq =~ s/U/T/gi;
	$seq =~ s/^\s+//;
	$seq =~ s/\s+$//;
	my $rev = revcom($seq);
	if ( exists($$idInfo{$line}) ) {
	    print STDERR "Error. $line $seq are duplicated.\n";
	    next;
	}
	@{$$idInfo{$line}} = ($seq,$rev);
	@{$$idHit{$line}} = ();
	for( my $m = 0; $m <= $maxMid; $m++ ) {
	    @{$$idHit{$line}[$m]} = ();
	    for( my $f = 0; $f <= $maxFlank; $f++ ) {
		@{$$idHit{$line}[$m][$f]} = (0, 0, 0);
	    }
	}
    }
    close( $in );
}

sub readOutfmt6Hit {
    my ( $outfmt6File, $maxFlank, $maxMid, $idHit ) = @_;

    # $qID{qseqid}[0] = in target sseqids
    # $qID{qseqid}[1] = out target sseqids
    my %qID;
    my $maxGap = 0;
    my $in;
    openFile( $outfmt6File, \$in );
    while ( my $line = readline($in) ) {
	$line =~ s/[\r\n]+\z//;
	if ( $line =~ /^#/ || $line eq "" ) {
	    next;
	}
	# $terms[0] = qseqid: query (e.g., gene) sequence id
	# $terms[1] = sseqid: subject (e.g., reference genome) sequence id
	# $terms[2] = pident: percentage of identical matches
	# $terms[3] = length: alignment length
	# $terms[4] = mismatch: number of mismatches
	# $terms[5] = gapopen: Number of gap openings
	# $terms[6] = qlen: Query sequence length
	# $terms[7] = qstart: Start of alignment in query
	# $terms[8] = qend: End of alignment in query
	# $terms[9] = slen: Subject sequence length
	# $terms[10]= sstart: Start of alignment in subject
	# $terms[11]= send: End of alignment in subject
	# $terms[12]= evalue: Expect value
	# $terms[13]= bitscore: Bit score
	my @terms = split( /\t/, $line );
	if ( !exists($$idHit{$terms[1]}) ) {
	    print STDERR "Error. $terms[1] is not found.\n";
	    next;
	}
	if ( !exists($qID{$terms[0]}) ) {
	    @{$qID{$terms[0]}} = ("NA","NA");
	}
	my ($f, $m, $o) = getMismatchNumber( \@terms );
	if ( $f > $maxFlank || $m > $maxMid || $o > $maxGap ) {
	    $qID{$terms[0]}[1] .= ",$terms[1]";
	    next;
	}
	$qID{$terms[0]}[0] .= ",$terms[1]";
	my $s = 0; # Same strand
	if ( $terms[11] < $terms[10] ) {
	    $s = 1;
	}
	for( my $n = $m; $n <= $maxMid; $n++ ) {
	    for( my $g = $f; $g <= $maxFlank; $g++ ) {
		$$idHit{$terms[1]}[$n][$g][$s]++;
		$$idHit{$terms[1]}[$n][$g][2]++;
	    }
	}
    }
    close($in);
    outputQueryHits( basename($outfmt6File,".blast+"), \%qID );
}

sub getMismatchNumber {
    my ( $terms ) = @_;

    my $f = $$terms[6] - $$terms[3];  # query length - alignmet length
    if ( $$terms[6] > $$terms[9] ) {
	$f = $$terms[9] - $$terms[3]; # subject length - alignmet length
    }
    if ( $f < 0 ) {
	$f = 0;
    }
    my $m = $$terms[4];
    my $o = $$terms[5];
    return($f, $m, $o);
}

sub outputQueryHits {
    my ( $outPrefix, $qID ) = @_;

    my $outName = $outPrefix . ".hitIDs.txt";
    open( OUT, "> $outName" ) || die "Cannot open $outName\n";
    print OUT "QueryID\tSubjectIDs\tHitSubjectIDs\n";
    foreach my $q ( keys %$qID ) {
	$$qID{$q}[0] =~ s/^NA,//;
	$$qID{$q}[1] =~ s/^NA,//;
	print OUT "$q\t$$qID{$q}[0]\t$$qID{$q}[1]\n";
    }
    close( OUT );
}

sub outputIdHit {
    my ( $out, $sep, $idInfo, $maxFlank, $maxMid, $idHit ) = @_;

    # Summation counts
    # $sumCount[$m][$f][0] = Total count same strand;
    # $sumCount[$m][$f][1] = Total count opposite strand;
    # $sumCount[$m][$f][2] = Total count both strand;
    my @sumCount = ();
    for( my $m = 0; $m <= $maxMid; $m++ ) {
	@{$sumCount[$m]} = ();
	for( my $f = 0; $f <= $maxFlank; $f++ ) {
	    @{$sumCount[$m][$f]} = (0,0,0);
	    foreach my $sid ( keys(%$idHit) ) {
		my $ref = $$idHit{$sid};
		$sumCount[$m][$f][0] += $$ref[$m][$f][0];
		$sumCount[$m][$f][1] += $$ref[$m][$f][1];
		$sumCount[$m][$f][2] += $$ref[$m][$f][2];
	    }
	}
    }

    print $out "BarcodeID${sep}BarcodeSeq${sep}BarcodeRevCompSeq";
    for( my $m = 0; $m <= $maxMid; $m++ ) {
	for( my $f = 0; $f <= $maxFlank; $f++ ) {
	    print $out "${sep}SeqCountM${m}F${f}${sep}RevCountM${m}F${f}${sep}BothCountM${m}F${f}";
	    print $out "${sep}SeqPropM${m}F${f}${sep}RevPropM${m}F${f}${sep}BothPropM${m}F${f}";
	}
    }
    print $out "\n";
    foreach my $sid ( sort(keys(%$idHit)) ) {
	my $refI = $$idInfo{$sid};
	if ( $sid !~ /${sep}/ ) {
	    print $out "$sid${sep}$$refI[0]${sep}$$refI[1]";
	} else {
	    print $out "\"$sid\"${sep}$$refI[0]${sep}$$refI[1]";
	}
	my $refH = $$idHit{$sid};
	for( my $m = 0; $m <= $maxMid; $m++ ) {
	    for( my $f = 0; $f <= $maxFlank; $f++ ) {
		outputTSVLineWithoutCR( $out, $$refH[$m][$f] );
		for( my $j = 0; $j < @{$sumCount[$m][$f]}; $j++ ) {
		    if ( $sumCount[$m][$f][$j] > 0 ) {
			print $out "${sep}",$$refH[$m][$f][$j]/$sumCount[$m][$f][$j];
		    } else {
			print $out "${sep}NA";
		    }
		}
	    }
	}
	print $out "\n";
    }
    print $out "#${sep}${sep}Sum";
    for( my $m = 0; $m <= $maxMid; $m++ ) {
	for( my $f = 0; $f <= $maxFlank; $f++ ) {
	    outputTSVLineWithoutCR( $out, $sumCount[$m][$f] );
	    print $out "${sep}${sep}${sep}";
	}
    }
    print $out "\n";
}

#
# Utilities for perl
#
sub openFile {
    my ( $inputFile, $in ) = @_;

    if ( ! -f $inputFile ) {
	print STDERR "Error. $inputFile is not found.\n";
	exit(1);
    }
    if ( $inputFile =~ /\.gz$/ ) {
	open( $$in, "gzip -dc $inputFile |" ) || die "Cannot open $inputFile\n";
    } elsif ( $inputFile =~ /\.bz2$/ ) {
	open( $$in, "bzip2 -dc $inputFile |") || die "Cannot open $inputFile\n";
    } elsif ( $inputFile =~ /\.zip$/ ) {
	open( $$in, "unzip -p $inputFile |" ) || die "Cannot open $inputFile\n";
    } else {
	open( $$in, "< $inputFile" ) || die "Cannot open $inputFile\n";
    }
}

sub outputTSVLineWithoutCR {
    my ( $out, $entry ) = @_;

    for ( my $i = 0; $i < @$entry; $i++ ) {
	print $out "\t$$entry[$i]";
    }
}

sub outputCSVLineWithoutCR {
    my ( $out, $entry ) = @_;

    for ( my $i = 0; $i < @$entry; $i++ ) {
	print $out ",$$entry[$i]";
    }
}

sub revcom {
    my ($seq) = @_;
    $seq = reverse $seq;
    $seq =~ tr/acgtACGT/tgcaTGCA/;
    return ($seq);
}
