#!/usr/bin/perl
#
# this converts a text file from unix format to DOS format
#
foreach (@ARGV) {
 $F = $_;
 `mv $F $F.tmp`;
 open (INFILE, "$F.tmp") || die "Couldn't open file $F";
 open (OUTFILE,  ">$F") || die "Couldn't write to file $F";
  while (<INFILE>) {
    s/\n/\r\n/g;
    s/\r\r/\r/g;		# fix it if it already had a CR before the LF
    print OUTFILE;
  }
 close INFILE;
 close OUTFILE;
 `rm $F.tmp`;
}


