#!/usr/bin/env perl
#
# Merge count CSV results
#
use strict;
use warnings;
use File::Basename;
#require("utilities.pl");

&usage() if ( @ARGV < 1 );
{
    my $type = "Prop";
    my $flank = 2;
    my $mid = 1;
    my $strand = "f";
    my $sep = ",";
    my $faFile = "";
    my @csvList = ();
    while ( scalar(@ARGV) ) {
	my $arg = shift @ARGV;
	if ( $arg eq "-h" ) {
	    usage();
	} elsif ( $arg eq "-t" ) {
	    $type = shift @ARGV;
	} elsif ( $arg eq "-x" ) {
	    $flank = shift @ARGV;
	} elsif ( $arg eq "-y" ) {
	    $mid = shift @ARGV;
	} elsif ( $arg eq "-s" ) {
	    $strand = shift @ARGV;
	} elsif ( $arg eq "-char" ) {
	    $sep = shift @ARGV;
	} elsif ( $arg eq "-fa" ) {
	    $faFile = shift @ARGV;
	} else {
	    push( @csvList, $arg );
	}
    }
    &usage() if ( @csvList < 1 );
    collectMFcsv( $type, $flank, $mid, $strand, $sep, $faFile, \@csvList );
}
exit(0);

#
# Subroutines
#
# usage
sub usage {
    print STDERR "Usage:\n";
    print STDERR "collectMFcsv.pl [-t Prop|Count] [-x 2] [-y 1] [-s f|r] [-char separator-char] {-fa blastDB.fa} *.csv > results.csv\n";
    exit(1);
}

sub collectMFcsv {
    my ( $type, $flank, $mid, $strand, $sep, $faFile, $csvList ) = @_;

    my $tag = "";
    if ( $strand eq "f" ) {
	$tag = "Seq${type}M${mid}F${flank}";
    } elsif ( $strand eq "r" ) {
	$tag = "Rev${type}M${mid}F${flank}";
    } else  {
	print STDERR "Error. Strand option -o must be f or r.\n";
	exit(1);
    }
    print STDERR "Selected tag: $tag\n";
    # $countHash{BarcodeID}[0] = BarcodeSeq
    # $countHash{BarcodeID}[1] = BarcodeRevCompSeq
    # $countHash{BarcodeID}[2] = Sample-1 proportion
    # $countHash{BarcodeID}[.] = ....
    my %countHash;
    my @headers = ("BarcodeID","BarcodeSeq","BarcodeRevCompSeq");
    foreach my $csvFile ( @$csvList ) {
	print STDERR "Reading $csvFile...\n";
	readCountCSV( $csvFile, $tag, \@headers, \%countHash );
    }
    my @barIDs;
    print STDERR "Reading $faFile...\n";
    readBarcodeIDfasta( $faFile, \@barIDs );
    outputCountHash( \*STDOUT, $type, $sep, \@headers, \@barIDs, \%countHash );
}

sub readCountCSV {
    my ( $csvFile, $tag, $headers, $countHash ) = @_;

    my $in;
    openFile( $csvFile, \$in );
    my $line = readline($in);
    $line =~ s/[\r\n]+\z//;
    my @heads = splitcsv($line);
    my $c = "NA";
    for( my $j = 3; $j < @heads; $j++ ) {
	if ( $heads[$j] eq $tag ) {
	    $c = $j;
	    last;
	}
    }
    if ( $c eq "NA" ) {
	print STDERR "Error. $tag not found in $csvFile.\n";
	return;
    }
    my $j = scalar(@$headers)-1;
    my $name = basename($csvFile,".csv");
    push( @$headers, $name );
    foreach my $id ( keys %$countHash ) {
	$$countHash{$id}[$j] = "NA";
    }
    while ( my $line = readline($in) ) {
	if ( $line =~ /^#/ || $line eq "\n" ) {
	     next;
	}
	$line =~ s/[\r\n]+\z//;
	# $terms[0] = BarcodeID
	# $terms[1] = BarcodeSeq
	# $terms[2] = BarcodeRevCompSeq
	# $terms[3] = SeqCountM0F0
	# $terms[4] = RevCountM0F0
	# $terms[5] = BothCountM0F0
	# $terms[6] = SeqPropM0F0
	# $terms[7] = RevPropM0F0
	# $terms[8] = BothPropM0F0
	# $terms[9] = ...
	my @terms = splitcsv($line);
	if ( !exists($$countHash{$terms[0]}) ) {
	    @{$$countHash{$terms[0]}} = ("NA") x ($j+1);
	    $$countHash{$terms[0]}[0] = $terms[1];
	    $$countHash{$terms[0]}[1] = $terms[2];
	}
	$$countHash{$terms[0]}[$j] = $terms[$c];
    }
    close( $in );
}

sub readBarcodeIDfasta {
    my ( $faFile, $barIDs ) = @_;

    my $in;
    openFile( $faFile, \$in );
    while ( my $line = readline($in) ) {
	$line =~ s/^\s+//;
	if ( $line =~ /^>/ ) {
	    $line =~ s/[\r\n]+\z//;
	    $line =~ s/\s+$//;
	    $line =~ s/^>//;
	    push( @$barIDs, $line );
	}
    }
    close( $in );
}

sub outputCountHash {
    my ( $out, $type, $sep, $headers, $barIDs, $countHash ) = @_;

    my @sum = (0) x scalar(@$headers);
    $sum[0] = "All";
    $sum[1] = $type;
    $sum[2] = "Summation";
    my $multi = 1;
    if ( $type eq "Prop" ) {
	$multi = 100;
    }
    outputSepLineNaive( $out, $sep, $headers );
#    foreach my $id ( sort(keys(%$countHash)) ) {
    foreach my $id ( @$barIDs ) {
	if ( !exists($$countHash{$id}) ) {
	    print STDERR "Error. $id is not found in CSV-file. Skpping.\n";
	    next;
	}
	print $out "${id}${sep}";
	outputSepLine( $out, $sep, $multi, $$countHash{$id}, \@sum );
    }
    outputSepLineNaive( $out, $sep, \@sum );
}

sub outputSepLineNaive {
    my ( $out, $sep, $entry ) = @_;

    print $out "$$entry[0]";
    for ( my $i = 1; $i < @$entry; $i++ ) {
	print $out "${sep}$$entry[$i]";
    }
    print $out "\n";
}

sub outputSepLine {
    my ( $out, $sep, $multi, $entry, $sum ) = @_;

    print $out "$$entry[0]${sep}$$entry[1]";
    for ( my $i = 2; $i < @$entry; $i++ ) {
	my $v = "NA";
	if ( $$entry[$i] ne "NA" ) {
	    $v = $multi * $$entry[$i];
	    $$sum[$i+1] += $v;
	}
	print $out "${sep}$v";
    }
    print $out "\n";
}

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

sub splitcsv {
    my ( $csvstr ) = @_;

    $csvstr .= ',';
    $csvstr =~ s/("([^"]|"")*"|[^,]*),/$1$;/g;
    $csvstr =~ s/"([^$;]*)"$;/$1$;/g;
    $csvstr =~ s/""/"/g;

    return split(/$;/, $csvstr);
}
