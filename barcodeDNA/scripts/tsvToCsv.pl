#!/usr/bin/env perl
#
# TSV to CSV
#
use strict;
use warnings;
#require("utilities.pl");

&usage() if ( @ARGV < 1 );
{
    tsvToCsv( \@ARGV );
}
exit(0);

#
# Subroutines
#
# usage
sub usage {
    print "Usage:\n";
    print "tsvToCsv.pl *.tsv > output.csv\n";
    exit(1);
}

sub tsvToCsv {
    my ( $tsvFiles ) = @_;

    foreach my $file ( @$tsvFiles ) {
	print STDERR "Reading $file...\n";
	my $in;
	openFile( $file, \$in );
	while ( my $line = readline($in) ) {
	    $line =~ s/[\r\n]+\z//;
	    my @terms = split(/\t/, $line);
	    if ( $terms[0] !~ /,/ ) {
		print "$terms[0]";
	    } else {
		print "\"$terms[0]\"";
	    }
	    for( my $j = 1; $j < @terms; $j++ ) {
		if ( $terms[$j] !~ /,/ ) {
		    print ",$terms[$j]";
		} else {
		    print ",\"$terms[$j]\"";
		}
	    }
	    print "\n";
	}
	close( $in );
    }
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
