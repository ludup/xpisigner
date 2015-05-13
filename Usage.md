# Introduction #

You'll need JDK 1.5 or higher to run xpisigner

# Details #

xpisigner.cmd pfxfile password basedir|listfile output

Parameters:
-  pfxfile The PKCS#12 file containing your signing credentials.
-  password The passphrase for pfxfile.
-  basedir Include all files under basedir.
-  listfile Include only the files found in listfile.
-  output Location to save the signed xpi.