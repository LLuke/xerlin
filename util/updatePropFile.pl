#!/usr/bin/perl -w
#
# this replaces certain keys in the properties file
#
$x=0;
while ($x <= $#ARGV) {
 # print "ARGV[$x] = $ARGV[$x]\n";
  if ($ARGV[$x] eq "-p" && $x+1 <= $#ARGV ) {
    $propfile = $ARGV[$x+1];
 #   print "propfile = '$propfile'\n";
    $x = $x + 2;
    next;
  }
  if ($ARGV[$x] eq "-m" && $x+1 <= $#ARGV ) {
    $manifestfile = $ARGV[$x+1];
 #   print "manifestfile = '$manifestfile'\n";
    $x = $x + 2;
    next;
  }
  $x = $x + 1;
}

if ($manifestfile ne "") {
#  print "manifestfile = $manifestfile\n";
  # find the build id
  open (MANIFEST, "$manifestfile") || die "Couldn't open manifest file: $manifestfile";
  while (<MANIFEST>) {
    $line = $_;
#    print "line = $line\n";
    if ($line =~ /^Implementation\-Version\:\s*(.*)$/) {
      $buildid = $1;
      $buildid =~ s/^(.*)\s*\(/$1\\n(/;
      print "Buildid = $buildid\n";
    }
    if ($line =~ /^Specification\-Version\:\s*(.*)$/) {
      $version = $1;
      print "Version = $version\n";
    }

  }
}
if ($buildid eq "" || $version eq "") {
  print "BuildId or Version not specified in manifest.\n";
  exit (-1);
}
if ($propfile eq "") {
  print "Error: no properties file specified\n";
  exit(-1);
}
$F = $propfile;
`mv $F $F.tmp`;
open (INFILE, "$F.tmp") || die "Couldn't open file $F";
open (OUTFILE,  ">$F") || die "Couldn't write to file $F";
while (<INFILE>) {
  s/%MVERSHORT%/$version/g;
  s/%MBUILDID%/$buildid/g;
  print OUTFILE;
}
close INFILE;
close OUTFILE;
`rm $F.tmp`;



