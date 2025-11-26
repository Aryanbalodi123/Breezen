#!/usr/bin/env python3
# tools/encode_level3.py
# Usage:
# python tools/encode_level3.py "YOUR_REAL_KEY" --outname GEMINI
# It will print C-style byte arrays for 5 parts, per-part xor keys, rotations and an expected checksum.
# Do this locally and paste only the printed hex arrays and lengths into keys_level3.cpp.

import sys, os, argparse
import math
import random

def split_into_parts(s, n=5):
    L = len(s)
    base = L // n
    rem = L % n
    parts = []
    i = 0
    for p in range(n):
        l = base + (1 if p < rem else 0)
        parts.append(s[i:i+l])
        i += l
    return parts

def encode_part_bytes(part_bytes, xorKey, rot):
    out = []
    for b in part_bytes:
        # encoding: rotate left then xor
        x = ((b << rot) & 0xff) | ((b & 0xff) >> (8 - rot))
        x = x ^ xorKey
        out.append(x)
    return bytes(out)

def to_c_literal(b):
    return ', '.join('0x{:02x}'.format(x) for x in b)

def checksum32(data):
    sumv = 0x811c9dc5
    for x in data:
        sumv ^= x
        sumv = (sumv * 0x01000193) & 0xffffffff
    return sumv

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('key', help='the secret key to encode (wrap in quotes)')
    parser.add_argument('--outname', default='PART', help='prefix for output (e.g., GEMINI)')
    parser.add_argument('--seed', type=int, default=None, help='seed for random XOR keys (optional)')
    args = parser.parse_args()

    if args.seed is not None:
        random.seed(args.seed)
    else:
        random.seed(os.urandom(8))

    key = args.key.encode('utf-8')
    parts = split_into_parts(key, 5)
    xor_keys = [random.randint(1, 255) for _ in range(5)]
    rots = [random.randint(1,7) for _ in range(5)]

    encoded_parts = []
    for i, part in enumerate(parts):
        enc = encode_part_bytes(part, xor_keys[i], rots[i])
        encoded_parts.append(enc)

    # compute checksum of assembled plaintext for sanity check
    full_plain = b''.join(parts)
    cs = checksum32(full_plain)

    # print outputs: arrays, lengths, meta
    print("// OUTPUT for key: {}".format(args.outname))
    for i, enc in enumerate(encoded_parts):
        print("static const uint8_t {}_PART_{}[] = {{ {} }};".format(args.outname, i, to_c_literal(enc)))
        print("static const size_t {}_PART_{}_LEN = {};".format(args.outname, i, len(enc)))
        print()
    print("// XOR KEYS (use these in native): {}".format(', '.join(hex(x) for x in xor_keys)))
    print("// ROTS (use these in native): {}".format(', '.join(str(x) for x in rots)))
    print("// CHECKSUM for plaintext assembled key: 0x{:08x}".format(cs))
    print("// NOTE: paste only the byte arrays and lengths into keys_level3.cpp and set XOR/ROTS and EXPECTED_CHECKSUM accordingly.")
    print()
    print("// To reproduce deterministic encoding, pass --seed N")
    print()

if __name__ == "__main__":
    main()
