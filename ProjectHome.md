XPISigner is a commandline tool that simplifies signing Firefox and Thunderbird extensions.

Written in Java and using the Bouncy Castle cryptographic libraries XPISigner produces signatures compatible with Firefox and Thunderbird

XPISigner v1.6
(http://o-regan.org/xpisigner-secure-your-firefox-extensions)
Copyright 2009 – Kevin O’Regan

xpisigner.cmd pfxfile password basedir|listfile output

Parameters:
- pfxfile The PKCS#12 file containing your signing credentials.
- password The passphrase for pfxfile.
- basedir Include all files under basedir.
- listfile Include only the files found in listfile.
- output Location to save the signed xpi.