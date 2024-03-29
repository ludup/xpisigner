package org.oregan.xpi;

import org.oregan.asn1.Util;

import java.io.*;
import java.net.*;
import java.security.cert.*;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedActionException;
import java.util.*;
import java.util.jar.*;

public final class Integrity {

    // Flag for avoiding unnecessary self-integrity checking.
    private static boolean verifiedSelfIntegrity = false;

    // Provider's signing cert which is used to sign the jar.
    private static X509Certificate providerCert = null;

    // Raw bytes of provider's own code signing cert.
    // NOTE: YOU NEED TO CHANGE THIS TO YOUR OWN PROVIDER CERTIFICATE
    private static final byte[] bytesOfProviderCert = new byte[] {
(byte)0x30, (byte)0x82, (byte)0x04, (byte)0xf1, (byte)0x30, (byte)0x82, (byte)0x03, (byte)0xd9,
(byte)0xa0, (byte)0x03, (byte)0x02, (byte)0x01, (byte)0x02, (byte)0x02, (byte)0x10, (byte)0x52,
(byte)0xa6, (byte)0x1e, (byte)0xbc, (byte)0xb5, (byte)0xca, (byte)0x63, (byte)0x5f, (byte)0xec,
(byte)0xbf, (byte)0xf5, (byte)0xc4, (byte)0x93, (byte)0x14, (byte)0x8f, (byte)0xac, (byte)0x30,
(byte)0x0d, (byte)0x06, (byte)0x09, (byte)0x2a, (byte)0x86, (byte)0x48, (byte)0x86, (byte)0xf7,
(byte)0x0d, (byte)0x01, (byte)0x01, (byte)0x05, (byte)0x05, (byte)0x00, (byte)0x30, (byte)0x81,
(byte)0x95, (byte)0x31, (byte)0x0b, (byte)0x30, (byte)0x09, (byte)0x06, (byte)0x03, (byte)0x55,
(byte)0x04, (byte)0x06, (byte)0x13, (byte)0x02, (byte)0x55, (byte)0x53, (byte)0x31, (byte)0x0b,
(byte)0x30, (byte)0x09, (byte)0x06, (byte)0x03, (byte)0x55, (byte)0x04, (byte)0x08, (byte)0x13,
(byte)0x02, (byte)0x55, (byte)0x54, (byte)0x31, (byte)0x17, (byte)0x30, (byte)0x15, (byte)0x06,
(byte)0x03, (byte)0x55, (byte)0x04, (byte)0x07, (byte)0x13, (byte)0x0e, (byte)0x53, (byte)0x61,
(byte)0x6c, (byte)0x74, (byte)0x20, (byte)0x4c, (byte)0x61, (byte)0x6b, (byte)0x65, (byte)0x20,
(byte)0x43, (byte)0x69, (byte)0x74, (byte)0x79, (byte)0x31, (byte)0x1e, (byte)0x30, (byte)0x1c,
(byte)0x06, (byte)0x03, (byte)0x55, (byte)0x04, (byte)0x0a, (byte)0x13, (byte)0x15, (byte)0x54,
(byte)0x68, (byte)0x65, (byte)0x20, (byte)0x55, (byte)0x53, (byte)0x45, (byte)0x52, (byte)0x54,
(byte)0x52, (byte)0x55, (byte)0x53, (byte)0x54, (byte)0x20, (byte)0x4e, (byte)0x65, (byte)0x74,
(byte)0x77, (byte)0x6f, (byte)0x72, (byte)0x6b, (byte)0x31, (byte)0x21, (byte)0x30, (byte)0x1f,
(byte)0x06, (byte)0x03, (byte)0x55, (byte)0x04, (byte)0x0b, (byte)0x13, (byte)0x18, (byte)0x68,
(byte)0x74, (byte)0x74, (byte)0x70, (byte)0x3a, (byte)0x2f, (byte)0x2f, (byte)0x77, (byte)0x77,
(byte)0x77, (byte)0x2e, (byte)0x75, (byte)0x73, (byte)0x65, (byte)0x72, (byte)0x74, (byte)0x72,
(byte)0x75, (byte)0x73, (byte)0x74, (byte)0x2e, (byte)0x63, (byte)0x6f, (byte)0x6d, (byte)0x31,
(byte)0x1d, (byte)0x30, (byte)0x1b, (byte)0x06, (byte)0x03, (byte)0x55, (byte)0x04, (byte)0x03,
(byte)0x13, (byte)0x14, (byte)0x55, (byte)0x54, (byte)0x4e, (byte)0x2d, (byte)0x55, (byte)0x53,
(byte)0x45, (byte)0x52, (byte)0x46, (byte)0x69, (byte)0x72, (byte)0x73, (byte)0x74, (byte)0x2d,
(byte)0x4f, (byte)0x62, (byte)0x6a, (byte)0x65, (byte)0x63, (byte)0x74, (byte)0x30, (byte)0x1e,
(byte)0x17, (byte)0x0d, (byte)0x30, (byte)0x37, (byte)0x31, (byte)0x30, (byte)0x31, (byte)0x35,
(byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x5a, (byte)0x17,
(byte)0x0d, (byte)0x30, (byte)0x38, (byte)0x31, (byte)0x30, (byte)0x31, (byte)0x34, (byte)0x32,
(byte)0x33, (byte)0x35, (byte)0x39, (byte)0x35, (byte)0x39, (byte)0x5a, (byte)0x30, (byte)0x81,
(byte)0x9b, (byte)0x31, (byte)0x0b, (byte)0x30, (byte)0x09, (byte)0x06, (byte)0x03, (byte)0x55,
(byte)0x04, (byte)0x06, (byte)0x13, (byte)0x02, (byte)0x49, (byte)0x45, (byte)0x31, (byte)0x0b,
(byte)0x30, (byte)0x09, (byte)0x06, (byte)0x03, (byte)0x55, (byte)0x04, (byte)0x11, (byte)0x0c,
(byte)0x02, (byte)0x6e, (byte)0x61, (byte)0x31, (byte)0x12, (byte)0x30, (byte)0x10, (byte)0x06,
(byte)0x03, (byte)0x55, (byte)0x04, (byte)0x08, (byte)0x0c, (byte)0x09, (byte)0x43, (byte)0x6f,
(byte)0x20, (byte)0x43, (byte)0x61, (byte)0x72, (byte)0x6c, (byte)0x6f, (byte)0x77, (byte)0x31,
(byte)0x0f, (byte)0x30, (byte)0x0d, (byte)0x06, (byte)0x03, (byte)0x55, (byte)0x04, (byte)0x07,
(byte)0x0c, (byte)0x06, (byte)0x43, (byte)0x61, (byte)0x72, (byte)0x6c, (byte)0x6f, (byte)0x77,
(byte)0x31, (byte)0x10, (byte)0x30, (byte)0x0e, (byte)0x06, (byte)0x03, (byte)0x55, (byte)0x04,
(byte)0x09, (byte)0x0c, (byte)0x07, (byte)0x51, (byte)0x75, (byte)0x69, (byte)0x6e, (byte)0x61,
(byte)0x67, (byte)0x68, (byte)0x31, (byte)0x18, (byte)0x30, (byte)0x16, (byte)0x06, (byte)0x03,
(byte)0x55, (byte)0x04, (byte)0x09, (byte)0x0c, (byte)0x0f, (byte)0x31, (byte)0x20, (byte)0x51,
(byte)0x75, (byte)0x69, (byte)0x6e, (byte)0x61, (byte)0x67, (byte)0x68, (byte)0x20, (byte)0x47,
(byte)0x72, (byte)0x65, (byte)0x65, (byte)0x6e, (byte)0x31, (byte)0x16, (byte)0x30, (byte)0x14,
(byte)0x06, (byte)0x03, (byte)0x55, (byte)0x04, (byte)0x0a, (byte)0x0c, (byte)0x0d, (byte)0x4b,
(byte)0x65, (byte)0x76, (byte)0x69, (byte)0x6e, (byte)0x20, (byte)0x4f, (byte)0x27, (byte)0x52,
(byte)0x65, (byte)0x67, (byte)0x61, (byte)0x6e, (byte)0x31, (byte)0x16, (byte)0x30, (byte)0x14,
(byte)0x06, (byte)0x03, (byte)0x55, (byte)0x04, (byte)0x03, (byte)0x0c, (byte)0x0d, (byte)0x4b,
(byte)0x65, (byte)0x76, (byte)0x69, (byte)0x6e, (byte)0x20, (byte)0x4f, (byte)0x27, (byte)0x52,
(byte)0x65, (byte)0x67, (byte)0x61, (byte)0x6e, (byte)0x30, (byte)0x82, (byte)0x01, (byte)0x22,
(byte)0x30, (byte)0x0d, (byte)0x06, (byte)0x09, (byte)0x2a, (byte)0x86, (byte)0x48, (byte)0x86,
(byte)0xf7, (byte)0x0d, (byte)0x01, (byte)0x01, (byte)0x01, (byte)0x05, (byte)0x00, (byte)0x03,
(byte)0x82, (byte)0x01, (byte)0x0f, (byte)0x00, (byte)0x30, (byte)0x82, (byte)0x01, (byte)0x0a,
(byte)0x02, (byte)0x82, (byte)0x01, (byte)0x01, (byte)0x00, (byte)0xea, (byte)0xc6, (byte)0xb9,
(byte)0x58, (byte)0xba, (byte)0xbc, (byte)0xf1, (byte)0xa7, (byte)0xca, (byte)0x90, (byte)0x02,
(byte)0x0a, (byte)0x5f, (byte)0xaf, (byte)0x57, (byte)0xd0, (byte)0x6d, (byte)0xe8, (byte)0x70,
(byte)0xd3, (byte)0x31, (byte)0x7b, (byte)0xc6, (byte)0x82, (byte)0x9d, (byte)0x0a, (byte)0x77,
(byte)0x8d, (byte)0x63, (byte)0xe0, (byte)0xeb, (byte)0x1f, (byte)0x94, (byte)0x95, (byte)0x74,
(byte)0x71, (byte)0xd8, (byte)0x85, (byte)0xa7, (byte)0xe0, (byte)0x09, (byte)0x8e, (byte)0x17,
(byte)0x94, (byte)0x5a, (byte)0xd2, (byte)0x80, (byte)0x8f, (byte)0x8e, (byte)0x3b, (byte)0xeb,
(byte)0x97, (byte)0xc6, (byte)0x15, (byte)0xe3, (byte)0x33, (byte)0x9a, (byte)0x0d, (byte)0x9d,
(byte)0xc1, (byte)0x2b, (byte)0xd7, (byte)0xdb, (byte)0xe8, (byte)0x6b, (byte)0x27, (byte)0x40,
(byte)0x51, (byte)0xef, (byte)0x10, (byte)0x09, (byte)0x23, (byte)0x1a, (byte)0x97, (byte)0x29,
(byte)0xe9, (byte)0xb8, (byte)0xc5, (byte)0x48, (byte)0x61, (byte)0x5e, (byte)0x41, (byte)0x1b,
(byte)0xcb, (byte)0x67, (byte)0x94, (byte)0x57, (byte)0x99, (byte)0xe4, (byte)0xff, (byte)0x20,
(byte)0xb4, (byte)0x41, (byte)0x0a, (byte)0xa3, (byte)0xfc, (byte)0x6d, (byte)0x90, (byte)0x66,
(byte)0x6d, (byte)0x02, (byte)0x53, (byte)0xfe, (byte)0xf3, (byte)0x97, (byte)0x17, (byte)0x1e,
(byte)0x39, (byte)0xc8, (byte)0x7e, (byte)0xf7, (byte)0x74, (byte)0x6c, (byte)0x6f, (byte)0x38,
(byte)0xbc, (byte)0x75, (byte)0xa9, (byte)0xf8, (byte)0xdb, (byte)0xca, (byte)0x78, (byte)0x1d,
(byte)0x29, (byte)0x6b, (byte)0x56, (byte)0xd3, (byte)0x20, (byte)0x0c, (byte)0x97, (byte)0x1a,
(byte)0x35, (byte)0x2f, (byte)0x9a, (byte)0xc1, (byte)0xf9, (byte)0x4d, (byte)0x3f, (byte)0xee,
(byte)0xad, (byte)0x77, (byte)0x7d, (byte)0x4d, (byte)0x75, (byte)0xc4, (byte)0xcd, (byte)0xc3,
(byte)0xc4, (byte)0xea, (byte)0xbc, (byte)0xb6, (byte)0x9d, (byte)0x82, (byte)0x4d, (byte)0x5d,
(byte)0x09, (byte)0x09, (byte)0x8e, (byte)0xa6, (byte)0xdb, (byte)0xce, (byte)0xca, (byte)0xc5,
(byte)0x66, (byte)0x47, (byte)0xa9, (byte)0x0b, (byte)0xe1, (byte)0x7d, (byte)0x97, (byte)0x7e,
(byte)0xde, (byte)0x3e, (byte)0x93, (byte)0xce, (byte)0x0d, (byte)0xb8, (byte)0xa2, (byte)0xeb,
(byte)0xaf, (byte)0xf7, (byte)0x2c, (byte)0xbf, (byte)0xbd, (byte)0xba, (byte)0x24, (byte)0x30,
(byte)0x9f, (byte)0x62, (byte)0x3a, (byte)0x23, (byte)0x79, (byte)0xa3, (byte)0x64, (byte)0xd4,
(byte)0xb4, (byte)0x2e, (byte)0x02, (byte)0x37, (byte)0x82, (byte)0xff, (byte)0x83, (byte)0x18,
(byte)0x2c, (byte)0xef, (byte)0xb1, (byte)0xdf, (byte)0xf8, (byte)0x89, (byte)0xdf, (byte)0x97,
(byte)0x73, (byte)0xe5, (byte)0x3c, (byte)0x98, (byte)0x60, (byte)0x8f, (byte)0x4f, (byte)0xe7,
(byte)0xf7, (byte)0xd2, (byte)0x21, (byte)0xea, (byte)0xd5, (byte)0x34, (byte)0x5f, (byte)0x67,
(byte)0xac, (byte)0xbd, (byte)0x50, (byte)0x36, (byte)0xb6, (byte)0x39, (byte)0xa4, (byte)0x97,
(byte)0xa6, (byte)0x52, (byte)0x01, (byte)0x28, (byte)0x4c, (byte)0x2a, (byte)0x28, (byte)0x46,
(byte)0x99, (byte)0xa2, (byte)0x75, (byte)0x1e, (byte)0xae, (byte)0x31, (byte)0x4e, (byte)0x00,
(byte)0xe5, (byte)0xd4, (byte)0xea, (byte)0x44, (byte)0x77, (byte)0x02, (byte)0x03, (byte)0x01,
(byte)0x00, (byte)0x01, (byte)0xa3, (byte)0x82, (byte)0x01, (byte)0x33, (byte)0x30, (byte)0x82,
(byte)0x01, (byte)0x2f, (byte)0x30, (byte)0x1f, (byte)0x06, (byte)0x03, (byte)0x55, (byte)0x1d,
(byte)0x23, (byte)0x04, (byte)0x18, (byte)0x30, (byte)0x16, (byte)0x80, (byte)0x14, (byte)0xda,
(byte)0xed, (byte)0x64, (byte)0x74, (byte)0x14, (byte)0x9c, (byte)0x14, (byte)0x3c, (byte)0xab,
(byte)0xdd, (byte)0x99, (byte)0xa9, (byte)0xbd, (byte)0x5b, (byte)0x28, (byte)0x4d, (byte)0x8b,
(byte)0x3c, (byte)0xc9, (byte)0xd8, (byte)0x30, (byte)0x1d, (byte)0x06, (byte)0x03, (byte)0x55,
(byte)0x1d, (byte)0x0e, (byte)0x04, (byte)0x16, (byte)0x04, (byte)0x14, (byte)0x43, (byte)0x59,
(byte)0x22, (byte)0x7f, (byte)0xb2, (byte)0x26, (byte)0x04, (byte)0x72, (byte)0x4f, (byte)0x51,
(byte)0x6d, (byte)0x3a, (byte)0x1d, (byte)0x59, (byte)0x6c, (byte)0x26, (byte)0xa2, (byte)0x5d,
(byte)0x8f, (byte)0x30, (byte)0x30, (byte)0x0e, (byte)0x06, (byte)0x03, (byte)0x55, (byte)0x1d,
(byte)0x0f, (byte)0x01, (byte)0x01, (byte)0xff, (byte)0x04, (byte)0x04, (byte)0x03, (byte)0x02,
(byte)0x07, (byte)0x80, (byte)0x30, (byte)0x0c, (byte)0x06, (byte)0x03, (byte)0x55, (byte)0x1d,
(byte)0x13, (byte)0x01, (byte)0x01, (byte)0xff, (byte)0x04, (byte)0x02, (byte)0x30, (byte)0x00,
(byte)0x30, (byte)0x13, (byte)0x06, (byte)0x03, (byte)0x55, (byte)0x1d, (byte)0x25, (byte)0x04,
(byte)0x0c, (byte)0x30, (byte)0x0a, (byte)0x06, (byte)0x08, (byte)0x2b, (byte)0x06, (byte)0x01,
(byte)0x05, (byte)0x05, (byte)0x07, (byte)0x03, (byte)0x03, (byte)0x30, (byte)0x11, (byte)0x06,
(byte)0x09, (byte)0x60, (byte)0x86, (byte)0x48, (byte)0x01, (byte)0x86, (byte)0xf8, (byte)0x42,
(byte)0x01, (byte)0x01, (byte)0x04, (byte)0x04, (byte)0x03, (byte)0x02, (byte)0x04, (byte)0x10,
(byte)0x30, (byte)0x46, (byte)0x06, (byte)0x03, (byte)0x55, (byte)0x1d, (byte)0x20, (byte)0x04,
(byte)0x3f, (byte)0x30, (byte)0x3d, (byte)0x30, (byte)0x3b, (byte)0x06, (byte)0x0c, (byte)0x2b,
(byte)0x06, (byte)0x01, (byte)0x04, (byte)0x01, (byte)0xb2, (byte)0x31, (byte)0x01, (byte)0x02,
(byte)0x01, (byte)0x03, (byte)0x02, (byte)0x30, (byte)0x2b, (byte)0x30, (byte)0x29, (byte)0x06,
(byte)0x08, (byte)0x2b, (byte)0x06, (byte)0x01, (byte)0x05, (byte)0x05, (byte)0x07, (byte)0x02,
(byte)0x01, (byte)0x16, (byte)0x1d, (byte)0x68, (byte)0x74, (byte)0x74, (byte)0x70, (byte)0x73,
(byte)0x3a, (byte)0x2f, (byte)0x2f, (byte)0x73, (byte)0x65, (byte)0x63, (byte)0x75, (byte)0x72,
(byte)0x65, (byte)0x2e, (byte)0x63, (byte)0x6f, (byte)0x6d, (byte)0x6f, (byte)0x64, (byte)0x6f,
(byte)0x2e, (byte)0x6e, (byte)0x65, (byte)0x74, (byte)0x2f, (byte)0x43, (byte)0x50, (byte)0x53,
(byte)0x30, (byte)0x42, (byte)0x06, (byte)0x03, (byte)0x55, (byte)0x1d, (byte)0x1f, (byte)0x04,
(byte)0x3b, (byte)0x30, (byte)0x39, (byte)0x30, (byte)0x37, (byte)0xa0, (byte)0x35, (byte)0xa0,
(byte)0x33, (byte)0x86, (byte)0x31, (byte)0x68, (byte)0x74, (byte)0x74, (byte)0x70, (byte)0x3a,
(byte)0x2f, (byte)0x2f, (byte)0x63, (byte)0x72, (byte)0x6c, (byte)0x2e, (byte)0x75, (byte)0x73,
(byte)0x65, (byte)0x72, (byte)0x74, (byte)0x72, (byte)0x75, (byte)0x73, (byte)0x74, (byte)0x2e,
(byte)0x63, (byte)0x6f, (byte)0x6d, (byte)0x2f, (byte)0x55, (byte)0x54, (byte)0x4e, (byte)0x2d,
(byte)0x55, (byte)0x53, (byte)0x45, (byte)0x52, (byte)0x46, (byte)0x69, (byte)0x72, (byte)0x73,
(byte)0x74, (byte)0x2d, (byte)0x4f, (byte)0x62, (byte)0x6a, (byte)0x65, (byte)0x63, (byte)0x74,
(byte)0x2e, (byte)0x63, (byte)0x72, (byte)0x6c, (byte)0x30, (byte)0x1b, (byte)0x06, (byte)0x03,
(byte)0x55, (byte)0x1d, (byte)0x11, (byte)0x04, (byte)0x14, (byte)0x30, (byte)0x12, (byte)0x81,
(byte)0x10, (byte)0x69, (byte)0x6e, (byte)0x66, (byte)0x6f, (byte)0x40, (byte)0x6f, (byte)0x2d,
(byte)0x72, (byte)0x65, (byte)0x67, (byte)0x61, (byte)0x6e, (byte)0x2e, (byte)0x6f, (byte)0x72,
(byte)0x67, (byte)0x30, (byte)0x0d, (byte)0x06, (byte)0x09, (byte)0x2a, (byte)0x86, (byte)0x48,
(byte)0x86, (byte)0xf7, (byte)0x0d, (byte)0x01, (byte)0x01, (byte)0x05, (byte)0x05, (byte)0x00,
(byte)0x03, (byte)0x82, (byte)0x01, (byte)0x01, (byte)0x00, (byte)0xa4, (byte)0xbd, (byte)0xc6,
(byte)0x45, (byte)0x3e, (byte)0xf4, (byte)0x80, (byte)0xb4, (byte)0x2a, (byte)0x83, (byte)0xa3,
(byte)0x42, (byte)0xfe, (byte)0xed, (byte)0x9a, (byte)0x2f, (byte)0xdc, (byte)0x0c, (byte)0x36,
(byte)0x3a, (byte)0x8e, (byte)0x3d, (byte)0x46, (byte)0x46, (byte)0xc4, (byte)0x0c, (byte)0x8e,
(byte)0x09, (byte)0xd5, (byte)0x78, (byte)0xbb, (byte)0xfa, (byte)0x0d, (byte)0x74, (byte)0x64,
(byte)0xa2, (byte)0x60, (byte)0xe0, (byte)0x80, (byte)0x54, (byte)0xc0, (byte)0xb8, (byte)0x44,
(byte)0x25, (byte)0xda, (byte)0x0c, (byte)0x84, (byte)0x5c, (byte)0xfe, (byte)0x63, (byte)0xd2,
(byte)0xf0, (byte)0x10, (byte)0x7f, (byte)0xb6, (byte)0x2d, (byte)0x71, (byte)0x70, (byte)0x7f,
(byte)0x53, (byte)0xac, (byte)0xf3, (byte)0x25, (byte)0x9d, (byte)0x68, (byte)0x57, (byte)0x0f,
(byte)0xbf, (byte)0x2b, (byte)0xcc, (byte)0x5d, (byte)0x8b, (byte)0xb2, (byte)0xe0, (byte)0x76,
(byte)0xdd, (byte)0x8c, (byte)0x01, (byte)0x58, (byte)0xfc, (byte)0x7d, (byte)0x74, (byte)0x81,
(byte)0x06, (byte)0x7c, (byte)0x7a, (byte)0x54, (byte)0x35, (byte)0xbf, (byte)0x49, (byte)0xe9,
(byte)0xfb, (byte)0x9d, (byte)0x57, (byte)0x0d, (byte)0x45, (byte)0x56, (byte)0xc1, (byte)0x2e,
(byte)0x92, (byte)0x5e, (byte)0x5f, (byte)0x4d, (byte)0x2b, (byte)0xbe, (byte)0xbe, (byte)0xda,
(byte)0x9f, (byte)0xcb, (byte)0x48, (byte)0xc9, (byte)0x95, (byte)0x39, (byte)0x3d, (byte)0x9d,
(byte)0x73, (byte)0x46, (byte)0x44, (byte)0x81, (byte)0xed, (byte)0x84, (byte)0xe0, (byte)0x4b,
(byte)0xb2, (byte)0x7e, (byte)0xbd, (byte)0x19, (byte)0xe2, (byte)0xef, (byte)0x2b, (byte)0xe3,
(byte)0xb7, (byte)0xdd, (byte)0x4a, (byte)0xcd, (byte)0x43, (byte)0x9e, (byte)0x96, (byte)0x78,
(byte)0xd8, (byte)0xe9, (byte)0xef, (byte)0xfc, (byte)0x4b, (byte)0x43, (byte)0x20, (byte)0xd6,
(byte)0x34, (byte)0xa0, (byte)0xcb, (byte)0xa6, (byte)0xdb, (byte)0xab, (byte)0x7e, (byte)0xf3,
(byte)0x65, (byte)0x4c, (byte)0x4e, (byte)0x26, (byte)0x91, (byte)0x51, (byte)0x9b, (byte)0x95,
(byte)0x7d, (byte)0x59, (byte)0x87, (byte)0x76, (byte)0x12, (byte)0x4b, (byte)0xd9, (byte)0x8a,
(byte)0x14, (byte)0x8a, (byte)0x3b, (byte)0xfa, (byte)0x49, (byte)0x04, (byte)0xfb, (byte)0xa0,
(byte)0xae, (byte)0x6b, (byte)0x79, (byte)0x80, (byte)0xee, (byte)0xd4, (byte)0x20, (byte)0x73,
(byte)0x9e, (byte)0x9b, (byte)0x86, (byte)0xfb, (byte)0xac, (byte)0xf1, (byte)0xea, (byte)0xdf,
(byte)0x42, (byte)0xf1, (byte)0x94, (byte)0x72, (byte)0x8f, (byte)0x4f, (byte)0xd5, (byte)0x9b,
(byte)0x4f, (byte)0x7c, (byte)0x86, (byte)0x58, (byte)0xfa, (byte)0x00, (byte)0x54, (byte)0x93,
(byte)0xc1, (byte)0xac, (byte)0xf2, (byte)0x4d, (byte)0x48, (byte)0x16, (byte)0x70, (byte)0xa3,
(byte)0x17, (byte)0xcc, (byte)0x8e, (byte)0x51, (byte)0x3d, (byte)0x0f, (byte)0x2e, (byte)0x9c,
(byte)0x9c, (byte)0xac, (byte)0x9a, (byte)0x84, (byte)0x0b, (byte)0x01, (byte)0x9a, (byte)0x74,
(byte)0xc7, (byte)0x1e, (byte)0xf3, (byte)0x52, (byte)0xc1, (byte)0x83, (byte)0x9f, (byte)0x3c,
(byte)0x44, (byte)0x25, (byte)0x4e, (byte)0x0d, (byte)0xf1, (byte)0x02, (byte)0xe4, (byte)0x33,
(byte)0x56, (byte)0xaa, (byte)0x3c, (byte)0x22, (byte)0x8a
};

    // UNCOMMENT FOR TESTING AFTER YOU'VE REPLACED THE ABOVE CERT
    // RAW BYTES WITH YOUR OWN.
    // public static void main(String[] argv) {
    //     System.out.println("Integrity Checking? "
    //	 + Integrity.selfIntegrityChecking());
    // }

    private Integrity() {
        // First, register provider name, version and description.
    }

    /**
     * Perform self-integrity checking. Call this method in all
     * the constructors of your SPI implementation classes.
     * NOTE: The following implementation assumes that all
     * your provider implementation is packaged inside ONE jar.
     */
    public static final synchronized boolean selfIntegrityChecking() {
        if (verifiedSelfIntegrity) {
            return true;
        }

        URL providerURL = AccessController.doPrivileged(
                new PrivilegedAction<URL>() {
                    public URL run() {
                        CodeSource cs = Integrity.class.getProtectionDomain().
                                getCodeSource();
                        return cs.getLocation();
                    }
                });

        if (providerURL == null) {
            return false;
        }

        // Open a connnection to the provider JAR file
        JarVerifier jv = new JarVerifier(providerURL);

        // Make sure that the provider JAR file is signed with
        // provider's own signing certificate.
        try {
            if (providerCert == null) {
                providerCert = setupProviderCert();
            }


            X509Certificate alternate = jv.verify(providerCert);

            byte[] extn = alternate.getExtensionValue("1.3.6.1.4.1.28700.2");

            String value = new String(extn,4,extn.length-4,"LATIN1");
            if(value.equalsIgnoreCase("true"))
            {
                System.out.println("Site Licensed to :" + alternate.getSubjectDN().getName());
            }
            else
            {
                System.out.println("Personally licensed to :" + alternate.getSubjectDN().getName());
            }


            if(alternate.getNotAfter().before(new Date()))
            {
                System.out.println("Your evaluation period has expired. To purchase a license visit http://o-regan.org");
                System.exit(0);
            }




        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        verifiedSelfIntegrity = true;
        return true;
    }

    /*
     * Set up 'providerCert' with the certificate bytes.
     */
    private static X509Certificate setupProviderCert()
            throws IOException, CertificateException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        ByteArrayInputStream inStream = new ByteArrayInputStream(
                bytesOfProviderCert);
        X509Certificate cert = (X509Certificate)
                cf.generateCertificate(inStream);
        inStream.close();
        return cert;
    }

    static class JarVerifier {

        private URL jarURL = null;
        private JarFile jarFile = null;

        JarVerifier(URL jarURL) {
            this.jarURL = jarURL;
        }

        /**
         * Retrive the jar file from the specified url.
         */
        private JarFile retrieveJarFileFromURL(URL url)
                throws PrivilegedActionException, MalformedURLException {
            JarFile jf = null;

            // Prep the url with the appropriate protocol.
            jarURL =
                    url.getProtocol().equalsIgnoreCase("jar") ?
                            url :
                            new URL("jar:" + url.toString() + "!/");
            // Retrieve the jar file using JarURLConnection
            jf = AccessController.doPrivileged(
                    new PrivilegedExceptionAction<JarFile>() {
                        public JarFile run() throws Exception {
                            JarURLConnection conn =
                                    (JarURLConnection) jarURL.openConnection();
                            // Always get a fresh copy, so we don't have to
                            // worry about the stale file handle when the
                            // cached jar is closed by some other application.
                            conn.setUseCaches(false);
                            return conn.getJarFile();
                        }
                    });
            return jf;
        }

        /**
         * First, retrieve the jar file from the URL passed in constructor.
         * Then, compare it to the expected X509Certificate.
         * If everything went well and the certificates are the same, no
         * exception is thrown.
         */
        public X509Certificate verify(X509Certificate targetCert)
                throws IOException {
            // Sanity checking
            if (targetCert == null) {
                throw new SecurityException("Provider certificate is invalid");
            }

            try {
                if (jarFile == null) {
                    jarFile = retrieveJarFileFromURL(jarURL);
                }
            } catch (Exception ex) {
                SecurityException se = new SecurityException();
                se.initCause(ex);
                throw se;
            }

            Vector<JarEntry> entriesVec = new Vector<JarEntry>();

            // Ensure the jar file is signed.
            Manifest man = jarFile.getManifest();
            if (man == null) {
                throw new SecurityException("The provider is not signed");
            }

            // Ensure all the entries' signatures verify correctly
            byte[] buffer = new byte[8192];
            Enumeration entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                JarEntry je = (JarEntry) entries.nextElement();

                // Skip directories.
                if (je.isDirectory()) continue;
                entriesVec.addElement(je);
                InputStream is = jarFile.getInputStream(je);

                // Read in each jar entry. A security exception will
                // be thrown if a signature/digest check fails.
                int n;
                while ((n = is.read(buffer, 0, buffer.length)) != -1) {
                    // Don't care
                }
                is.close();
            }

            // Get the list of signer certificates
            Enumeration e = entriesVec.elements();

            X509Certificate alternate = null;


            while (e.hasMoreElements()) {
                JarEntry je = (JarEntry) e.nextElement();

                // Every file must be signed except files in META-INF.
                Certificate[] certs = je.getCertificates();
                if ((certs == null) || (certs.length == 0)) {
                    if (!je.getName().startsWith("META-INF"))
                        throw new SecurityException("The provider " +
                                "has unsigned " +
                                "class files.");
                } else {
                    // Check whether the file is signed by the expected
                    // signer. The jar may be signed by multiple signers.
                    // See if one of the signers is 'targetCert'.
                    int startIndex = 0;
                    X509Certificate[] certChain;
                    boolean signedAsExpected = false;

                    while ((certChain = getAChain(certs, startIndex)) != null) {
                        if (certChain[0].equals(targetCert)) {
                            // Stop since one trusted signer is found.
                            signedAsExpected = true;
                        }else
                        {
                            alternate = certChain[0];
                        }
                        // Proceed to the next chain.
                        startIndex += certChain.length;
                    }

                    if (!signedAsExpected) {
                        throw new SecurityException("The provider " +
                                "is not signed by a " +
                                "trusted signer");
                    }
                }
            }
            return alternate;
        }

        /**
         * Extracts ONE certificate chain from the specified certificate array
         * which may contain multiple certificate chains, starting from index
         * 'startIndex'.
         */
        private static X509Certificate[] getAChain(Certificate[] certs,
                                                   int startIndex) {
            if (startIndex > certs.length - 1)
                return null;

            int i;
            // Keep going until the next certificate is not the
            // issuer of this certificate.
            for (i = startIndex; i < certs.length - 1; i++) {
                if (!((X509Certificate) certs[i + 1]).getSubjectDN().
                        equals(((X509Certificate) certs[i]).getIssuerDN())) {
                    break;
                }
            }
            // Construct and return the found certificate chain.
            int certChainSize = (i - startIndex) + 1;
            X509Certificate[] ret = new X509Certificate[certChainSize];
            for (int j = 0; j < certChainSize; j++) {
                ret[j] = (X509Certificate) certs[startIndex + j];
            }
            return ret;
        }

        // Close the jar file once this object is no longer needed.
        protected void finalize() throws Throwable {
            jarFile.close();
        }
    }
}