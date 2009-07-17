#!/usr/bin/perl -w
#
# This is an URL Redirector Script for squid that can be 
# used to bundle Paxle and Squid together via the squid
# redirector support.
# See: http://wiki.squid-cache.org/Features/Redirectors
#
# This scripts forwards URLs from squid to Paxle where the
# URLs are used to download and index the content of the URLs.
use strict;
use Socket qw(:DEFAULT :crlf);
use IO::Handle;

$|=1 ;

my @requestData;
my ($msg_in,$msg_out);

my $host = "192.168.10.204";
my $port = "8090";

# connecting to paxle
my $protocol = getprotobyname('tcp');
$host = inet_aton($host) or die "$host: unknown host";

socket(SOCK, AF_INET, SOCK_STREAM, $protocol) or die "socket() failed: $!";
my $dest_addr = sockaddr_in($port,$host);
connect(SOCK,$dest_addr) or die("connect() failed: $!");
SOCK->autoflush(1);

# 1) Reading URLs from stdIn 
# 2) Send it to Paxle
# 3) Receive response from Paxle
# 4) Print response to StdOut
while (defined($msg_out = <STDIN>)) {
    chomp $msg_out;

    # splitting request into it's various parts 
    #
    # One squid redirector request line typically looks like this:
    # http://www.pageresource.com/styles/tuts.css 192.168.0.5/- - GET
    @requestData =  split(/\s+/, $msg_out);

    # sending the whole request line to Paxle
    $msg_out .= CRLF;
    print SOCK $msg_out;

    # reading the response
    if (defined($msg_in = <SOCK>)) {
       print STDOUT $msg_in;
    } else {
      print STDERR "Socket closed".CRLF;
      close SOCK;
      exit(1);
    }

    if  ($requestData[0] =~ /^http/) {
    	print STDOUT "$requestData[0]\n";
    } else {
	print STDOUT "$requestData[1]\n";
    }
}

close SOCK;

