#!/usr/bin/env perl
#
# FASTQ to fasta file for blast+
#
use strict;
use warnings;
#use Bio::Seq;
#require("utilities.pl");

&usage() if ( @ARGV < 1 );
{
    my $rmFirst = 0;
    my $rmLast = 0;
    my $minL = 15;
    my @fastqFiles;
    while ( scalar(@ARGV) ) {
	my $arg = shift @ARGV;
	if ( $arg eq "-h" ) {
	    usage();
	} elsif ( $arg eq "-t" ) {
	    $rmFirst = shift @ARGV;
	} elsif ( $arg eq "-b" ) {
	    $rmLast = shift @ARGV;
	} elsif ( $arg eq "-l" ) {
	    $minL = shift @ARGV;
	} else {
	    push( @fastqFiles, $arg );
	}
    }
    fastqToFastaEx( $rmFirst, $rmLast, $minL, \@fastqFiles );
}
exit(0);

#
# Subroutines
#
# usage
sub usage {
    print "Usage:\n";
    print "fastqToFastaEx.pl [-t #remove-first-bases] [-b #remove-last-bases] [-l minimum-read-length] *.fq >! output.fa\n";
    exit(1);
}

sub fastqToFastaEx {
    my ( $rmFirst, $rmLast, $minL, $fastqFiles ) = @_;

    my @count = (0, 0); # removed-reads, total-reads
    my $rmLen = $rmFirst + $rmLast;
    foreach my $file ( @$fastqFiles ) {
	print STDERR "Reading $file...\n";
	my $in;
	openFile( $file, \$in );
	while ( my $tag = readline($in) ) {
	    $tag =~ s/[\r\n]+\z//;
	    $tag =~ s/^@//;
	    my @terms = split(/\s+/, $tag);
	    my $seq = readline($in); # Sequence
	    my $tmp = readline($in); # ID to remove
	    my $qv  = readline($in); # Q-values
	    $seq =~ s/[\r\n]+\z//;
	    $seq = substr( $seq, $rmFirst, length($seq)-$rmLen );
	    $seq =~ s/^N+//;
	    $seq =~ s/N+$//;
	    if ( $minL <= length($seq) ) {
		print ">$terms[0]\n${seq}\n";
	    } else {
		$count[0]++;
	    }
	    $count[1]++;
	}
	close( $in );
    }
    print STDERR "Total input reads: $count[1], removed reads: $count[0], remained reads: ", $count[0]+$count[1], "\n";
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
